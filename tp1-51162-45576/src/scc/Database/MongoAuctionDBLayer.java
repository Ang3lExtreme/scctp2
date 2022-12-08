package scc.Database;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;

import scc.Data.DAO.AuctionDAO;

import java.sql.Timestamp;
import java.util.Date;

import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoAuctionDBLayer {

	// https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/connection/connect/
	// fetch your own connection string from the mongo DB connect menu online
	// replace sccTP241888:o1f39bZM8mOMUKsZ with the user and password from your own
	// mongo DB
	private static final String CONNECTION_URL = "mongodb+srv://sccTP241888:o1f39bZM8mOMUKsZ@cluster0.afd7s1x.mongodb.net/?retryWrites=true&w=majority";
	// replace this with a different DB name if necessary
	private static final String DB_NAME = "SCCMongo";
	// create database layer to create and update auctions

	private static MongoAuctionDBLayer instance;

	public static synchronized MongoAuctionDBLayer getInstance() {
		if (instance != null) {
			return instance;
		}

		MongoClientURI connectionString = new MongoClientURI(CONNECTION_URL);
		MongoClient client = new MongoClient(connectionString);

		try {
			instance = new MongoAuctionDBLayer(client);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instance;
	}

	private MongoClient client;
	private MongoDatabase db;
	private MongoCollection<Document> auctions;

	public MongoAuctionDBLayer(MongoClient client) {
		this.client = client;
	}

	private synchronized void init() {
		if (db != null) {
			return;
		}
		db = client.getDatabase(DB_NAME);
		auctions = db.getCollection("auction");
	}

	// TODO: Delete this or the other get auctions
	public FindIterable<Document> getAuctionsById(String id) {
		init();
		// ID is unique therefore we can do it this way
		Bson query = eq("_id", id);
		FindIterable<Document> iterable = auctions.find(query);
		return iterable;
	}

	public Response putAuction(AuctionDAO auction) {
		init();
		Document doc = toBsonDoc(auction);
		auctions.insertOne(doc);
		return Response.ok().build();
	}

	public Response updateAuction(AuctionDAO auction) {
		init();
		Bson query = eq("_id", auction.getId());
		Document auct = toBsonDoc(auction);
		auctions.replaceOne(query, auct);
		return Response.ok().build();
	}

	public FindIterable<Document> getAuctionById(String id) {
		init();
		// ID is unique therefore we can do it this way
		Bson query = eq("_id", id);
		FindIterable<Document> iterable = auctions.find(query);
		return iterable;
	}

	public FindIterable<Document> getAuctions() {
		init();
		FindIterable<Document> iterable = auctions.find();
		return iterable;
	}

	public FindIterable<Document> getAuctionsOfUser(String id) {
		init();
		// get all auctions where the user is the seller
		Bson query = eq("seller", id);
		FindIterable<Document> iterauctions = auctions.find(query);
		return iterauctions;
	}

	public void close() {
		client.close();
	}

	private static final Document toBsonDoc(AuctionDAO auction) {
		Document doc;
		doc = new Document("_id", auction.getId());
		doc.append("seller", auction.getOwnerId());
		doc.append("title", auction.getTitle());
		doc.append("description", auction.getDescription());
		doc.append("image", auction.getImageId());
		doc.append("endtime", auction.getEndTime());
		doc.append("minimumprice", auction.getMinPrice());
		doc.append("status", auction.getStatus());
		return doc;
	}

	public FindIterable<Document> getAuctionsToClose() {
		init();
		// get all auctions that are less than 1 hour away from and are not closed
		Timestamp timestamp = new Timestamp(System.currentTimeMillis() + 3600000);
		Bson query = lt("endtime", timestamp.getTime());
		FindIterable<Document> iterable = auctions.find(query);
		return iterable;

	}
}
