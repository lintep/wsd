package nlp.wsd.co.datatype;

import nlp.preprocess.datatype.Word;
import nlp.preprocess.en.StanfordPreProcessor;
import tools.util.collection.KeyValueSimple;

import java.util.*;

/**
 * Created by Saeed on 8/27/15.
 */
public class Sentence {

    int sentenceId;

    HashMap<Integer,Word> indexWordMap;

    HashMap<Integer,String> indexWordToAmbiguousFeatureSetId;

    HashMap<String,Integer> ambiguousFeatureSetIdToIndexWord;

    HashMap<String,AmbiguousFeatureSet> idAmbiguousFeaturesMap;

    HashMap<String,ArrayList<String>> ambiguousFeatureSetIdToSatsList;

    HashMap<String,Integer> satIdToWordIndex;

    int wordCounter;
    int ambiguousWordCounter;

    public Sentence(int sentenceId){
        this.sentenceId=sentenceId;
        this.indexWordMap=new HashMap<Integer, Word>();
        this.wordCounter=0;
        this.idAmbiguousFeaturesMap=new HashMap<String, AmbiguousFeatureSet>();
        this.ambiguousWordCounter=0;
        this.indexWordToAmbiguousFeatureSetId=new HashMap<Integer, String>();
        this.ambiguousFeatureSetIdToIndexWord=new HashMap<String,Integer>();
        this.ambiguousFeatureSetIdToSatsList =new HashMap<String, ArrayList<String>>();
        this.satIdToWordIndex =new HashMap<String,Integer>();
    }

    public HashSet<Word> getAmbiguousWord(){
        HashSet<Word> result=new HashSet<Word>();
        for (Map.Entry<String, Integer> wordIndex : ambiguousFeatureSetIdToIndexWord.entrySet()) {
            Word ambWord = this.indexWordMap.get(wordIndex.getValue());
            if(ambWord!=null){
                result.add(ambWord);
            }
            else{
                System.out.println(wordIndex.getKey()+" -> null");
            }
        }

        return result;
    }

    public Word getAmbiguousWord(String ambiguousFeatureSetId){
        if (this.ambiguousFeatureSetIdToIndexWord.containsKey(ambiguousFeatureSetId)) {
            return this.indexWordMap.get(ambiguousFeatureSetIdToIndexWord.get(ambiguousFeatureSetId));
        }
        return null;
    }

    public boolean contain(String token){
        for (Word word : indexWordMap.values()) {
            if(word.getLemma().equals(token) || word.getTokenValue().equals(token)){
                return true;
            }
        }
        return false;
    }

    public HashMap<String, AmbiguousFeatureSet> getIdAmbiguousFeaturesMap() {
        return idAmbiguousFeaturesMap;
    }

    public HashMap<Integer, Word> getIndexWordMap() {
        return indexWordMap;
    }

    public int wordCount(){
        return wordCounter;
    }

    public void addWord(Word word){
        this.indexWordMap.put(wordCounter,word);
        wordCounter++;
    }

    public String getLastAddedWord(){
        if(wordCounter>0){
            return indexWordMap.get(wordCounter-1).getTokenValue();
        }
        else{
            return null;
        }
    }

    public void addWord(Word word, String satId){
        this.indexWordMap.put(wordCounter,word);
        this.satIdToWordIndex.put(satId, wordCounter);
        wordCounter++;
    }

    public void addWord(Word word, AmbiguousFeatureSet ambiguousFeatures){
        if(word==null){
            System.out.println("NAMANA");
        }
        this.indexWordMap.put(wordCounter,word);
        this.idAmbiguousFeaturesMap.put(ambiguousFeatures.getId(),ambiguousFeatures);
        this.indexWordToAmbiguousFeatureSetId.put(wordCounter,ambiguousFeatures.getId());
        this.ambiguousFeatureSetIdToIndexWord.put(ambiguousFeatures.getId(),wordCounter);
        wordCounter++;
    }

    public void addWord(Word word, String ambiguousWordId, ArrayList<String> satIds) throws Exception {
        if(word==null){
            System.out.println("NAMANA");
        }
        for (String satId : satIds) {
            if(ambiguousWordId.equals(satId)){
                throw new Exception("ALABA:\t"+ambiguousWordId);
            }
        }
        this.indexWordMap.put(wordCounter,word);
        this.ambiguousFeatureSetIdToSatsList.put(ambiguousWordId,satIds);
        this.ambiguousFeatureSetIdToIndexWord.put(ambiguousWordId,wordCounter);
        wordCounter++;
    }

    public HashMap<String, ArrayList<String>> getAmbiguousFeatureSetIdToSatsList() {
        return ambiguousFeatureSetIdToSatsList;
    }

    public void updateWord(int wordIndex, Word word, AmbiguousFeatureSet ambiguousFeatures){
        this.indexWordMap.put(wordIndex,word);
        this.idAmbiguousFeaturesMap.put(ambiguousFeatures.getId(),ambiguousFeatures);
        this.indexWordToAmbiguousFeatureSetId.put(wordIndex,ambiguousFeatures.getId());
        this.ambiguousFeatureSetIdToIndexWord.put(ambiguousFeatures.getId(),wordIndex);
    }

