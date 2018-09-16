package nlp.wsd.co;

import com.google.common.base.Stopwatch;
import edu.mit.jwi.item.POS;
import nlp.languagemodel.BiTrigramScoreDB;
import nlp.languagemodel.BigramScoreDB;
import nlp.languagemodel.LanguageModelScorer;
import nlp.languagemodel.SimpleBigramModel;
import nlp.preprocess.datatype.Word;
import nlp.preprocess.en.StanfordPreProcessor;
import nlp.wordnet.WordNetTools;
import nlp.wordnet.WordSenseFeature;
import nlp.wsd.co.datatype.*;
import nlp.wsd.co.scorer.BigramScorer;
import nlp.wsd.co.scorer.WordWeightier;
import tools.database.DbConnection;
import tools.database.DbTools;
import tools.util.ConsuleInput;
import tools.util.PrintWriterWithBuffer;
import tools.util.collection.HashSertDouble;
import tools.util.collection.HashSertInteger;
import tools.util.collection.KeyValueSimple;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Saeed on 9/5/15.
 */
public class WSDSimpleCoScore implements Serializable {

    static int staticCounter = 0;
    LanguageModelScorer languageModelScorer;
    StanfordPreProcessor stanfordPreProcessor;
    Set<POS> correctPos;
    HashSertInteger<String> notFoundLemma;
    WordNetTools wordNetTools;

    PrintWriterWithBuffer logFileWriter;

    public void setLogFileWriter(PrintWriterWithBuffer logFileWriter) {
        this.logFileWriter = logFileWriter;
    }


    public Set<POS> getCorrectPos() {
        return correctPos;
    }

    public long getCoScore(String... tokens) throws Exception {
        switch (tokens.length) {
            case 1:
                return this.languageModelScorer.getScore(tokens[0]);
            case 2:
                return this.languageModelScorer.getScore(tokens[0], tokens[1]);
            case 3:
                return this.languageModelScorer.getScore(tokens[0], tokens[1], tokens[2]);
        }
        return -1;
    }

    public WSDSimpleCoScore(String savedBigramModelPath, Set<POS> correctPos, WordNetTools wordNetTools,
                            StanfordPreProcessor stanfordPreProcessor) throws
            Exception {
        this.correctPos = new HashSet<POS>(correctPos);
//        for (String pos : correctPos) {
//            this.correctPos.add(WordNetTools.getPos(pos));
//        }
        this.languageModelScorer = SimpleBigramModel.newInstance(savedBigramModelPath);
        this.notFoundLemma = new HashSertInteger<String>();
        this.wordNetTools = wordNetTools;
        this.stanfordPreProcessor = stanfordPreProcessor;
    }

    private WSDSimpleCoScore() {
    }

