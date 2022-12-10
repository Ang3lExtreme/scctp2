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
import scc.Data.DTO.Auction;
import scc.Data.DTO.Session;
import scc.Data.DTO.Status;
import scc.Database.MongoAuctionDBLayer;
import scc.Database.MongoUserDBLayer;
import scc.cache.RedisCache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;

import static scc.mgt.AzureManagement.USE_CACHE;

//testing
@Path("/auction")
public class AuctionControllerMongo {

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

    MongoAuctionDBLayer mongo =  new MongoAuctionDBLayer(client);
    MongoUserDBLayer mongoUser = new MongoUserDBLayer(client);

    @POST()
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Auction createAuction(@CookieParam("scc:session") Cookie session, Auction auction) throws JsonProcessingException {
        initCache();
        //create a AuctionDAO object
        AuctionDAO au = new AuctionDAO(auction.getAuctionId(), auction.getTitle(), auction.getDescription(),
                auction.getImageId(), auction.getOwnerId(), auction.getEndTime(), auction.getMinPrice());

        //if time is past, return null
        if(au.getEndTime().before(new Date())){
            throw new WebApplicationException("Cannot create auction in this time" , 409);
        }

        Session s = new Session();
        String res = s.checkCookieUser(session, auction.getOwnerId());
        if(!"ok".equals(res))
            throw new WebApplicationException(res, Response.Status.UNAUTHORIZED);

        String auk = "auc:" + auction.getAuctionId();
        String userk = "user:" + auction.getOwnerId();

        if(!(USE_CACHE && jedis.exists(userk))) {
            FindIterable<Document> user = mongoUser.getUserById(auction.getOwnerId());
            if (!user.iterator().hasNext()) {
                throw new WebApplicationException("Owner does not exist", 404);
            }
        }

        if(!(USE_CACHE && jedis.exists(auk))) {
            FindIterable<Document> auctionDAO = mongo.getAuctionById(auction.getAuctionId());
            if (auctionDAO.iterator().hasNext()) {
                throw new WebApplicationException("Auction already exists", 409);
            }
        } else
            throw new WebApplicationException("Auction already exists", 409);

        @SuppressWarnings("unused")
		javax.ws.rs.core.Response response = mongo.putAuction(au);

        if(USE_CACHE) {
            ObjectMapper mapper = new ObjectMapper();
            jedis.set("auc:" + auction.getAuctionId(), mapper.writeValueAsString(au));
        }

        return auction;
    }

    @PUT()
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Auction updateAuction(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, Auction auction) throws JsonProcessingException {
        initCache();
        String key = "auc:" + auction.getAuctionId();
        Document auc = null;
        AuctionDAO auc2 = documentToDAO(auc);
        if(USE_CACHE) {
            if(jedis.exists(key)) {
                String s = jedis.get(key);
                ObjectMapper mapper = new ObjectMapper();
                
                auc2 = mapper.readValue(s, AuctionDAO.class);
            }
        }

        if(auc == null) {
            FindIterable<Document> aucDB = mongo.getAuctionById(id);

            if (!aucDB.iterator().hasNext()) {
                throw new WebApplicationException("Auction dont exists", 404);
            }

            auc = aucDB.iterator().next();
        }

        AuctionDAO newau = new AuctionDAO(auction.getAuctionId(), auction.getTitle(), auction.getDescription(),
                auction.getImageId(), auction.getOwnerId(), auction.getEndTime(), auction.getMinPrice(), auction.getWinnerId(), auction.getStatus());

        Session s = new Session();
        String res = s.checkCookieUser(session, auc2.getOwnerId());
        if(!"ok".equals(res))
            throw new WebApplicationException(res, Response.Status.UNAUTHORIZED);

        verifyAuction(auc2, auction);
        @SuppressWarnings("unused")
		javax.ws.rs.core.Response response = mongo.updateAuction(newau);

        if(USE_CACHE) {
            ObjectMapper mapper = new ObjectMapper();
            jedis.set("auc:" + auction.getAuctionId(), mapper.writeValueAsString(newau));
        }

        return auction;

    }

