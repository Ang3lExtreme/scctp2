package scc.Database;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import scc.Data.DAO.QuestionsDAO;

//same as CosmosBidDBLayer
public class MongoQuestionsDBLayer {

	// https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/connection/connect/
		// fetch your own connection string from the mongo DB connect menu online
		// replace sccTP241888:o1f39bZM8mOMUKsZ with the user and password from your own
		// mongo DB
		private static final String CONNECTION_URL = "mongodb+srv://sccTP241888:o1f39bZM8mOMUKsZ@cluster0.afd7s1x.mongodb.net/?retryWrites=true&w=majority";
		// replace this with a different DB name if necessary
		private static final String DB_NAME = "SCCMongo";
		// create database layer to create and update auctions

    private static MongoQuestionsDBLayer instance;

    public static synchronized MongoQuestionsDBLayer getInstance() {
    	if (instance != null) {
			return instance;
		}

		MongoClientURI connectionString = new MongoClientURI(CONNECTION_URL);
		MongoClient client = new MongoClient(connectionString);

		try {
			instance = new MongoQuestionsDBLayer(client);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instance;

    }
    private MongoClient client;
    private MongoDatabase db;
    private MongoCollection<Document> questions;

    public MongoQuestionsDBLayer(MongoClient client) {
        this.client = client;
    }

    private synchronized void init() {
        if( db != null)
            return;
        db = client.getDatabase(DB_NAME);
        questions = db.getCollection("questions");
    }

    public Response putQuestion(QuestionsDAO question) {
    	init();
		Document doc = toBsonDoc(question);
		questions.insertOne(doc);
		return Response.ok().build();
    }

    public FindIterable<Document> getQuestionById(String auctionId,String questionId) {
        init();
        Bson query = and(eq("_id", questionId),eq("auction", auctionId));
        FindIterable<Document> iterable = questions.find(query);
        return iterable;
    }

    public FindIterable<Document> getQuestions(String auctionId) {
    	init();
        Bson query = eq("auction", auctionId);
        FindIterable<Document> iterable = questions.find(query);
        return iterable;
    }
    public void close() {
        client.close();
    }

    public Response replyQuestion(QuestionsDAO qu) {
        init();
		Bson query = eq("_id", qu.getId());
		Document question = toBsonDoc(qu);
		questions.replaceOne(query, question);
		return Response.ok().build();
    }
    
    private static final Document toBsonDoc(QuestionsDAO question) {
		Document doc;
		doc = new Document("_id", question.getId());
		doc.append("auction", question.getAuctionId());
		doc.append("user", question.getUserId());
		doc.append("message", question.getMessage());
		doc.append("reply", question.getReply());
		doc.append("ts", question.get_ts());
		doc.append("rid", question.get_rid());
		return doc;
	}
}
