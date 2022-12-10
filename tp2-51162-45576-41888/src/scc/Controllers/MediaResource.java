package scc.Controllers;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import scc.utils.Hash;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import jakarta.ws.rs.core.MediaType;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource {
	public static final String DIR = "/mnt/vol";

	Map<String, byte[]> map = new HashMap<String, byte[]>();
	String storageConnectionString = System.getenv("BlobStoreConnection");
	BlobContainerClient containerClientImages = new BlobContainerClientBuilder()
			.connectionString(storageConnectionString).containerName("images").buildClient();

	BlobClient blob;

	// upload image
	@POST
	@Path("/")
	@Consumes(MediaType.MEDIA_TYPE_WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public String UploadImages(byte[] data) throws IOException {
		// TODO: review key generation...
		// String key = UUID.randomUUID().toString().substring(0,8);

		String filename = Hash.of(data);
		try {
			File f = new File(DIR + filename);

			try (FileOutputStream outputstream = new FileOutputStream(f)) {
				outputstream.write(data);
			}
			return filename;
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException(e.getMessage());
		} catch (IOException e) {
			throw new IOException(e.getMessage());
		}
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response DownloadImages(@PathParam("id") String filename) {
		try {
			File file = new File(DIR + filename);
			if (!file.exists())
				return Response.status(400).entity("Image does not exist.").build();
			byte[] bytes = Files.readAllBytes(file.toPath());
			return Response.ok(bytes).build();
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}

	}
}
