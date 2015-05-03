import java.util.HashMap;
import java.util.Map;

public class Judger {
    public String[] scale = null;
    public Map<String, Integer> indexer = new HashMap<String, Integer>();
    public Judger(String[] scale){
        this.scale = scale;
        for(int i=0;i<scale.length;i++){
            this.indexer.put(scale[i], i);
        }
    }
    public int[] getCounter(){
        return new int[scale.length];
    }
    public String judge(int[] counts){
        if(counts.length!=scale.length) return null;
        int num = 0;
        int sum = 0;
        for(int i=0;i<counts.length;i++){
            sum += (i+1)*counts[i];
            num += counts[i];
        }
        int idx = (sum-1)/num;
        return this.scale[idx];
    }
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
}
