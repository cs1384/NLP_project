import java.io.File;
import java.util.ArrayList;
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

    public Evaluator(Grader gd) {
        grader3ScaleGrade_G = new ArrayList<>();
        grader3ScaleGrade_NG = new ArrayList<>();
        grader7ScaleGrade_G = new ArrayList<>();
        grader7ScaleGrade_NG = new ArrayList<>();
        tamotoGrade7Scale = new ArrayList<>();
        tamotoGrade3Scale = new ArrayList<>();
        GradeScale scale7 = new GradeScale();
        scale7.add7Scale();
        jg7Scale = new Judger(scale7);
        GradeScale scale3 = new GradeScale();
        scale3.add3Scale();
        jg3Scale = new Judger(scale3);
        this.grader = gd;
    }

    public void addGrade(String mName, List<String> genres) throws Exception {
        List<String> predictGrade = grader.getGrade(mName,genres);
        String grade7ScaleTom = jg7Scale.judge();
        String grade3ScaleTom = jg3Scale.judge();
        tomatoGrade7Scale.add(grade7ScaleTom);
        tomatoGrade3Scale.add(grade3ScaleTom);
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
    public List<Double> precision(){
        double totalGrade = 0;
        double correct3G = 0, corret3NG = 0, correct7G = 0, correct7NG = 0;
        for(int i = 0; i < tomatoGrade3Scale.size(); i++){
            if(tomatoGrade3Scale.get(i).trim().equals("unknown") || tomatoGrade7Scale.get(i).trim().equals("unknown")){
                continue;
            }
            totalGrade ++;
            if(grader3ScaleGrade_G.get(i).equals(tomatoGrade3Scale)){
                correct3G++;
            }
            if(grader3ScaleGrade_NG.get(i).equals(tomatoGrade3Scale)){
                corret3NG++;
            }
            if(grader7ScaleGrade_G.get(i).equals(tomatoGrade7Scale)){
                correct7G++;
            }
            if(grader7ScaleGrade_NG.get(i).equals(tomatoGrade7Scale)){
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

    public boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }



//    public double getAvgScoreFromPool(String mid, GradeLabeler gl) throws IOException {
//        File file = new File("data/reviews_pool/pool.txt");
//        InputStream fis = new FileInputStream(file);
//        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
//        BufferedReader br = new BufferedReader(isr);
//
//        String line;
//        while( (line = br.readLine()) != null) {
//            String[] words = line.split(" ");
//            if(!isNumeric(words[0])){
//                break;
//            }
//            double score = Double.parseDouble(words[0]);
//            words[0] = .getGrade(score);
//            for (String word : words) {
//                writer.write(word + " ");
//            }
//            writer.write("\n");
//        }
//        writer.flush();
//
//        return 0.0;
//    }


    public static void main(String[] args) throws Exception {
        GradeScale gradeScale = new GradeScale();
        gradeScale.add7Scale();
        Judger judger1 = new Judger(gradeScale);
        File file = new File("data/review_eval_labeled.txt");
        String mid = "377510718";
        String grade = judger1.getAvgTamatoGradeBymid(file, mid);
        System.out.println(mid + " <> " + grade);
    }

}
