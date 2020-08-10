package server.src;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import server.src.Data.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AuctionHttpServer extends AbstractHttpServer<Auction> {

    private final String TokenHeaderKey = "Authorization";
    private final String ErrorCodeMessage400 = "{\"message\":\"Invalid ID supplied\"}";
    private final String ErrorCodeMessage404 = "{\"message\":\"Auction Not Found\"}";
    private final String ErrorCodeMessage405 = "{\"message\":\"Invalid input\"}";
    private static List<Auction> auctions;
    private static List<Bid> bids;
    private static JSONObject jsonObject;
    private static OutputStream outputStream;
    private Gson jsonCreator;
    private List<UserToken> userTokenList;
    private JsonServerSerializeWrapper serializer;

    public AuctionHttpServer(int portNum){
        auctions = new ArrayList<>();
        bids = new ArrayList<>();
        jsonCreator = new Gson();
        port = portNum;
        userTokenList = new ArrayList<>();
        serializer = new JsonServerSerializeWrapper();
    }

    @Override
    public void handle(HttpExchange httpExchange) {

    }

    @Override
    public void start() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), port);
            System.out.println(httpServer.getAddress()+ "addr");
            httpServer.createContext("/api/user",httpExchange -> addUser(httpExchange));
            httpServer.createContext("/getState",httpExchange -> getStatus(httpExchange));
            httpServer.createContext("/setState",httpExchange -> setStatus(httpExchange));
            httpServer.createContext("/api/user/login",httpExchange -> {
                try {
                    loginUser(httpExchange);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            httpServer.createContext("/api/auctions", httpExchange -> getAuctions(httpExchange));
            httpServer.createContext("/",httpExchange -> defineUriAndDo(httpExchange));
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Stop(){
        try {
            httpServer.stop(0);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //Returns via HttpExchange a json with the current status of the server - current users,auctions and bids
    public void getStatus(HttpExchange httpExchange) throws IOException {
        try {
            List<User> userList = new ArrayList<>();
            userTokenList.forEach(u -> userList.add(u.User));
            JSONObject jsonObject = JsonServerSerializeWrapper.serialize(userTokenList, auctions, bids);
            responseMessage(httpExchange,200, "", jsonObject.toString());
        } catch (IOException e) {
            responseMessage(httpExchange,501, "","Unhandled server error");
        }
    }
    //Gets via the HttpExchnage a json with status of users,auctions and bids - and store them all in correct variables
    public void setStatus(HttpExchange httpExchange) throws IOException {
        try{
            JsonServerSerializer jsonServerSerializer = serializer.deserialize(parseBody(httpExchange));
            auctions = jsonServerSerializer.Auctions;
            bids = jsonServerSerializer.Bids;
            userTokenList = jsonServerSerializer.UsersTokens;
            responseMessage(httpExchange,200, "","{}");
        }
        catch (Exception e){
            responseMessage(httpExchange, 405,"", "{\"mes\":\""+e.getMessage()+"\"}}");
        }
    }

    //Auctions
    public void getAuctions(HttpExchange httpExchange) throws IOException {
        try{
            //testToken(httpExchange);
            Gson gson = new Gson();
            String res = gson.toJson(auctions);
            jsonObject = new JSONObject("{\"Auctions\": "+res+"}");
            if(auctions.isEmpty()) responseMessage(httpExchange,200, "/api/auctions",ErrorCodeMessage404);
            else responseMessage(httpExchange,200, "/api/auctions", jsonObject.toString());}
        catch (Exception e){
            //System.out.println(e);
            responseMessage(httpExchange,501, "/api/auctions","Unhandled server error");
        }
    }
    public void deleteAuction(HttpExchange httpExchange, int auctionId) throws IOException {
        testToken(httpExchange);
        try {
            Auction auction = auctions.stream().filter(auctionToFind -> auctionToFind.id == auctionId).findFirst().orElse(null);
            if (auction == null || auction.sellerId != getUser(httpExchange).id){
                responseMessage(httpExchange, 404, "", ErrorCodeMessage404);
            return;}
            auctions.remove(auction);
            responseMessage(httpExchange, 200, "", "{\"message\":\"Auction id : " + auctionId + " was deleted successfully\"}");
        } catch (Exception e) {
        }
        responseMessage(httpExchange, 400, "", ErrorCodeMessage400);
    }
    public void updateAuction(HttpExchange httpExchange, int auctionId, String name, String status) throws IOException {
        testToken(httpExchange);
        Auction auction = auctions.stream().filter(auctionToFind -> auctionToFind.id == auctionId).findFirst().orElse(null);
        if(auction == null){
            responseMessage(httpExchange, 404,"", ErrorCodeMessage404);
            return;
        }
        //TODO:lock function
        auctions.stream().filter(auctionToFind -> auctionToFind.id == auctionId).forEach(item -> {
            item.status = status;
            item.name = name;
        });
        responseMessage(httpExchange, 200, "", new JSONObject(auction).toString());
    }
    public void getAuction(HttpExchange httpExchange, int auctionId) throws IOException {
        testToken(httpExchange);
        try {
            Auction auction = auctions.stream().filter(auctionToFind -> auctionToFind.id == auctionId).findFirst().orElse(null);
            //TODO:lock function
            if (auction == null) {
                responseMessage(httpExchange, 404, "", ErrorCodeMessage404);
                return;
            }
            Gson gson = new Gson();
            String res = gson.toJson(auction);
            responseMessage(httpExchange, 200, "", res);
        } catch (Exception e) {
            responseMessage(httpExchange, 501, "", "Internal Server error");
        }
    }
    public void addAuction(HttpExchange httpExchange) throws IOException {
        testToken(httpExchange);
        jsonObject = parseBodyToJson(httpExchange);
        if (jsonObject == null){
            responseMessage(httpExchange,405, "", ErrorCodeMessage405);
        return;}
        //TODO:Lock function
        try {
            int id = getNewAuctionId();
            Auction auction = new Auction(id,jsonObject.optString("name"),"Avail",jsonObject.optFloat("firstBid", (float) 0.0),jsonObject.optInt("sellerId"),StoreAvail.available);
            if (auction.name.equals("")|| auction.firstBid == 0 || auction.sellerId==0){
                responseMessage(httpExchange,405, "", ErrorCodeMessage405);
                return;
            }
            auctions.add(auction);
            responseMessage(httpExchange, 200, "", jsonCreator.toJson(auction));
        }catch (Exception e){
            responseMessage(httpExchange,405, "", ErrorCodeMessage405);
        }
    }
    //Bid
    public void getBids(HttpExchange httpExchange, int auctionId) throws IOException {
        testToken(httpExchange);
        try {
            Object[] bidsForAuction = bids.stream().filter(bid -> bid.auctionId == auctionId).toArray();
            String res = jsonCreator.toJson(bidsForAuction);
            jsonObject = new JSONObject("{\"bids\":" + res + "}");
            responseMessage(httpExchange, 200, "", jsonObject.toString());
        } catch (Exception e) {
            responseMessage(httpExchange, 405, "", ErrorCodeMessage405);
        }


    }
    public void bidAuction(HttpExchange httpExchange, int auctionId, float bidAmount, int bidderId) throws IOException {
        testToken(httpExchange, bidderId);
        try {
            Auction auction = auctions.stream().filter(auctionToFind -> auctionToFind.id == auctionId).findFirst().orElse(null);
            if(auction == null || auction.sellerId == bidderId || bidAmount <= auction.firstBid)
            {
                responseMessage(httpExchange, 405, "", ErrorCodeMessage405);
                return;
            }
            Bid newBid = new Bid(bids.size() + 1, auctionId, bidAmount, bidderId);
            bids.add(newBid);
            Gson json = new Gson();
            responseMessage(httpExchange, 200, "", json.toJson(newBid));
        } catch (Exception e) {
            responseMessage(httpExchange, 405, "", ErrorCodeMessage405);
        }
    }
    //User Section
    public void addUser(HttpExchange httpExchange) throws IOException {
        JSONObject jsonObject = parseBodyToJson(httpExchange);
        if (jsonObject == null) {
            responseMessage(httpExchange, 404, "", ErrorCodeMessage404);
        return;}
        try {
            String userName = jsonObject.optString("username");
            String password = jsonObject.optString("password");
            if(userTokenList.stream().filter(u -> u.User.username.equals(userName) && u.User.password.equals(password)).findFirst().orElse(null)!=null)
                responseMessage(httpExchange, 400, "", ErrorCodeMessage400);
            else {
                createUser(userName, password);
                responseMessage(httpExchange, 200, "", new JSONObject(userTokenList.get(userTokenList.size()-1)).toString());
            }
        } catch (Exception e) {
            responseMessage(httpExchange, 400, "", ErrorCodeMessage400);
        }
    }
    public void loginUser(HttpExchange httpExchange) throws IOException, NoSuchAlgorithmException, InstantiationException, IllegalAccessException {
        JSONObject jsonObject = parseBodyToJson(httpExchange);
        String userName =  jsonObject.optString("username");
        String password =jsonObject.optString("password");
        System.out.println(userName + password);
        UserToken user = new UserToken(new User(0,userName,password),"");
        if((user = findUserInUsersList(user) )!= null){
           // if(user.Token.length()<5){
             //   System.out.println("false??"+userTokenList.get(user.User.id-1).Token);
              //  responseMessage(httpExchange, 200, "", userTokenList.get(user.User.id-1).Token);
               // return;
            //}
            userTokenList.get(user.User.id-1).Token = new JWTRunner().encode(userName);
            System.out.println("user logged in - token: " + userTokenList.get(user.User.id-1).Token);
            responseMessage(httpExchange, 200, "", userTokenList.get(user.User.id-1).Token);
        }
        else {
            System.out.println("shit");
            responseMessage(httpExchange, 405, "", ErrorCodeMessage405);
        }
    }

    //Static function - receives HttpExchange with data to send and returns it client
    public static void responseMessage(HttpExchange httpExchange, int code, String uri, String data) throws IOException {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type","application/json");
        httpExchange.sendResponseHeaders(code, bytes.length);
        outputStream = httpExchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.close();
    }
    public static String parseBody(HttpExchange httpExchange) throws IOException {
        System.out.println("trying prase body");
        String contentType = getRequestContentType(httpExchange);
        switch (contentType.toLowerCase()){
            case "application/json":{
                System.out.println("its json");
                return parseBodyToString(httpExchange);}
            case "application/x-www-form-urlencoded":
                return parseEncodedTypeBody(httpExchange);
            default:
                return parseBodyToString(httpExchange);
        }
    }

    //Private methods
    private static String getRequestContentType(HttpExchange httpExchange) {
        try{
            Set<Map.Entry<String, List<String>>> requestHeaders = httpExchange.getRequestHeaders().entrySet();
            Map.Entry<String, List<String>> contentTypeHeader = requestHeaders.stream().filter(header -> header.getKey().toLowerCase().equals("content-type")).findFirst().orElse(null);
            if(contentTypeHeader ==null)return "";
            System.out.println( String.valueOf(contentTypeHeader.getValue().get(0)));
            return String.valueOf(contentTypeHeader.getValue().get(0));}
        catch (Exception e){
            System.out.println("exce"+e);
            System.out.println("exce"+e.getMessage());
            e.printStackTrace();
            return null;}
    }
    private static String parseEncodedTypeBody(HttpExchange httpExchange) throws IOException  {
        String bodyEncoded = parseBodyToString(httpExchange);
        String body = URLDecoder.decode(bodyEncoded, "UTF8");
        if(body.charAt(0) == '=')
            return body.substring(1);
        return body;
    }
    private static String parseBodyToString(HttpExchange httpExchange) throws IOException {
        try{
            System.out.println("parsing json body here");
            if(httpExchange.getRequestBody()== null)return "{}";
            InputStream requestBody = httpExchange.getRequestBody();

        StringBuilder sb = new StringBuilder();
System.out.println("created sb" );
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(requestBody));
            System.out.println("created buffer");
        String i;int ii =0;
        while ((i = bufferedReader.readLine()) != null) {
            System.out.println("in"+ii+i);
            sb.append(i);
            ii++;
        }
        System.out.println(sb);
        if(ii==0){
            System.out.println("empty");
            return "{}";
        }
        return String.valueOf(sb);}
        catch (Exception e){
            System.out.println("error parsing body"+ e);
            return "";
        }
    }
    private String getToken(HttpExchange httpExchange){
        String token = String.valueOf(httpExchange.getRequestHeaders().get(TokenHeaderKey));
        token = token.replaceAll("Bearer ","");
        token  = token.substring(1, token.length()-1);
        return token;
    }
    private void testToken(HttpExchange httpExchange) throws IOException {
        String token = getToken(httpExchange);
        if(!isTokenValid(token)){
            responseMessage(httpExchange,405, "/api/auctions",ErrorCodeMessage405);
        }
    }
    private void testToken(HttpExchange httpExchange, int userId) throws IOException {
        String token = getToken(httpExchange);
        if(!isTokenValid(token)) {
            userTokenList.stream().filter(u -> u.Token.equals(token)).forEach(tok -> tok.Token = "");
            responseMessage(httpExchange, 405, "", ErrorCodeMessage405);
            return;
        }
        UserToken user = userTokenList.stream().filter(u -> u.User.id == userId).findFirst().orElse(null);
        if(user == null || (!user.Token.equals(token))) responseMessage(httpExchange, 400, "", "Token not valid.. please log in again");

    }
    private boolean isTokenValid(String token) {
        if (!isTokenInUsersList(token))return false;
        String decodedToken = null;
        try {
            decodedToken = new JWTRunner().decode(token);
            long tokenGeneratedExp = new JSONObject(decodedToken.split("\\.")[1]).optLong("exp");
            long currTime =System.currentTimeMillis();
            return tokenGeneratedExp > currTime;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }
    private boolean isTokenInUsersList(String token){
        AtomicBoolean ret = new AtomicBoolean(false);
        userTokenList.forEach(user ->{
            if(user.Token.equals(token))
               ret.set(true);
        });
        return ret.get();
    }
    private UserToken findUserInUsersList(UserToken userToFind) {
        List<UserToken> res = userTokenList.stream()
                .filter(a -> Objects.equals(a.User.username, userToFind.User.username) && Objects.equals(a.User.password,userToFind.User.password))
                .collect(Collectors.toList());
        if(res.size() > 0 )
            return res.get(0);
        return null;
    }
    private JSONObject parseBodyToJson(HttpExchange httpExchange) {
        try {
            String body = parseBody(httpExchange);
            JSONObject jsonObject = new JSONObject(body);
            return jsonObject;
        }catch (Exception e){
            return null;
        }
    }
    private void createUser(String userName, String password) {
        User user = new User(userTokenList.size()+1, userName, password);
        String token = "";
        UserToken userStringTuple = new UserToken(user, token);
        userTokenList.add(userStringTuple);
    }
    private void defineUriAndDo(HttpExchange httpExchange1) throws IOException {
        String uri = httpExchange1.getRequestURI().toString();
        String[] uriSplit = uri.split("/");
        String id = "";
        if(uriSplit.length <=3) id = uriSplit[uriSplit.length-1];
        else  id = uri.split("/")[3];
        if(uri.contains("/bids")) getBids(httpExchange1, Integer.parseInt(id));
        else if(uri.contains("/bid")){
            jsonObject = parseBodyToJson(httpExchange1);
            bidAuction(httpExchange1, Integer.parseInt(id),jsonObject.optFloat("bidAmount"),jsonObject.optInt("bidderId"));
        }
        else if(uri.contains("/api/auction")) defineAuctionFuncAndDo(uri, httpExchange1);
        else responseMessage(httpExchange1, 405, "" , ErrorCodeMessage405);
    }
    private void defineAuctionFuncAndDo(String uri, HttpExchange httpExchange) throws IOException {
        try{
        String Method = httpExchange.getRequestMethod();
        switch (Method.toLowerCase()){
            case "get":{
                int id = getIdFromUri(uri);
                if(id  == -1) responseMessage(httpExchange, 400,"",ErrorCodeMessage400);
                else getAuction(httpExchange, id);
                break;}
            case "delete":{
                int id = getIdFromUri(uri);
                if(id  == -1) responseMessage(httpExchange, 400,"",ErrorCodeMessage400);
                else deleteAuction(httpExchange, id);
                break;
            }
            case "post":{
                String[] uriSplited =uri.split("\\?");
                if(uriSplited.length < 2){
                    addAuction(httpExchange);return;}
                int id = getIdFromUri(uriSplited[0]);
                if(id == -1) responseMessage(httpExchange, 400,"",ErrorCodeMessage400);
                else try{String[] queryParams = uriSplited[uriSplited.length-1].split("&");
                    String nameParam =  queryParams[0].split("=")[1].replaceAll("%22","");
                    String statusParam =  queryParams[1].split("=")[1].replaceAll("%22","");
                    updateAuction(httpExchange, id,nameParam, statusParam );}
                catch (Exception e){
                    responseMessage(httpExchange, 405, "","Invalid Input");}

                break;
            }
            default:
                responseMessage(httpExchange, 404, "","{\"message\":\"url not found\"}");
        }}
        catch (Exception e){
            responseMessage(httpExchange, 405, "", "{\"message\":\"Invalid url input\"}");
        }
    }
    private int getIdFromUri(String uri) {
        try {
            String[] splittedUri = uri.split("/");
            int id = Integer.parseInt(splittedUri[splittedUri.length - 1]);
            return id;
        } catch (Exception e) {
            return -1;
        }
    }
    private User getUser(HttpExchange httpExchange) {
        String token = getToken(httpExchange);
        UserToken user = userTokenList.stream().filter(userStringTuple -> userStringTuple.Token.equals(token)).findFirst().orElse(null);
        if (user!= null)return user.User;
        return null;
    }
    private int getNewAuctionId() {
        for (int i =1;i<=auctions.size();i++){
            int finalI = i;
            if(auctions.stream().filter(auction -> auction.id == finalI).findFirst().orElse(null)==null)
                return i;
        }
        return auctions.size();
    }
}
