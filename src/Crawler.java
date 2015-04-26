import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Crawler {
    Set<String> ids;
    Set<String> genres;
    String delimiter = " <|###|> ";
    
    public Crawler(){
        ids = new HashSet<String>();
    }
    
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
    
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }
    
    public Set<String> getKeyWords(String filePath) throws FileNotFoundException{
        File file = new File(filePath);
        Set<String> keywords = new HashSet<String>();
        Scanner sc = new Scanner(new FileReader(file));
        while(sc.hasNextLine()){
            String[] words = sc.nextLine().trim().split(" ");
            for(String s : words){
                keywords.add(s);
            }
        }
        return keywords;
    }
    
    public void getIds(Set<String> keywords) throws IOException, JSONException, InterruptedException{
        String api = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=x6usx7bn33cdn9vverg9f2v7&q=";
        StringBuilder sb = new StringBuilder(api);
        int len = sb.length();
        for(String key : keywords){
            if(key.length()<=3) continue;
            sb.append(key);
            System.out.println(sb.toString());
            JSONObject json = readJsonFromUrl(sb.toString());
            JSONArray arr = json.getJSONArray("movies");
            for(int i=0;i<arr.length();i++){
                JSONObject j = arr.getJSONObject(i);
                //System.out.println(j.get("title").toString());
                ids.add(j.get("id").toString());
            }
            sb.setLength(len);
            sb.trimToSize();
            Thread.sleep(500);
        }
        
        System.out.println("======== get "+ids.size()+" ids ========");
        FileOutputStream fileOut = new FileOutputStream("data/ids.ser");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(ids);
        out.close();
        fileOut.close();
    }
    
    public void collectReviewsForGenres(String genresPath, String idsPath, int max) throws IOException, ClassNotFoundException, JSONException, InterruptedException{
        // get ids content from serialized file
        FileInputStream fileIn = new FileInputStream(idsPath);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        ids = (Set<String>) in.readObject();
        in.close();
        fileIn.close();
        // get genre content from serialized file
        fileIn = new FileInputStream(genresPath);
        in = new ObjectInputStream(fileIn);
        genres = (Set<String>) in.readObject();
        in.close();
        fileIn.close();
        // prepare the files to write reviews
        File file = new File("data/reviews");
        file.mkdir();
        Map<String, BufferedWriter> map = new HashMap<String, BufferedWriter>();
        for(String g : genres){
            file = new File("data/reviews/"+g+".txt");
            map.put(g, new BufferedWriter(new FileWriter(file)));
        }
        // RESTful APIs
        String apiInfo = "http://api.rottentomatoes.com/api/public/v1.0/movies/%s.json?apikey=x6usx7bn33cdn9vverg9f2v7";
        String apiReview = "http://api.rottentomatoes.com/api/public/v1.0/movies/%s/reviews.json?apikey=x6usx7bn33cdn9vverg9f2v7&review_type=all&page_limit=50";
        // process reviews
        int k = 0;
        int count = 0;
        for(String id : ids){
            // get genres
            List<String> list = new ArrayList<String>();
            JSONObject json = readJsonFromUrl(String.format(apiInfo, id));
            JSONArray arr = json.getJSONArray("genres");
            for(int i=0;i<arr.length();i++){
                list.add(arr.getString(i));
            }
            Thread.sleep(500);
            // get reviews 
            json = readJsonFromUrl(String.format(apiReview, id));
            arr = json.getJSONArray("reviews");
            for(int i=0;i<arr.length();i++){
                JSONObject j = arr.getJSONObject(i);
                System.out.println(j.toString());
                double score;
                // if exception happened or no quote, no reviews need to be written
                // could result in JSONException (no json node) or IllegalArgumentException (score not in correct format)
                try {
                    score = fractionToScore(j.get("original_score").toString());
                } catch (Exception e) {
                    continue;
                }
                String quote = j.getString("quote");
                if(quote.equals("")) continue;
                for(String g : list){
                    map.get(g).write(score+this.delimiter+quote+"\n");
                }
                count++;
            }
            Thread.sleep(500);
            if(++k==max) break;
        }
        for(String g : genres) map.get(g).close();
        System.out.println("=== PROCESSED "+count+" REVIEWS ===");
    }
    
    public void collectReviews(String idsPath, int max) throws ClassNotFoundException, IOException, InterruptedException, JSONException{
        // get ids content from serialized file
        FileInputStream fileIn = new FileInputStream(idsPath);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        ids = (Set<String>) in.readObject();
        in.close();
        fileIn.close();
        // prepare the file to write reviews
        File file = new File("data/reviews.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file)); 
        // RESTful APIs
        String apiInfo = "http://api.rottentomatoes.com/api/public/v1.0/movies/%s.json?apikey=x6usx7bn33cdn9vverg9f2v7";
        String apiReview = "http://api.rottentomatoes.com/api/public/v1.0/movies/%s/reviews.json?apikey=x6usx7bn33cdn9vverg9f2v7&review_type=all&page_limit=50";
        // instantiate the genres set
        genres = new HashSet<String>();
        // collect review for each id
        int k = 0;
        for(String id : ids){
            // get genres
            JSONObject json = readJsonFromUrl(String.format(apiInfo, id));
            JSONArray arr = json.getJSONArray("genres");
            //System.out.println(arr.toString());
            for(int i=0;i<arr.length();i++){
                genres.add(arr.getString(i));
            }
            Thread.sleep(500);
            
            // get reviews 
            json = readJsonFromUrl(String.format(apiReview, id));
            arr = json.getJSONArray("reviews");
            for(int i=0;i<arr.length();i++){
                JSONObject j = arr.getJSONObject(i);
                System.out.println(j.toString());
                double score;
                // if exception happened or no quote, no reviews need to be written
                // could result in JSONException (no json node) or IllegalArgumentException (score not in correct format)
                try {
                    score = fractionToScore(j.get("original_score").toString());
                } catch (Exception e) {
                    continue;
                }
                String quote = j.getString("quote");
                if(quote.equals("")) continue;
                bw.write((score+this.delimiter+quote));
                bw.write("\n");
            }
            Thread.sleep(500);
            if(++k==max) break;
        }
        bw.close();
        // write genres to serialized file
        FileOutputStream fileOut = new FileOutputStream("data/genres.ser");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(genres);
        out.close();
        fileOut.close();
    }
    private double fractionToScore(String fra){
        int i=0;
        while(i<fra.length() && fra.charAt(i)!='/') i++;
        if(i==fra.length()) throw new IllegalArgumentException();
        double n = Double.parseDouble(fra.substring(0,i));
        double d = Double.parseDouble(fra.substring(i+1));
        return n/d;
    }
    
    public static void main(String[] args) throws IOException, JSONException, InterruptedException, ClassNotFoundException {
        Crawler cl = new Crawler();
        // get keywords list
        //Set<String> keywords = cl.getKeyWords("data/movie_list.txt");
        // get ids based on those keywords and store ids to .ser
        //cl.getIds(keywords);
        // get genres based on first 500 ids and store genres to .ser
        //cl.collectReviews("data/ids.ser", 500);
        cl.collectReviewsForGenres("data/genres.ser", "data/ids.ser", 100);
    }

}
