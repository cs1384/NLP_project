import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;

public class PoolQueryHandler implements HttpHandler{
    
    private Trainer predictor = null;
    private TwitterCommunicator tc = null;
    private Judger judger = null;
    
    public PoolQueryHandler(Trainer predictor, Judger judger){
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
            for(String t : tweets){
                String grade = predictor.categorize(t);
                judger.addReviewGrade(grade);
            }
            // judge the movie based on the predictions
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("status", "success");
            obj.put("evaluation", judger.judge());
            this.respondWithMsg(arg0, obj.toString());
        } catch (JSONException e) {
            this.respondWithMsg(arg0, "json failed");
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
