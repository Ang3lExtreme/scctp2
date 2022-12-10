package scc.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import redis.clients.jedis.Jedis;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DAO.BidDAO;
import scc.Data.DTO.Auction;
import scc.Data.DTO.Bid;
import scc.Data.DTO.Session;
import scc.Data.DTO.Status;
import scc.Database.MongoAuctionDBLayer;
import scc.Database.MongoBidDBLayer;
import scc.Database.MongoUserDBLayer;
import scc.cache.RedisCache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import static scc.mgt.AzureManagement.USE_CACHE;

@Path("/auction/{id}/bid")
public class BidControllerMongo {
    @PathParam("id")
    private String id;

 // https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/connection/connect/
 		// fetch your own connection string from the mongo DB connect menu online
 		// replace sccTP241888:o1f39bZM8mOMUKsZ with the user and password from your own
 		// mongo DB
 		private static final String CONNECTION_URL = "mongodb+srv://sccTP241888:o1f39bZM8mOMUKsZ@cluster0.afd7s1x.mongodb.net/?retryWrites=true&w=majority";
 		// replace this with a different DB name if necessary

    private Jedis jedis;

    MongoClientURI connectionString = new MongoClientURI(CONNECTION_URL);
	MongoClient client = new MongoClient(connectionString);

    private synchronized void initCache() {
        if(jedis != null)
            return;
        jedis = RedisCache.getCachePool().getResource();
    }

    MongoBidDBLayer mongo =  new MongoBidDBLayer(client);
    MongoAuctionDBLayer mongoAuction = new MongoAuctionDBLayer(client);
    MongoUserDBLayer mongoUser = new MongoUserDBLayer(client);

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Bid createBid(@CookieParam("scc:session") Cookie session, Bid bid) throws JsonProcessingException {
        initCache();
        AuctionDAO auctionDAO;
        if(!bid.getAuctionId().equals(id)) {
            throw new WebApplicationException("Auction id does not match", 400);
        }
        //create bid
        BidDAO b = new BidDAO(bid.getId(),bid.getAuctionId(), bid.getUserId(), bid.getTime(),bid.getValue());

        Session s = new Session();
        String res = s.checkCookieUser(session, bid.getUserId());
        if(!"ok".equals(res))
            throw new WebApplicationException(res, Response.Status.UNAUTHORIZED);

        if(true) {   //!(USE_CACHE && jedis.exists("auc:" + id))
            //id auction dont exist
            FindIterable<Document> auction = mongoAuction.getAuctionById(id);
            if (!auction.iterator().hasNext()) {
                throw new WebApplicationException("Auction does not exist", 404);
            }
            auctionDAO = documentToDAOAuction(auction.iterator().next());



        } else {
            String get = jedis.get("auc:" + id);
            ObjectMapper mapper = new ObjectMapper();
            auctionDAO = mapper.readValue(get, AuctionDAO.class);

        }

        if(!(USE_CACHE && jedis.exists("user:" + b.getUserId()))) {
            //check if user is exist
            FindIterable<Document> userDAO = mongoUser.getUserById(b.getUserId());
            if (!userDAO.iterator().hasNext()) {
                throw new WebApplicationException("User does not exist", 404);
            }
        }

        //make AuctionDAO to AuctionDTO
       Auction auctionDTO = new Auction(auctionDAO.getId(), auctionDAO.getTitle(), auctionDAO.getDescription(),
               auctionDAO.getImageId(), auctionDAO.getOwnerId(), auctionDAO.getEndTime(), auctionDAO.getMinPrice(),"",auctionDAO.getStatus());

        if(!(USE_CACHE && jedis.exists("bid:" + bid.getId()))) {
            //if bid exists, return error
            FindIterable<Document> bidDAO = mongo.getBidById(bid.getId(), bid.getAuctionId());
            if (bidDAO.iterator().hasNext()) {
                throw new WebApplicationException("Bid already exists", 409);
            }
        }

        verifyBid(auctionDTO,bid);

        @SuppressWarnings("unused")
		javax.ws.rs.core.Response response = mongo.putBid(b);

        if(USE_CACHE) {
            ObjectMapper mapper = new ObjectMapper();
            jedis.set("bid:" + bid.getId(), mapper.writeValueAsString(bid));
        }

        return bid;
    }

