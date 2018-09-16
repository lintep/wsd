package nlp.similarity;

import java.io.IOException;

/**
 * Created by Saeed on 12/22/14.
 */
public class TestSentenceSimilarity {
    public static void main(String[] args) throws IOException {
//        System.out.println("by:");
//        try {
//            tools.util.File.createRandomTestFile(100.0,tools.util.file.Reader.getTextFromFile("resources/ctext", false).split("(\\s)+|(\\.)"),2,"resources/ctextRandom");
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        SentenceSimilarity sentenceSimilarity=new SentenceSimilarity("resources/ctext");
        String q="Where I can buy good oil for massage ?";
        String c1="I  ve done it once at the Sharq Village & Spa ... It  s great";
        String c2="You might be able to find Body Massage Oil in Body Shop at Landmark or City Centre , and if they do have it there , its guaranteed to be good for massage , as some places sell duplicates , so watch out as this could cause allergies to the skin . Good Luck !";
        System.out.println(sentenceSimilarity.getTFIDFCosinSimilarity(q, c1));
        System.out.println(sentenceSimilarity.getTFIDFCosinSimilarity(q, c2));
    }

}
