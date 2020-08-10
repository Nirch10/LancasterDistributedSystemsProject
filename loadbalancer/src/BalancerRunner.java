package loadbalancer.src;

import client.src.AuctionsHttpClient;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import server.src.AuctionHttpServer;
import server.src.Data.Tuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BalancerRunner implements HttpHandler {
    public Jedis redisRunner;
    public List<Tuple<server.src.Data.Tuple<String,Integer>,AuctionsHttpClient>> servers;
    public int currentServer;
    public HttpServer httpServer;
    private List<server.src.Data.Tuple<String, String>> headers;
    private int loadBalancerPort;
    private Timer timer;

    public BalancerRunner(){
        redisRunner = new Jedis(System.getenv("REDIS_HOST"));
        servers = new ArrayList<>();
        currentServer = 0;
        headers = new ArrayList<>();
    }
    public BalancerRunner(List<Tuple<String,Integer>> serversUrlList){
        redisRunner = new Jedis(System.getenv("REDIS_HOST"));
        //redisRunner = new Jedis();
        servers = new ArrayList<>();
        serversUrlList.forEach(ser -> {
           AuctionsHttpClient client = new AuctionsHttpClient("http://" + ser.t1 + ":" + ser.t2);
           servers.add(new Tuple<>(ser,client));
        });
        currentServer = 0;
        headers = new ArrayList<>();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

    }
    public void start(int listeningPort){
        try {
            timer= new Timer("MyTimer");//create a new Timer
            loadBalancerPort = listeningPort;
            httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(loadBalancerPort), loadBalancerPort);
            httpServer.createContext("/addServer", httpExchange -> addServer(httpExchange));
            httpServer.createContext("/", httpExchange -> proxy(httpExchange));
            manageRedisKeys();
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stop(){
        httpServer.stop(1);
        timer.cancel();
    }

    private void manageRedisKeys(){
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                addServer();
            }
        };


        timer.schedule(timerTask,10,30000);
}

    public void addServer(){
        int i=0;
        String url ="";
        System.out.println("trying to find servers");
        while ( redisRunner.get(System.getenv("USERNAME")+i)!=null){
            url = redisRunner.get(System.getenv("USERNAME")+i);
            System.out.println(url);
            String ip = url.split(":")[0];
            int port = Integer.parseInt(url.split(":")[1]);
            AuctionsHttpClient client = new AuctionsHttpClient("http://" + ip + ":" + port);
            servers.add(new Tuple<>(new Tuple<>(ip, port), client));
            redisRunner.del(System.getenv("USERNAME")+i);
            i++;
        }
    }

    public void addServer(HttpExchange httpExchange) throws IOException {
        try {
            //uri comes as /addServer?ip=1.1.1.1&port=1010
            //so in order to take the params we should split it
            String[] uri = String.valueOf(httpExchange.getRequestURI()).split("&");
            int port = Integer.parseInt(uri[1].split("=")[1]);
            String ip = uri[0].split("=")[1];
            if (servers.size() > 0) updateServer(ip, port);
            AuctionsHttpClient client = new AuctionsHttpClient("http://" + ip + ":" + port);
            servers.add(new Tuple<>(new Tuple<>(ip, port), client));
        } catch (Exception e) {
            return;
        }
    }

    private void updateServer(String ip, int port) throws IOException {
        if(!testServerAvailability(ip,port))return;
        int i = findAvailServerIndex();
        HttpResponse response = servers.get(i).t2.Get("/getState",headers);
        AuctionsHttpClient client =new AuctionsHttpClient( "http://"+ip+":"+port);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        client.Post("/setState",headers,new JSONObject(responseString));
        servers.add(new Tuple<>(new Tuple<>(ip,port),client));
    }
    private void proxy(HttpExchange httpExchange) throws IOException {
        HttpResponse requestHttpResponse = null;
            int i = findAvailServerIndex();
            if(i==-1){
                AuctionHttpServer.responseMessage(httpExchange, 501,"","{\"message\":\"No server avialable\"}");
                return;
            }
            String jsonState = redisRunner.get("state");
            System.out.println(jsonState);
            boolean resCode200 = true;
            if(jsonState != null) {
                JSONObject jsonObject = new JSONObject(jsonState);
                HttpResponse getResponse = servers.get(i).t2.Post("/setState", headers, jsonObject);
                if( getResponse.getStatusLine().getStatusCode() != 200)resCode200 = false;
            }
        if(resCode200){
            System.out.println("trying to make req");
            requestHttpResponse = (pass(httpExchange, servers.get(i)));
            currentServer = i;

        if (requestHttpResponse!=null){
            if (requestHttpResponse.getStatusLine().getStatusCode() == 200)
            {
                HttpResponse getStateResponse = servers.get(i).t2.Get("/getState",headers);
                if(getStateResponse.getStatusLine().getStatusCode()==200){
                    String data = getHttpResponseBody(getStateResponse);
                    redisRunner.set("state",data);
                }
            }
            String responseString = getHttpResponseBody(requestHttpResponse);
            AuctionHttpServer.responseMessage(httpExchange, requestHttpResponse.getStatusLine().getStatusCode(),"",responseString);
        }}
    }
    private String getHttpResponseBody(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        return responseString;
    }
    //search in the servers list a server that listens in its correct port so the balncaer can transfer the request
    private int findAvailServerIndex(){
       int i = currentServer ;boolean flag = false;
        while (i < currentServer + servers.size() && !flag){
            String ipAddr = servers.get(i%servers.size()).t1.t1;
            int port = servers.get(i%servers.size()).t1.t2;
            if(testServerAvailability(ipAddr, port))
                flag = true;
            else
                servers.remove(servers.get(i));
        }
        if(!flag)return -1;
        currentServer = (currentServer+1)%servers.size();
        return i%servers.size();
    }
    private HttpResponse pass(HttpExchange httpExchange,Tuple<server.src.Data.Tuple<String,Integer>,AuctionsHttpClient> serverWithClient) throws IOException {
        HttpResponse httpResponse = null;
        System.out.println("entered pass");
        JSONObject requestBody = new JSONObject(AuctionHttpServer.parseBody(httpExchange));
        System.out.println("body : "+requestBody.toString());
        Headers requestHeaders = httpExchange.getRequestHeaders();
        requestHeaders.keySet().stream().filter(key -> key.toLowerCase().equals("content-type") || key.toLowerCase().equals("authorization")).forEach(key -> headers.add(new Tuple<>(key,requestHeaders.getFirst(key).toString())));
        System.out.println("setHeaders : " + headers.size());
        System.out.println("Whyyy " + httpExchange.getRequestMethod() + httpExchange.getRequestURI() );
        System.out.println(httpExchange.getRequestMethod().toLowerCase());
        switch (httpExchange.getRequestMethod().toLowerCase()){
            case "get":{
                System.out.println(serverWithClient.t2.Url+"get");
                httpResponse = serverWithClient.t2.Get(httpExchange.getRequestURI().toString(),headers);
            break;}

                case "post":{
                    System.out.println(serverWithClient.t2.Url+"post");
                httpResponse = serverWithClient.t2.Post(httpExchange.getRequestURI().toString(),headers,requestBody);
break;}
            case "delete":{
                System.out.println(serverWithClient.t2.Url+"delete");
                httpResponse = serverWithClient.t2.Delete(httpExchange.getRequestURI().toString(),headers,requestBody);
break;}
            default:{
                httpResponse = null;
                System.out.println(serverWithClient.t2.Url+"wtf");}
        }
        headers.removeAll(headers);
        return httpResponse;
    }
    private boolean testServerAvailability(String ip, int port){
        Socket pingSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            pingSocket = new Socket(ip, port);
            out = new PrintWriter(pingSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(pingSocket.getInputStream()));
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
