import java.io.IOException;

public class Server {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        
        // prepare predictor
        Trainer tn = new Trainer();
        String modelPath = "data/Pool.model";
        tn.setModel(modelPath);


        /*
        //prepare judger
        Judger jg = new Judger(new String[]{"BAD", "FAIR", "GOOD"});

        // prepare handler
        RegularQueryHandler rqh = new RegularQueryHandler(tn, jg);
        
        // prepare server
        InetSocketAddress addr1 = new InetSocketAddress(20001);
        HttpServer server1 = HttpServer.create(addr1, -1);
        server1.createContext("/", rqh);
        server1.setExecutor(Executors.newCachedThreadPool());
        server1.start();
        System.out.println("Listening on port: 20001");
        
        /*
        InetSocketAddress addr2 = new InetSocketAddress(20002);
        HttpServer server2 = HttpServer.create(addr2, -1);
        server2.createContext("/", new GenreQueryHandler());
        server2.setExecutor(Executors.newCachedThreadPool());
        server2.start();
        System.out.println("Listening on port: 20002");
        */

    }

}
