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
    Set<String> ids = null;
    Set<String> genres = null;
    public static String delimiter = " <###> ";
    
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
    
    public void getIds(Set<String> keywords, String outputPath) throws IOException, JSONException, InterruptedException{
        String api = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=x6usx7bn33cdn9vverg9f2v7&q=";
        StringBuilder sb = new StringBuilder(api);
        ids = new HashSet<String>();
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
        FileOutputStream fileOut = new FileOutputStream(outputPath);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(ids);
        out.close();
        fileOut.close();
    }
    
    public void processRawReviesByGenre(String rawDataPath, String genresPath) throws IOException, ClassNotFoundException, JSONException, InterruptedException{
        // get raw data
        File fileIn = new  File(rawDataPath);
        Scanner sc = new Scanner(new FileReader(fileIn));

        // get genre content from serialized file
        FileInputStream fileObj = new FileInputStream(genresPath);
        ObjectInputStream in = new ObjectInputStream(fileObj);
        genres = (Set<String>) in.readObject();
        in.close();
        fileObj.close();
        
        // prepare the files to write reviews
        File file = new File("data/reviews_by_genre");
        file.mkdir();
        Map<String, BufferedWriter> map = new HashMap<String, BufferedWriter>();
        for(String g : genres){
            file = new File("data/reviews_by_genre/"+g+".txt");
            map.put(g, new BufferedWriter(new FileWriter(file)));
        }
        
        // process reviews
        int k = 0;
        int count = 0;
        String preid = "";
        while(sc.hasNextLine()){
            String[] op = sc.nextLine().split(this.delimiter);
            String[] list = op[2].split(";");
            for(String g : list){
                map.get(g).write(op[3]+" "+op[4]+"\n");
            }
            count++;
            if(!op[0].equals(preid)){
                preid = op[0];
            }
        }
        
        // close up all IO streams
        sc.close();
        for(String g : genres) map.get(g).close();
        System.out.println("=== PROCESSED "+count+" REVIEWS ===");
    }
    
    public void processRawReviesPool(String rawDataPath) throws IOException, ClassNotFoundException, JSONException, InterruptedException{
        // get raw data
        File fileIn = new  File(rawDataPath);
        Scanner sc = new Scanner(new FileReader(fileIn));
        
        // prepare the files to write reviews
        File file = new File("data/reviews_pool");
        file.mkdir();
        file = new File("data/reviews_pool/pool.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        
        // process reviews
        int k = 0;
        int count = 0;
        String preid = "";
        while(sc.hasNextLine()){
            String[] op = sc.nextLine().split(this.delimiter);
            bw.write(op[3]+" "+op[4]+"\n");
            count++;
            if(!op[0].equals(preid)){
                preid = op[0];
            }
        }
        
        // close up every IO stream
        sc.close();
        bw.close();
        System.out.println("=== PROCESSED "+count+" REVIEWS ===");
    }
    
    public void collectRawReviews(String idsPath, int trainMax, int evalMax) throws ClassNotFoundException, IOException, InterruptedException, JSONException{
        // get ids content from serialized file
        FileInputStream fileIn = new FileInputStream(idsPath);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        ids = (Set<String>) in.readObject();
        in.close();
        fileIn.close();
        // prepare the file to write reviews
        File file = new File("data/reviews_eval.txt");
        BufferedWriter bw_eval = new BufferedWriter(new FileWriter(file));
        file = new File("data/reviews_train.txt");
        BufferedWriter bw_train = new BufferedWriter(new FileWriter(file));
        // RESTful APIs
        String apiInfo = "http://api.rottentomatoes.com/api/public/v1.0/movies/%s.json?apikey=x6usx7bn33cdn9vverg9f2v7";
        String apiReview = "http://api.rottentomatoes.com/api/public/v1.0/movies/%s/reviews.json?apikey=x6usx7bn33cdn9vverg9f2v7&review_type=all&page_limit=50";
        // instantiate the genres set
        genres = new HashSet<String>();
        // collect reviews for each id
        int k = 0;
        for(String mid : ids){
            // get movie info
            JSONObject json = readJsonFromUrl(String.format(apiInfo, mid));
            String title = json.getString("title");
            JSONArray arr = json.getJSONArray("genres");
            StringBuilder sb = new StringBuilder(); 
            for(int i=0;i<arr.length();i++){
                String temp = arr.getString(i);
                genres.add(temp);
                sb.append(temp);sb.append(';');
            }
            sb.deleteCharAt(sb.length()-1);
            Thread.sleep(500);
            
            // get reviews
            json = readJsonFromUrl(String.format(apiReview, mid));
            arr = json.getJSONArray("reviews");
            for(int i=0;i<arr.length();i++){
                JSONObject j = arr.getJSONObject(i);
                System.out.println(j.toString());
                double score;
                // if exception happened or no quote, no reviews need to be written
                // could result in JSONException (no json node) or IllegalArgumentException (score not in correct format)
                try {
                    score = fractionToScore(j.getString("original_score"));
                } catch (Exception e) {
                    continue;
                }
                String quote = j.getString("quote");
                if(quote.equals("")) continue;
                quote = quote.replaceAll("\\p{P}", " ").trim().replaceAll("\\s+", " ").toLowerCase();
                
                String line = this.getALine(mid, title, sb.toString(), score, quote);
                System.out.println(line);
                if(k<trainMax) bw_train.write(line);
                else bw_eval.write(line);
            }
            Thread.sleep(500);
            if(++k==evalMax) break;
        }
        bw_train.close();
        bw_eval.close();
        // write genres to serialized file
        FileOutputStream fileOut = new FileOutputStream("data/genres.ser");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(genres);
        out.close();
        fileOut.close();
    }
    
    // "770672122  <|###|> Toy Story 3 <|###|> Animation;Kids & Family;Science Fiction & Fantasy;Comedy <|###|> 0.8 <|###|> amazing animation movies!
    private String getALine(String mid, String title, String genres, double score, String quote){
        StringBuilder sb = new StringBuilder();
        sb.append(mid);sb.append(this.delimiter);
        sb.append(title);sb.append(this.delimiter);
        sb.append(genres);sb.append(this.delimiter);
        sb.append(String.valueOf(score));sb.append(this.delimiter);
        sb.append(quote);sb.append("\n");
        return sb.toString();
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
        //Set<String> keywords = cl.getKeyWords("data/raw/movie_list.txt");
        // get ids based on those keywords and store ids to .ser
        //cl.getIds(keywords, "data/raw/ids.ser");
        // get genres based on first 500 ids and store genres to .ser
        cl.collectRawReviews("data/raw/ids.ser", 3000, 4000);
        // get two different traing sets
        cl.processRawReviesPool("data/reviews_train.txt");
        cl.processRawReviesByGenre("data/reviews_train.txt", "data/genres.ser");
    }

}
