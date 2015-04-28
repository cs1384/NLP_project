import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;


public class Trainer {
    
    DoccatModel model = null;

    public void setModel(String modelPath) throws IOException, ClassNotFoundException{
        FileInputStream fileIn = new FileInputStream(modelPath);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        this.model = (DoccatModel) in.readObject();
        in.close();
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
        int from = inputPath.lastIndexOf('/');
        int to = inputPath.lastIndexOf('.');
        String name = inputPath.substring(from+1, to);
        String modelPath = "data/model_"+name+".ser";
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
        /*
        FileOutputStream fileOut = new FileOutputStream(modelPath);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(model);
        out.close();
        fileOut.close();
        */
        return modelPath;
    }
    
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Trainer tn = new Trainer();
        String modelPath = tn.trainModel("data/reviews/Animation.txt");
        //tn.setModel(modelPath);
        System.out.println(tn.categorize("amazing"));
        System.out.println(tn.categorize("animated"));
        System.out.println(tn.categorize("unexciting"));
        System.out.println(tn.categorize("A few funny lines and a great vocal turn by Hugh Grant. All else is unexciting. Can't see the kids going wild over this one."));
        System.out.println(tn.categorize("misstep"));
        System.out.println(tn.categorize("poor"));
    }

}
