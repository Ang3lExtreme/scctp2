package scc.data;

public class Login {
	
	String pwd;
	Object u;
	
	
	public Login() {
		
	}
	
	public Login(String pwd, Object user) {
		this.pwd = pwd;
		this.u = user;
	}
	
	public String getPwd() {
		return pwd;
	}
	
	public void setPwd(String id) {
		this.pwd = id;
	}
	
	public void setUser(User u) {
		this.u = u;
	}
	
	public String toString() {
		return pwd + u.toString();
	}
}
