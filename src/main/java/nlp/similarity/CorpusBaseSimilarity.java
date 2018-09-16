package nlp.similarity;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import nlp.similarity.type.LanguageModelType;
import tools.util.file.BufferedIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Saeed on 12/22/14.
 */
public class CorpusBaseSimilarity {
    ArrayList<String> sentences;
    Multiset<String> tokenDf;
    Multiset<String> tokenTokenDf;
    Multiset<String> tokenTokenTokenDf;

    public CorpusBaseSimilarity(String fileAddress) throws IOException {
        this.tokenDf =HashMultiset.create();
        this.tokenTokenDf =HashMultiset.create();
        this.tokenTokenTokenDf =HashMultiset.create();
        this.sentences=new ArrayList<String>();
        BufferedIterator bufferedIterator = new BufferedIterator(tools.util.file.Reader.getFileBufferReader(fileAddress));
        String newSentence;
        int lineCounter=0;
        while (bufferedIterator.hasNext()){
            lineCounter++;
            newSentence=bufferedIterator.next();
            if(newSentence.trim().length()==0)
                continue;
            this.sentences.add(newSentence);
            String[] tokens = newSentence.split("\\s+");
            addTokenSentence(tokens);
            addTokenTokenSentence(tokens);
            addTokenTokenTokenSentence(tokens);
            if(lineCounter%10000==0)
                System.out.println("loading ... "+lineCounter+" line load complete.");
        }
        bufferedIterator.close();
        System.out.println(this.getClass().getName()+": "+sentences.size()+" sentence load complete with "+this.tokenDf.size()+" distinct token.");
    }

    private void addTokenSentence(String[] tokens){
        HashSet<String> distinctClauses=new HashSet<String>();
        for (int i=0;i<tokens.length;i++){
            distinctClauses.add(tokens[i]);
        }
        for(String distinctClause :distinctClauses){
            this.tokenDf.add(distinctClause);
        }
    }

    private void addTokenTokenSentence(String[] tokens){
        HashSet<String> distinctClauses=new HashSet<String>();
        for (int i=0;i<tokens.length-1;i++){
            distinctClauses.add(tokens[i]+" "+tokens[i+1]);
        }
        for(String distinctClause :distinctClauses){
            this.tokenTokenDf.add(distinctClause);
        }
    }

    private void addTokenTokenTokenSentence(String[] tokens){
        HashSet<String> distinctClauses=new HashSet<String>();
        for (int i=0;i<tokens.length-2;i++){
            distinctClauses.add(tokens[i]+" "+tokens[i+1]+" "+tokens[i+2]);
        }
        for(String distinctClause :distinctClauses){
            this.tokenTokenTokenDf.add(distinctClause);
        }
    }

    public int getTokenDF(String token){
        return this.tokenDf.count(token);
    }

    public int getTokenTokenDF(String token){
        return this.tokenTokenDf.count(token);
    }

    public int getTokenTokenTokenDF(String token){
        return this.tokenTokenTokenDf.count(token);
    }

    public HashMap<String, Double> getTokenVector(String inText) {
        HashMap<String, Integer> tempIntegerresult=new HashMap<String, Integer>();
        for (String token : inText.split("\\s+")) {
            if(tempIntegerresult.containsKey(token)) {
                tempIntegerresult.put(token, tempIntegerresult.get(token)+1);
            }
            else {
                tempIntegerresult.put(token, 1);
            }
        }
        return getVector(tempIntegerresult);
    }

    public HashMap<String, Double> getTokenTokenVector(String inText) {
        HashMap<String, Integer> tempIntegerresult=new HashMap<String, Integer>();
        String[] clauses = inText.split("\\s+");
        for (int i=0;i<clauses.length-1;i++) {
            String clause = clauses[i] + " " + clauses[i + 1];
            if(tempIntegerresult.containsKey(clause)) {
                tempIntegerresult.put(clause, tempIntegerresult.get(clause)+1);
            }
            else {
                tempIntegerresult.put(clause, 1);
            }
        }
        return getVector(tempIntegerresult);
    }

