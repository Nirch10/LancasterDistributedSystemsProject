package server.src.Data;

import java.util.ArrayList;
import java.util.List;

public class JsonServerSerializer {
    public List<UserToken> UsersTokens;
    public List<Auction> Auctions;
    public List<Bid> Bids;

    public JsonServerSerializer() {
        UsersTokens = new ArrayList<>();
        Auctions = new ArrayList<>();
        Bids = new ArrayList<>();
    }
}
