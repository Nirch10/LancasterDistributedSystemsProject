package loadbalancer.src;

import server.src.Data.Tuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LoadBalancer {
  public static void main(String[] args) throws IOException {
    System.out.println("Balancing...");
    List<Tuple<String,Integer>> servers = new ArrayList<>();
    //servers.add(new Tuple<>("127.0.0.1",8080));
    BalancerRunner balancerRunner = new BalancerRunner(servers);
    balancerRunner.start(8080);
    System.out.println("Balancer Serving");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    br.readLine();
  }
}