    public static WSDSimpleCoScore getDBInstance(Properties dbProperties, String tokenFileAddress, Set<POS> correctPos, WordNetTools wordNetTools,
                                                 StanfordPreProcessor stanfordPreProcessor) throws
            Exception {
        System.out.println("Creating WSDSimpleCoScore ...");
        Stopwatch stopwatch = Stopwatch.createStarted();

        WSDSimpleCoScore wsdSimpleCoScore = new WSDSimpleCoScore();

        wsdSimpleCoScore.correctPos = new HashSet<POS>(correctPos);

        String dbUrl = DbTools.getDbUrl(dbProperties.getProperty("mysqlDbServerAddress"),
                dbProperties.containsKey("mysqlDbPort")?Integer.parseInt(dbProperties.getProperty("mysqlDbPort")):DbTools.getDefaultPort(DbTools.DbType.MYSQl),
                dbProperties.getProperty("mysqlDbName"),
                DbTools.DbType.MYSQl);
        DbConnection dbConnection = DbConnection.getInstance(dbUrl, DbTools.getClass(DbTools.DbType.MYSQl), dbProperties.getProperty("mysqlDbUsername"),
                dbProperties.getProperty("mysqlDbPass"));
        wsdSimpleCoScore.languageModelScorer = new BigramScoreDB(tokenFileAddress,
                dbConnection,
                dbProperties.getProperty("mysqlDbTableName"),
                Integer.parseInt(dbProperties.getProperty("tablePartitionCount")),
                stanfordPreProcessor);

        wsdSimpleCoScore.notFoundLemma = new HashSertInteger<String>();
        wsdSimpleCoScore.wordNetTools = wordNetTools;
        wsdSimpleCoScore.stanfordPreProcessor = stanfordPreProcessor;
        System.out.println("WsdSimpleCoScore created after " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
        return wsdSimpleCoScore;
    }

    public WSDSimpleCoScore(String tokenFileAddress, Map<String, Long> termDf, String dbServerName, String dbName,
                            String user, String pass,
                            int tableCount, String wordnetDictDir) throws SQLException, ClassNotFoundException,
            IOException {
        this.correctPos = new HashSet<POS>();
        for (String pos : "EX,FW,JJ,JJR,JJS,NN,NNS,NNP,NNPS,WP".split(",")) {
            this.correctPos.add(WordNetTools.getPos(pos));
        }
        DbConnection dbConnection = DbConnection.getInstance(DbTools.getDbUrl(dbServerName, DbTools.getDefaultPort(DbTools.DbType.MYSQl), dbName, DbTools.DbType.MYSQl),
                DbTools.getClass(DbTools.DbType.MYSQl), user, pass);
        this.languageModelScorer = new BiTrigramScoreDB(tokenFileAddress, termDf, dbConnection,
                tableCount);
        this.notFoundLemma = new HashSertInteger<String>();
        this.wordNetTools = new WordNetTools(wordnetDictDir);
    }


    public HashMap<String, Integer> removeAllNotFoundLemma() {
        HashMap<String, Integer> map = new HashMap<String, Integer>(this.notFoundLemma.getHashMap());
        this.notFoundLemma.clear();
        return map;
    }


    public Map<String, String> getDisambiguatedSenseDocument(Map<String, String> senseKey,
                                                             Document document, int wordDist,
                                                             int sentenceDist,
                                                             boolean containsSenseInSentenceEnable,
                                                             NgramType ngramType, ScoreFunction scoreFunction,
                                                             WordWeightier.WeightFunction weightFunction,
                                                             List<DebugItem> tempDebugLog
    ) throws
            Exception {
        int counter = 0;
        Map<String, String> result = new HashMap<String, String>();

        String contextText = document.getLemmaText();

        for (int sentenceIndex = 0; sentenceIndex < document.getSentenceCount(); sentenceIndex++) {

            Map<Integer, Sentence> contextSentenceMap = document.getContextSentence(sentenceIndex, sentenceDist);
            Sentence sentence = document.getSentence(sentenceIndex);

            int senseCount = 0;
            if (sentence.getIdAmbiguousFeaturesMap().size() > 0) {
                senseCount = sentence.getIdAmbiguousFeaturesMap().size();

                if (logFileWriter != null) {
                    logFileWriter.println("\n\n\n**************** docId:" + document.getDocId() + " , " +
                            "sentenceIndex:" + sentenceIndex + "\tsenseCount:" + senseCount);
                    logFileWriter.flush();
                }

                int ambigCounter = 0;
                for (Map.Entry<String, AmbiguousFeatureSet> stringAmbiguousFeatureSetEntry : sentence
                        .getIdAmbiguousFeaturesMap().entrySet()) {


                    String ambiguousFeatureSetId = stringAmbiguousFeatureSetEntry.getValue().getId();
                    Word ambiguousWord = sentence.getAmbiguousWord(ambiguousFeatureSetId);

                    if (logFileWriter != null) {
                        logFileWriter.println
                                ("####################################################################################\n@" +
                                        " " + ++ambigCounter + "/" + senseCount + ") ambiguousFeatureSetId:" +
                                        ambiguousFeatureSetId +
                                        " , " + "ambiguousWord:" + ambiguousWord.toString());
                    }

                    String allSense = "";
                    for (WordSenseFeature sense : stringAmbiguousFeatureSetEntry.getValue()
                            .getAmbiguousFeatureList()) {
                        if (logFileWriter != null) {
                            logFileWriter.println(sense.getOriginalSenseKey());
                        }
                        allSense += sense.getOriginalSenseKey() + "\t";
                    }
                    if (logFileWriter != null) {
                        logFileWriter.println("\n________________________________-> result:" + senseKey.get
                                (ambiguousFeatureSetId) + "\t[" + allSense + "]" +
                                "\n" + sentence.getStringValue(StanfordPreProcessor.PreprocessType.Tokenize) +
                                "\n" + sentence.getStringValue(StanfordPreProcessor.PreprocessType.Lemmatize) +
                                "\n" + sentence.getStringValue(StanfordPreProcessor.PreprocessType.PosTagged));
                        logFileWriter.println("all context:: ");
                    }
                    boolean sentenceAdded = false;
                    for (Map.Entry<Integer, Sentence> indexItem : contextSentenceMap.entrySet().stream().sorted((a, b) -> a.getKey().compareTo(b.getKey())).collect(Collectors.toList())) {
                        if (indexItem.getKey() == 1 && !sentenceAdded) {
                            if (logFileWriter != null) {
                                logFileWriter.println(sentence.getStringValue(StanfordPreProcessor.PreprocessType.Tokenize));
                            }
                        }
                        if (logFileWriter != null) {
                            logFileWriter.println(indexItem.getValue().getStringValue(StanfordPreProcessor.PreprocessType.Tokenize));
                        }
                        if (indexItem.getKey() == -1 && !sentenceAdded) {
                            if (logFileWriter != null) {
                                logFileWriter.println(sentence.getStringValue(StanfordPreProcessor.PreprocessType.Tokenize));
                            }
                        }
                    }

                    for (WordSenseFeature wordSenseFeature : stringAmbiguousFeatureSetEntry.getValue().getAmbiguousFeatureList()) {
                        if (logFileWriter != null) {
                            logFileWriter.println("______");
                            logFileWriter.println(wordSenseFeature.toString());
                        }
                    }
                    if (logFileWriter != null) {
                        logFileWriter.flush();
                        logFileWriter.getAndClearTempBuffer();
                    }

                    if (logFileWriter != null) {
                        logFileWriter.println(ambiguousFeatureSetId + "\t" + sentence.getAmbiguousFeatureSetIdWordIndex
                                (ambiguousFeatureSetId) + "\t" + ambiguousWord.getLemma());
                    }


                    Set<KeyValueSimple<Integer, Word>> bigramContextWordSet = null;
                    Set<KeyValueSimple<KeyValueSimple<Integer, Word>, KeyValueSimple<Integer, Word>>>
                            trigramContextWordSet = null;

                    HashMap<Word, Integer> bigramContextWordTFNeighbourSentence = null;
                    HashMap<KeyValueSimple<Word, Word>, Integer> trigramContextWordTFNeighbourSentence = null;

                    if (ngramType == NgramType.BIGRAM || ngramType == NgramType.BIGRAMTRIGRAM) {
                        bigramContextWordSet = sentence.getContextWordByAmbiguousFeatureSetId(ambiguousFeatureSetId,
                                wordDist, containsSenseInSentenceEnable);
                        bigramContextWordTFNeighbourSentence = getBigramContext(contextSentenceMap.values());
                    }
                    if (logFileWriter != null) {
                        logFileWriter.println();
                    }
                    Map<String, Double> senseScore = new HashMap<String, Double>();


                    double maxScore = 0;
                    double beforeMaxScore = 0;
                    String senseByMaxScore = "";
                    boolean isContain = false;
                    String containSense = "";
                        int senseCounter = 0;
                        for (WordSenseFeature wordSenseFeature : stringAmbiguousFeatureSetEntry.getValue()
                                .getAmbiguousFeatureList()) {
                            if (logFileWriter != null) {
                                logFileWriter.println("\n________________________________ " + ++senseCounter + ") " +
                                        wordSenseFeature.getOriginalSenseKey() + " -> result:" + senseKey.get
                                        (ambiguousFeatureSetId) + "\t[" + allSense + "]" +
                                        "\n" + sentence.getStringValue(StanfordPreProcessor.PreprocessType.Tokenize) +
                                        "\n" + sentence.getStringValue(StanfordPreProcessor.PreprocessType.Lemmatize) +
                                        "\n" + sentence.getStringValue(StanfordPreProcessor.PreprocessType.PosTagged));
                            }

                            double score = 0;
                            if (ngramType == NgramType.BIGRAM) {
                                score = getBigramScore(contextText, ambiguousWord, bigramContextWordSet,
                                        stringAmbiguousFeatureSetEntry.getValue(),
                                        wordSenseFeature.getOriginalSenseKey(), scoreFunction,
                                        weightFunction,
                                        bigramContextWordTFNeighbourSentence
                                );
                            }
                            if (logFileWriter != null) {
                                logFileWriter.println(wordSenseFeature.getOriginalSenseKey() + " -> " + tools.util.Str
                                        .format
                                                (score, 10));
                            }
                            senseScore.put(wordSenseFeature.getOriginalSenseKey(), score);
                        }

                    //getMaxScore
                    for (Map.Entry<String, Double> stringLongEntry : senseScore.entrySet()) {

                        if (containsSenseInSentenceEnable && sentence.contain(stringLongEntry.getKey())) {
                            String logString = "CONTAIN SENSE IN SENTENCE -->  " + ++staticCounter + " : " +
                                    ambiguousFeatureSetId + " ---> " + stringLongEntry.getKey();
                            System.out.println(logString);
                            if (logFileWriter != null) {
                                logFileWriter.println(logString);
                            }
                            if (isContain) {
                                if (logFileWriter != null) {
                                    logFileWriter.println("********** YA Abar Farz");
                                }
                                System.out.println("********** YA Abar Farz");
                            } else {
                                isContain = true;
                                containSense = stringLongEntry.getKey();
                            }
                        }

                        if (stringLongEntry.getValue() >= maxScore) {
                            senseByMaxScore = stringLongEntry.getKey();
                            beforeMaxScore = maxScore;
                            maxScore = stringLongEntry.getValue();
                        }
                    }

                    String ambiguousFeatureSetIdLog = logFileWriter != null ? logFileWriter.getAndClearTempBuffer() : "loggerIsNull";


                    if (logFileWriter != null) {
                        logFileWriter.println("\nselection result:");

                        String key = "";// what happen key is null
                        if (senseKey.containsKey(ambiguousFeatureSetId)) {
                            String[] splits = senseKey.get(ambiguousFeatureSetId).split("\t");
                            key = splits[0];
                            if (splits.length > 1) {
                                key = splits[1];
                            }
                        } else {
//                        if(logFileWriter!=null) {
                            logFileWriter.println("\t\t\t\t key is null what?????????????????");
//                        }
                        }
                        int k = 0;
                        for (Map.Entry<String, Double> senseScoreItem : tools.util.sort.Collection
                                .mapSortedByValuesDecremental(senseScore)) {
                            String yesNo = "";
                            if (k == 0) {
                                yesNo = "\t* " + key.equals(senseScoreItem.getKey()) + " *\t";
                            } else {
                                if (key.equals(senseScoreItem.getKey())) {
                                    yesNo = "\t\t<-correct answer missed";
                                }
                            }
//                        if(logFileWriter!=null) {
                            logFileWriter.println("\t\t" + ++k + ") " + senseScoreItem.getKey() + "\t" + senseScoreItem
                                    .getValue()
                                    + yesNo);
//                        }
                        }
                    }

                    if (maxScore >= 0) {//???????????????????????? recall <================= *******************
                        if (containsSenseInSentenceEnable && isContain) {
                            result.put(ambiguousFeatureSetId, containSense);
                            if (tempDebugLog != null) {
                                tempDebugLog.add(new DebugItem(ambiguousFeatureSetId, senseByMaxScore,
                                        maxScore, beforeMaxScore, ambiguousFeatureSetIdLog, senseCount, true));
                            }
                        } else {
                            result.put(ambiguousFeatureSetId, senseByMaxScore);
                            if (tempDebugLog != null) {
                                tempDebugLog.add(new DebugItem(ambiguousFeatureSetId, senseByMaxScore,
                                        maxScore, beforeMaxScore, ambiguousFeatureSetIdLog, senseCount, false));
                            }
                        }
                    }

                    System.out.println("doc_" + document.getDocId() + ": " + ++counter + " handled [" +
                            stringAmbiguousFeatureSetEntry.getKey() + "].");
                }

            }
        }
        return result;
    }

    private void printAmbiguousWordVectorItemsStat(AmbiguousFeatureSet ambiguousFeatureSet, WordSenseFeature wordSenseFeature, AmbiguousFeatureSetVector.AmbiguousFeatureSetVectorConfig vectorConf)
            throws Exception {
        AmbiguousFeatureSetVector ambiguousFeatureSetVector = new AmbiguousFeatureSetVector
                (ambiguousFeatureSet, this.languageModelScorer, this.stanfordPreProcessor, this.wordNetTools);

        ArrayList<AmbiguousFeatureSetVector.VectorCombinationType> typeList = new ArrayList<>();
        typeList.add(AmbiguousFeatureSetVector.VectorCombinationType.FINE_GRAIN);
        typeList.add(AmbiguousFeatureSetVector.VectorCombinationType.SYN_SET);
        typeList.add(AmbiguousFeatureSetVector.VectorCombinationType.GLOSS);
        typeList.add(AmbiguousFeatureSetVector.VectorCombinationType.RELATED_GLOSS);


        AmbiguousFeatureSetVector.AmbiguousFeatureSetVectorConfig newVectorConf;

        for (AmbiguousFeatureSetVector.VectorCombinationType type : typeList) {
            newVectorConf = new AmbiguousFeatureSetVector
                    .AmbiguousFeatureSetVectorConfig(type, vectorConf.getWeightMethod(),
                    vectorConf.isExtendSompoundWord(), vectorConf.getLegalPos(), vectorConf
                    .isDoNormalizeVectorWeights(), vectorConf.isDoNormalizeNegativeValue(), vectorConf.isDoNormalizeValueByMin());
            Map<String, Double> vector = ambiguousFeatureSetVector.getVector(wordSenseFeature
                    .getOriginalSenseKey(), newVectorConf);
            this.logFileWriter.println("________");
            for (Map.Entry<String, Double> item : vector.entrySet().stream()
                    .sorted((a, b) -> -a.getValue().compareTo(b.getValue()))
                    .collect(Collectors.toList())) {
                this.logFileWriter.println(type.name() + ": " + item.getKey()
                        + "\t" + item.getValue());
            }
        }
        this.logFileWriter.println();
    }

    private HashMap<Word, Integer> getBigramContext(Collection<Sentence> contextSentenceSet) {
        HashSertInteger<Word> result = new HashSertInteger<Word>();
        for (Sentence sentence : contextSentenceSet) {
            for (Word word : sentence.getAllWord()) {
                result.put(word);
            }
        }
        return result.getHashMap();
    }

    private HashMap<KeyValueSimple<Word, Word>, Integer> getTrigramContext(Collection<Sentence> contextSentenceSet) {
        HashSertInteger<KeyValueSimple<Word, Word>> result = new HashSertInteger<KeyValueSimple<Word, Word>>();
        ArrayList<Word> allContextWordsList = new ArrayList<Word>();
        for (Sentence sentence : contextSentenceSet) {
            allContextWordsList.addAll(sentence.getAllWord());
        }

        for (int i = 0; i < allContextWordsList.size(); i++) {
            for (int j = i + 1; j < allContextWordsList.size(); j++) {
                if (!allContextWordsList.get(i).getLemma().equals(allContextWordsList.get(j))) {
                    result.add(new KeyValueSimple<Word, Word>(allContextWordsList.get(i), allContextWordsList.get(j)));
                }
            }
        }

        return result.getHashMap();
    }

    private Set<KeyValueSimple<KeyValueSimple<Integer, Word>, KeyValueSimple<Integer, Word>>> getTrigramContextWord
            (Sentence sentence, String ambiguousFeatureSetId, int dist) {
        Set<KeyValueSimple<KeyValueSimple<Integer, Word>, KeyValueSimple<Integer, Word>>> contextWordsSet = new
                HashSet<KeyValueSimple<KeyValueSimple<Integer, Word>, KeyValueSimple<Integer, Word>>>();

        List<KeyValueSimple<Integer, Word>> contextWordList = new ArrayList<KeyValueSimple<Integer, Word>>(sentence
                .getContextWordByAmbiguousFeatureSetId(ambiguousFeatureSetId, dist));

        int baseIndex = sentence.getAmbiguousFeatureSetIdWordIndex(ambiguousFeatureSetId);

        for (int i = 0; i < (contextWordList.size() - 1); i++) {
            for (int j = i + 1; j < contextWordList.size(); j++) {
                Word word1 = sentence.getWord(baseIndex + contextWordList.get(i).getKey());
                Word word2 = sentence.getWord(baseIndex + contextWordList.get(j).getKey());
                if (this.correctPos.contains(word1.getPosValue()) && this.correctPos.contains(word2.getPosValue())) {
                    contextWordsSet.add(new KeyValueSimple<KeyValueSimple<Integer, Word>, KeyValueSimple<Integer,
                            Word>>(contextWordList.get(i), contextWordList.get(j)));
                }
            }
        }
        return contextWordsSet;
    }


    /**
     * Main scorer method used fo BIGRAM
     *
     * @param ambiguousWord
     * @param bigramContextWordSet
     * @param scoreFunction
     * @param weightFunction
     * @param bigramContextWordTFNeighbourSentence
//     * @param graphNormalizationMethod
//     * @param graphFunction
//     * @param vectorDiffType
     * @return
     * @throws Exception
     */
    private double getBigramScore(String contextText,
                                  Word ambiguousWord,
                                  Set<KeyValueSimple<Integer, Word>> bigramContextWordSet,
                                  AmbiguousFeatureSet ambiguousFeatureSet,
                                  String ambiguousWordOriginalKey,
                                  ScoreFunction scoreFunction,
                                  WordWeightier.WeightFunction weightFunction,
                                  HashMap<Word, Integer> bigramContextWordTFNeighbourSentence
    ) throws
            Exception {

        WordSenseFeature wordSenseFeature = ambiguousFeatureSet.getAmbiguousFeature
                (ambiguousWordOriginalKey);

        HashMap<Word, Double> contextLemma = new HashMap<Word, Double>();
//        bigramContextWordTFNeighbourSentence.entrySet().forEach(s -> contextLemma.put(s.getKey(),0.));
        bigramContextWordSet.forEach(s -> contextLemma.put(s.getValue(), 1.));
        Map<String, Double> lemmaWeightMap = WordWeightier.getWeight(contextText, ambiguousWord, contextLemma, languageModelScorer,
                weightFunction, logFileWriter).entrySet().stream().filter(s -> s.getValue() > 0).collect(Collectors
                .toMap(Map.Entry::getKey, Map.Entry::getValue));

        HashSertDouble<String> tempBufferNeighborVectorWordScore = new HashSertDouble<String>();
        Map<String, Double> contextScore = getDocumentContextBigramScore(ambiguousFeatureSet,
                ambiguousWordOriginalKey, bigramContextWordTFNeighbourSentence, scoreFunction, //senseScorerType,
//                vectorConf,
                tempBufferNeighborVectorWordScore).entrySet().stream().filter(s -> s.getValue() > 0).collect(Collectors
                .toMap(Map.Entry::getKey, Map.Entry::getValue));
        ;

        double sumBigramScoreDocumentContext = 0;
        for (Map.Entry<String, Double> lemmaScore : contextScore.entrySet()) {
            double weight = lemmaWeightMap.containsKey(lemmaScore.getKey()) ? lemmaWeightMap.get(lemmaScore.getKey())
                    : 1;
            sumBigramScoreDocumentContext += weight * lemmaScore.getValue();
        }
        if (sumBigramScoreDocumentContext > 0) {
            logFileWriter.println("sumBigramScoreDocumentContext: " + sumBigramScoreDocumentContext);
        }

        HashMap<String, Double> tempBufferContextWordScore = new HashMap<String, Double>();
        HashSertDouble<String> tempBufferVectorWordScore = new HashSertDouble<String>();
        double sumScore = 0;
        for (KeyValueSimple<Integer, Word> integerWordKeyValueSimple : bigramContextWordSet) {
            Word word = integerWordKeyValueSimple.getValue();
            double score = getScore(ambiguousFeatureSet, ambiguousWordOriginalKey, word, scoreFunction,
//                    senseScorerType,
//                    vectorConf,
                    tempBufferVectorWordScore);
            if (score > 0) {
                tempBufferContextWordScore.put(word.getLemma() + "\t" + word.getPos(), score);
                if (lemmaWeightMap.containsKey(word.getLemma())) {
                    double weight = lemmaWeightMap.get(word.getLemma());
                    sumScore += score * weight;
                } else {
                    if (logFileWriter != null) {
                        logFileWriter.println("\t\t\t" + word.getLemma() + " -> weight:0");
                    }
                }
            }
        }

        if (logFileWriter != null) {
            logFileWriter.println("\n--------------- context feature scores");
        }
        int i = 0;
        for (Map.Entry<String, Double> stringDoubleEntry : tools.util.sort.Collection.mapSortedByValuesDecremental
                (tempBufferContextWordScore)) {
            if (logFileWriter != null) {
                logFileWriter.println(++i + ": " + stringDoubleEntry.getKey() + "  score:" + tools.util.Str.format
                        (stringDoubleEntry.getValue(), 15));
            }
        }

        if (tempBufferVectorWordScore.getAllPutScore() > 0) {
            if (logFileWriter != null) {
                logFileWriter.println("\n---------------- expanded feature scores");
            }
            i = 0;
            for (Map.Entry<String, Double> stringDoubleEntry : tools.util.sort.Collection.mapSortedByValuesDecremental
                    (tempBufferVectorWordScore.getHashMap())) {
                if (logFileWriter != null) {
                    logFileWriter.println(++i + ": " + stringDoubleEntry.getKey() + "  score:" + tools.util.Str.format
                            (stringDoubleEntry.getValue(), 15));
                }
            }
        }

        if (tempBufferNeighborVectorWordScore.getAllPutScore() > 0) {
            if (logFileWriter != null) {
                logFileWriter.println("\n----------------- expanded feature neighbor scores");
            }
            i = 0;
            for (Map.Entry<String, Double> stringDoubleEntry : tools.util.sort.Collection.mapSortedByValuesDecremental
                    (tempBufferNeighborVectorWordScore.getHashMap())) {
                if (logFileWriter != null) {
                    logFileWriter.println(++i + ": " + stringDoubleEntry.getKey() + "  score:" + tools.util.Str.format
                            (stringDoubleEntry.getValue(), 15));
                }
            }
        }

        double finalScore = sumScore + 0.3 * sumBigramScoreDocumentContext;

        if (sumBigramScoreDocumentContext > 0) {
            logFileWriter.println("finalScore=sumScore:" + sumScore + "[innerSentenceContext] + 0.3 * " +
                    "" + sumBigramScoreDocumentContext + "[neighborSentenceContext]");
        }

        if (logFileWriter != null) {
            logFileWriter.println("beforeNormalizeFinalScore: " + tools.util.Str.format(finalScore, 8));
        }

        if (wordSenseFeature.getSensePos() != null) {
            System.out.println(wordSenseFeature.getOriginalSenseKey() + " \t\t " + wordSenseFeature.getSensePos()
                    .name());
        }

        return finalScore;
    }



    private Map<String, Double> getDocumentContextBigramScore(AmbiguousFeatureSet ambiguousFeatureSet,
                                                              String ambiguousSenseOriginalKey,
                                                              HashMap<Word, Integer>
                                                                      bigramContextWordTFNeighbourSentence,
                                                              ScoreFunction scoreFunction, //SenseScorerType
                                                              // senseScorerType,
//                                                              AmbiguousFeatureSetVector
//                                                                      .AmbiguousFeatureSetVectorConfig vectorConf,
                                                              HashSertDouble<String> tempBufferVectorWordScore)
            throws Exception {
        Map<String, Double> result = new HashMap<>();
        double sumScore = 0;
        for (Map.Entry<Word, Integer> wordTf : bigramContextWordTFNeighbourSentence.entrySet()) {
            Word word = wordTf.getKey();
            double score = getScore(ambiguousFeatureSet, ambiguousSenseOriginalKey, word,
                    scoreFunction,
//                    senseScorerType,
//                    vectorConf,
                    tempBufferVectorWordScore);//.getValue();
            if (result.containsKey(word.getLemma())) {
                result.put(word.getLemma(), score + result.get(word.getLemma()));
            } else {
                result.put(word.getLemma(), score);
            }

            logFileWriter.println(score < 0 ? "\t\t\t\t\t\t\t" : "" + word + "  score:" + score);
            if (score > 0) {
                sumScore += score;
            }
        }
        return result;
    }


    //WordSenseFeature wordSenseFeature,
    private double getScore(AmbiguousFeatureSet ambiguousFeatureSet, String
            ambiguousSenseKey, Word word, ScoreFunction scoreFunction, //SenseScorerType senseScorerType,
//                            AmbiguousFeatureSetVector.AmbiguousFeatureSetVectorConfig vectorConf,
                            HashSertDouble
                                    <String> tempBufferVectorWordScore) throws
            Exception {
        BigramScorer bigramScorer = new BigramScorer(this.languageModelScorer);
//        WordSenseFeature wordSenseFeature = ambiguousFeatureSet.getAmbiguousFeature(ambiguousSenseKey);
        double score = -3;
        POS pos = word.getPosValue();
        if (pos != null && this.correctPos.contains(pos)) {
            String lemma = word.getLemma();
//            if (vectorConf == null) {//senseScorerType == SenseScorerType.JUST_SENSE) {
                try {
                    //extended sense
                    String[] senseLemmas = ambiguousFeatureSet.getAmbiguousFeature(ambiguousSenseKey).getSenseKey().split("~");
                    if (senseLemmas.length > 1) {
                        for (String senseLemma : senseLemmas) {
                            score += bigramScorer.getScore1(BigramScorer.BigramScoreFunction.valueOf(scoreFunction.name()),
                                    senseLemma, lemma);
                        }
                    } else {
                        score = bigramScorer.getScore1(BigramScorer.BigramScoreFunction.valueOf(scoreFunction.name()),
                                ambiguousFeatureSet.getAmbiguousFeature(ambiguousSenseKey).getSenseKey(), lemma);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ConsuleInput.readStringUTF8("Press any key ????????????");
                    throw e;
                }

            if (score > 0) {
                if (this.languageModelScorer.getScore(lemma) > 0) {
                } else {
                    logFileWriter.println("notFoundLemma: " + lemma + "  score:" + score + " -> 0");
                    score = 0;
                    this.notFoundLemma.put(word + "\t" + lemma);
                }
            }
        }
        return score;
    }

    public enum NgramType {
        BIGRAM("BIGRAM"),
        TRIGRAM("TRIGRAM"),
        BIGRAMTRIGRAM("BIGRAMTRIGRAM");

        String string;

        NgramType(String string) {
            string = string;
        }

        public String getString() {
            return string;
        }
    }

    public enum ScoreFunction {
        LINEAR("LINEAR"),
        LINEAR_NORMALIZED_WORDDF("LINEAR_NORMALIZED_WORDDF"),
        LINEAR_NORMALIZED_WORDDF_SENSEDF("LINEAR_NORMALIZED_WORDDF_SENSEDF"),
        LINEAR_NORMALIZED_WORDDF_LOGSENSEDF("LINEAR_NORMALIZED_WORDDF_LOGSENSEDF"),
        LINEAR_NORMALIZED_LOGWORDDF("LINEAR_NORMALIZED_LOGWORDDF"),
        LINEAR_NORMALIZED_LOGWORDDF_SENSEDF("LINEAR_NORMALIZED_LOGWORDDF_SENSEDF"),
        LINEAR_NORMALIZED_LOGWORDDF_LOGSENSEDF("LINEAR_NORMALIZED_LOGWORDDF_LOGSENSEDF"),
        LOGLINEAR_NORMALIZED_LOGWORDDF_LOGSENSEDF("LOGLINEAR_NORMALIZED_LOGWORDDF_LOGSENSEDF");

        String string;

        ScoreFunction(String string) {
            string = string;
        }

        public String getString() {
            return string;
        }

    }


}