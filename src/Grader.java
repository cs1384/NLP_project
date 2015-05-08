import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Grader {
    List<Trainer> poolPredictors = new ArrayList<Trainer>();
    List<Map> genrePredictors = new ArrayList<Map>();
    List<Judger> genreJudgers = new ArrayList<Judger>();
    public void addPoolPredictor(Trainer pd){
        this.poolPredictors.add(pd);
    }
    public void addGenrePredictor(Map map){
        this.genrePredictors.add(map);
    }
    public Grader(boolean def) throws ClassNotFoundException, IOException{
        if(!def) return;
        
        // pool predictors
        Trainer tn_pool_7label = new Trainer();
        tn_pool_7label.setModel("data/reviews_pool_after_negation_7scale/model/pool.model");
        Trainer tn_pool_3label = new Trainer();
        tn_pool_7label.setModel("data/reviews_pool_after_negation_3scale/model/pool.model");
        this.addPoolPredictor(tn_pool_3label);
        this.addPoolPredictor(tn_pool_7label);
        
        // genre predictors
        
        // judgers
        GradeScale gradeScale7 = new GradeScale();
        gradeScale7.add7Scale();
        Judger judger_7label = new Judger(gradeScale7);
        GradeScale gradeScale3 = new GradeScale();
        gradeScale3.add3Scale();
        Judger judger_3label = new Judger(gradeScale3);
        // predictors
        Map<String, Trainer> map_7label = new HashMap<String, Trainer>();
        File folder = new File("data/reviews_genres_after_negation_3scale/model");
        File[] listOfFiles = folder.listFiles();
        for(File f : listOfFiles){
            Trainer tn = new Trainer();
            tn.setModel(f.getAbsolutePath());
            map_7label.put(f.getName().replace(".model", ""), tn);
        }
        Map<String, Trainer> map_3label = new HashMap<String, Trainer>();
        folder = new File("data/reviews_genres_after_negation_3scale/model");
        listOfFiles = folder.listFiles();
        for(File f : listOfFiles){
            Trainer tn = new Trainer();
            tn.setModel(f.getAbsolutePath());
            map_3label.put(f.getName().replace(".model", ""), tn);
        }
        this.genrePredictors.add(map_3label);
        this.genreJudgers.add(judger_3label);
        this.genrePredictors.add(map_7label);
        this.genreJudgers.add(judger_7label);
    }
    public List<String> getGrade(String review, List<String> genres) throws Exception{
        List<String> res = new ArrayList<String>();
        for(Trainer tn : this.poolPredictors){
            res.add(tn.categorize(review));
        }
        for(int i=0;i<this.genrePredictors.size();i++){
            Map<String, Trainer> map = this.genrePredictors.get(i);
            Judger judger = this.genreJudgers.get(i);
            judger.clearAllScore();
            for(String g: genres){
                System.out.println(map.keySet());
                System.out.println(g);
                if(!map.containsKey(g)) continue;
                Trainer t = map.get(g);
                String score = t.categorize(review);
                judger.addReviewGrade(score);
            }
            res.add(judger.judge());
        }
        return res;
    }
    
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        Grader grader = new Grader(true);

    }

}
