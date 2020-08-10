package server.src.api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import server.src.Data.Auction;
import server.src.AbstractHttpServer;
import server.src.Data.StoreAvail;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Path("/api/auctions")
public class AuctionHttpServer extends AbstractHttpServer<Auction> {

    private List<Auction> auctions;
    private JSONObject jsonObject;
    private OutputStream outputStream;

    public AuctionHttpServer( int portNum){
        auctions = new ArrayList<>();
        auctions.add(new Auction(1,"A","old", (float) 1.5,1, StoreAvail.available));
        port = portNum;

    }



    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

    }

    @Override
    public void start() {

        try {
            httpServer = HttpServer.create(new InetSocketAddress(port),port);
            httpServer.createContext("/",h ->{
                get();
            });
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void post() {

    }

    @Override
    public void get(int auction) {
        if(auctions.contains(auction)){
            jsonObject = new JSONObject(auction);
            ResponseMessage(200, "/api/auction/{id}", jsonObject.toString());
        }
        else return;
    }

    @Override
    public void post(Auction auction) {

    }

    @GET
    @Produces("application/json")
    @Override
    public void get(){
        try{
            System.out.println("Received get");
            Gson gson = new Gson();
            String res = gson.toJson(auctions);
        jsonObject = new JSONObject("{\"Auctions\": "+res+"}");
        if(auctions.isEmpty()) ResponseMessage(501, "/api/auctions","No auction found");
        else ResponseMessage(200, "/api/auctions", jsonObject.toString());}
        catch (Exception e){
            System.out.println(e);
        }
    }

    private void ResponseMessage(int code, String uri, String data){
        httpServer.createContext(uri, handler ->{
            handler.sendResponseHeaders(code, data.length());
            outputStream = handler.getResponseBody();
            outputStream.write(data.toString().getBytes());
            outputStream.close();
        });
    }
}
