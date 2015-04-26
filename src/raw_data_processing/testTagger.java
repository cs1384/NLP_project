package raw_data_processing;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by
 * Name: Zhibin Li
 * UID: N15748535
 * Email: zl791@nyu.edu
 */
public class testTagger {

    public static void main(String[] args){
        InputStream modelIn = null;

        try {
            modelIn = new FileInputStream("model/en-pos-maxent.bin");
            POSModel model = new POSModel(modelIn);
            POSTaggerME tagger = new POSTaggerME(model);
            String sent[] = new String[]{"Most", "large", "cities", "in", "the", "US", "had",
                    "morning", "and", "afternoon", "newspapers", "."};
            String tags[] = tagger.tag(sent);
            double probs[] = tagger.probs();
            for(String tag: tags) {
                System.out.println(tag);
            }
            POSSample sample = new POSSample(sent, tags);
            System.out.println(sample.toString());
        }
        catch (IOException e) {
            // Model loading failed, handle the error
            e.printStackTrace();
        }
        finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException e) {
                }
            }
        }
    }
}
