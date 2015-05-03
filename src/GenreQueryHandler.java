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

public class GenreQueryHandler implements HttpHandler{

    @Override
    public void handle(HttpExchange arg0) throws IOException{
        URI uri = arg0.getRequestURI();
        String name = movieName(uri.getQuery());
        if(name.equals("")) respondWithMsg(arg0, "No movie name specified");
        String id;
        try {
            id = getMovieId(name);
            if(id.equals("")) respondWithMsg(arg0, "No matching movie name in the database");
            List<String> genres = getGenreList(id);
            
            TwitterCommunicator tc = new TwitterCommunicator();
            List<String> tweets = tc.getTweets(name);
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private List<String> getGenreList(String id) throws IOException, JSONException{
        List<String> res = new ArrayList<String>();
        String apiInfo = "http://api.rottentomatoes.com/api/public/v1.0/movies/%s.json?apikey=x6usx7bn33cdn9vverg9f2v7";
        String url = URLEncoder.encode(String.format(apiInfo, id), "UTF-8");
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
        OutputStream body = arg0.getResponseBody();
        body.write(msg.getBytes());
        body.close();
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

    private String getMovieId(String name) throws IOException, JSONException{
        String api = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=x6usx7bn33cdn9vverg9f2v7&q=";
        String url = URLEncoder.encode(api+name, "UTF-8");
        JSONObject json = Crawler.readJsonFromUrl(url);
        JSONArray arr = json.getJSONArray("movies");
        for(int i=0;i<arr.length();i++){
            JSONObject j = arr.getJSONObject(i);
            if(j.get("title").toString().equals(name)){
                return j.get("id").toString();
            }
        }
        return "";
    }
}