    @GET()
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Bid> listBids() {
        FindIterable<Document> auction = mongoAuction.getAuctionById(id);
        if(!auction.iterator().hasNext()){
            throw new WebApplicationException("Auction does not exist", 404);
        }
        //list bids
        FindIterable<Document> bids = mongo.getBids(id);
        //if auction doesnt exist, return error

        List<Bid> bidList = new ArrayList<>();
        for (Document bid : bids) {
        	BidDAO tbid = documentToDAOBid(bid);
            bidList.add(new Bid(tbid.getId(), tbid.getAuctionId(), tbid.getUserId(), tbid.getTime(),tbid.getValue()));
        }
        return bidList;
    }

    private void verifyBid(Auction auction, Bid bid) {
        //if auction is closed or deleted, return error
        if(auction.getStatus().equals("CLOSED") || auction.getStatus().equals("DELETED")){
            throw new WebApplicationException("Auction is closed or deleted", 409);
        }
        //if bid is lower than min price, return error
        if(bid.getValue() < auction.getMinPrice()){
            throw new WebApplicationException("Bid is lower than min price", 409);
        }
        //if bid is lower than last bid, return error
        Bid lastBid = getLastBid();
        if(lastBid != null && bid.getValue() <= lastBid.getValue()){
            throw new WebApplicationException("Bid is lower or equal than last bid", 409);
        }

        //if auction has ended, return error
        if(auction.getEndTime().before(new Date())){
            throw new WebApplicationException("Auction has ended", 409);
        }

    }

    //return bid that have lowest _ts
    private Bid getLastBid(){

        FindIterable<Document> bids = mongo.getBids(id);
        BidDAO lastBid = null;
        for (Document bid : bids) {
        	BidDAO tbid = documentToDAOBid(bid);
            if(lastBid == null){
                lastBid = tbid;
            }else{
                if(Integer.parseInt(tbid.get_ts()) > Integer.parseInt(lastBid.get_ts())){
                    lastBid =tbid;
                }
            }
        }

        if (lastBid == null) {
            return null;
        }

        Bid bid = new Bid(lastBid.getId(), lastBid.getAuctionId(), lastBid.getUserId(),new Date(),lastBid.getValue());
        return bid;

    }
    
    private static final BidDAO documentToDAOBid(Document bid) {
    	BidDAO dao;
    	dao = new BidDAO();
    	dao.setId(bid.getString("_id"));
    	dao.set_ts(bid.getString("ts"));
    	dao.set_rid(bid.getString("rid"));
    	dao.setAuctionId(bid.getString("auction"));
    	dao.setUserId(bid.getString("user"));
    	dao.setValue((float)bid.get("value"));
    	dao.setTime(bid.getDate(dao));	
    	return dao;
    }
    
    private static final AuctionDAO documentToDAOAuction(Document auction) {
		AuctionDAO auc;
		auc = new AuctionDAO();
		auc.setId(auction.getString("_id"));
		auc.setOwnerId(auction.getString("seller"));
		auc.setTitle(auction.getString("title"));
		auc.setDescription(auction.getString("description"));
		auc.setImageId(auction.getString("image"));
		auc.Date(auction.getString("endtime"));
		auc.setMinPrice((float)auction.get("minimumprice"));
		auc.setStatus((Status)auction.get("status"));
		auc.setWinnerId(auction.getString("winner"));
		auc.set_rid(auction.getString("rid"));
		auc.set_ts(auction.getString("ts"));
		return auc;
	}
    
}
