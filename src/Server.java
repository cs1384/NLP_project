import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;


public class Server {
    private static int port = 20001;
    public static void main(String[] args) throws IOException {
        
        InetSocketAddress addr = new InetSocketAddress(port);
        HttpServer server = HttpServer.create(addr, -1);
        server.createContext("/", new QueryHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Listening on port: " + Integer.toString(port));

    }

}
