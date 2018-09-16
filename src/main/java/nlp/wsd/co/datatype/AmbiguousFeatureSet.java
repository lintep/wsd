package nlp.wsd.co.datatype;

import nlp.wordnet.WordNetTools;
import nlp.wordnet.WordSenseFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saeed on 8/27/15.
 */
public class AmbiguousFeatureSet {

    String id;

    List<WordSenseFeature> ambiguousFeatureList;
    int ambiguousFeatureCounter;

    public AmbiguousFeatureSet(String id,List<WordSenseFeature> wordSenseFeature){
        this.id=id;
        this.ambiguousFeatureCounter=0;
        this.ambiguousFeatureList =new ArrayList<WordSenseFeature>();
        for (WordSenseFeature senseFeature : wordSenseFeature) {
            this.ambiguousFeatureList.add(senseFeature);
            ambiguousFeatureCounter++;
        }
    }

    public AmbiguousFeatureSet(String id){
        this.id=id;
        this.ambiguousFeatureCounter=0;
        this.ambiguousFeatureList =new ArrayList<WordSenseFeature>();
    }

    public String getId() {
        return id;
    }

//    public void addAmbiguousFeature(String id,Set<String> synset, Set<String> glossSet,int frequency){
//        this.ambiguousFeatureList.put(this.ambiguousFeatureCounter,new AmbiguousFeature(id,synset,glossSet,frequency));
//        this.ambiguousFeatureCounter++;
//    }

    public List<WordSenseFeature> getAmbiguousFeatureList() {
        return ambiguousFeatureList;
    }

    public WordSenseFeature getAmbiguousFeature(String originalSenseKey) {

        for(WordSenseFeature wordSenseFeature:ambiguousFeatureList){
            if(wordSenseFeature.getOriginalSenseKey().equals(originalSenseKey)){
                return wordSenseFeature;
            }
        }
        return null;
    }

    public WordSenseFeature getAmbiguousFeatureWithMaxWordNetFreq() {
        WordSenseFeature result=null;
        int maxFreq=-1;
        for (WordSenseFeature wordSenseFeature : ambiguousFeatureList) {
            if(wordSenseFeature.getWordNetFrequency()>maxFreq){
                result = wordSenseFeature;
                maxFreq=wordSenseFeature.getWordNetFrequency();
            }
        }
        return result;
    }

}
