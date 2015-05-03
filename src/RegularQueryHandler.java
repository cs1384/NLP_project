import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;

import org.json.JSONException;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class RegularQueryHandler implements HttpHandler{
    
    private Trainer predictor = null;
    private TwitterCommunicator tc = null;
    private Judger judger = null;
    
    public RegularQueryHandler(Trainer predictor, Judger judger){
        this.predictor = predictor;
        this.judger = judger;
        this.tc = new TwitterCommunicator();
    }
    
    @Override
    public void handle(HttpExchange arg0) throws IOException {
        URI uri = arg0.getRequestURI();
        String name = movieName(uri.getQuery());
        if(name.equals("")){
            respondWithMsg(arg0, "No movie name specified");
            return;
        }
        try {
            // predict all tweets
            List<String> tweets = this.tc.getTweets(name);
            int[] counts = this.judger.getCounter();
            for(String t : tweets){
                String cate = predictor.categorize(t);
                counts[this.judger.indexer.get(cate)]++;
            }
            // judge the movie based on the predictions
            String res = this.judger.judge(counts);
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("success", true);
            obj.put("evaluation", res);
            this.respondWithMsg(arg0, obj.toString());
            
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private String movieName(String query) throws UnsupportedEncodingException{
        String[] pairs = query.split("&");
        for(String p : pairs){
            int index = p.indexOf('=');
            if(URLDecoder.decode(p.substring(0, index), "UTF-8").equals("q")){
                return URLDecoder.decode(p.substring(index+1), "UTF-8");
            }
        }
        return "";
    }
    
    private void respondWithMsg(HttpExchange arg0, String msg) throws IOException{
        Headers header = arg0.getResponseHeaders();
        header.set("Content-Type", "text/html");
        OutputStream body = arg0.getResponseBody();
        body.write(msg.getBytes());
        body.close();
    }

}
