package raw_data_processing;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by
 * Name: Zhibin Li
 * UID: N15748535
 * Email: zl791@nyu.edu
 */
public class RawreviewGradeLabler extends GradeLabeler {

    public RawreviewGradeLabler(){
        super();
    }


    /**
     * label grade for raw review
     * 770672122  <|###|> Toy Story 3 <|###|> Animation;Kids & Family;Science Fiction & Fantasy;Comedy <|###|> 0.8 <|###|> amazing animation movies!!!!
     * @param file
     * @param outPath
     * @throws java.io.IOException
     */
    @Override
    public void labelScaleAndSave(File file, String outPath) throws IOException {
        InputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPath),"utf-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        while( (line = br.readLine()) != null) {
            String[] words = line.trim().split("<###>");
            double score = Double.parseDouble(words[3]);
            words[3] = getGrade(score);
            for (String word : words) {
                writer.write(word + " <###> ");
            }
            writer.write("\n");
        }
        writer.flush();
    }

    public static void main(String args[]) throws IOException {

        RawreviewGradeLabler labler = new RawreviewGradeLabler();
        labler.set7Scale();
        File file = new File("data/reviews_eval.txt");
        System.out.println("begin");
        labler.labelScaleAndSave(file, "data/review_eval_labeled.txt");
        System.out.println("end");

    }
}
