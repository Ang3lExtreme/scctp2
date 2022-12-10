package scc.Database;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

import static com.mongodb.client.model.Filters.*;

import javax.ws.rs.core.Response;
import scc.Data.DAO.UserDAO;

public class MongoUserDBLayer {
	// https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/connection/connect/
	// fetch your own connection string from the mongo DB connect menu online
	// replace sccTP241888:o1f39bZM8mOMUKsZ with the user and password from your own
	// mongo DB
	private static final String CONNECTION_URL = "mongodb+srv://sccTP241888:o1f39bZM8mOMUKsZ@cluster0.afd7s1x.mongodb.net/?retryWrites=true&w=majority";
	// replace this with a different DB name if necessary
	private static final String DB_NAME = "SCCMongo";
	// create database layer to create and update auctions

	private static MongoUserDBLayer instance;

	public static synchronized MongoUserDBLayer getInstance() {
		if (instance != null) {
			return instance;
		}

		MongoClientURI connectionString = new MongoClientURI(CONNECTION_URL);
		MongoClient client = new MongoClient(connectionString);

		try {
			instance = new MongoUserDBLayer(client);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instance;

	}

	private MongoClient client;
	private MongoDatabase db;
	private MongoCollection<Document> users;

	public MongoUserDBLayer(MongoClient client) {
		this.client = client;
	}

	private synchronized void init() {
		if (db != null)
			return;
		db = client.getDatabase(DB_NAME);
		users = db.getCollection("users");

	}

	public Response delUserById(String id) {
		init();

		Bson query = eq("_id", id);
		try {
			DeleteResult result = users.deleteOne(query);
			if (result.wasAcknowledged()) {
				return Response.ok().build();
			} else {
				return Response.noContent().build();
			}

		} catch (MongoException e) {
			System.err.println("Could not delete, error: " + e);
		}

		return Response.ok().build();
	}

	public Response delUser(UserDAO user) {
		init();

		Bson query = eq("_id", user.getId());
		try {
			DeleteResult result = users.deleteOne(query);
			if (result.wasAcknowledged()) {
				return Response.ok().build();
			} else {
				return Response.noContent().build();
			}

		} catch (MongoException e) {
			System.err.println("Could not delete, error: " + e);
		}

		return Response.ok().build();
	}

	public Response putUser(UserDAO user) {
		init();
		Document doc = toBsonDoc(user);
		users.insertOne(doc);
		return Response.ok().build();
	}

	public Response updateUser(UserDAO user) {
		init();
		Bson query = eq("_id", user.getId());
		Document auct = toBsonDoc(user);
		users.replaceOne(query, auct);
		return Response.ok().build();
	}

	public FindIterable<Document> getUserById(String id) {
		init();
		Bson query = eq("_id", id);
		FindIterable<Document> iterable = users.find(query);
		return iterable;
	}

	public FindIterable<Document> getUsers() {
		init();
		FindIterable<Document> iterable = users.find();
		return iterable;
	}

	public FindIterable<Document> getUserByNickname(String nickname) {
		init();
		Bson query = eq("nickname", nickname);
		FindIterable<Document> iterable = users.find(query);
		return iterable;
		
	}

	public void close() {
		client.close();
	}

	private static final Document toBsonDoc(UserDAO user) {
		Document doc;
		doc = new Document("_id", user.getId());
		doc.append("rid", user.get_rid());
		doc.append("ts", user.get_ts());
		doc.append("name", user.getName());
		doc.append("nickname", user.getNickname());
		doc.append("password", user.getPwd());
		doc.append("photo", user.getPhotoId());
		return doc;
	}
}