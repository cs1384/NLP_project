import raw_data_processing.RawreviewGradeLabler;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Evaluator {
    private List<String> grader3ScaleGrade_G;
    private List<String> grader3ScaleGrade_NG;
    private List<String> grader7ScaleGrade_G;
    private List<String> grader7ScaleGrade_NG;
    private Judger jg7Scale;
    private Judger jg3Scale;
    private List<String> tomatoGrade7Scale;
    private List<String> tomatoGrade3Scale;
    private Grader grader;

    private RawreviewGradeLabler lb7Scale;
    private RawreviewGradeLabler lb3Scale;

    public Evaluator(Grader gd) {
        grader3ScaleGrade_G = new ArrayList<>();
        grader3ScaleGrade_NG = new ArrayList<>();
        grader7ScaleGrade_G = new ArrayList<>();
        grader7ScaleGrade_NG = new ArrayList<>();
        tomatoGrade7Scale = new ArrayList<>();
        tomatoGrade3Scale = new ArrayList<>();
        GradeScale scale7 = new GradeScale();
        scale7.add7Scale();
        jg7Scale = new Judger(scale7);
        GradeScale scale3 = new GradeScale();
        scale3.add3Scale();
        jg3Scale = new Judger(scale3);
        this.grader = gd;
        lb7Scale = new RawreviewGradeLabler();
        lb7Scale.set7Scale();
        lb3Scale = new RawreviewGradeLabler();
        lb3Scale.set3Scale();
    }

    private void addTomGrade(String rawReview) throws Exception {
        String[] words = rawReview.trim().split("<###>");
        double score = Double.parseDouble(words[3]);
        String grade7 = lb7Scale.getGrade(score);
        String grade3 = lb3Scale.getGrade(score);
        jg7Scale.addReviewGrade(grade7);
        jg3Scale.addReviewGrade(grade3);
        String grade7ScaleTom = jg7Scale.judge();
        String grade3ScaleTom = jg3Scale.judge();
        tomatoGrade7Scale.add(grade7ScaleTom);
        tomatoGrade3Scale.add(grade3ScaleTom);
        jg7Scale.clearAllScore();
        jg3Scale.clearAllScore();

    }



    private void addPredictGrade(String mName, List<String> genres) throws Exception {
        List<String> predictGrade = grader.getGrade(mName,genres);
//        for( String s : predictGrade){
//            System.out.println();
//        }

        int i = 0;
        grader3ScaleGrade_NG.add(predictGrade.get(i++));
        grader7ScaleGrade_NG.add(predictGrade.get(i++));
        grader3ScaleGrade_G.add(predictGrade.get(i++));
        grader7ScaleGrade_G.add(predictGrade.get(i++));
    }

    /**
     *
     * @return 3scaleNG, 7scaleNG, 3scaleG, 7scaleG
     */
    private List<Double> precision(){
        double totalGrade = 0;
        double correct3G = 0, corret3NG = 0, correct7G = 0, correct7NG = 0;
        for(int i = 0; i < tomatoGrade3Scale.size(); i++){
            if(tomatoGrade3Scale.get(i).trim().equals("unknown") || tomatoGrade7Scale.get(i).trim().equals("unknown") ||
                    grader3ScaleGrade_G.get(i).equals("unknown") || grader3ScaleGrade_NG.get(i).equals("unknown")
                    || grader7ScaleGrade_G.get(i).equals("unknown") || grader7ScaleGrade_NG.get(i).equals("unknown")){
                System.out.println("unknown");
                continue;
            }
            totalGrade ++;
            if(grader3ScaleGrade_G.get(i).equals(tomatoGrade3Scale.get(i))){
                correct3G++;
            }
            if(grader3ScaleGrade_NG.get(i).equals(tomatoGrade3Scale.get(i))){
                corret3NG++;
            }
            if(grader7ScaleGrade_G.get(i).equals(tomatoGrade7Scale.get(i))){
                correct7G++;
            }
            if(grader7ScaleGrade_NG.get(i).equals(tomatoGrade7Scale.get(i))){
                correct7NG++;
            }
        }
        List<Double> precision = new ArrayList<>();
        precision.add(corret3NG/totalGrade);
        precision.add(correct7NG/totalGrade);
        precision.add(correct3G/totalGrade);
        precision.add(correct7G/totalGrade);
        return precision;
    }

    /**
     *
     * @return 3scaleNG, 7scaleNG, 3scaleG, 7scaleG
     */
    public List<Double> precision(File testSet) throws Exception {
        InputStream fis = new FileInputStream(testSet);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        while( (line = br.readLine()) != null) {
            addTomGrade(line);
            String[] words = line.trim().split("<###>");
            String mName = words[1];
            String rawGenre = words[2];
            String[] genAy = rawGenre.split(";");
            for(int j = 0 ; j < genAy.length; j++){
                genAy[j] = genAy[j].trim();
            }
            ArrayList<String> genreList = new ArrayList(Arrays.asList(genAy));
            addPredictGrade(mName,genreList);
        }
        return precision();
    }


    public boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }


    public static void main(String[] args) throws Exception {
//        GradeScale gradeScale = new GradeScale();
//        gradeScale.add7Scale();
//        Judger judger1 = new Judger(gradeScale);
//        File file = new File("data/review_eval_labeled.txt");
//        String mid = "377510718";
//        String grade = judger1.getAvgTamatoGradeBymid(file, mid);
//        System.out.println(mid + " <> " + grade);

        Grader gd = new Grader(true);
        Evaluator evl = new Evaluator(gd);
        File testSet = new File("data/reviews_eval.txt");
        List<Double> result = evl.precision(testSet);
        for(Double pre : result){
           System.out.println(pre);
        }
    }

}
