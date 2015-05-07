import java.io.*;
import java.nio.charset.Charset;

public class Evaluator {
    private Judger judger;

    public Evaluator(Judger judger){
        //sevenJudger = new Judger()
        this.judger = judger;
    }

    public double getAvgScoreFromPool(String mid) throws FileNotFoundException {
        File file = new File("data/reviews_pool/pool.txt");
        InputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));

        return 0.0;
    }


    public static void main(String[] args) throws IOException {
        GradeScale gradeScale = new GradeScale();
        gradeScale.add7Scale();
        Judger judger1 = new Judger(gradeScale);
        Evaluator evl = new Evaluator(judger1);
        File file = new File("data/review_eval_label.txt");
        //String mid = "770672122";
        //System.out.println(mid + " <> " + grade);
    }

}
