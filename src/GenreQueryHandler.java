import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenreQueryHandler implements HttpHandler{

    Map<String, Trainer> predictors = null;
    private TwitterCommunicator tc = null;
    private Judger judger = null;
    
    public GenreQueryHandler(Map<String, Trainer> predictors, Judger judger){
        this.predictors = predictors;
        this.judger = judger;
        this.tc = new TwitterCommunicator();
    }
    
    @Override
    public void handle(HttpExchange arg0) throws IOException{
        URI uri = arg0.getRequestURI();
        System.out.println("============ genre handler!");
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
        String id;
        try {
            id = getMovieId(name);
            if(id.equals("")){
                respondWithMsg(arg0, "No matching movie name in the database");
                return;
            }
            System.out.println("movie_id: "+ id);
            List<String> genres = getGenreList(id);
            TwitterCommunicator tc = new TwitterCommunicator();
            List<String> tweets = tc.getTweets(name);
            System.out.println("Got "+tweets.size()+ " tweets");
            //for(String k : predictors.keySet()) System.out.println(k);
            for(String t : tweets){
                for(String g : genres){
                    if(!predictors.containsKey(g)){
                        System.out.println(g);
                        continue;
                    }
                    String grade = predictors.get(g).categorize(t);
                    judger.addReviewGrade(grade);
                }
            }
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("status", "success");
            obj.put("evaluation", judger.getGradeCount());
            this.respondWithMsg(arg0, obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private List<String> getGenreList(String id) throws IOException, JSONException{
        List<String> res = new ArrayList<String>();
        String apiInfo = "http://api.rottentomatoes.com/api/public/v1.0/movies/%s.json?apikey=x6usx7bn33cdn9vverg9f2v7";
        //String url = URLEncoder.encode(String.format(apiInfo, id), "UTF-8");
        String url = String.format(apiInfo, id);
        System.out.println(url);
        JSONObject json = Crawler.readJsonFromUrl(url);
        JSONArray arr = json.getJSONArray("genres");
        for(int i=0;i<arr.length();i++){
            res.add(arr.getString(i));
        }
        return res;
    }
    
    private void respondWithMsg(HttpExchange arg0, String msg) throws IOException{
        Headers header = arg0.getResponseHeaders();
        header.set("Content-Type", "text/html");
        arg0.sendResponseHeaders(0, 0);
        OutputStream body = arg0.getResponseBody();
        body.write(msg.getBytes());
        body.close();
    }
    
    private String movieName(String query) throws UnsupportedEncodingException{
        String[] pairs = query.split("&");
        for(String p : pairs){
            int index = p.indexOf('=');
            if(URLDecoder.decode(p.substring(0, index), "UTF-8").equals("q")){
                return URLDecoder.decode(p.substring(index+1), "UTF-8").toLowerCase();
            }
        }
        return "";
    }

    private String getMovieId(String name) throws IOException, JSONException{
        String api = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=x6usx7bn33cdn9vverg9f2v7&q=";
        String temp = URLEncoder.encode(name, "UTF-8");
        String url = api+temp;
        System.out.println(url);
        JSONObject json = Crawler.readJsonFromUrl(url);
        JSONArray arr = json.getJSONArray("movies");
        for(int i=0;i<arr.length();i++){
            JSONObject j = arr.getJSONObject(i);
            System.out.println(j.getString("title"));
            if(j.get("title").toString().toLowerCase().equals(name)){
                return j.get("id").toString();
            }
        }
        return "";
    }
}
