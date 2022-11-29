package main.resources;

import java.util.Locale;

import com.azure.cosmos.models.CosmosItemResponse;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import scc.data.CosmosDBLayer;
import scc.utils.Configurations;

/**
 * Class with control endpoints.
 */
@Path("/ctrl")
public class ControlResource
{

	/**
	 * This methods just prints a string. It may be useful to check if the current 
	 * version is running on Azure.
	 */
	@Path("/version")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
		return "v: "+Configurations.CONTROL_VERSION;
	}
	
	@Path("/database")
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	public String deleteAllDatabaseItems() {
		Locale.setDefault(Locale.US);
		CosmosDBLayer db = CosmosDBLayer.getInstance();
		
		return db.delAllItems();
	}


}
