package scc.data;


public class Bid {

	String id;
	String bidderid;

	String targetauctionid;

	int value;

	public Bid() {
		
	}
	public Bid(String id, String bidderid, String targetauctionid, int value) {
		this();
		this.id=id;
		this.bidderid = bidderid;
		this.targetauctionid = targetauctionid;
		this.value = value;
	}

	public String getTargetAuction() {
		return targetauctionid;
	}

	public String getBidder() {
		return bidderid;
	}

	public int getValue() {
		return value;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setTargetAuction(String targetid) {
		this.targetauctionid = targetid;
	}

	public void setBidder(String bidderid) {
		this.bidderid = bidderid;
	}

	public void setValue(int value) {
		this.value = value;
	}

}