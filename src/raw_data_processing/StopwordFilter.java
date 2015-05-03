package raw_data_processing;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;

/**
 * Created by
 * Name: Zhibin Li
 * UID: N15748535
 * Email: zl791@nyu.edu
 */
public class StopwordFilter {
    public HashSet<String> stopwords;

    public StopwordFilter() {
        stopwords = new HashSet<>();
    }

    public void addStopWords(String filePath) throws IOException {
        InputStream fis = new FileInputStream(new File(filePath));
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        while( (line = br.readLine())!=null ){
            stopwords.add(line);
        }
        fis.close();
    }

    public boolean isStopword(String word){
        if(stopwords.contains(word)){
            return true;
        }
        return false;
    }
}
