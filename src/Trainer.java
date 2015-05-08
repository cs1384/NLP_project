import java.io.BufferedOutputStream;
import java.io.File;
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
    public String trainModel(String inputPath, String modelPath) throws IOException{
        InputStream dataIn = null;
        try {
            dataIn = new FileInputStream(inputPath);
            ObjectStream<String> lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
            ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);
            model = DocumentCategorizerME.train("en", sampleStream);
        } catch (IOException e) {
          // Failed to read or parse training data, training failed
          e.printStackTrace();
        } catch (NullPointerException e){
            System.out.println("=================msg: Data set too small");
            return "none";
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
        OutputStream modelOut = null;
        try {               
          File file = new File(modelPath);
          modelOut = new BufferedOutputStream(new FileOutputStream(file));
          model.serialize(modelOut);
        }
        catch (IOException e) {
          // Failed to save model
          e.printStackTrace();
        }
        finally {
            modelOut.close();
            System.out.println("=================output: "+ modelPath);
        }
        
        return modelPath;
    }
    
    public void batchTraining(String dir) throws IOException{
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles.length>0){
            File model = new File(dir+"/model/");
            model.mkdirs();
        }
        for(File f : listOfFiles){
            if(!f.getName().contains(".txt")) continue;
            System.out.println("\n=================input: "+f.getPath());
            String outputPath = dir+"/model/"+f.getName().replaceAll(".txt", "")+".model";
            //System.out.println("=================output: "+ outputPath);
            String modelPath = this.trainModel(f.getPath(), outputPath);
        }
        System.out.println("DONE: "+dir);
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String pool_2labal_dir = "data/reviews_pool_after_negation_2scale";
        String pool_3labal_dir = "data/reviews_pool_after_negation_3scale";
        String pool_7labal_dir = "data/reviews_pool_after_negation_7scale";
        String genre_2labal_dir = "data/reviews_genres_after_negation_2scale";
        String genre_3labal_dir = "data/reviews_genres_after_negation_3scale";
        String genre_7labal_dir = "data/reviews_genres_after_negation_7scale";
        String test3 = "data/reviews_pool_3scale";
        String test7 = "data/reviews_pool_7scale";
        
        
        Trainer tn = new Trainer();
        tn.batchTraining(genre_3labal_dir);
        tn.batchTraining(genre_7labal_dir);
        tn.batchTraining(pool_3labal_dir);
        tn.batchTraining(pool_7labal_dir);
        //tn.batchTraining(test3);
        //tn.batchTraining(test7);
        
        /*
        String modelPath = "data/Animation.model";
        tn.setModel(modelPath);
        System.out.println(tn.categorize("amazing"));
        System.out.println(tn.categorize("animated"));
        System.out.println(tn.categorize("unexciting"));
        System.out.println(tn.categorize("A few funny lines and a great vocal turn by Hugh Grant. All else is unexciting. Can't see the kids going wild over this one."));
        System.out.println(tn.categorize("misstep"));
        System.out.println(tn.categorize("poor"));
        */
        
    }

}
