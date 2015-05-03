package raw_data_processing;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by
 * Name: Zhibin Li
 * UID: N15748535
 * Email: zl791@nyu.edu
 */
public class GradeLabeler {
    private TreeMap<Double,String> scale;

    public GradeLabeler(){
        scale = new TreeMap<>();

    }

    public void setScale(TreeMap myScale){
        scale = myScale;
    }

    public void set7Scale(){
        scale.put(0.0,"terrible");
        scale.put(0.30, "very_bad");
        scale.put(0.40, "bad");
        scale.put(.60, "fair");
        scale.put(.70, "good");
        scale.put(.80, "very_good");
        scale.put(.90, "best");
    }

    public void labelScaleAndSave(File file, String outPath) throws IOException {
        InputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPath),"utf-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        while( (line = br.readLine()) != null) {
            String[] words = line.split(" ");
            double score = Double.parseDouble(words[0]);
            words[0] = getGrade(score);
            for (String word : words) {
                writer.write(word + " ");
            }
            writer.write("\n");
        }
        writer.flush();
    }




    public String getGrade(double score){

        Iterator<Double> iterator = scale.descendingKeySet().iterator();
        double scl = 100.;
        while(iterator.hasNext()){
            scl = iterator.next();
            if(score > scl){
                break;
            }
        }
        return scale.get(scl);

    }

    public static void main(String[] args) throws IOException {
        GradeLabeler gl = new GradeLabeler();
        gl.set7Scale();
        File file = new File("/Users/Benson/Documents/workspace/NLP_project/src/raw_data_processing/data/review/Action & Adventure.txt");
        gl.labelScaleAndSave(file,"src/raw_data_processing/out.txt");;
    }

}
