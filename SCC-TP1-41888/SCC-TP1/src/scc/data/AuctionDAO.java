package scc.data;



/**
 * Represents a User, as stored in the database
 */
public class AuctionDAO {
	private String _rid;
	private String _ts;
	
	String id;
	String title;
	String description;

	String photoId;
	String ownerid;
	int minimumbid;

	int endtime;
	String winnerid;

	int status;
	
	
	public AuctionDAO() {
		
	}
	
	public AuctionDAO(String id, String title, String description, String photoId, String ownerId, int endtime, int minimumBid) {
		this();
		this.id = id;
		this.title = title;
		this.description = description;
		this.photoId = photoId;
		this.ownerid = ownerId;
		this.minimumbid = minimumBid;
		//sets the end time to 3 hours from current date for now as a means to test, will change later
		this.endtime = endtime;
		this.winnerid = "None";
		this.status = 1;
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
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPhotoId() {
		return photoId;
	}
	public void setPhotoId(String imageid) {
		this.photoId= imageid;
	}
	public String getOwner() {
		return ownerid;
	}
	public void setOwner(String owner) {
		this.ownerid = owner;
	}
	public int getMinimumBid() {
		return minimumbid;
	}
	public void setMinimumBid(int minbid) {
		this.minimumbid = minbid;
	}
	
	public void setEndTime(int endTime) {
		this.endtime = endTime;
	}
	
	public int getEndTime() {
		return endtime;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getStatus() {
		return status;
	}
	
	@Override
	
	//TODO Complete method
	public String toString() {
		return String.format("{\n"
				+ "    \"id\": \"%s\",\n"
				+ "    \"title\": \"%s\",\n"
				+ "    \"description\": \"%s\",\n"
				+ "    \"photoId\": \"%s\",\n"
				+ "    \"ownerId\": \"%s\",\n"
				+ "    \"minimumBid\": %s\n"
				+ "}   ", id,title,description,photoId,ownerid,minimumbid);
	}

}
