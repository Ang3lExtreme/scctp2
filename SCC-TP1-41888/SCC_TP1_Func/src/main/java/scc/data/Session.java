package scc.data;

public class Session {

	String id;
	User user;
	
	public Session() {
		
	}
	public Session(String id, User u) {
		this.id = id;
		this.user = u;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
