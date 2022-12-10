package scc.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import redis.clients.jedis.Jedis;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DAO.UserDAO;
import scc.Data.DTO.Auction;
import scc.Data.DTO.Login;
import scc.Data.DTO.Session;
import scc.Data.DTO.Status;
import scc.Data.DTO.User;
import scc.Database.MongoAuctionDBLayer;
import scc.Database.MongoBidDBLayer;

import scc.Database.MongoUserDBLayer;
import scc.cache.RedisCache;

import static jakarta.ws.rs.core.Response.Status.*;
import static scc.mgt.AzureManagement.USE_CACHE;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;

@Path("/user")
public class UserControllerMongo {

	private static final String HASHCODE = "SHA-256";
	private Jedis jedis;

	// https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/connection/connect/
	// fetch your own connection string from the mongo DB connect menu online
	// replace sccTP241888:o1f39bZM8mOMUKsZ with the user and password from your own
	// mongo DB
	private static final String CONNECTION_URL = "mongodb+srv://sccTP241888:o1f39bZM8mOMUKsZ@cluster0.afd7s1x.mongodb.net/?retryWrites=true&w=majority";
	// replace this with a different DB name if necessary

	MongoClientURI connectionString = new MongoClientURI(CONNECTION_URL);
	MongoClient client = new MongoClient(connectionString);

	private synchronized void initCache() {
		if (jedis != null)
			return;
		jedis = RedisCache.getCachePool().getResource();
	}

	private MongoUserDBLayer mongo = new MongoUserDBLayer(client);
	private MongoAuctionDBLayer mongoAuction = new MongoAuctionDBLayer(client);
	private MongoBidDBLayer mongoBid = new MongoBidDBLayer(client);

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User writeUser(User user) throws NoSuchAlgorithmException, JsonProcessingException {
		initCache();
		String userk = "user:" + user.getId();
		String pass = user.getPwd();
		if (USE_CACHE && jedis.exists(userk))
			throw new WebApplicationException("User already exists", 409);

		// get user first by id
		FindIterable<Document> userDAO = mongo.getUserById(user.getId());
		// if user exists, return error
		if (userDAO.iterator().hasNext()) {
			throw new WebApplicationException("User already exists", 409);
		}

		// if user have same nickname, return error
		FindIterable<Document> userDAO2 = mongo.getUserByNickname(user.getNickname());
		if (userDAO2.iterator().hasNext()) {
			throw new WebApplicationException("Nickname already exists", 409);
		}

		MessageDigest messageDigest = MessageDigest.getInstance(HASHCODE);
		messageDigest.update(user.getPwd().getBytes());

		String passHashed = new String(messageDigest.digest());
		user.setPwd(passHashed);
		UserDAO u = new UserDAO(user.getId(), user.getName(), user.getNickname(), user.getPwd(), user.getPhotoId());

		@SuppressWarnings("unused")
		javax.ws.rs.core.Response response = mongo.putUser(u);

		if (USE_CACHE) {
			ObjectMapper mapper = new ObjectMapper();
			jedis.set(userk, mapper.writeValueAsString(user));
		}

		user.setPwd(pass);
		return user;

	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@PathParam("id") String id) throws JsonProcessingException {
		initCache();
		String userk = "user:" + id;
		User u;

		if (USE_CACHE && jedis.exists(userk)) {
			ObjectMapper mapper = new ObjectMapper();
			u = mapper.readValue(jedis.get(userk), User.class);
		} else {
			FindIterable<Document> user = mongo.getUserById(id);
			if (!user.iterator().hasNext()) {
				throw new WebApplicationException("User not found", 404);
			}

			UserDAO userDAO = documentToDAOUser(user.iterator().next());
			u = new User(userDAO.getId(), userDAO.getName(), userDAO.getNickname(), userDAO.getPwd(),
					userDAO.getPhotoId());
		}

		if (USE_CACHE) {
			ObjectMapper mapper = new ObjectMapper();
			jedis.set(userk, mapper.writeValueAsString(u));
		}

		return u;
	}

