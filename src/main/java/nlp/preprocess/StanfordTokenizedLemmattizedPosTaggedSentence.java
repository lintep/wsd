package nlp.preprocess;

import edu.mit.jwi.item.POS;
import nlp.preprocess.datatype.Word;
import nlp.wordnet.WordNetTools;
import tools.util.collection.KeyValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahmani on 1/4/17.
 */
public class StanfordTokenizedLemmattizedPosTaggedSentence {
    String tokenizedSentence;
    String lemmatizedSentence;
    String posTagSentence;


    public StanfordTokenizedLemmattizedPosTaggedSentence(String tokenizedSentence, String lemmatizedSentence, String posTagSentence) {
        this.tokenizedSentence =tokenizedSentence;
        this.lemmatizedSentence=lemmatizedSentence;
        this.posTagSentence=posTagSentence;
    }

    public String getTokenizedSentence() {
        return tokenizedSentence;
    }

    public String getLemmatizedSentence() {
        return lemmatizedSentence;
    }

    public String getPosTagSentence() {
        return posTagSentence;
    }

    public List<KeyValue<String,POS>> getLemmaPos(){
        String[] lemmaArray = this.getLemmatizedSentence().split(" ");
        String[] posArray = this.getPosTagSentence().split(" ");

        List<KeyValue<String,POS>> result=new ArrayList<KeyValue<String, POS>>(lemmaArray.length);

        for (int i = 0; i <lemmaArray.length ; i++) {
            result.add(new KeyValue<String, POS>(lemmaArray[i], WordNetTools.getPos(posArray[i])));
        }

        return result;
    }

    public List<Word> getWordList(){
        String[] tokenArray = this.getTokenizedSentence().split(" ");
        String[] lemmaArray = this.getLemmatizedSentence().split(" ");
        String[] posArray = this.getPosTagSentence().split(" ");

        List<Word> result=new ArrayList<>(lemmaArray.length);

        for (int i = 0; i <lemmaArray.length ; i++) {
            result.add(new Word(tokenArray[i],posArray[i],lemmaArray[i]));
        }

        return result;
    }
}
