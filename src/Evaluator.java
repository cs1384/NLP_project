import java.io.File;
import java.io.IOException;

public class Evaluator {
    private Judger judger;

    public Evaluator(Judger judger){
//        sevenJudger = new Judger()
        this.judger = judger;
    }




    public static void main(String[] args) throws IOException {


        GradeScale gradeScale = new GradeScale();
        gradeScale.add7Scale();
        Judger judger1 = new Judger(gradeScale);

        Evaluator evl = new Evaluator(judger1);
        File file = new File("data/review_eval_label.txt");
//        String mid = "770672122";
//        System.out.println(mid + " <> " + grade);
    }

}