    public HashMap<String, Double> getTokenTokenTokenVector(String inText) {
        HashMap<String, Integer> tempIntegerresult=new HashMap<String, Integer>();
        String[] clauses = inText.split("\\s+");
        for (int i=0;i<clauses.length-2;i++) {
            String clause = clauses[i] + " " + clauses[i + 1]+ " " + clauses[i + 2];
            if(tempIntegerresult.containsKey(clause)) {
                tempIntegerresult.put(clause, tempIntegerresult.get(clause)+1);
            }
            else {
                tempIntegerresult.put(clause, 1);
            }
        }
        return getVector(tempIntegerresult);
    }

    private HashMap<String, Double> getVector(HashMap<String, Integer> tempIntegerresult) {
        HashMap<String, Double> result=new HashMap<String, Double>();
        double maxTF=0;
        for (Map.Entry<String,Integer> entry : tempIntegerresult.entrySet()){
            if(entry.getValue()>maxTF)
                maxTF=entry.getValue();
        }
        for (Map.Entry<String,Integer> entry : tempIntegerresult.entrySet()){
            result.put(entry.getKey(),entry.getValue()/maxTF);
        }
        return result;
    }

    public double cosineSimilarity(String sentence1, String sentence2){
        return getTokenTFIDFCosineSimilarity(sentence1,sentence2);
    }

    public double cosineSimilarity(String sentence1, String sentence2 , LanguageModelType languageModelType){
        switch (languageModelType){
            case UNIGRAM:
                return getTokenTFIDFCosineSimilarity(sentence1, sentence2);

            case BIGRAM:
                return getTokenTokenTFIDFCosineSimilarity(sentence1, sentence2);

            case TRIGRAM:
                return getTokenTokenTokenTFIDFCosineSimilarity(sentence1, sentence2);
        }

        return -1;
    }

    private double getTokenTFIDFCosineSimilarity(String sentence1, String sentence2){
        double similarityScore=0.;
        HashMap<String, Double> sentence1Vector = getTokenVector(sentence1);
        HashMap<String, Double> sentence2Vector = getTokenVector(sentence2);
        HashSet<String> sentence2VectorOvarlappedWithSentence1Token=new HashSet<String>(sentence1Vector.size());
        for(Map.Entry<String, Double> entry : sentence2Vector.entrySet()){
            if(sentence1Vector.containsKey(entry.getKey())){
                sentence2VectorOvarlappedWithSentence1Token.add(entry.getKey());
            }
        }
        double sumScore=0;
        double sumSentence1SquaredSize=0;
        for(Map.Entry<String, Double> entry : sentence1Vector.entrySet()){
            double temp = entry.getValue() * getTokenIDF(entry.getKey());
            sumSentence1SquaredSize+=temp*temp;
        }
        double sumSentence2SquaredSize=0;
        for(Map.Entry<String, Double> entry : sentence2Vector.entrySet()){
            double temp = entry.getValue() * getTokenIDF(entry.getKey());
            sumSentence2SquaredSize+=temp*temp;
        }
        for (String overlappedToken : sentence2VectorOvarlappedWithSentence1Token) {
            double sentence1Score = sentence1Vector.get(overlappedToken)
                    * getTokenIDF(overlappedToken);
            double sentence2Score = sentence2Vector.get(overlappedToken)
                    * getTokenIDF(overlappedToken);
            sumScore += sentence1Score * sentence2Score;
        }
        return sumScore/(Math.sqrt(sumSentence1SquaredSize)*Math.sqrt(sumSentence2SquaredSize));
    }

    private double getTokenIDF(String documentFrequency) {
        int docFreq = tokenDf.count(documentFrequency)+1;
        double result = (1+Math.log((double) this.sentences.size() / docFreq));
        return result;
    }

