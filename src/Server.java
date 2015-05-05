import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;


public class Server {
    
    /**
     * 1. pool server
     * request 127.0.0.1:20001/three?q=movie+name
     * response {"status":"success", "evaluation":"good"}
     * request 127.0.0.1:20001/seven?q=movie+name
     * response {"status":"success", "evaluation":"terrible"}
     * 
     * 2. genre server
     * request 127.0.0.1:20002/three?q=movie+name
     * response {"status":"success", "evaluation":"good"}
     * request 127.0.0.1:20002/seven?q=movie+name
     * response {"status":"success", "evaluation":"terrible"}
     * 
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        
        // prepare judger_7label
        GradeScale gradeScale = new GradeScale();
        gradeScale.add7Scale();
        Judger judger_7label = new Judger(gradeScale);
        // prepare judger_3label
        gradeScale = new GradeScale();
        gradeScale.add3Scale();
        Judger judger_3label = new Judger(gradeScale);
        
        // prepare handler pool_7label handler
        Trainer tn_pool_7label = new Trainer();
        tn_pool_7label.setModel("data/reviews_pool_after_negation_7label/model/pool.model");
        PoolQueryHandler pqh_7 = new PoolQueryHandler(tn_pool_7label, judger_7label);
        // prepare handler pool_3label handler
        Trainer tn_pool_3label = new Trainer();
        tn_pool_7label.setModel("data/reviews_pool_after_negation_3label/model/pool.model");
        PoolQueryHandler pqh_3 = new PoolQueryHandler(tn_pool_3label, judger_3label);
        // prepare pool server
        int port1 = 20001;
        InetSocketAddress addr1 = new InetSocketAddress(port1);
        HttpServer server1 = HttpServer.create(addr1, -1);
        server1.createContext("/three", pqh_3);
        server1.createContext("/seven", pqh_7);
        server1.setExecutor(Executors.newCachedThreadPool());
        server1.start();
        System.out.println("Listening on port: "+port1);

        // prepare handler genre_7label handler
        Map<String, Trainer> map_7label = new HashMap<String, Trainer>();
        File folder = new File("data/reviews_genre_after_negation_3label/model");
        File[] listOfFiles = folder.listFiles();
        for(File f : listOfFiles){
            Trainer tn = new Trainer();
            tn.setModel(f.getAbsolutePath());
            map_7label.put(f.getName(), tn);
        }
        GenreQueryHandler gqh_7 = new GenreQueryHandler(map_7label, judger_7label);
        // prepare handler genre_3label handler
        Map<String, Trainer> map_3label = new HashMap<String, Trainer>();
        folder = new File("data/reviews_genre_after_negation_3label/model");
        listOfFiles = folder.listFiles();
        for(File f : listOfFiles){
            Trainer tn = new Trainer();
            tn.setModel(f.getAbsolutePath());
            map_3label.put(f.getName(), tn);
        }
        GenreQueryHandler gqh_3 = new GenreQueryHandler(map_3label, judger_3label);
        // prepare genre server
        int port2 = 20002;
        InetSocketAddress addr2 = new InetSocketAddress(port2);
        HttpServer server2 = HttpServer.create(addr1, -1);
        server1.createContext("/three", gqh_3);
        server1.createContext("/seven", gqh_7);
        server1.setExecutor(Executors.newCachedThreadPool());
        server1.start();
        System.out.println("Listening on port: "+port2);

    }

}
