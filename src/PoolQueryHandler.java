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
        System.out.println(this.predictor.model);
        this.judger = judger;
        this.tc = new TwitterCommunicator();
    }
    
    @Override
    public void handle(HttpExchange arg0) throws IOException {
        URI uri = arg0.getRequestURI();
        System.out.println("handle!");
        if(uri.getQuery().equals("")){
            respondWithMsg(arg0, "No query specified");
            return;
        }
        String name = movieName(uri.getQuery());
        if(name.equals("")){
            respondWithMsg(arg0, "No movie name specified");
            return;
        }
        System.out.println("movie_name: "+ name);
        try {
            // predict all tweets
            List<String> tweets = this.tc.getTweets(name);
            System.out.println("Got "+tweets.size()+ " tweets");
            for(String t : tweets){
                String grade = predictor.categorize(t);
                //System.out.println(grade);
                judger.addReviewGrade(grade);
            }
            // judge the movie based on the predictions
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("status", "success");
            obj.put("evaluation", judger.getGradeCount());
            System.out.println(obj);
            this.respondWithMsg(arg0, obj.toString());
        } catch (JSONException e) {
            this.respondWithMsg(arg0, "json failed");
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    private String movieName(String query) throws UnsupportedEncodingException{
        //System.out.println(query);
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
        System.out.println(msg);
        Headers header = arg0.getResponseHeaders();
        header.set("Content-Type", "text/html");
        arg0.sendResponseHeaders(0, 0);
        OutputStream body = arg0.getResponseBody();
        body.write(msg.getBytes());
        body.close();
    }

}
