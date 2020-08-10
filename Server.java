package server.src;

import com.sun.net.httpserver.HttpServer;
import server.src.Data.Auction;
import server.src.api.AuctionHttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Server {
  public static void main(String[] args) throws IOException {

    AbstractHttpServer<Auction> serverHandler = new AuctionHttpServer(8080);
    serverHandler.start();
    System.out.println("Serving...");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    br.readLine();
  }
}