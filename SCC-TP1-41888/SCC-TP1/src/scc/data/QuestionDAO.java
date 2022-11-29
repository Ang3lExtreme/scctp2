package scc.data;

/**
 * Represents a User, as stored in the database
 */
public class QuestionDAO {
	private String _rid;
	private String _ts;
	private String id;
	private String question;
	private String reply;
	private String askerid;
	private String targetauctionid;

	public QuestionDAO() {
	}
	/*public UserDAO( User u) {
		this(u.getId(), u.getName(), u.getPwd(), u.getPhotoId(), u.getChannelIds());
	}*/
	public QuestionDAO(String id, String question, String reply, String askerid, String targetauctionid) {
		this();
		this.id = id;
		this.question = question;
		this.reply = reply;
		this.askerid = askerid;
		this.targetauctionid = targetauctionid;
	}
	public String get_rid() {
		return _rid;
	}
	public void set_rid(String _rid) {
		this._rid = _rid;
	}
	public String get_ts() {
		return _ts;
	}
	public void set_ts(String _ts) {
		this._ts = _ts;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getReply() {
		return reply;
	}
	public void setReply(String reply) {
		this.reply = reply;
	}
	public String getAsker() {
		return askerid;
	}
	public void setAsker(String asker) {
		this.askerid = asker;
	}
	
	public void setTarget(String auctionid) {
		this.targetauctionid =  auctionid;
	}
	
	public String getTarget() {
		return targetauctionid;
	}
	
	public Question toQuestion() {
		return new Question(id, question, askerid, targetauctionid, reply );
	}
	@Override
	//TODO complete
	public String toString() {
		return String.format("{\n"
				+ "    \"id\": \"%s\",\n"
				+ "    \"question\": \"%s\",\n"
				+ "    \"reply\": \"%s\",\n"
				+ "    \"askerid\": \"%s\",\n"
				+ "    \"targetauctionid\": \"%s\"\n"
				+ "}   ", id,question,reply,askerid,targetauctionid);
	}

}
