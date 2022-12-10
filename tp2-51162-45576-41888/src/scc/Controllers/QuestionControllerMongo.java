package scc.Controllers;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import redis.clients.jedis.Jedis;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DAO.BidDAO;
import scc.Data.DAO.QuestionsDAO;
import scc.Data.DAO.UserDAO;
import scc.Data.DTO.Auction;
import scc.Data.DTO.Questions;
import scc.Data.DTO.Reply;
import scc.Data.DTO.Session;
import scc.Data.DTO.Status;
import scc.Database.CosmosAuctionDBLayer;
import scc.Database.CosmosQuestionsDBLayer;
import scc.Database.CosmosUserDBLayer;
import scc.Database.MongoAuctionDBLayer;
import scc.Database.MongoQuestionsDBLayer;
import scc.Database.MongoUserDBLayer;
import scc.cache.RedisCache;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import static scc.mgt.AzureManagement.USE_CACHE;

@Path("/auction/{id}/question")
public class QuestionControllerMongo {
	// https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/connection/connect/
	// fetch your own connection string from the mongo DB connect menu online
	// replace sccTP241888:o1f39bZM8mOMUKsZ with the user and password from your own
	// mongo DB
	private static final String CONNECTION_URL = "mongodb+srv://sccTP241888:o1f39bZM8mOMUKsZ@cluster0.afd7s1x.mongodb.net/?retryWrites=true&w=majority";
	// replace this with a different DB name if necessary

	MongoClientURI connectionString = new MongoClientURI(CONNECTION_URL);
	MongoClient client = new MongoClient(connectionString);

	MongoQuestionsDBLayer mongo = new MongoQuestionsDBLayer(client);
	MongoAuctionDBLayer mongoAuction = new MongoAuctionDBLayer(client);
	MongoUserDBLayer mongoUser = new MongoUserDBLayer(client);

	private Jedis jedis;

	private synchronized void initCache() {
		if (jedis != null)
			return;
		jedis = RedisCache.getCachePool().getResource();
	}

	@PathParam("id")
	private String id;

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Questions createQuestion(Questions question) {
		initCache();

		if (!(USE_CACHE && jedis.exists("auc:" + question.getAuctionId()))) {
			// if auction does not exist throw error
			FindIterable<Document> auction = mongoAuction.getAuctionById(id);
			if (!auction.iterator().hasNext()) {
				throw new WebApplicationException("Auction does not exist", 404);
			}

			// if auction is closed or deleted throw error
			if (auction.iterator().next().get("status").equals("CLOSED")
					|| auction.iterator().next().get("status").equals("DELETED")) {
				throw new WebApplicationException("Auction is closed or deleted", 409);
			}
		}

		if (!(USE_CACHE && jedis.exists("user:" + question.getUserId()))) {
			// if user does not exist throw error
			FindIterable<Document> user = mongoUser.getUserById(question.getUserId());
			if (!user.iterator().hasNext()) {
				throw new WebApplicationException("User does not exist", 404);
			}
		}

		if (!(USE_CACHE && jedis.exists("quest:" + question.getId()))) {
			// if question already exists throw error
			FindIterable<Document> questions = mongo.getQuestionById(question.getAuctionId(), question.getId());
			if (questions.iterator().hasNext()) {
				throw new WebApplicationException("Question already exists", 409);
			}
		}

		// create question
		QuestionsDAO qu = new QuestionsDAO(question.getId(), question.getAuctionId(), question.getUserId(),
				question.getMessage());
		javax.ws.rs.core.Response response = mongo.putQuestion(qu);
		return question;
	}

	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Questions> listQuestions() {
		initCache();
		if (!(USE_CACHE && jedis.exists("auc:" + id))) {
			// if auction does not exist return 404
			FindIterable<Document> auction = mongoAuction.getAuctionById(id);

			if (!auction.iterator().hasNext()) {
				throw new WebApplicationException("Auction does not exist", 404);
			}
		}

		// list all questions using cosmos
		List<Questions> questions = new ArrayList<>();
		FindIterable<Document> questionsDAO = mongo.getQuestions(id);
		for (Document q : questionsDAO) {
			questions.add(new Questions(q.getString("_id"), q.getString("auction"), q.getString("user"), q.getString("message")));
		}
		return questions;

	}

	@POST
	@Path("/{QuestionId}/reply")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Questions replyQuestion(@CookieParam("scc:session") Cookie session,
			@PathParam("QuestionId") String questionId, Reply reply) throws JsonProcessingException {
		initCache();
		// TODO: reply to question
		AuctionDAO auctionDAO;
		QuestionsDAO questionDAO;

		if (!(USE_CACHE && jedis.exists("auc:" + id))) {
			// if auction does not exist return 404
			FindIterable<Document> auction = mongoAuction.getAuctionById(id);
			if (!auction.iterator().hasNext()) {
				throw new WebApplicationException("Auction does not exist", 404);
			}
			auctionDAO = documentToDAOAuction(auction.iterator().next());
		} else {
			String get = jedis.get("auc:" + id);
			ObjectMapper mapper = new ObjectMapper();
			auctionDAO = mapper.readValue(get, AuctionDAO.class);

		}

		Session s = new Session();
		String res = s.checkCookieUser(session, auctionDAO.getOwnerId());
		if (!"ok".equals(res))
			throw new WebApplicationException(res, Response.Status.UNAUTHORIZED);

		if (!(USE_CACHE && jedis.exists("quest:" + questionId))) {
			// if question does not exist return 404
			FindIterable<Document> question = mongo.getQuestionById(id, questionId);
			if (!question.iterator().hasNext()) {
				throw new WebApplicationException("Question does not exist", 404);
			}
			// if question already has a reply return 409
			questionDAO = documentToDAOQuestion(question.iterator().next());
		} else {
			String get = jedis.get("quest:" + questionId);
			ObjectMapper mapper = new ObjectMapper();
			Questions question = mapper.readValue(get, Questions.class);
			questionDAO = new QuestionsDAO(question.getId(), question.getAuctionId(), question.getUserId(),
					question.getMessage());
		}

		if (questionDAO.getReply() != null) {
			throw new WebApplicationException("Question already has a reply", 409);
		}

		questionDAO.setReply(reply.getReply());
		javax.ws.rs.core.Response response = mongo.replyQuestion(questionDAO);
		return new Questions(questionDAO.getId(), questionDAO.getAuctionId(), questionDAO.getUserId(),
				questionDAO.getMessage(), questionDAO.getReply());

	}

	private static final QuestionsDAO documentToDAOQuestion(Document question) {
		QuestionsDAO dao;
		dao = new QuestionsDAO();
		dao.setId(question.getString("_id"));
		dao.set_ts(question.getString("ts"));
		dao.set_rid(question.getString("rid"));
		dao.setMessage(question.getString("message"));
		dao.setAuctionId(question.getString("auction"));
		dao.setUserId(question.getString("user"));
		dao.setReply(question.getString("reply"));
		return dao;
	}

	private static final AuctionDAO documentToDAOAuction(Document auction) {
		AuctionDAO auc;
		auc = new AuctionDAO();
		auc.setId(auction.getString("_id"));
		auc.setOwnerId(auction.getString("seller"));
		auc.setTitle(auction.getString("title"));
		auc.setDescription(auction.getString("description"));
		auc.setImageId(auction.getString("image"));
		auc.Date(auction.getString("endtime"));
		auc.setMinPrice((float) auction.get("minimumprice"));
		auc.setStatus((Status) auction.get("status"));
		auc.setWinnerId(auction.getString("winner"));
		return auc;
	}
}
