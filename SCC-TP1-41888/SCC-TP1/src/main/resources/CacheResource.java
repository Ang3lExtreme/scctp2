package main.resources;

import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import scc.cache.RedisLayer;

@Path("/cache")
public class CacheResource {
	
	@GET
	@Path("/users/list")
	@Produces(MediaType.APPLICATION_JSON)
	public String getMostRecentUsers() {
		String res = "";
		List<String> l = RedisLayer.listMostRecentUsers();
		for (String s: l) {
			res = res + s + "\n";
		}
		return res;
	}
	
	@GET
	@Path("/session/list")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCurrentSessions() {
		String res = "";
		List<String> l = RedisLayer.listCurrentSessions();
		for (String s: l) {
			res = res + s + "\n";
		}
		return res;
	}
	
	@GET
	@Path("/session/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCurrentSessions(@PathParam("id") String id) {
		return RedisLayer.getSession(id).getUser().toString();
	}
	
	@DELETE
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public void deleteCache() {
		RedisLayer.deleteAllSessions();
	}
}
