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
        
        // prepare judger_7scale
        GradeScale gradeScale = new GradeScale();
        gradeScale.add7Scale();
        Judger judger_7scale = new Judger(gradeScale);
        // prepare judger_3scale
        gradeScale = new GradeScale();
        gradeScale.add3Scale();
        Judger judger_3scale = new Judger(gradeScale);
        // prepare judger_2scale
        gradeScale = new GradeScale();
        gradeScale.add2Scale();
        Judger judger_2scale = new Judger(gradeScale);
        
        // prepare handler pool_7scale handler
        Trainer tn_pool_7scale = new Trainer();
        tn_pool_7scale.setModel("data/reviews_pool_after_negation_7scale/model/pool.model");
        PoolQueryHandler pqh_7 = new PoolQueryHandler(tn_pool_7scale, judger_7scale);
        // prepare handler pool_3scale handler
        Trainer tn_pool_3scale = new Trainer();
        tn_pool_3scale.setModel("data/reviews_pool_after_negation_3scale/model/pool.model");        
        PoolQueryHandler pqh_3 = new PoolQueryHandler(tn_pool_3scale, judger_3scale);
        // prepare handler pool_2scale handler
        Trainer tn_pool_2scale = new Trainer();
        tn_pool_2scale.setModel("data/reviews_pool_after_negation_2scale/model/pool.model");        
        PoolQueryHandler pqh_2 = new PoolQueryHandler(tn_pool_2scale, judger_2scale);
        
        // prepare pool server
        int port1 = 20001;
        InetSocketAddress addr1 = new InetSocketAddress(port1);
        HttpServer server1 = HttpServer.create(addr1, -1);
        server1.createContext("/two", pqh_2);
        server1.createContext("/three", pqh_3);
        server1.createContext("/seven", pqh_7);
        server1.setExecutor(Executors.newCachedThreadPool());
        server1.start();
        System.out.println("Listening on port: "+port1);

        // prepare handler genre_7scale handler
        Map<String, Trainer> map_7scale = new HashMap<String, Trainer>();
        File folder = new File("data/reviews_genres_after_negation_3scale/model");
        File[] listOfFiles = folder.listFiles();
        for(File f : listOfFiles){
            Trainer tn = new Trainer();
            tn.setModel(f.getAbsolutePath());
            map_7scale.put(f.getName(), tn);
        }
        GenreQueryHandler gqh_7 = new GenreQueryHandler(map_7scale, judger_7scale);
        // prepare handler genre_3scale handler
        Map<String, Trainer> map_3scale = new HashMap<String, Trainer>();
        folder = new File("data/reviews_genres_after_negation_3scale/model");
        listOfFiles = folder.listFiles();
        for(File f : listOfFiles){
            Trainer tn = new Trainer();
            tn.setModel(f.getAbsolutePath());
            map_3scale.put(f.getName(), tn);
        }
        GenreQueryHandler gqh_3 = new GenreQueryHandler(map_3scale, judger_3scale);
        // prepare handler genre_2scale handler
        Map<String, Trainer> map_2scale = new HashMap<String, Trainer>();
        folder = new File("data/reviews_genres_after_negation_2scale/model");
        listOfFiles = folder.listFiles();
        for(File f : listOfFiles){
            Trainer tn = new Trainer();
            tn.setModel(f.getAbsolutePath());
            map_2scale.put(f.getName(), tn);
        }
        GenreQueryHandler gqh_2 = new GenreQueryHandler(map_2scale, judger_2scale);
        // prepare genre server
        int port2 = 20002;
        InetSocketAddress addr2 = new InetSocketAddress(port2);
        HttpServer server2 = HttpServer.create(addr2, -1);
        server2.createContext("/two", gqh_2);
        server2.createContext("/three", gqh_3);
        server2.createContext("/seven", gqh_7);
        server2.setExecutor(Executors.newCachedThreadPool());
        server2.start();
        System.out.println("Listening on port: "+port2);

    }

}
