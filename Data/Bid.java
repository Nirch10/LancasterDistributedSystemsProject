package server.src.Data;

public class Bid {
    public int id;
    public int auctionId;
    public float bidAmount;
    public int bidderId;

    public Bid(int bidId, int bidAuctionId, float amount, int bidBidderId){
        id= bidId;
        auctionId = bidAuctionId;
        bidAmount = amount;
        bidderId = bidBidderId;
    }
}
