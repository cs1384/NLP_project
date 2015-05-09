import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Judger {
    private GradeScale scale;
    private HashMap<Integer,Integer> score;
    public Judger( GradeScale scale){
        this.scale = scale;
        score = new HashMap<>();
    }

    public GradeScale getScale(){
        return scale;
    }

    /**
     *
     * @return total count of score
     */
    public int getCount(){
        Iterator it = score.entrySet().iterator();
        int count = 0;
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            count += (int)pair.getValue();
        }
        return count;
    }

    public HashMap<String, Integer> getGradeCount() {
        HashMap<String, Integer> result = new HashMap<>();
        Iterator it = score.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            int score = (int)pair.getKey();
            int count = (int)pair.getValue();
            String grade = scale.getGrade(score);
            result.put(grade,count);
        }
        return result;
    }

    public boolean clearAllScore(){
        score = new HashMap<>();
        return true;
    }

    public int getScoreSum(){
        Iterator it = score.entrySet().iterator();
        int sum = 0;
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            int count =  (int)pair.getValue();
            sum += count * (int)pair.getKey();
//            System.out.println("score: "+pair.getKey()+ " count: "+pair.getValue());
        }
        return sum;
    }

    public void addReviewGrade(String grade){
        if(!scale.isValidGrade(grade)){
            throw new IllegalArgumentException(grade);
        }
        int scoreOfGrade = scale.getScore(grade);
        int curCount = 0;
        if(score.containsKey(scoreOfGrade)){
          curCount =  score.get(scoreOfGrade);
        }
        score.put(scoreOfGrade,curCount+1);
    }

    public String getAvgTamatoGradeBymid(File labeledFile, String mid) throws Exception {
        InputStream fis = new FileInputStream(labeledFile);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        while( (line = br.readLine()) != null) {
            String tokens[] = line.split("<###>");
            if(tokens[0].trim().equals(mid)){
                addReviewGrade(tokens[3]);
            }
        }
        return judge();
    }


    public String judge() throws Exception {
        int sum = getScoreSum();
        int count = getCount();
        if(count == 0){
            return "unknown";
        }
        double avgScore = (sum+0.)/count + 0.5;
        int score = (int)avgScore;
        return scale.getGrade(score);
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
}
