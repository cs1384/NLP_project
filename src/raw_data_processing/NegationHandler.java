package raw_data_processing;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

/**
 * Created by
 * Name: Zhibin Li
 * UID: N15748535
 * Email: zl791@nyu.edu
 */
public class NegationHandler {

    public void handleNegation() throws IOException {
        String text = new String(Files.readAllBytes(Paths.get("src/raw_data_processing/data/testFile")));
        text = text.replace("\n", "").replace("\r", "");


        String[] splitSencence = SentenceDetection.splitSentence("src/model/en-sent.bin", text);
        for(String sentence:splitSencence){
            System.out.println(sentence);
        }

        HashSet<String> negation_words = new HashSet<String>();
        List<String> lines=Files.readAllLines(Paths.get("src/raw_data_processing/data/negation_word_list"), Charset.forName("UTF-8"));
        for(String line:lines){
            negation_words.add(line);
        }

        InputStream modelIn = null;
        modelIn = new FileInputStream("src/model/en-pos-maxent.bin");
        POSModel model = new POSModel(modelIn);
        POSTaggerME tagger = new POSTaggerME(model);

        for(String sentence:splitSencence){
            String[] sent = sentence.split(" ");
            String[] tags = tagger.tag(sent);
            double probs[] = tagger.probs();
            for(String tag: tags) {
                System.out.println(tag);
            }
            POSSample sample = new POSSample(sent, tags);
            for(String s : negation_words) {
                System.out.println("-"+s);
            }

            for( int i = 0; i < tags.length; i++){
                String word = sent[i];
                if(negation_words.contains(word)){
                    System.out.println("========="+word);
                    int endIndex = i;
                    for(endIndex = i; endIndex < tags.length; endIndex++){
                        if(tags[endIndex].equals("CC")){
                            System.out.println("------"+tags[endIndex]);
                            break;
                        }
                    }
                    for(int j = i; j < endIndex; j++){
                        sent[j] = "NOT_"+sent[j];
                    }

                }
            }

            for(String word: sent){
                System.out.println(word);
            }
        }
    }
}
