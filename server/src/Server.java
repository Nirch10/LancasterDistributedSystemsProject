package server.src;

import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

public class Server {
  public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InstantiationException, IllegalAccessException {
   AuctionHttpServer serverHandler = new AuctionHttpServer(8080);
    serverHandler.start();
    System.out.println("Serving...");
    String ip = InetAddress.getLocalHost().getHostName();
    System.out.println(ip);
    Jedis redis = new Jedis(System.getenv("REDIS_HOST"));
    loadToRedis(redis,ip,8080);
   // AuctionsHttpClient client = new AuctionsHttpClient("http://127.0.0.1:9090");
   // try {
    //  client.Post("/addServer?ip="+ip+"&port=8080",new ArrayList<>(),new JSONObject());
    //}catch (Exception e){
     // System.out.println("Serving alone - couldnot connect load balancer");
    //}
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    br.readLine();
  }

  private static void loadToRedis(Jedis redis, String ip, int port) {
    int i=0;
    while (redis.get(System.getenv("USERNAME")+i)!=null){
      i++;
    }
    redis.set(System.getenv("USERNAME")+i,ip+":"+port);
    System.out.println(redis.get(System.getenv("USERNAME")+i));
    System.out.println(System.getenv("USERNAME")+i);
  }

  public static String getIp(String interfaceName) throws SocketException {
    String ip = "";
    NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
    Enumeration<InetAddress> inetAddress = networkInterface.getInetAddresses();
    InetAddress currentAddress;
    currentAddress = inetAddress.nextElement();
    while(inetAddress.hasMoreElements())
    {
      currentAddress = inetAddress.nextElement();

      if(currentAddress instanceof Inet4Address )
      {
        ip = currentAddress.toString();
        break;
      }
    }
    if (ip!= "")return ip;
    return null;
  }

}