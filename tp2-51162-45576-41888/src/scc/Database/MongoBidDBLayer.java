package scc.Database;

import static com.mongodb.client.model.Filters.*;

import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import scc.Data.DAO.BidDAO;

public class MongoBidDBLayer {
	// https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/connection/connect/
	// fetch your own connection string from the mongo DB connect menu online
	// replace sccTP241888:o1f39bZM8mOMUKsZ with the user and password from your own when you have your own string
	private static final String CONNECTION_URL = "mongodb+srv://sccTP241888:o1f39bZM8mOMUKsZ@cluster0.afd7s1x.mongodb.net/?retryWrites=true&w=majority";
	// replace this with a different DB name if necessary
	private static final String DB_NAME = "SCCMongo";
	// create database layer to create and update auctions
	private static MongoBidDBLayer instance;

	public static synchronized MongoBidDBLayer getInstance() {
		if (instance != null) {
            return instance;
        }

        MongoClientURI connectionString = new MongoClientURI(CONNECTION_URL);
        MongoClient client = new MongoClient(connectionString);

        try {
			instance = new MongoBidDBLayer(client);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return instance;

	}

	private MongoClient client;
	private MongoDatabase db;
	private MongoCollection<Document> bids;

	public MongoBidDBLayer(MongoClient client) {
		this.client = client;
	}

	private synchronized void init() {
		if (db != null)
			return;
		db = client.getDatabase(DB_NAME);
		bids = db.getCollection("bid");
	}

	public Response putBid(BidDAO bid) {
		init();
		Document doc = toBsonDoc(bid);
        bids.insertOne(doc);
        return Response.ok().build();
	}

	public FindIterable<Document> getBids(String id) {
		init();
		Bson query = eq("_id", id);
		FindIterable<Document> iterable = bids.find(query);
        return iterable;
	}

	public FindIterable<Document> getBidsByUser(String userId) {
		init();
		Bson query = eq("user", userId);
		FindIterable<Document> iterable = bids.find(query);
        return iterable;
	}

	public FindIterable<Document> getBidById(String bidId, String auctionId) {
		init();
		
		Bson query = and(eq("_id", bidId),eq("auction", auctionId));
		FindIterable<Document> iterable = bids.find(query);
        return iterable;
	}

	public void close() {
		client.close();
	}
	
	private static final Document toBsonDoc(BidDAO bid) {
    	Document doc;
    	doc = new Document("_id", bid.getId());
    	doc.append("auction", bid.getAuctionId());
    	doc.append("user", bid.getUserId());
    	doc.append("value", bid.getValue());
    	doc.append("time", bid.getTime());
    	doc.append("rid", bid.get_rid());
    	doc.append("ts", bid.get_ts());
    	return doc;
    }

}
