package client.src;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import server.src.Data.Tuple;

import java.io.IOException;
import java.util.List;

public class AuctionsHttpClient extends AbstractHttpClient {

    public String Url;

    public AuctionsHttpClient(String url){
        Url = url;
    }

    public HttpResponse Get(String uri, List<Tuple<String,String>> headers) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        return GetRequest(httpClient,Url + uri,headers);
    }
    public HttpResponse Post(String uri, List<Tuple<String,String>> headers, JSONObject requestBody) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        return PostRequest(httpClient, Url + uri,headers , requestBody);
    }
    public HttpResponse Delete(String uri, List<Tuple<String,String>> headers, JSONObject requestBody) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        return DeleteRequest(httpClient, Url + uri,headers , requestBody);
    }

    private HttpResponse GetRequest(CloseableHttpClient client, String url, List<Tuple<String,String>> headers){
        try {
            HttpGet httpGet = new HttpGet(url.toString());
            headers.forEach(header -> httpGet.addHeader(header.t1,header.t2));
            System.out.println("Executing request " + httpGet.getRequestLine());
            ResponseHandler<String> responseHandler = HandleResponse();
            HttpResponse response = client.execute(httpGet);
            System.out.println("----------------------------------------");
            System.out.println(response);
            return response;
        }
        catch (Exception e){}
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private HttpResponse PostRequest(CloseableHttpClient client, String url, List<Tuple<String,String>> headers, JSONObject requestBody) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        headers.forEach(header -> httpPost.addHeader(header.t1,header.t2));
        if(requestBody.length() > 0)httpPost.setEntity(new StringEntity(requestBody.toString()));
        //if(queryParams.size()>0)httpPost.setEntity(new UrlEncodedFormEntity(queryParams));
        System.out.println("Executing request " + httpPost.getRequestLine());
        ResponseHandler<String> responseHandler = HandleResponse();
        HttpContext httpContext = new HttpCoreContext();
        HttpResponse response = client.execute(httpPost);

        System.out.println(response);
        System.out.println("----------------------------------------");
        System.out.println("----------------------------------------");
        return response;
    }
    private HttpResponse DeleteRequest(CloseableHttpClient client, String url, List<Tuple<String,String>> headers, JSONObject requestBody) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        headers.forEach(header -> httpDelete.addHeader(header.t1,header.t2));
        System.out.println("Executing request " + httpDelete.getRequestLine());
        ResponseHandler<String> responseHandler = HandleResponse();
        HttpResponse response = client.execute(httpDelete);
        System.out.println("----------------------------------------");
        System.out.println(response);
        return response;
    }
    private ResponseHandler<String> HandleResponse() {
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

            @Override
            public String handleResponse(
                    final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };
        return responseHandler;
    }
}
