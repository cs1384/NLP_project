import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class TwitterCommunicator {

    private String consumerKey = "NHjEQk4qTzp1OH6hsZoMsavlH";
    private String consumerSecret = "8aFp2xDZ6LI18cU2no0HiNSHnAneBzC4k4LlqjHdXblDgsESke";
    private Long max_id = Long.MAX_VALUE;
    
    public List<String> getTweets(String name) throws IOException, JSONException{
        String credentials = this.getCredentials();
        String token = this.getBearedToken(credentials);
        List<String> res = new ArrayList<String>();
        for(int i=0;i<10;i++){
            res.addAll(getTimelineTweets(name, token));
        }
        System.out.println(res.size());
        return res;
    }
    
    private List<String> getTimelineTweets(String name, String bearerToken) throws IOException, JSONException{
        List<String> res = new ArrayList<String>();
        
        String api = "https://api.twitter.com/1.1/search/tweets.json?q=%s&count=100&lang=en&result_type=mixed&max_id=%s";
        String query = makeQuery(name);
        //System.out.println(query);
        URL url = new URL(String.format(api, query, String.valueOf(this.max_id))); 
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();           
        connection.setDoOutput(true);
        connection.setDoInput(true); 
        connection.setRequestMethod("GET"); 
        connection.setRequestProperty("Host", "api.twitter.com");
        connection.setRequestProperty("User-Agent", "cs1384NLPproject");
        connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
        connection.setUseCaches(false);
        
        JSONObject json = new JSONObject(readResponse(connection));
        JSONArray arr = json.getJSONArray("statuses");
        for(int i=0;i<arr.length();i++){
            JSONObject j = arr.getJSONObject(i);
            String text =  j.get("text").toString();
            long id = Long.parseLong(j.get("id").toString());
            if(id<this.max_id) this.max_id = id;
            System.out.println(id + " " + text);
            res.add(text);
        }
        //JSONArray obj = (JSONArray)JSONValue.parse(readResponse(connection));
        return res;
    }
    
    private String makeQuery(String name) throws UnsupportedEncodingException{
        List<String> tags = new ArrayList<String>();
        name = name.replaceAll("( )+", " ");
        String[] arr = name.split(" ");
        StringBuilder tag1 = new StringBuilder();tag1.append("#");
        StringBuilder tag2 = new StringBuilder();tag2.append("#");
        for(String s : arr){
            char c = s.charAt(0);
            if(Character.isLetterOrDigit(c)){
                tag1.append(c);
                tag2.append(s);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name);sb.append(" ");
        sb.append(tag1.toString());sb.append(" OR ");
        sb.append(tag2.toString());
        //sb.append(" lang:en");
        //sb.append(" result_type:mixed");
        //sb.append(" count:50");
        return URLEncoder.encode(sb.toString(), "UTF-8");
    }
    
    private String getCredentials() throws UnsupportedEncodingException{
        String encodedKey = URLEncoder.encode(this.consumerKey, "UTF-8");
        String encodedSecret = URLEncoder.encode(this.consumerSecret, "UTF-8");
        String token = encodedKey+":"+encodedSecret;
        //String token = "xvz1evFS4wEEPTGEFPHBog:L8qq9PZyRg6ieKGEKhZolGC0vJWLw8iEJ88DRdyOg";
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(token.getBytes());
    }
    
    private String getBearedToken(String credentials) throws IOException, JSONException{
        URL url = new URL("https://api.twitter.com/oauth2/token");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Host", "api.twitter.com");
        connection.setRequestProperty("User-Agent", "Your Program Name");
        connection.setRequestProperty("Authorization", "Basic " + credentials);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        connection.setRequestProperty("Content-Length", "29");
        connection.setUseCaches(false);
        writeRequest(connection, "grant_type=client_credentials");
        
        JSONObject json = new JSONObject(readResponse(connection));
        //System.out.println(json.get("token_type"));
        return json.get("access_token").toString();
    }
    
    private static String readResponse(HttpsURLConnection connection) throws IOException {
        StringBuilder sb = new StringBuilder();                
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = "";
        while((line = br.readLine()) != null) {
            sb.append(line + System.getProperty("line.separator"));
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }
    
    private static boolean writeRequest(HttpsURLConnection connection, String textBody) {
        try {
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            wr.write(textBody);
            wr.flush();
            wr.close();
            return true;
        }
        catch (IOException e) { return false; }
    }
    
    public static void main(String[] args) throws IOException, JSONException {
        TwitterCommunicator tc = new TwitterCommunicator();
        List<String> tweets = tc.getTweets("furious 7");
    }

}