    private double getTokenTokenTFIDFCosineSimilarity(String sentence1, String sentence2){
        double similarityScore=0.;
        HashMap<String, Double> sentence1Vector = getTokenTokenVector(sentence1);
        HashMap<String, Double> sentence2Vector = getTokenTokenVector(sentence2);
        HashSet<String> sentence2VectorOvarlappedWithSentence1Token=new HashSet<String>(sentence1Vector.size());
        for(Map.Entry<String, Double> entry : sentence2Vector.entrySet()){
            if(sentence1Vector.containsKey(entry.getKey())){
                sentence2VectorOvarlappedWithSentence1Token.add(entry.getKey());
            }
        }
        double sumScore=0;
        double sumSentence1SquaredSize=0;
        for(Map.Entry<String, Double> entry : sentence1Vector.entrySet()){
            double temp = entry.getValue() * getTokenTokenIDF(entry.getKey());
            sumSentence1SquaredSize+=temp*temp;
        }
        double sumSentence2SquaredSize=0;
        for(Map.Entry<String, Double> entry : sentence2Vector.entrySet()){
            double temp = entry.getValue() * getTokenTokenIDF(entry.getKey());
            sumSentence2SquaredSize+=temp*temp;
        }
        for (String overlappedToken : sentence2VectorOvarlappedWithSentence1Token) {
            double sentence1Score = sentence1Vector.get(overlappedToken)
                    * getTokenTokenIDF(overlappedToken);
            double sentence2Score = sentence2Vector.get(overlappedToken)
                    * getTokenTokenIDF(overlappedToken);
            sumScore += sentence1Score * sentence2Score;
        }
        return sumScore/(Math.sqrt(sumSentence1SquaredSize)*Math.sqrt(sumSentence2SquaredSize));
    }

    private double getTokenTokenIDF(String documentFrequency) {
        int docFreq = tokenTokenDf.count(documentFrequency)+1;
        double result = (1+Math.log((double) this.sentences.size() / docFreq));
        return result;
    }

    private double getTokenTokenTokenTFIDFCosineSimilarity(String sentence1, String sentence2){
        double similarityScore=0.;
        HashMap<String, Double> sentence1Vector = getTokenTokenTokenVector(sentence1);
        HashMap<String, Double> sentence2Vector = getTokenTokenTokenVector(sentence2);
        HashSet<String> sentence2VectorOvarlappedWithSentence1Token=new HashSet<String>(sentence1Vector.size());
        for(Map.Entry<String, Double> entry : sentence2Vector.entrySet()){
            if(sentence1Vector.containsKey(entry.getKey())){
                sentence2VectorOvarlappedWithSentence1Token.add(entry.getKey());
            }
        }
        double sumScore=0;
        double sumSentence1SquaredSize=0;
        for(Map.Entry<String, Double> entry : sentence1Vector.entrySet()){
            double temp = entry.getValue() * getTokenTokenIDF(entry.getKey());
            sumSentence1SquaredSize+=temp*temp;
        }
        double sumSentence2SquaredSize=0;
        for(Map.Entry<String, Double> entry : sentence2Vector.entrySet()){
            double temp = entry.getValue() * getTokenTokenIDF(entry.getKey());
            sumSentence2SquaredSize+=temp*temp;
        }
        for (String overlappedToken : sentence2VectorOvarlappedWithSentence1Token) {
            double sentence1Score = sentence1Vector.get(overlappedToken)
                    * getTokenTokenIDF(overlappedToken);
            double sentence2Score = sentence2Vector.get(overlappedToken)
                    * getTokenTokenIDF(overlappedToken);
            sumScore += sentence1Score * sentence2Score;
        }
        return sumScore/(Math.sqrt(sumSentence1SquaredSize)*Math.sqrt(sumSentence2SquaredSize));
    }

    private double getTokenTokenTokenIDF(String documentFrequency) {
        int docFreq = tokenTokenTokenDf.count(documentFrequency)+1;
        double result = (1+Math.log((double) this.sentences.size() / docFreq));
        return result;
    }
}
