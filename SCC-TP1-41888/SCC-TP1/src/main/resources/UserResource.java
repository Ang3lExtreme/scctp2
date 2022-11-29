package main.resources;

import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.google.gson.Gson;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import scc.cache.RedisLayer;
import scc.data.CosmosDBLayer;
import scc.data.Session;
import scc.data.User;
import scc.data.UserDAO;
import scc.mgt.AzureManagement;

@Path("/user")
public class UserResource {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String createUser(String json) {
		Gson g = new Gson();  
		User user = g.fromJson(json, User.class)  ;
		
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosItemResponse<UserDAO> res = null;
		UserDAO u = new UserDAO();
		u.setId(user.getId());
		u.setName(user.getName());
		u.setPwd(user.getPwd());
		u.setPhotoId(user.getPhotoId());
		
		try {
			res = db.putUser(u);
			
		} catch (CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
		
		if(AzureManagement.CREATE_REDIS)
			RedisLayer.addMostRecentUser(u);
		
		String result = res.getItem().toString();
		
		return result;
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUser(@PathParam("id") String userId) {
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosPagedIterable<UserDAO> res = null;
		
		res = db.getUserById(userId);
		
		Iterator<UserDAO> it = res.iterator();
		
		if(it.hasNext()) {
			UserDAO u = it.next();
			return u.toString();
		} else throw new WebApplicationException(Status.NOT_FOUND);
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String updateUser(@PathParam("id") String userId, String json) {
		Gson g = new Gson();  
		User user = g.fromJson(json, User.class)  ;
		
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosItemResponse<UserDAO> res = null;
		UserDAO u = new UserDAO();
		u.setId(user.getId());
		u.setName(user.getName());
		u.setPwd(user.getPwd());
		u.setPhotoId(user.getPhotoId());

		res = db.upsertUser(u);
		
		String result = res.getItem().toString();
		
		return result;
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteUser(@PathParam("id") String userId) {
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		CosmosItemResponse<Object> res = null;
		
		res = db.delUserById(userId);
		String result = String.valueOf(res.getStatusCode());
		
		return result;
	}
	
	@POST
	@Path("/auth")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response auth(@QueryParam("pwd") String pwd, String json) {
		Gson g = new Gson();  
		User user = g.fromJson(json, User.class)  ;
		// Check pwd
		if(user.getPwd().equals(pwd)) {
		String uid = UUID.randomUUID().toString();
		NewCookie cookie = new NewCookie.Builder("scc_session")
		.value(uid)
		.path("/")
		.comment("sessionid")
		.maxAge(3600)
		.secure(false)
		.httpOnly(true)
		.build();
		RedisLayer.putSession( new Session( uid, user));
		return Response.ok().cookie(cookie).build();
		} else
		throw new NotAuthorizedException("Incorrect login");
		}
	
}
