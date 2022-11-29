package scc.cache;

import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import redis.clients.jedis.Jedis;
import scc.data.Session;
import scc.data.UserDAO;
import scc.utils.Configurations;

/**
 * TODO: needs following functionality
 * add highest bid for each auctions
 * add ...
 */
public class RedisLayer {
	
	private static final int MAX_USER_COUNT = 50;
	private static final int MAX_SESSION_COUNT = 50;
	
	/**
	 * Example method 1 from lab4
	 * does really make sense in the scope of the project so we comment it out
	 */
	public static void addMostRecentUser(UserDAO u) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			Locale.setDefault(Configurations.LOCALE);

			try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			    jedis.set("user:"+u.getId(), mapper.writeValueAsString(u));
			    Long cnt = jedis.lpush("MostRecentUsers", mapper.writeValueAsString(u));
			    if (cnt > MAX_USER_COUNT)
			        jedis.ltrim("MostRecentUsers", 0, -1);
			    cnt = jedis.incr("NumUsers");
			    //System.out.println( "Num users : " + cnt);
			    
	    
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Example method 2 from lab4
	 * does really make sense in the scope of the project so we comment it out
	 */
	public static String getUser(String id) {
		Locale.setDefault(Configurations.LOCALE);

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			String res = jedis.get("user:"+id);
			return "GET value = " + res;	
		}
	}
	
	/**
	 * Example method 3 from lab4
	 * does really make sense in the scope of the project so we comment it out
	 */
	public static List<String> listMostRecentUsers(){
		Locale.setDefault(Configurations.LOCALE);

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			List<String> lst = jedis.lrange("MostRecentUsers", 0, -1);			
			return lst;
		}
		
	}
	
	public static void addBestBidForAuction() {
		//TODO
	}


	public static void putSession(Session session) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			Locale.setDefault(Configurations.LOCALE);

			try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			    jedis.set("session:"+ session.getId(), mapper.writeValueAsString(session));
			    Long cnt = jedis.lpush("CurrentSessions", mapper.writeValueAsString(session));
			    if (cnt > MAX_SESSION_COUNT)
			        jedis.ltrim("MostRecentUsers", 0, -1);
			    cnt = jedis.incr("NumSessions");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static Session getSession(String value) {
		Locale.setDefault(Configurations.LOCALE);

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			String res = jedis.get("session:"+value);
			Gson g = new Gson();  
			Session user = g.fromJson(res, Session.class)  ;
			
			return user;	
		}
	}
	
	public static List<String> listCurrentSessions(){
		Locale.setDefault(Configurations.LOCALE);

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			List<String> lst = jedis.lrange("CurrentSessions", 0, -1);			
			return lst;
		}
	}
	
	public static void deleteAllSessions() {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
		    jedis.flushDB();
		}
	}
}
