package scc.data;

public class Question{
	
	String id;
	String question;
	
	String reply;
	
	String askerid;
	
	String targetauctionid;
	
	
	public Question(String id, String question, String reply,
			String askerid, String targetauctionid) {
		super();
		this.id = id;
		this.question = question;
		this.reply = reply;
		this.askerid = askerid;
		this.targetauctionid = targetauctionid;
	}
	
	public void setReply(String reply) {
		this.reply = reply;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public String getReply() {
		return reply;
	}
	
	public String getAsker() {
		return askerid;
	}
	
	public void setTargetAuction(String targetauctionid) {
		this.targetauctionid = targetauctionid;
	}
	
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public void setAsker(String askerid) {
		this.askerid = askerid;
	}
	
	public String getTargetAuction() {
		return targetauctionid;
	}
}