    //get Auctions about to close
    @GET()
    @Path("/auctionsToClose")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Auction> getAuctionsToClose() throws JsonProcessingException {
        initCache();
        List<Auction> auctionList = new ArrayList<>();
        String key = "AuctionsToClose";

        if(USE_CACHE && jedis.exists(key)) {
            String s = jedis.get(key);
            ObjectMapper mapper = new ObjectMapper();
            auctionList = mapper.readValue(s, List.class);
        } else {
            //get auctions to close
            FindIterable<Document> auctions = mongo.getAuctionsToClose();

            for(Document auction : auctions){
    
                auctionList.add(new Auction(auction.getString("_id"), auction.getString("title"), auction.getString("description"),
                        auction.getString("image"), auction.getString("seller"), auction.getDate("endtime"), (float)auction.get("minimumprice"), auction.getString("winner"), (Status)auction.get("status")));
            }
        }

        //if USE_CACHE==true put auctionstoclose in cache with timetrigger

        //convert to Auction
        return auctionList;


    }

   /* @GET()
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public AuctionDAO getAuction() throws JsonProcessingException {
        initCache();
        String get = jedis.get("auc:" + "11");
        ObjectMapper mapper = new ObjectMapper();
        AuctionDAO auction = mapper.readValue(get, AuctionDAO.class);
        AuctionDAO au = new AuctionDAO(auction.getId(), auction.getTitle(), auction.getDescription(),
                auction.getImageId(), auction.getOwnerId(), auction.getEndTime(), auction.getMinPrice());

        return auction;
    }*/


    private void verifyAuction(AuctionDAO auctionToEdit, Auction edit){
        //if ownerid or winnerid dont exist, return error
        FindIterable<Document> user = mongoUser.getUserById(edit.getOwnerId());
        if(!user.iterator().hasNext()){
            throw new WebApplicationException("Owner does not exist", 404);
        }
        user = mongoUser.getUserById(edit.getWinnerId());
        if(!user.iterator().hasNext()){
            throw new WebApplicationException("Winner does not exist", 404);
        }
        //if auction if deleted, return error
        if(auctionToEdit.getStatus() == Status.DELETED){
            throw new WebApplicationException("Auction is deleted", 409);
        }
        //if auction is closed and status is OPEN, return error
        if(auctionToEdit.getStatus() == Status.CLOSED && edit.getStatus() == Status.OPEN){
            throw new WebApplicationException("Auction is closed", 409);
        }

        //if minprice is negative, return error
        if(edit.getMinPrice() < 0){
            throw new WebApplicationException("Min price is negative", 400);
        }

        //if edit.endtime is before now, return error
        if(edit.getEndTime().before(new Date())){
            throw new WebApplicationException("End time is before now", 400);
        }
        //if edit.id is different from auctionToEdit.id, return error
        if(!edit.getAuctionId().equals(auctionToEdit.getId())){
            throw new WebApplicationException("Auction id is different", 400);
        }

    }
    
    private static final AuctionDAO documentToDAO(Document auction) {
		AuctionDAO auc;
		auc = new AuctionDAO();
		auc.setId(auction.getString("_id"));
		auc.set_rid(auction.getString("rid"));
		auc.set_ts(auction.getString("ts"));
		auc.setOwnerId(auction.getString("seller"));
		auc.setTitle(auction.getString("title"));
		auc.setDescription(auction.getString("description"));
		auc.setImageId(auction.getString("image"));
		auc.Date(auction.getString("endtime"));
		auc.setMinPrice((float)auction.get("minimumprice"));
		auc.setStatus((Status)auction.get("status"));
		auc.setWinnerId(auction.getString("winner"));
		return auc;
	}

}