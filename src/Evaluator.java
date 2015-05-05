import raw_data_processing.RawreviewGradeLabler;

import java.io.File;
import java.io.IOException;

public class Evaluator {
    private Judger judger;
    private RawreviewGradeLabler labeler;

    public Evaluator(Judger judger){
        //sevenJudger = new Judger()
        labeler = new RawreviewGradeLabler();
        this.judger = judger;
    }

    public void labelGrade(File input,String outPath) throws IOException {
        labeler.set7Scale();
        labeler.labelScaleAndSave(input,outPath);
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
