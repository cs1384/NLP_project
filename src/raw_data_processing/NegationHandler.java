package raw_data_processing;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by
 * Name: Zhibin Li
 * UID: N15748535
 * Email: zl791@nyu.edu
 */
public class NegationHandler {
    public HashSet<String> negation_words;
    public List<String[]> splitSentences;
    public HashSet<String> tagsToBeModified;
    public List<String[]> resultSentences;
    public String inputFileName;
    public String inputFilePath;
    public String outputFilePath;
    public String outputFileName;
    public StopwordFilter filter;

    public void clearSentences() {
        splitSentences = new ArrayList<>();
        tagsToBeModified = new HashSet<>();
        resultSentences = new ArrayList<>();
    }


    public NegationHandler() {
        splitSentences = new ArrayList<String[]>();
        negation_words = new HashSet<String>();
        tagsToBeModified = new HashSet<String>();
        resultSentences = new ArrayList<String[]>();
        filter = new StopwordFilter();
        tagsToBeModified.add("RB");
        tagsToBeModified.add("JJR");
        tagsToBeModified.add("RBR");
        tagsToBeModified.add("JJ");
        tagsToBeModified.add("JJS");
        tagsToBeModified.add("RBS");
        tagsToBeModified.add("RB");
        tagsToBeModified.add("VB");
        tagsToBeModified.add("VBD");
        tagsToBeModified.add("VBG");
        tagsToBeModified.add("VBN");
        tagsToBeModified.add("VBP");
        tagsToBeModified.add("VBZ");
    }

    public void addSplitSentence(File file) throws IOException {
        String line;
        InputStream fis = new FileInputStream(file);
        String absolutePath = file.getAbsolutePath();
        inputFilePath = absolutePath.
                substring(0,absolutePath.lastIndexOf(File.separator));
        inputFileName= file.getName();

        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        while ((line = br.readLine()) != null) {
            line = line.replace("\n", "");
            String[] splitSent = SentenceDetection.splitSentence("src/model/en-sent.bin", line.toLowerCase());
            splitSentences.add(splitSent);

        }
        fis.close();
        br.close();
    }

    public void addNegationWords(String file) throws IOException {
        List<String> lines=Files.readAllLines(Paths.get(file), Charset.forName("UTF-8"));
        if(lines.isEmpty()) throw new NoSuchElementException("negation lists empty");
        for(String line:lines){
            negation_words.add(line.toLowerCase());
        }
    }

    public void handleAndSaveToFile(String path, String name) throws IOException {
        InputStream modelIn = new FileInputStream("src/model/en-pos-maxent.bin");
        POSModel model = new POSModel(modelIn);
        POSTaggerME tagger = new POSTaggerME(model);

        if(splitSentences == null) throw new NullPointerException("splitSentence is null");
        for(String[] splitSent : splitSentences){
            List<String> modifiedLine = new ArrayList<String>();
            for(String sentence:splitSent){
                String[] words = sentence.split(" ");
                String[] tags = tagger.tag(words);
                double probs[] = tagger.probs();

                // one sentence in a line
                for( int i = 0; i < tags.length; i++){
                    String curWord = words[i];
                    if(negation_words.contains(curWord.toLowerCase())){
                        int endIndex;
                        for(endIndex = i; endIndex < tags.length; endIndex++){
                            if(tags[endIndex].equals("CC") || tags[endIndex].equals(",") || tags[endIndex].equals(";")){
                                break;
                            }
                        }
                        for(int j = i+1; j < endIndex; j++){
                            if(tagsToBeModified.contains(tags[j])){
                                words[j] = "NOT_"+words[j];
                            }
                        }
                    }

                    //filter out stopwords
                    filter.addStopWords("src/raw_data_processing/data/stopwords");
                    if(filter.isStopword(words[i])){
                        words[i] = "";
                    }

                }
                modifiedLine.add(makeSentence(words));

            }
            String[] sentArray = new String[modifiedLine.size()];
            sentArray = modifiedLine.toArray(sentArray);
            resultSentences.add(sentArray);


        }

        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path+"/"+name),"utf-8"));
        for(String[] sent : resultSentences){
            for(String s : sent){
                writer.write(s);
            }
            writer.write("\n");
        }
        writer.flush();
        writer.close();
        clearSentences();
    }

    public String makeSentence(String[] words) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < words.length; i++){
            if(words[i].length() >= 0){
                sb.append(words[i].toLowerCase());
                if(i != words.length - 1){
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        NegationHandler negationHandler = new NegationHandler();
        File folder = new File("data/reviews_genres");
        //File folder = new File("data/reviews_genres");

        File[] listOfFiles = folder.listFiles();


        System.out.println("task begin");
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                negationHandler.addSplitSentence(listOfFiles[i]);
                negationHandler.addNegationWords("src/raw_data_processing/data/negation_word_list");
                String outputPath = negationHandler.inputFilePath+"_after_negation";
                File outDir = new File(outputPath);
                if(!outDir.exists()){
                    outDir.mkdir();
                }
                System.out.println("#"+i+": "+negationHandler.inputFileName+" - begin");
                negationHandler.handleAndSaveToFile(outputPath,negationHandler.inputFileName);
                System.out.println("#"+i+": "+negationHandler.inputFileName+" - done");

            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        System.out.println("all done");

//        NegationHandler nh = new NegationHandler();
//        File folder = new File("data/reviews");
//        nh.handleAllFileInDir(folder);
    }



}
