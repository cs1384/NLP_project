import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Set;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;


public class Trainer{
    
    DoccatModel model = null;

    public void setModel(String modelPath) throws IOException, ClassNotFoundException{
        FileInputStream fileIn = new FileInputStream(modelPath);
        model = new DoccatModel(fileIn);
        fileIn.close();
    }
    public String categorize(String input){
        if(this.model==null) return null;
        DocumentCategorizerME myCategorizer = new DocumentCategorizerME(this.model);
        double[] outcomes = myCategorizer.categorize(input);
        String category = myCategorizer.getBestCategory(outcomes);
        return category;
    }
    public String trainModel(String inputPath) throws IOException{
        InputStream dataIn = null;
        try {
            dataIn = new FileInputStream(inputPath);
            ObjectStream<String> lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
            ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);
            model = DocumentCategorizerME.train("en", sampleStream);
        } catch (IOException e) {
          // Failed to read or parse training data, training failed
          e.printStackTrace();
        } finally {
            if (dataIn != null) {
                try {
                    dataIn.close();
                }
                catch (IOException e) {
                    // Not an issue, training already finished.
                    // The exception should be logged and investigated
                    // if part of a production system.
                    e.printStackTrace();
                }
            }
        }
        // output the model
        int from = inputPath.lastIndexOf('/');
        int to = inputPath.lastIndexOf('.');
        String name = inputPath.substring(from+1, to);
        String modelPath = "data/"+name+".model";
        
        OutputStream modelOut = null;
        try {
          modelOut = new BufferedOutputStream(new FileOutputStream(modelPath));
          model.serialize(modelOut);
        }
        catch (IOException e) {
          // Failed to save model
          e.printStackTrace();
        }
        finally {
          if (modelOut != null) {
            try {
               modelOut.close();
            }
            catch (IOException e) {
              // Failed to correctly save model.
              // Written model might be invalid.
              e.printStackTrace();
            }
          }
        }
        
        return modelPath;
    }
    
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Trainer tn = new Trainer();
        //String modelPath = tn.trainModel("data/reviews/Animation.txt");
        String modelPath = "data/Animation.model";
        tn.setModel(modelPath);
        System.out.println(tn.categorize("amazing"));
        System.out.println(tn.categorize("animated"));
        System.out.println(tn.categorize("unexciting"));
        System.out.println(tn.categorize("A few funny lines and a great vocal turn by Hugh Grant. All else is unexciting. Can't see the kids going wild over this one."));
        System.out.println(tn.categorize("misstep"));
        System.out.println(tn.categorize("poor"));
    }

}
