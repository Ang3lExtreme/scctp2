package scc.data;

/**
 * Represents a User, as stored in the database
 */
public class BidDAO {
	private String _rid;
	private String _ts;
	private String id;
	private String bidderid;
	private String targetauctionid;
	private int value;

	public BidDAO() {
	}
	
	public BidDAO(String id, String bidderid, String targetauctionid, int value) {
		this();
		this.id=id;
		this.bidderid = bidderid;
		this.targetauctionid = targetauctionid;
		this.value = value;
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
	public String getBidderId() {
		return bidderid;
	}
	public void setBidderId(String bidderid) {
		this.bidderid = bidderid;
	}
	public String getTargetAuctionId() {
		return targetauctionid;
	}
	public void setTargetAuctionId(String auctionid) {
		this.targetauctionid = auctionid;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}

	public Bid toBid() {
		return new Bid( id,bidderid, targetauctionid, value);
	}
	@Override
	public String toString() {
		return "BidDAO [_rid=" + _rid + ", _ts=" + _ts + ", bidderid=" + bidderid + "]";
	}

}
