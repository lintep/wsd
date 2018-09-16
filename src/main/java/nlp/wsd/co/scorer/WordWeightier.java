package nlp.wsd.co.scorer;

import nlp.languagemodel.LanguageModelScorer;
import nlp.preprocess.datatype.Word;
import tools.util.PrintWriterWithBuffer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Saeed on 10/24/2016.
 */
public class WordWeightier {

    public static Map<String,HashMap<String, Double>> textRankCash=new HashMap<>();

    public static Map<String, Double> getWeight(String contextText,
                                                Word ambiguousWord, HashMap<Word, Double> contextLemma,
                                                LanguageModelScorer languageModelScorer,
                                                WeightFunction weightFunction,
                                                PrintWriterWithBuffer logFileWriter){
        return getWeight(contextText, ambiguousWord.getLemma(), contextLemma, languageModelScorer, weightFunction, logFileWriter);
    }

    public static Map<String, Double> getWeight(String contextText,
                                                String baseWordLemma, HashMap<Word, Double> contextLemma,
                                                LanguageModelScorer languageModelScorer,
                                                WeightFunction weightFunction,
                                                PrintWriterWithBuffer logFileWriter)
    {
        Map<String, Double> result=new HashMap<>();
        switch (weightFunction){
            case UNIFY:
                contextLemma.entrySet().forEach(s -> result.put(s.getKey().getLemma(),1.));
                return result;

            case AMBIGUOUS_SIMILARITY:
                double sumScore=0.;
                for(Word word:contextLemma.keySet()){
                    try {
                        double score=languageModelScorer.getScore(baseWordLemma,word.getLemma());
                        if(score>0){
                            score/=languageModelScorer.getScore(word.getLemma());
                            sumScore+=score;
                            result.put(word.getLemma(),score);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (result.size()==0){
                    return result;
                }
                else{
                    for(String lemma:result.keySet()){
                        result.put(lemma,result.get(lemma)/sumScore);
                        logFileWriter.println(lemma + " -> weight1:" + tools.util.Str.format(result.get(lemma), 2));
                    }
                }
                return result;

            case CONTEXT_UNIFY_GRAPH:
                sumScore=0.;
                result.clear();
                Set<String> context = new HashSet<>();
                contextLemma.keySet().forEach(s -> context.add(s.getLemma()));
                context.add(baseWordLemma);
                BigramScorer bigramScorer=new BigramScorer(languageModelScorer);
                for(String lemma1:context){
                    double score=0.;
                    for(String lemma2:context) {
                        if(lemma1.equals(lemma2)){
                            continue;
                        }
                        try {
                            double tempScore = bigramScorer.getScore1(BigramScorer.BigramScoreFunction
                                    .LINEAR_NORMALIZED_WORDDF_SENSEDF, lemma1, lemma2);
                            if(Double.isFinite(tempScore) && tempScore>0){
                                score+=tempScore;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    result.put(lemma1,score);
                    sumScore+=score;
                }
                if (result.size()==0){
                    return result;
                }
                else{
                    for(String lemma:result.keySet()){
                        result.put(lemma,result.get(lemma)/sumScore);
                        if(logFileWriter!=null) {
                            logFileWriter.println(lemma + " -> weight2:" + tools.util.Str.format(result.get(lemma), 2));
                        }
                    }
                }
                return result;

            case TEXT_RANK:
                sumScore=0;
                HashMap<String, Double> map = new HashMap<String, Double>();
                map=textRankCash.get(contextText);

                for (Map.Entry<Word, Double> wordDoubleEntry : contextLemma.entrySet()) {
                    String lemma = wordDoubleEntry.getKey().getLemma();
                    if(map.containsKey(lemma)) {
                        sumScore += map.get(lemma);
                        result.put(lemma, map.get(lemma));
                    }
                    else{
                        if(logFileWriter!=null) {
                            logFileWriter.println(lemma + " -> not exist in graph.");
                        }
                        result.put(lemma,0.);
                    }
                }

                if (result.size()==0){
                    return result;
                }
                else{
                    for(String lemma:result.keySet()){
                        result.put(lemma,result.get(lemma)/sumScore);
                        if(logFileWriter!=null) {
                            logFileWriter.println(lemma + " -> weight1:" + tools.util.Str.format(result.get(lemma), 2));
                        }
                    }
                }

                return result;

        }

        return null;
    }

    public enum WeightFunction {
        UNIFY("UNIFY"),
        AMBIGUOUS_SIMILARITY("AMBIGUOUS_SIMILARITY"),
        CONTEXT_UNIFY_GRAPH("CONTEXT_UNIFY_GRAPH")
        ,TEXT_RANK("TEXT_RANK")
        ;

        String string;

        WeightFunction(String string) {
            string = string;
        }

        public String getString() {
            return string;
        }
    }

}
