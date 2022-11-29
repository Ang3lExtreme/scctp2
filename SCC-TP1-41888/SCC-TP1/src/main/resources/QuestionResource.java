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
import scc.data.Auction;
import scc.data.AuctionDAO;
import scc.data.CosmosDBLayer;
import scc.data.Question;
import scc.data.QuestionDAO;
import scc.data.UserDAO;

@Path("/question")
public class QuestionResource {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String createQuestion(String json) {
		Gson g = new Gson();  
		Question question = g.fromJson(json, Question.class)  ;
		
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosItemResponse<QuestionDAO> res = null;
		QuestionDAO q = new QuestionDAO();
		
		q.setId(question.getId());
		q.setQuestion(question.getQuestion());
		q.setReply(question.getReply());
		q.setAsker(question.getAsker());
		q.setTarget(question.getTargetAuction());
		
		// check if given ownerid exists
		CosmosPagedIterable<UserDAO> resU = null;
		
		resU = db.getUserById(q.getAsker());
		
		Iterator<UserDAO> it = resU.iterator();
		
		if(!it.hasNext())
			throw new WebApplicationException(Status.NOT_FOUND);
		
		// check if targetauctionid exists
		CosmosPagedIterable<AuctionDAO> resA = null;
		
		resA = db.getAuctionById(q.getTarget());
		
		Iterator<AuctionDAO> itA = resA.iterator();
		
		if(!itA.hasNext())
			throw new WebApplicationException(Status.NOT_FOUND);
		
		
		res = db.putQuestion(q);
		

		String result = res.getItem().toString();
		return result;
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getBid(@PathParam("id") String id) {
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosPagedIterable<QuestionDAO> res = null;
		
		res = db.getQuestionById(id);	

		Iterator<QuestionDAO> it = res.iterator();
		
		if(it.hasNext()) {
			QuestionDAO u = it.next();
			return u.toString();
		} else throw new WebApplicationException(Status.NOT_FOUND);
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String updateBid(@PathParam("id") String id, String json) {
		Gson g = new Gson();  
		Question question = g.fromJson(json, Question.class)  ;
		
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosItemResponse<QuestionDAO> res = null;
		QuestionDAO q = new QuestionDAO();
		
		q.setId(question.getId());
		q.setQuestion(question.getQuestion());
		q.setReply(question.getReply());
		q.setAsker(question.getAsker());
		q.setTarget(question.getTargetAuction());
		
		res = db.upsertQuestion(q);
		

		String result = res.getItem().toString();
		return result;
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteQuestion(@PathParam("id") String id) {
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosItemResponse<Object> res = null;
		
		res = db.delQuestionById(id);
		String result = String.valueOf(res.getStatusCode());
		
		return result;
	}

}
