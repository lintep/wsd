package nlp.preprocess;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saeed on 12/30/2016.
 */
public interface PreprocessorInterface {

    List<StanfordTokenizedLemmattizedPosTaggedSentence> getSTLPTS(String text);

    List<PreprocessedSentence> preprocess(String text);

    String preprocess(String text, PreprocessType preprocessType);

    public static class PreprocessedSentence {
        List<PreprocessedItem> preprocessedItems;

        public PreprocessedSentence(){
            this.preprocessedItems =new ArrayList<>();
        }

        public void addItem(PreprocessedItem newItem){
            this.preprocessedItems.add(newItem);
        }

        public PreprocessedItem getPreproccedItem(int index){
            return this.preprocessedItems.get(index);
        }

        public List<PreprocessedItem> getPreprocessedItems() {
            return preprocessedItems;
        }
    }

    public static class PreprocessedItem {
        String token;
        String lemma;
        String pos;

        public PreprocessedItem(String token, String lemma, String pos) {
            this.token = token;
            this.lemma = lemma;
            this.pos = pos;
        }

        public String getToken() {
            return token;
        }

        public String getLemma() {
            return lemma;
        }

        public String getPos() {
            return pos;
        }

        @Override
        public String toString() {
            return token+"\t"+lemma+"\t"+pos;
        }
    }

    public static enum PreprocessType{
        NORMALIZE,
        TOKENIZE,
        LEMMATIZE,
        POSTAG
    }
}
