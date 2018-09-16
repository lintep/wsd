package nlp.wsd.co.datatype;

import edu.mit.jwi.item.POS;
import nlp.languagemodel.LanguageModelScorer;
import nlp.preprocess.en.StanfordPreProcessor;
import nlp.wordnet.WordNetTools;
import nlp.wordnet.WordSenseFeature;
import tools.util.collection.HashSertInteger;
import tools.util.collection.PairValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Saeed on 8/7/2016.
 */
public class AmbiguousFeatureSetVector {

    LanguageModelScorer languageModelScorer;
    StanfordPreProcessor stanfordPreProcessor;
    WordNetTools wordNetTools;
    String ambiguousFeatureSetId;

    Map<String, WordSenseFeature> wordSenseFeatureMap;

    public AmbiguousFeatureSetVector(AmbiguousFeatureSet ambiguousFeatureSet, LanguageModelScorer languageModelScorer,
                                     StanfordPreProcessor stanfordPreProcessor, WordNetTools wordNetTools) {
        this.languageModelScorer = languageModelScorer;

        this.stanfordPreProcessor = stanfordPreProcessor;

        this.wordNetTools = wordNetTools;

        this.ambiguousFeatureSetId = ambiguousFeatureSet.getId();
        this.wordSenseFeatureMap = new HashMap<>();
        ambiguousFeatureSet.getAmbiguousFeatureList().forEach(s -> this.wordSenseFeatureMap.put(
                s.getOriginalSenseKey(), s));
    }

    public String getAmbiguousFeatureSetId() {
        return ambiguousFeatureSetId;
    }

    public Map<String, Double> getVector(String originalSenseKey, AmbiguousFeatureSetVectorConfig vectorConf) throws
            Exception {

        WordSenseFeature wordSenseFeature = this.wordSenseFeatureMap.get(originalSenseKey);

        String sense = wordSenseFeature.getSenseKey();

        switch (vectorConf.vectorCombinationType) {
            case SYN_SET:
                return getScoredMap(sense, getLemmaSet(wordSenseFeature.getSynonyms(),
                        wordSenseFeature.getSensePos(), vectorConf.extendSompoundWord), vectorConf.weightMethod);

            case GLOSS:
                return getScoredMap(sense, getLemmaSet(wordSenseFeature.getGlosses(),vectorConf.legalPos, vectorConf
                        .extendSompoundWord),
                        vectorConf.weightMethod);

            case RELATED_GLOSS:
                return getScoredMap(sense, getLemmaSet(wordSenseFeature.getGlossesRelatedWords(),vectorConf.legalPos, vectorConf.extendSompoundWord),
                        vectorConf.weightMethod);

            case FINE_GRAIN:
                String fineGrain = WordNetTools.getFineGrain(originalSenseKey);
                if (fineGrain.length() > 0) {
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(fineGrain);
                    return getScoredMap(sense, getLemmaSet(list,
                            wordSenseFeature.getSensePos(), vectorConf.extendSompoundWord), vectorConf.weightMethod);
                } else {
                    return new HashMap<>();
                }

            case SYN_SET__GLOSS://synSet2 gloss 1
                HashSertInteger<String> lemmaScore = new HashSertInteger<String>();
                getLemmaSet(wordSenseFeature.getSynonyms(), wordSenseFeature.getSensePos(),
                        vectorConf.extendSompoundWord).forEach(s -> lemmaScore.add(s, 2));
                for (Map.Entry<String, Integer> item : getLemmaSet(wordSenseFeature.getGlosses(),
                       vectorConf.legalPos, vectorConf.extendSompoundWord).entrySet()) {
                    lemmaScore.add(item.getKey(), item.getValue());
                }
                return getScoredMap(sense, lemmaScore.getHashMap(),
                        vectorConf.weightMethod);

            case SYN_SET__GLOSS_RELATED__GLOSS://synSet3 gloss 2 relatedGloss 1
                lemmaScore = new HashSertInteger<String>();
                getLemmaSet(wordSenseFeature.getSynonyms(), wordSenseFeature.getSensePos(),
                        vectorConf.extendSompoundWord).forEach(s -> lemmaScore.add(s, 3));
                for (Map.Entry<String, Integer> item : getLemmaSet(wordSenseFeature.getGlosses(),
                       vectorConf.legalPos, vectorConf.extendSompoundWord).entrySet()) {
                    lemmaScore.add(item.getKey(), 2 * item.getValue());
                }
                for (Map.Entry<String, Integer> item : getLemmaSet(wordSenseFeature.getGlossesRelatedWords(),
                       vectorConf.legalPos, vectorConf.extendSompoundWord).entrySet()) {
                    lemmaScore.add(item.getKey(), item.getValue());
                }
                return getScoredMap(sense, lemmaScore.getHashMap(),
                        vectorConf.weightMethod);

            case FINE_GRAIN__SYN_SET__GLOSS_RELATED__GLOSS://fineGrain4 synSet3 gloss 2 relatedGloss 1
                lemmaScore = new HashSertInteger<String>();
                getLemmaSet(wordSenseFeature.getSynonyms(), wordSenseFeature.getSensePos(),
                        vectorConf.extendSompoundWord).forEach(s -> lemmaScore.add(s, 3));
                for (Map.Entry<String, Integer> item : getLemmaSet(wordSenseFeature.getGlosses(),
                       vectorConf.legalPos, vectorConf.extendSompoundWord).entrySet()) {
                    lemmaScore.add(item.getKey(), 2 * item.getValue());
                }
                for (Map.Entry<String, Integer> item : getLemmaSet(wordSenseFeature.getGlossesRelatedWords(),
                       vectorConf.legalPos, vectorConf.extendSompoundWord).entrySet()) {
                    lemmaScore.add(item.getKey(), item.getValue());
                }
                fineGrain = WordNetTools.getFineGrain(originalSenseKey);
                if (fineGrain.length() > 0) {
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(fineGrain);
                    for (Map.Entry<String, Integer> item : getLemmaSet(list,
                           vectorConf.legalPos, vectorConf.extendSompoundWord).entrySet()) {
                        lemmaScore.add(item.getKey(), 4 * item.getValue());
                    }
                }
                return getScoredMap(sense, lemmaScore.getHashMap(),
                        vectorConf.weightMethod);

            case GLOSS__RELATED_GLOSS://gloss2 relatedGloss1
                lemmaScore = new HashSertInteger<String>();
                getLemmaSet(wordSenseFeature.getGlosses(),
                       vectorConf.legalPos, vectorConf.extendSompoundWord).entrySet().forEach(s -> lemmaScore.add(s.getKey(), 2 * s.getValue()));
                for (Map.Entry<String, Integer> item : getLemmaSet(wordSenseFeature.getGlossesRelatedWords(),
                       vectorConf.legalPos, vectorConf.extendSompoundWord).entrySet()) {
                    lemmaScore.add(item.getKey(), item.getValue());
                }
                return getScoredMap(sense, lemmaScore.getHashMap(),
                        vectorConf.weightMethod);
        }

        return new HashMap<>();
    }

