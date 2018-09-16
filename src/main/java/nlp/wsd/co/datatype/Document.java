package nlp.wsd.co.datatype;

import nlp.preprocess.datatype.Word;
import tools.util.collection.KeyValueSimple;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Saeed on 8/27/15.
 */
public class Document {
    String docId;

    HashMap<Integer,Sentence> indexSentenceMap;

    int sentenceCounter;

    public Document(String docId){
        this.docId=docId;
        this.indexSentenceMap=new HashMap<Integer, Sentence>();
        this.sentenceCounter=0;
    }

    public HashMap<Integer, Sentence> getIndexSentenceMap() {
        return indexSentenceMap;
    }

    public void addSentence(Sentence sentence){
        this.indexSentenceMap.put(sentence.getSentenceId(),sentence);
        sentenceCounter++;
    }

    public Sentence getSentence(int sentenceId){
        if(sentenceId<0 || sentenceId>=this.indexSentenceMap.size()){
            return null;
        }
        return this.indexSentenceMap.get(sentenceId);
    }

    public String getDocId(){
        return this.docId;
    }

    public KeyValueSimple<Word,AmbiguousFeatureSet> getWordAmbiguousFeatureSet(int sentenceId, String ambiguousFeatureSetId){
        return this.indexSentenceMap.get(sentenceId).getWordAmbiguousFeatureSet(ambiguousFeatureSetId);
    }

    public int getSentenceCount() {
        return this.indexSentenceMap.size();
    }

    public Map<Integer,Sentence> getContextSentence(int sentenceIndex, int sentenceDist){
        Map<Integer,Sentence> result= new HashMap<>();

        for (int diff=1;diff<=sentenceDist;diff++) {
            Sentence afterSentence = getSentence(sentenceIndex + diff);
            if(afterSentence!=null){
                result.put(sentenceIndex+diff,afterSentence);
            }
            Sentence beforeSentence = getSentence(sentenceIndex - diff);
            if(beforeSentence!=null){
                result.put(sentenceIndex-diff,beforeSentence);
            }
        }

        return result;
    }

    public String getLemmaText() {
        StringBuilder stringBuilder=new StringBuilder();
        for (int i = 0; i < this.indexSentenceMap.size(); i++) {
            for (int j = 0; j < this.indexSentenceMap.get(i).wordCount(); j++) {
                stringBuilder.append(this.indexSentenceMap.get(i).getWord(j).getLemma());
                stringBuilder.append(" ");
            }
        }
        stringBuilder.setLength(stringBuilder.length()-1);
        return stringBuilder.toString();
    }
}
