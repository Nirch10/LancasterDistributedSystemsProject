package server.src;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

public abstract class AbstractHttpServer<T> implements HttpHandler {

    public HttpServer httpServer;
    public int port;
    public IDataGenerator<T> dataGenerator;

    public abstract void start();
    public abstract void stop();

    public abstract void  post();

    public abstract void get(int t);

    public abstract void post(T t);

    public abstract void get();
}