    private Collection<String> getLemmaSet(List<String> tokens, POS pos, boolean extendCompoundWord) {
        HashSet<String> lemmas = new HashSet<>();

        for (String token : tokens) {
            String lemma = wordNetTools.getWordLemmaDaba(token, pos);
            if (lemma.length() > 0) {
                lemmas.add(lemma);
            }
        }

        if (extendCompoundWord) {
            HashSet<String> newWordSet = expandCompoundWord(lemmas);
            for (String token : newWordSet) {
                String lemma = wordNetTools.getWordLemmaDaba(token, pos);
                if (lemma.length() > 0) {
                    lemmas.add(lemma);
                }
            }
        }

        return lemmas;
    }

    private Map<String, Integer> getLemmaSet(List<String> strings,Set<POS> legalPos,boolean extendCompoundWord) {
        HashSertInteger<String> lemmas = new HashSertInteger<>();

        for (String string : strings) {
            stanfordPreProcessor.getLemma(string, legalPos).entrySet().forEach(s -> lemmas.add(s.getKey(), s
                    .getValue()));
        }

        if (extendCompoundWord) {
            Set<Map.Entry<String, Integer>> entries = lemmas.getHashMap().entrySet();
            for (Map.Entry<String, Integer> s : entries) {
                HashSet<String> newTokens = getNewExpandedCompoundWord(s.getKey());
                for (String token : newTokens) {
                    lemmas.add(token, s.getValue());
                }
            }
        }

        return lemmas.getHashMap();
    }

    Map<String, Double> getScoredMap(String sense, Map<String, Integer> lemmaMap, WeightMethod weightMethod) {
//        Map<String, Double> resultMap=new HashMap<>();
//        getScoredMap(sense,lemmaMap.keySet(),weightMethod).entrySet().forEach(s -> {
//            resultMap.put(s.getKey(),s.getValue()*lemmaMap.get(s.getKey()));
//        });
        return getScoredMap(sense, lemmaMap.keySet(), weightMethod);
    }

