package raw_data_processing;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by
 * Name: Zhibin Li
 * UID: N15748535
 * Email: zl791@nyu.edu
 */
public class GradeLabeler {
    private SortedMap<Float,String> scale;

    public Grade_labeler

    public void labelScaleAndSave(File file, String outPath, String outName) throws IOException {
        InputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        while( (line = br.readLine()) != null){
            String[] words = line.split(" ");
        }
    }

    public String getGrade(float score){
        Iterator iterator =
    }

}
