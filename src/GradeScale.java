import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by
 * Name: Zhibin Li
 * UID: N15748535
 * Email: zl791@nyu.edu
 */
public class GradeScale {
    private HashMap<String, Integer> scale;

    public GradeScale() {
        scale = new HashMap<>();
    }

    public boolean isValidGrade(String grade) {
        return scale.containsKey(grade);
    }

    public void add7Scale(){
        int i = 1;
        scale.put("terrible",i++);
        scale.put( "very_bad",i++);
        scale.put( "bad",i++);
        scale.put( "fair",i++);
        scale.put( "good",i++);
        scale.put( "very_good",i++);
        scale.put( "best",i++);
    }

    public int getScore(String grade){
        if(!scale.containsKey(grade))
            throw new NoSuchElementException();
        return scale.get(grade);
    }

    public String getGrade(int score){
        if(!scale.containsValue(score)){
            throw new IllegalArgumentException();
        }
        Iterator it = scale.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            if((int)pair.getValue() == score){
                return (String)pair.getKey();
            }
        }
        throw new IllegalArgumentException("no such grade");
    }
}
