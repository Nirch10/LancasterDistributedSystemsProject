package server.src;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public abstract class AbstractHttpServer<T> implements HttpHandler {

    public static HttpServer httpServer;
    public int port;

    public abstract void start();
}