package client.src;

import java.io.IOException;

public class Client {
  public static void main(String[] args) throws IOException {
    //System.out.println("Type the ip addr");
    //Scanner scanner = new Scanner(System.in);
    //String ipAddr = scanner.nextLine();
    //Docker ip = 172.17.0.1
    ClientRunner client = new ClientRunner("http://127.0.0.1:8080");
    //client.PostNewUser("nirChod","111");
    client.PostLoginUser("nirChod","111");
    client.Headers.forEach(h ->System.out.println(h.t1 +":"+ h.t2));
    client.PostNewAction("item1", (float) 9.9,1);
    client.PostNewAction("item2", (float) 78,1);
    client.GetBidsForAuction(1);
    client.GetAuctions();
    client.GetAuction(1);
    client.PostNewAction("item3", (float) 14.2,1);
    client.GetAuctions();
    client.PostUpdateAuction(2,"item2","sold");
    client.GetAuctions();
    client.PostBidPlace(1, (float) 10.0,1);
    client.PostBidPlace(2, (float) 15.0,1);
    client.PostBidPlace(3, (float) 12.0,1);
    client.PostBidPlace(3, (float) 15.3,1);
    client.GetBidsForAuction(1);
    client.GetBidsForAuction(2);
    client.GetBidsForAuction(3);
    client.DeleteAuction(2);
    client.GetBidsForAuction(2);
    client.GetAuctions();
    System.out.println("Making requests...");
  }
}