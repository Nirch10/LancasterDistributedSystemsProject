package server.src.Data;

public class Auction {
    public int id;
    public String name;
    public String status;
    public float firstBid;
    public int sellerId;
    public StoreAvail availability;

    public Auction(int objId, String objName, String objStatus, float objFirstBid, int objSellerId, StoreAvail objAvail){
        id= objId;
        name  = objName;
        status = objStatus;
        firstBid = objFirstBid;
        sellerId = objSellerId;
        availability = objAvail;
    }
}
