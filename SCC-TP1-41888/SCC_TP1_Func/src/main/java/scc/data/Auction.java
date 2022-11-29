package scc.data;

//import java.sql.Timestamp;

public class Auction {

	String id;
	String title;
	String description;
	String photoId;
	String ownerid;
	int endTime;
	int minimumBid;
	String winnerid;
	int status; // 1: means the auction is open; 0: means the auction is closed;

	public Auction() {
		
	}

	public Auction(String id, String title, String description, String photoId,
			 String ownerid,  int minimumBid) {
		this();
		this.id=id;
		this.title = title;
		this.description = description;
		this.photoId = photoId;
		this.ownerid = ownerid;
		this.endTime= 60;
		this.minimumBid = minimumBid;
		this.winnerid = "None";
		this.status = 1;
	}

	public void setWinner(String winnerid) {
		this.winnerid = winnerid;
		status = 0;
	}

	public void setStatus(int status) {
		this.status = status;
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

	public String getDescription() {
		return description;
	}

	public String getPhotoId() {
		return photoId;
	}

	public String getOwner() {
		return ownerid;
	}

	public int getMinimumBid() {
		return minimumBid;
	}

	// TODO: change this according to future variable changes
	public int getEndTime() {
		return endTime;
	}
	

	public void setTitle(String title) {
		this.title = title;
		
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setPhotoId(String imageid) {
		this.photoId = imageid;
	}

	public void setOwner(String ownerid) {
		this.ownerid = ownerid;
	}

	public void setMinimumBid(int minimumbid) {
		this.minimumBid = minimumbid;
	}

	// TODO: change this according to future variable changes
	public void setEndTime(int endtime) {
		this.endTime = endtime;
	}

	
}
