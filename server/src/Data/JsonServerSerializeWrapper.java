package server.src.Data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;
import java.util.List;

public class JsonServerSerializeWrapper {
    private JsonServerSerializer JsonServerSerializer;

    public JsonServerSerializeWrapper(){
        JsonServerSerializer = new JsonServerSerializer();
    }

    public static JSONObject serialize(List<UserToken> userTokenList, List<Auction> auctionList, List<Bid> bidList){
        Gson gson = new Gson();
        String auctionsList = gson.toJson(auctionList);
        String usersList = gson.toJson(userTokenList);
        String bidsList = gson.toJson(bidList);
        try{
            JSONObject jsonObject = new JSONObject("{\"Auctions\": "+auctionsList+",\"UsersTokens\":"+usersList+",\"Bids\":"+bidsList+"}");
            return jsonObject;
        }
        catch (Exception e){
            return null;
        }
    }
    public JsonServerSerializer deserialize(String json) {
        try {
            Gson gson = new GsonBuilder().create();
            JsonServerSerializer = gson.fromJson(json, JsonServerSerializer.getClass());
            return JsonServerSerializer;
        } catch (Exception e) {
            return null;
        }
    }
}
