package main.resources;

import java.util.Iterator;
import java.util.Locale;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.google.gson.Gson;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import scc.data.AuctionDAO;
import scc.data.Bid;
import scc.data.BidDAO;
import scc.data.CosmosDBLayer;
import scc.data.UserDAO;

@Path("/bid")
public class BidResource {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String createBid(String json) {
		Gson g = new Gson();  
		Bid bid = g.fromJson(json, Bid.class);
		
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosItemResponse<BidDAO> res = null;
		BidDAO b = new BidDAO();
		
		// check if given ownerid exists
		CosmosPagedIterable<UserDAO> resU = null;
		
		resU = db.getUserById(bid.getBidder());
		
		Iterator<UserDAO> it = resU.iterator();
		
		if(!it.hasNext())
			throw new WebApplicationException(Status.NOT_FOUND);
		
		// check if targetauctionid exists
		CosmosPagedIterable<AuctionDAO> resA = null;
		
		resA = db.getAuctionById(bid.getTargetAuction());
		
		Iterator<AuctionDAO> itA = resA.iterator();
		
		if(!itA.hasNext())
			throw new WebApplicationException(Status.NOT_FOUND);
		
		b.setId(bid.getId());
		b.setBidderId(bid.getBidder());
		b.setTargetAuctionId(bid.getTargetAuction());
		b.setValue(bid.getValue());
		
		res = db.putBid(b);

		String result = res.getItem().toString();
		return result;
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getBid(@PathParam("id") String id) {
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosPagedIterable<BidDAO> res = null;
		
		res = db.getBidsById(id);
		Iterator<BidDAO> it = res.iterator();
		
		if(it.hasNext()) {
			BidDAO u = it.next();
			return u.toString();
		} else throw new WebApplicationException(Status.NOT_FOUND);
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String updateBid(@PathParam("id") String id, String json) {
		Gson g = new Gson();  
		Bid bid = g.fromJson(json, Bid.class);
		
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosItemResponse<BidDAO> res = null;
		BidDAO b = new BidDAO();
		
		b.setId(bid.getId());
		b.setBidderId(bid.getBidder());
		b.setTargetAuctionId(bid.getTargetAuction());
		b.setValue(bid.getValue());
		
		res = db.upsertBid(b);
		
		String result = res.getItem().toString();
		return result;
	}

}
