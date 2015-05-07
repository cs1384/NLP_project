package raw_data_processing;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.TreeMap;

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

    public void set3Scale(){
        scale.put(0.0,"bad");
        scale.put(0.6,"fair");
        scale.put(0.8,"good");
    }

    public boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    public void labelScaleAndSave(File file, String outPath) throws IOException {
        InputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPath),"utf-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        while( (line = br.readLine()) != null) {
            String[] words = line.split(" ");
            if(!isNumeric(words[0])){
                break;
            }
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
//        gl.set7Scale();
        gl.set3Scale();
        File folder = new File("data/reviews_pool_after_negation");
        String outdir = "data/reviews_pool_after_negation_3scale/";
        File[] listOfFiles = folder.listFiles();


        System.out.println("task begin");
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                File outDir = new File(outdir);
                if(!outDir.exists()){
                    outDir.mkdir();
                }
                File input = listOfFiles[i].getAbsoluteFile();
                System.out.println("#"+i+": "+listOfFiles[i].getName()+" - begin");
                gl.labelScaleAndSave(input,outdir+listOfFiles[i].getName());
                System.out.println("#"+i+": "+ listOfFiles[i].getName()+" - done");

            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }

}