    Map<String, Double> getScoredMap(String sense, Collection<String> lemmaSet, WeightMethod weightMethod) {
        Map<String, Double> resultMap = new HashMap<>();
        double sumScore = 0.0;
        for (String lemma : lemmaSet) {
            if (lemma.equals(sense)) {
                resultMap.put(lemma, 0.);
                continue;
            }

            try {
                double score = getWeight(sense, lemma, weightMethod);
                if (score == 0) {
                    resultMap.put(lemma, 0.);
                    continue;
                }
                resultMap.put(lemma, score);
                sumScore += score;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (resultMap.containsKey(sense)) {
            resultMap.put(sense, 0.1);
        }

        return resultMap;
    }

    public static double maxScore = 0;

    double getWeight(String sense, String lemma, WeightMethod weightMethod) throws Exception {
        long co = this.languageModelScorer.getScore(sense, lemma);
        if (co == 0) {
            return 0.;
        }
        long lemmaDf = this.languageModelScorer.getScore(lemma);
        double score = co / (lemmaDf + 1.);
        switch (weightMethod) {
            case CO_ISF:
                break;

            case CO_ISF_LOGSF:
                score = score * Math.log(lemmaDf);
                break;
        }

        if (maxScore < score) {
            maxScore = score;
        }
        return score;
    }

    HashSet<String> expandCompoundWord(HashSet<String> words) {
        return expandCompoundWord(words, false);
    }

    HashSet<String> expandCompoundWord(HashSet<String> words, boolean getJustNewItem) {
        HashSet<String> resultExpandedTokens;
        if (getJustNewItem) {
            resultExpandedTokens = new HashSet<String>();
        } else {
            resultExpandedTokens = new HashSet<String>(words);
        }

        for (String word : words) {
            HashSet<String> newTokens = getNewExpandedCompoundWord(word);
            for (String split : newTokens) {
                resultExpandedTokens.add(split);
            }
        }

        return resultExpandedTokens;
    }

    public static HashSet<String> getNewExpandedCompoundWord(String word) {
        HashSet<String> result = new HashSet<>();
        String[] splits = word.split("_");
        if (splits.length > 1) {
            for (String token : splits) {
                result.add(token);
            }
        }
        return result;
    }

    public static Map<String, Double> filterZeroScores(Map<String, Double> wordWeight) {
        return wordWeight.entrySet().stream().filter(s -> s.getValue() != 0).collect
                (Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static List<PairValue<String, Double>> getSortedPairValue(Map<String, Double> weightedItems, boolean
            doNormalize) {
        List<PairValue<String, Double>> result = new ArrayList<>();
        double sumScore = 0;
        for (Map.Entry<String, Double> item :
                tools.util.sort.Collection.mapSortedByValues(weightedItems)) {
            sumScore += item.getValue();
            result.add(new PairValue<String, Double>(item.getKey(), item.getValue()));
        }

        if (doNormalize) {
            sumScore = sumScore > 0 ? sumScore : 1;

            for (int i = 0; i < result.size(); i++) {
                result.get(i).setValue2(result.get(i).getValue2() / sumScore);
            }
        }

        return result;
    }

    public static enum VectorCombinationType {
        SYN_SET, GLOSS, RELATED_GLOSS, FINE_GRAIN, SYN_SET__GLOSS, GLOSS__RELATED_GLOSS,
        SYN_SET__GLOSS_RELATED__GLOSS, FINE_GRAIN__SYN_SET__GLOSS_RELATED__GLOSS
    }

    public static enum WeightMethod {
        CO_ISF, CO_ISF_LOGSF
    }

    public static class AmbiguousFeatureSetVectorConfig {
        /**
         * parameters
         */
        
        final VectorCombinationType vectorCombinationType;
        final WeightMethod weightMethod;//confing.weightMethod = WeightMethod.CO_ISF;
        final boolean extendSompoundWord;//EXTEND_COMPOUND_WORD = true;
        final Set<POS> legalPos;//LEGAL_POS = Stream.of(POS.NOUN, POS.ADJECTIVE).
        final boolean doNormalizeVectorWeights;
        final boolean doNormalizeNegativeValue;
        final boolean doNormalizeValueByMin;//mainly used for convert negative scores to positives
        // collect(Collectors.toCollection(HashSet::new));

        public AmbiguousFeatureSetVectorConfig(VectorCombinationType vectorCombinationType,
                                               WeightMethod weightMethod,
                                               boolean extendCompoundWord,
                                               Set<POS> legalPos,
                                               boolean doNormalizeVectorWeights,
                                               boolean doNormalizeNegativeValue,
                                               boolean doNormalizeValueByMin){
            this.vectorCombinationType=vectorCombinationType;
            this.weightMethod=weightMethod;
            this.extendSompoundWord=extendCompoundWord;
            this.legalPos=legalPos;
            this.doNormalizeVectorWeights=doNormalizeVectorWeights;
            this.doNormalizeNegativeValue=doNormalizeNegativeValue;
            this.doNormalizeValueByMin=doNormalizeValueByMin;
        }

        public boolean isDoNormalizeVectorWeights(){
            return this.doNormalizeVectorWeights;
        }

        public boolean isDoNormalizeNegativeValue() {
            return doNormalizeNegativeValue;
        }

        public VectorCombinationType getVectorCombinationType() {
            return vectorCombinationType;
        }

        public WeightMethod getWeightMethod() {
            return weightMethod;
        }

        public boolean isExtendSompoundWord() {
            return extendSompoundWord;
        }

        public Set<POS> getLegalPos() {
            return legalPos;
        }

        public String name() {
            String posStr = "";
            for (POS pos : legalPos) {
                posStr+=pos.name()+"-";
            }
            posStr=posStr.substring(0,posStr.length()-1);

            return this.vectorCombinationType.name()+"__"+this.weightMethod.name()+"__"+
                    posStr+"__"+(this.extendSompoundWord?"1":"0")+"__"+(this.doNormalizeVectorWeights?"1":"0")+
                    "__"+(this.doNormalizeNegativeValue?"1":"0"+"__"+(this.doNormalizeValueByMin?"1":"0"));
        }

        public boolean isDoNormalizeValueByMin() {
            return doNormalizeValueByMin;
        }
    }
    
}