	@DELETE()
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public FindIterable<Document> delUser(@CookieParam("scc:session") Cookie session, @PathParam("id") String id)
			throws JsonProcessingException {
		initCache();
		FindIterable<Document> user = null;
		Session s = new Session();
		if ("ok".equals(s.checkCookieUser(session, id))) {
			jedis.del("scc:session" + id);
			String userk = "user:" + id;

			if (USE_CACHE && jedis.exists(userk)) {
				jedis.del(userk);
			} else {

				user = mongo.getUserById(id);

				if (!user.iterator().hasNext()) {
					throw new WebApplicationException("User not found", 404);
				}
			}
			// else delete user
			@SuppressWarnings("unused")
			javax.ws.rs.core.Response response = mongo.delUserById(id);
		}
		return user;
	}

	@PUT()
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User updateUser(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, User user)
			throws NoSuchAlgorithmException, JsonProcessingException {
		initCache();
		Session s = new Session();
		String res = s.checkCookieUser(session, user.getId());
		if (!"ok".equals(res))
			throw new WebApplicationException(res, Response.Status.UNAUTHORIZED);

		String userk = "user:" + id;
		User u;
		if (USE_CACHE && jedis.exists(userk)) {
			ObjectMapper mapper = new ObjectMapper();
			u = mapper.readValue(jedis.get(userk), User.class);
		} else {
			FindIterable<Document> userDB = mongo.getUserById(id);

			if (!userDB.iterator().hasNext()) {
				throw new WebApplicationException("User not found", 404);
			}

			UserDAO udao = documentToDAOUser(userDB.iterator().next());
			u = new User(udao.getId(), udao.getName(), udao.getNickname(), udao.getPwd(), udao.getPhotoId());
		}

		// if User user have different nickname, check if nickname is already taken
		if (!u.getNickname().equals(user.getNickname())) {
			FindIterable<Document> userDAO2 = mongo.getUserByNickname(user.getNickname());
			if (userDAO2.iterator().hasNext()) {
				throw new WebApplicationException("Nickname already exists", 409);
			}
		}

		MessageDigest messageDigest = MessageDigest.getInstance(HASHCODE);
		messageDigest.update(user.getPwd().getBytes());

		String passHashed = new String(messageDigest.digest());
		user.setPwd(passHashed);

		UserDAO udao2 = new UserDAO(user.getId(), user.getName(), user.getNickname(), user.getPwd(), user.getPhotoId());

		@SuppressWarnings("unused")
		javax.ws.rs.core.Response response = mongo.updateUser(udao2);

		return user;
	}

	@GET
	@Path("/{id}/auctions")
	@Produces(MediaType.APPLICATION_JSON)
	// get auctions by user id
	public List<Auction> getAuctionsOfUser(@PathParam("id") String id) {
		FindIterable<Document> user = mongo.getUserById(id);

		FindIterable<Document> auctions;
		if (!user.iterator().hasNext()) {
			throw new WebApplicationException("User not found", 404);
		} else {
			auctions = mongoAuction.getAuctionsOfUser(id);
		}
		// put auctions in list
		List<Auction> auctionList = new ArrayList<>();
		for (Document auction : auctions) {
			auctionList.add(new Auction(auction.getString("_id"), auction.getString("title"),
					auction.getString("description"), auction.getString("image"), auction.getString("seller"),
					auction.getDate("endtime"), (float) auction.get("minimumprice"), auction.getString("winner"),
					(Status) auction.get("status")));
		}
		return auctionList;

	}

