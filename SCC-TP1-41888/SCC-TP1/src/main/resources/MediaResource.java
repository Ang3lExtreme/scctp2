package main.resources;

import scc.utils.AzureProperties;
import scc.utils.Configurations;
import scc.utils.Hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource
{
	Map<String,byte[]> map = new HashMap<String,byte[]>();

	/**
	 * Post a new image.The id of the image is its hash.
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {
		String key = Hash.of(contents);
		map.put( key, contents);
		
		try {
			BinaryData data = BinaryData.fromBytes(contents);

			// Get container client
			BlobContainerClient containerClient = new BlobContainerClientBuilder()
														.connectionString(System.getenv("BlobStoreConnection"))
														.containerName("images")
														.buildClient();

			// Get client to blob
			BlobClient blob = containerClient.getBlobClient(key);

			// Upload contents from BinaryData (check documentation for other alternatives)
			blob.upload(data);
			
			System.out.println( "File updloaded : " + key);
			
		} catch( Exception e) {
			return null;
		}
		return key;
	}

	/**
	 * Return the contents of an image. Throw an appropriate error message if
	 * id does not exist.
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] download(@PathParam("id") String id) {
		// Get container client
		BlobContainerClient containerClient = new BlobContainerClientBuilder()
													.connectionString(System.getenv("BlobStoreConnection"))
													.containerName("images")
													.buildClient();
	
		// Get client to blob
		BlobClient blob = containerClient.getBlobClient(id);
	
		// Download contents to BinaryData (check documentation for other alternatives)
		BinaryData data = blob.downloadContent();
		
		byte[] arr = data.toBytes();
		
		return arr;
	}
	
}



