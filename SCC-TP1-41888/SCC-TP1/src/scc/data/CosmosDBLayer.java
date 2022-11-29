package scc.data;

import java.util.Iterator;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;

public class CosmosDBLayer {
	private static CosmosDBLayer instance;
	

	public static synchronized CosmosDBLayer getInstance() {
		if( instance != null)
			return instance;
		
		

		CosmosClient client = new CosmosClientBuilder()
		         .endpoint(System.getenv("COSMOSDB_URL"))
		         .key(System.getenv("COSMOSDB_KEY"))
		         //.directMode()
		         .gatewayMode()		
		         // replace by .directMode() for better performance
		         .consistencyLevel(ConsistencyLevel.SESSION)
		         .connectionSharingAcrossClientsEnabled(true)
		         .contentResponseOnWriteEnabled(true)
		         .buildClient();
		instance = new CosmosDBLayer( client);
		return instance;
		
	}
	
	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer users;
	private CosmosContainer bids;
	private CosmosContainer questions;
	private CosmosContainer auctions;
	
	public CosmosDBLayer(CosmosClient client) {
		this.client = client;
	}
	
	private synchronized void init() {
		if( db != null)
			return;
		
		
		db = client.getDatabase(System.getenv("COSMOSDB_DATABASE"));
		users = db.getContainer("users");
		bids = db.getContainer("bid");
		questions = db.getContainer("question");
		auctions = db.getContainer("auction");
	}
	
	// Auxiliarry
	public String delAllItems() {
		init();
		String result = "";
		
		// delete users
		CosmosPagedIterable<UserDAO> allusers = getUsers();
		Iterator<UserDAO> itUser = allusers.iterator();
		
		result = result + "Deleting users: \n";
		while(itUser.hasNext()) {
			UserDAO u = itUser.next();
			String id = u.getId();
			result = result + id + " " + String.valueOf(delUserById(id).getStatusCode()) + "\n";
		}
		result = result + "\n";
		
		// delete auctions
		CosmosPagedIterable<AuctionDAO> allauctions = getAuctions();
		Iterator<AuctionDAO> itAuction = allauctions.iterator();
		
		result = result + "Deleting auctions: \n";
		while(itAuction.hasNext()) {
			AuctionDAO a = itAuction.next();
			String id = a.getId();
			result = result + id + " " + String.valueOf(delAuctionById(id).getStatusCode()) + "\n";
		}
		result = result + "\n";
		
		// delete questions
		CosmosPagedIterable<QuestionDAO> allquestions = getQuestions();
		Iterator<QuestionDAO> itQuestions = allquestions.iterator();
		
		result = result + "Deleting questions: \n";
		while(itQuestions.hasNext()) {
			QuestionDAO q = itQuestions.next();
			String id = q.getId();
			result = result + id + " " + String.valueOf(delQuestionById(id).getStatusCode()) + "\n";
		}
		result = result + "\n";
		
		// delete bids
		CosmosPagedIterable<BidDAO> allbid = getBids();
		Iterator<BidDAO> itBids = allbid.iterator();
		
		result = result + "Deleting bids: \n";
		while(itBids.hasNext()) {
			BidDAO b = itBids.next();
			String id = b.getId();
			result = result + id + " " + String.valueOf(delBidById(id).getStatusCode()) + "\n";
		}
		
		return result;
	}

	// Users
	public CosmosItemResponse<Object> delUserById(String id) {
		init();
		PartitionKey key = new PartitionKey( id);
		return users.deleteItem(id, key, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<Object> delUser(UserDAO user) {
		init();
		return users.deleteItem(user, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<UserDAO> putUser(UserDAO user) {
		init();
		return users.createItem(user);
	}
	
	public CosmosPagedIterable<UserDAO> getUserById( String id) {
		init();
		return users.queryItems("SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public CosmosPagedIterable<UserDAO> getUsers() {
		init();
		return users.queryItems("SELECT * FROM users ", new CosmosQueryRequestOptions(), UserDAO.class);
	}
	
	public CosmosItemResponse<UserDAO> upsertUser(UserDAO u) {
		init();
		return users.upsertItem(u);
	}
	
	// Auctions
	public CosmosItemResponse<Object> delAuctionById(String id) {
		init();
		PartitionKey key = new PartitionKey( id);
		return auctions.deleteItem(id, key, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<Object> delAuction(AuctionDAO auction) {
		init();
		return auctions.deleteItem(auction, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<AuctionDAO> putAuction(AuctionDAO auction) {
		init();
		return auctions.createItem(auction);
	}
	public CosmosItemResponse<AuctionDAO> upsertAuction(AuctionDAO auction) {
		init();
		return auctions.upsertItem(auction);
	}
	
	//TODO: review this one..
	public CosmosPagedIterable<AuctionDAO> getAuctionById( String id) {
		init();
		return auctions.queryItems("SELECT * FROM auction WHERE auction.id=\"" + id + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}

	public CosmosPagedIterable<AuctionDAO> getAuctions() {
		init();
		return auctions.queryItems("SELECT * FROM auction ", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}
	
	// Questions
	
	public CosmosPagedIterable<QuestionDAO> getQuestions() {
		init();
		return questions.queryItems("SELECT * FROM question", new CosmosQueryRequestOptions(), QuestionDAO.class);
	}
	
	public CosmosItemResponse<Object> delQuestionById(String id) {
		init();
		PartitionKey key = new PartitionKey( id);
		return questions.deleteItem(id, key, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<QuestionDAO> putQuestion(QuestionDAO question) {
		init();
		return questions.createItem(question);
	}
	
	public CosmosItemResponse<QuestionDAO> upsertQuestion(QuestionDAO question) {
		init();
		return questions.upsertItem(question);
	}
	public CosmosPagedIterable<QuestionDAO> getQuestionById( String id) {
		init();
		return questions.queryItems("SELECT * FROM question WHERE question.id=\"" + id + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
	}
	
	public CosmosPagedIterable<QuestionDAO> getQuestionsByAuctionId(String id) {
		init();
		return questions.queryItems("SELECT * FROM question WHERE question.targetauctionid=\"" + id + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
	}
	
	// Bids
	//TODO: review what's necessary here...
	public CosmosPagedIterable<BidDAO> getBids() {
		init();
		return bids.queryItems("SELECT * FROM bid", new CosmosQueryRequestOptions(), BidDAO.class);
	}
	
	public CosmosItemResponse<Object> delBidById(String id) {
		init();
		PartitionKey key = new PartitionKey( id);
		return bids.deleteItem(id, key, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<BidDAO> putBid(BidDAO bid) {
		init();
		return bids.createItem(bid);
	}
	
	public CosmosItemResponse<BidDAO> upsertBid(BidDAO bid) {
		init();
		return bids.upsertItem(bid);
	}

	public CosmosPagedIterable<BidDAO> getBidsById(String id) {
		init();
		return bids.queryItems("SELECT * FROM bid WHERE bid.id=\"" + id+ "\"", new CosmosQueryRequestOptions(), BidDAO.class);
	}
	
	public CosmosPagedIterable<BidDAO> getBidByBidderId(String id) {
		init();
		return bids.queryItems("SELECT * FROM bid WHERE bid.bidderid=\"" + id +"\"", new CosmosQueryRequestOptions(), BidDAO.class);
	}
	
	public void close() {
		client.close();
	}
	
	
}