	@GET
	@Path("/{id}/auctionsopen")
	@Produces(MediaType.APPLICATION_JSON)
	// get auctions by user id
	public List<Auction> getOpenAuctionsOfUser(@PathParam("id") String id, @QueryParam("status") String status) {
		FindIterable<Document> user = mongo.getUserById(id);

		FindIterable<Document> auctions;
		if (!user.iterator().hasNext()) {
			throw new WebApplicationException("User not found", 404);
		} else {
			auctions = mongoAuction.getAuctionsOfUser(id);
		}
		// put auctions in list
		List<Auction> auctionList = new ArrayList<>();
		for (Document auction : auctions) {
			AuctionDAO tauction = documentToDAOAuction(auction);
			if (status == "OPEN" && tauction.getStatus().equals("OPEN")) {
				auctionList.add(new Auction(tauction.getId(), tauction.getTitle(), tauction.getDescription(),
						tauction.getImageId(), tauction.getOwnerId(), tauction.getEndTime(), tauction.getMinPrice(),
						tauction.getWinnerId(), tauction.getStatus()));
			}
		}
		return auctionList;

	}

	@GET
	@Path("/{id}/auctions/following")
	@Produces(MediaType.APPLICATION_JSON)
	// get auctions that user is following
	public List<Auction> getAuctionsUserFollow(@PathParam("id") String id) {

		FindIterable<Document> user = mongo.getUserById(id);

		FindIterable<Document> bids;
		if (!user.iterator().hasNext()) {
			throw new WebApplicationException("User not found", 404);
		} else {
			bids = mongoBid.getBidsByUser(id);
		}

		// put auctions in list
		List<Auction> auctionList = new ArrayList<>();
		for (Document bid : bids) {
			// get auction of bid
			FindIterable<Document> auction = mongoAuction.getAuctionById(bid.getString("auction"));
			if (auction.iterator().hasNext()) {
				auctionList.add(new Auction(auction.iterator().next().getString("_id"),
						auction.iterator().next().getString("title"),
						auction.iterator().next().getString("description"),
						auction.iterator().next().getString("image"), auction.iterator().next().getString("seller"),
						auction.iterator().next().getDate("endtime"),
						(float) auction.iterator().next().get("minimumprice"),
						auction.iterator().next().getString("winner"),
						(Status) auction.iterator().next().get("status")));
			}
		}
		return auctionList;

	}

	// authenticate user

	@POST
	@Path("/auth")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response auth(Login login) throws NoSuchAlgorithmException, JsonProcessingException {
		initCache();
		String user = login.getUser();
		String pwd = login.getPwd();
		if (user == null || user.equals("") || pwd == null || pwd.equals(""))
			return Response.status(NO_CONTENT).entity("Invalid user login").build();
		FindIterable<Document> userDB = mongo.getUserByNickname(user);
		if (!userDB.iterator().hasNext()) {
			return Response.status(NOT_FOUND).entity("User not found").build();
		}
		Document u = userDB.iterator().next();
		// hash password
		MessageDigest messageDigest = MessageDigest.getInstance(HASHCODE);
		messageDigest.update(pwd.getBytes());
		String passHashed = new String(messageDigest.digest());

		if (!u.getString("password").equals(passHashed)) {
			return Response.status(FORBIDDEN).entity("Wrong password").build();
		}

		String uid = UUID.randomUUID().toString();
		NewCookie cookie = new NewCookie.Builder("scc:session").value(uid).path("/").comment("sessionid").maxAge(3600)
				.secure(false).httpOnly(true).build();

		Session s = new Session(uid, user);
		ObjectMapper mapper = new ObjectMapper();
		jedis.set("userSession:" + u.getString("_id"), mapper.writeValueAsString(s));

		return Response.ok().cookie(cookie).build();

	}

	private static final AuctionDAO documentToDAOAuction(Document auction) {
		AuctionDAO auc;
		auc = new AuctionDAO();
		auc.setId(auction.getString("_id"));
		auc.set_rid(auction.getString("rid"));
		auc.set_ts(auction.getString("ts"));
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

	private static final UserDAO documentToDAOUser(Document bid) {
		UserDAO dao;
		dao = new UserDAO();
		dao.setId(bid.getString("_id"));
		dao.set_ts(bid.getString("ts"));
		dao.set_rid(bid.getString("rid"));
		dao.setName(bid.getString("name"));
		dao.setNickname(bid.getString("nickname"));
		dao.setPwd(bid.getString("password"));
		dao.setPhotoId(bid.getString("photo"));
		return dao;
	}
}