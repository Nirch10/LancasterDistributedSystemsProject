package client.src;

import org.json.JSONObject;
import server.src.Data.Tuple;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientRunner {
    public AuctionsHttpClient HttpClient;
    public List<Tuple<String,String >> Headers ;
    public String Token;

    public ClientRunner(String serverBasicUrl){
        HttpClient = new AuctionsHttpClient(serverBasicUrl);
        Headers = new ArrayList<>();
        Headers.add(new Tuple<>("Content-Type","application/x-www-form-urlencoded"));
    }

    //Gets
    public void GetAuctions(){
        HttpClient.Get("/api/auctions",Headers);
    }
    public void GetAuction(int id){
        HttpClient.Get("/api/auction/"+String.valueOf(id),Headers);
    }
    public void GetBidsForAuction(int id){
        HttpClient.Get("/api/auction/"+String.valueOf(id)+"/bids",Headers);
    }

    //Posts
    public void PostNewAction(String name, float firstBid, int sellerId) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name",name);
        jsonObject.put("firstBid",firstBid);
        jsonObject.put("sellerId",sellerId);
        HttpClient.Post("/api/auction",Headers,jsonObject);
    }
    public void PostUpdateAuction(int auctionId, String name, String status) throws IOException {
        String params = "?name=" + name + "&status=" + status;
        HttpClient.Post("/api/auction/" + auctionId + params, Headers, new JSONObject());
    }
    public void PostBidPlace(int auctionId, float bidAmount, int bidderId) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("bidAmount",(float)bidAmount);
        jsonObject.put("bidderId",(int)bidderId);
        HttpClient.Post("/api/auction/"+auctionId+"/bid",Headers,jsonObject);
    }
    public void PostNewUser(String username, String password) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username",username);
        jsonObject.put("password",password);
        HttpClient.Post("/api/user", Headers, jsonObject);
    }
    public void PostLoginUser(String username, String password) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("password", password);
        HttpClient.Post("/api/user/login", Headers, jsonObject);
        Headers.add(new Tuple<>("Authorization", "Bearer "+ Token));
    }

    //Deletes
    public void DeleteAuction(int auctionId) throws IOException {
        HttpClient.Delete("/api/auction/"+auctionId,Headers,new JSONObject());
    }
}
