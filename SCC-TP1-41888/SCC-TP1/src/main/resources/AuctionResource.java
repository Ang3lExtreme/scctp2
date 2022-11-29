package main.resources;

import java.util.Iterator;
import java.util.Locale;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.google.gson.Gson;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import scc.cache.RedisLayer;
import scc.data.Auction;
import scc.data.AuctionDAO;
import scc.data.CosmosDBLayer;
import scc.data.User;
import scc.data.UserDAO;
import scc.mgt.AzureManagement;

@Path("/auction")
public class AuctionResource {
	
	//TODO

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String postAuction(@CookieParam("scc_session") Cookie session, String json) {		
		Gson g = new Gson();  
		Auction auction = g.fromJson(json, Auction.class)  ;
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		
		// auth check
		if(AzureManagement.CREATE_REDIS) {	// does not authenticate if does not have cache		
			try {
				// Check that auction is correct
				String sessionId = session.getValue();
				User user = RedisLayer.getSession(sessionId).getUser();
				if(!user.getId().equals(auction.getOwner()))
					throw new NotAuthorizedException("Invalid user : " + user);
				// Code to create auction
			} catch( WebApplicationException e) {
				throw e;
			} catch( Exception e) {
				throw new InternalServerErrorException( e);
			}	
		}
		// check if given ownerid exists
		CosmosPagedIterable<UserDAO> resU = null;
		
		resU = db.getUserById(auction.getOwner());
		
		Iterator<UserDAO> it = resU.iterator();
		
		if(!it.hasNext())
			throw new WebApplicationException(Status.NOT_FOUND);
		
		// create auction
		CosmosItemResponse<AuctionDAO> res = null;
		AuctionDAO a = new AuctionDAO();
		
		a.setId(auction.getId());
		a.setTitle(auction.getTitle());
		a.setDescription(auction.getDescription());
		a.setPhotoId(auction.getPhotoId());
		a.setOwner(auction.getOwner());
		a.setEndTime(60);
		a.setMinimumBid(auction.getMinimumBid());
		
		// check if auction exists
		try {			
			res = db.putAuction(a);
		} catch (CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
		
		
		return res.getItem().toString();
		
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAuction(@PathParam("id") String id) {
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosPagedIterable<AuctionDAO> res = null;
		
		res = db.getAuctionById(id);
		
		Iterator<AuctionDAO> it = res.iterator();
		
		if(it.hasNext()) {
			AuctionDAO u = it.next();
			return u.toString();
		} else throw new WebApplicationException(Status.NOT_FOUND);
	}
	
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllAuctions() {
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosPagedIterable<AuctionDAO> res = null;
		
		res = db.getAuctions();
		
		Iterator<AuctionDAO> it = res.iterator();
		
		String al = "";
		
		while(it.hasNext()) {
			AuctionDAO u = it.next();
			al += u.toString();
			al += "\n";
		} 
		return al;
	}
	
	@GET
	@Path("/fromuser/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllAuctionsFromUser(@PathParam("id") String id) {
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosPagedIterable<AuctionDAO> res = null;
		
		res = db.getAuctions();
		
		Iterator<AuctionDAO> it = res.iterator();
		
		String al = "";
		
		while(it.hasNext()) {
			AuctionDAO u = it.next();
			if(u.getOwner().equals(id)) {
				al += u.toString();
				al += "\n";
			}
		} 
		return al;
	}
	
	@GET
	@Path("/abouttoclose")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAuctionsAboutToClose() {
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosPagedIterable<AuctionDAO> res = null;
		res = db.getAuctions();
		
		Iterator<AuctionDAO> it = res.iterator();
		
		String al = "";
		
		while(it.hasNext()) {
			AuctionDAO u = it.next();
			if(u.getEndTime() <= 60) {
				al += u.toString();
				al += "\n";
			}
		} 
		return al;
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String updateAuction(@PathParam("id") String id, String json) {
		Gson g = new Gson();  
		Auction auction = g.fromJson(json, Auction.class)  ;
		
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosItemResponse<AuctionDAO> res = null;
		AuctionDAO a = new AuctionDAO();
		
		a.setId(auction.getId());
		a.setTitle(auction.getTitle());
		a.setDescription(auction.getDescription());
		a.setPhotoId(auction.getPhotoId());
		a.setOwner(auction.getOwner());
		a.setEndTime(auction.getEndTime());
		a.setMinimumBid(auction.getMinimumBid());
		
		res = db.upsertAuction(a);
		
		String result = res.getItem().toString();
		
		return result;
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteAuction(@PathParam("id") String id) {
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosItemResponse<Object> res = null;
		
		res = db.delAuctionById(id);
		String result = String.valueOf(res.getStatusCode());
		
		return result;
	}
}