    public KeyValueSimple<Word,AmbiguousFeatureSet> getWordAmbiguousFeatureSet(String ambiguousFeatureSetId){
        KeyValueSimple<Word,AmbiguousFeatureSet> result=null;
        if(this.ambiguousFeatureSetIdToIndexWord.containsKey(ambiguousFeatureSetId)){
            int wordIndex=this.ambiguousFeatureSetIdToIndexWord.get(ambiguousFeatureSetId);
            AmbiguousFeatureSet ambiguousFeatureSet = this.idAmbiguousFeaturesMap.get(ambiguousFeatureSetId);
            Word word = this.indexWordMap.get(wordIndex);
            result=new KeyValueSimple<Word, AmbiguousFeatureSet>(word,ambiguousFeatureSet);
        }
        return result;
    }

    public int getAmbiguousFeatureSetIdWordIndex(String ambiguousFeatureSetId){
        if(this.ambiguousFeatureSetIdToIndexWord.containsKey(ambiguousFeatureSetId)){
            return this.ambiguousFeatureSetIdToIndexWord.get(ambiguousFeatureSetId);
        }
        return -1;
    }

    /**
     *
     * @param ambiguousFeatureSetId
     * @param dist window size = 2*dist+1
     * @return
     */
    public Set<KeyValueSimple<Integer,Word>> getContextWordByAmbiguousFeatureSetId(String ambiguousFeatureSetId, int dist){
        int wordIndex = getAmbiguousFeatureSetIdWordIndex(ambiguousFeatureSetId);
        if(wordIndex>=0){
            Set<KeyValueSimple<Integer,Word>> result=new HashSet<KeyValueSimple<Integer, Word>>();
//            int skippedCount = 0;
            for(int rightIndex=1;rightIndex<=Math.min(wordCount()-1-wordIndex,dist);rightIndex++){
                result.add(new KeyValueSimple<Integer, Word>(rightIndex,this.getWord(wordIndex+rightIndex)));
            }
            for(int leftIndex=-1;leftIndex+wordIndex>=Math.max(0,wordIndex-dist);leftIndex--){
                result.add(new KeyValueSimple<Integer, Word>(leftIndex,this.getWord(wordIndex+leftIndex)));
            }
            return result;
        }
        return null;
    }

    /**
     *
     * @param ambiguousFeatureSetId
     * @param dist window size = 2*dist+1
     * @return
     */
    public Set<KeyValueSimple<Integer,Word>> getContextWordByAmbiguousFeatureSetId(String ambiguousFeatureSetId, int dist, boolean containsSenseInSentenceEnable){
        Word ambigWord = getWord(getAmbiguousFeatureSetIdWordIndex(ambiguousFeatureSetId));
        int wordIndex = getAmbiguousFeatureSetIdWordIndex(ambiguousFeatureSetId);
        if(wordIndex>=0){
            Set<KeyValueSimple<Integer,Word>> result=new HashSet<KeyValueSimple<Integer, Word>>();
//            int skippedCount = 0;
            for(int rightIndex=1;rightIndex<=Math.min(wordCount()-1-wordIndex,dist);rightIndex++){
                Word word = this.getWord(wordIndex + rightIndex);
                if(!containsSenseInSentenceEnable){
                    if(word.getLemma()==ambigWord.getLemma()){
                        continue;
                    }
                }
                result.add(new KeyValueSimple<Integer, Word>(rightIndex,word));
            }
            for(int leftIndex=-1;leftIndex+wordIndex>=Math.max(0,wordIndex-dist);leftIndex--){
                Word word = this.getWord(wordIndex + leftIndex);
                if(!containsSenseInSentenceEnable){
                    if(word.getLemma()==ambigWord.getLemma()){
                        continue;
                    }
                }
                result.add(new KeyValueSimple<Integer, Word>(leftIndex,word));
            }
            return result;
        }
        return null;
    }

    public AmbiguousFeatureSet getAmbiguousFeatureSetByWordIndex(int wordIndex){
        if(isAmbiguousWordByIndex(wordIndex)){
            String ambiguousFeatureSetId = this.indexWordToAmbiguousFeatureSetId.get(wordIndex);
            return this.idAmbiguousFeaturesMap.get(ambiguousFeatureSetId);
        }
        return null;
    }

    public AmbiguousFeatureSet getAmbiguousFeatureSetByAmbiguousId(String ambiguousId){
        if(this.ambiguousFeatureSetIdToIndexWord.containsKey(ambiguousId)){
            return this.idAmbiguousFeaturesMap.get(ambiguousId);
        }
        return null;
    }

    public String getAmbiguousFeatureSetId(int wordIndex){
        if(isAmbiguousWordByIndex(wordIndex)){
            return this.indexWordToAmbiguousFeatureSetId.get(wordIndex);
        }
        return null;
    }

    public boolean isAmbiguousWordByIndex(int wordIndex){
        if(this.indexWordToAmbiguousFeatureSetId.containsKey(wordIndex)){
            return true;
        }
        return false;
    }

    public int getWordIndexBySatId(String satId){
        if(this.satIdToWordIndex.containsKey(satId)){
            return this.satIdToWordIndex.get(satId);
        }
        return -1;
    }
    public Word getWord(int index){
        return this.indexWordMap.get(index);
    }

    public int getSentenceId() {
        return sentenceId;
    }

    public String getStringValue(StanfordPreProcessor.PreprocessType preprocessType) {
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<wordCount();i++){
            sb.append(getWord(i).get(preprocessType));
            sb.append(' ');
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

    public Collection<Word> getAllWord(){
        return this.getIndexWordMap().values();
    }
}

