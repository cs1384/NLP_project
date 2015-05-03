
public class Evaluator {

    public static void main(String[] args) {
        String instring = "How I met & your-he mother!!. ";
        String str = instring.replaceAll("\\p{P}", " ").toLowerCase();
        String[] words = str.split("\\s+");
        System.out.println(str);
    }

}
