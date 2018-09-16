package nlp.wsd;

import edu.mit.jwi.item.POS;
import nlp.preprocess.PreprocessorInterface;
import nlp.preprocess.StanfordTokenizedLemmattizedPosTaggedSentence;
import nlp.preprocess.TokenUtil;
import nlp.preprocess.datatype.Word;
import nlp.preprocess.en.StanfordPreProcessor;
import nlp.preprocess.fa.PersianPreprocessor;
import nlp.wordnet.WordNetTools;
import nlp.wordnet.WordSenseFeature;
import nlp.wsd.co.DebugItem;
import nlp.wsd.co.WSDSimpleCoScore;
import nlp.wsd.co.datatype.AmbiguousFeatureSet;
import nlp.wsd.co.datatype.Document;
import nlp.wsd.co.datatype.Sentence;
import nlp.wsd.co.scorer.WordWeightier;
import org.apache.log4j.PropertyConfigurator;
import tools.util.ConsuleInput;
import tools.util.PrintWriterWithBuffer;
import tools.util.file.Reader;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class WSDSystem {

    WordNetTools wordNetTools;
    Set<POS> posSet;

    WSDSimpleCoScore wsdSimpleCoScore;

    PreprocessorInterface persianPreprocessor;
    PreprocessorInterface englishPreprocessor;

    WSDConf defaultConf;

    public WSDSystem(WSDSimpleCoScore wsdSimpleCoScore, WordNetTools wordNetTools, Set<POS> posSet, PreprocessorInterface stanfordPreProcessor, PreprocessorInterface persianPreprocessor) {
        this(wsdSimpleCoScore, wordNetTools, posSet, stanfordPreProcessor, persianPreprocessor, new WSDConf());
    }

    public WSDSystem(WSDSimpleCoScore wsdSimpleCoScore, WordNetTools wordNetTools, Set<POS> posSet, PreprocessorInterface englishPreProcessor, PreprocessorInterface persianPreprocessor, WSDConf defaultConf) {
        this.wsdSimpleCoScore = wsdSimpleCoScore;
        this.wordNetTools = wordNetTools;
        this.posSet = posSet;
        this.englishPreprocessor = englishPreProcessor;
        this.persianPreprocessor = persianPreprocessor;
        this.defaultConf = defaultConf;
    }

    public Map<String, String> disambiguate(String contextDocument, String ambiguousTerm, int ambiguousWordOrderIndexInContext, Set<String> senses) throws Exception {
        return disambiguate(contextDocument, ambiguousTerm, ambiguousWordOrderIndexInContext, senses,defaultConf);
    }

    public Map<String, String> disambiguate(String contextDocument, String ambiguousTerm, int ambiguousWordOrderIndexInContext, Set<String> senses, WSDConf wsdConf) throws Exception {

        PreprocessorInterface preprocessor;
        if (TokenUtil.hasPersianChar(TokenUtil.getTokenType(contextDocument))) {
            preprocessor = persianPreprocessor;
        } else {
            preprocessor = englishPreprocessor;
        }

        Map<String, String> senseKey = null;

//        List<PreprocessorInterface.PreprocessedSentence> pp = persianPreprocessor.preprocess(contextDocument);
        Document document = new Document("test");

        List<WordSenseFeature> wordSenseFeatureList = new ArrayList<>();
        int senseDf = -1;
        senses.forEach(sense -> wordSenseFeatureList.add(new WordSenseFeature(sense, null, senseDf, null, null, null)));

        int ambiguousTermOrder = 0;
        int sentenceCounter = 0;
        for (StanfordTokenizedLemmattizedPosTaggedSentence rawSentence : preprocessor.getSTLPTS(contextDocument)) {
            Sentence sentence = new Sentence(sentenceCounter);

            String[] tokens = rawSentence.getTokenizedSentence().split(" ");
            String[] lemmas = rawSentence.getLemmatizedSentence().split(" ");
            String[] poses = rawSentence.getPosTagSentence().split(" ");


            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].equals(ambiguousTerm) || tokens[i].indexOf(ambiguousTerm + "-") == 0 || tokens[i].indexOf("-" + ambiguousTerm) > 0) {
                    ambiguousTermOrder++;

                    if (ambiguousTermOrder == ambiguousWordOrderIndexInContext) {
                        AmbiguousFeatureSet ambiguousFeatureSet = new AmbiguousFeatureSet(document.getDocId(), wordSenseFeatureList);
                        if (tokens[i].indexOf(ambiguousTerm + "-") == 0) {
                            sentence.addWord(new Word(tokens[i], poses[i], lemmas[i]), ambiguousFeatureSet);
                            String part2Token = tokens[i].substring(tokens[i].indexOf('-') + 1);//tokens[i].substring(ambiguousTerm.length() + 1);
                            sentence.addWord(new Word(part2Token, poses[i], part2Token));
                        } else if (tokens[i].indexOf("-" + ambiguousTerm) > 0) {
                            String part1Token = tokens[i].substring(0, tokens[i].indexOf('-'));
                            sentence.addWord(new Word(part1Token, poses[i], part1Token));
                            sentence.addWord(new Word(tokens[i], poses[i], lemmas[i]), ambiguousFeatureSet);
                        } else {
                            sentence.addWord(new Word(tokens[i], poses[i], lemmas[i]), ambiguousFeatureSet);
                        }
                        continue;
                    }

                }

                sentence.addWord(new Word(tokens[i], poses[i], lemmas[i]));
            }

            document.addSentence(sentence);

            sentenceCounter++;
        }


        List<DebugItem> tempDebugLog = null;

//        Writer o = new Writer() {
//            @Override
//            public void write(char[] cbuf, int off, int len) throws IOException {
//                System.out.println(new String(cbuf, off, len));
//            }
//
//            @Override
//            public void flush() throws IOException {
//
//            }
//
//            @Override
//            public void close() throws IOException {
//
//            }
//        };
//        PrintWriterWithBuffer fakeLogger = new PrintWriterWithBuffer(o);
//        senseKey = new HashMap<>();
//        senseKey.put("test", "پ");
//        this.wsdSimpleCoScore.setLogFileWriter(fakeLogger);

        return wsdSimpleCoScore.getDisambiguatedSenseDocument(senseKey, document, wsdConf.wordDist, wsdConf.sentenceDist, wsdConf.containsSenseInSentenceEnable, wsdConf.ngramType, wsdConf.scoreFunction, wsdConf.weightFunction, tempDebugLog);
    }

    public static class WSDConf {
        int wordDist = 5;
        int sentenceDist = 5;
        boolean containsSenseInSentenceEnable = false;
        WSDSimpleCoScore.NgramType ngramType = WSDSimpleCoScore.NgramType.BIGRAM;
        WSDSimpleCoScore.ScoreFunction scoreFunction = WSDSimpleCoScore.ScoreFunction.LINEAR;
        WordWeightier.WeightFunction weightFunction = WordWeightier.WeightFunction.CONTEXT_UNIFY_GRAPH;

        public void setWordDist(int wordDist) {
            this.wordDist = wordDist;
        }

        public void setSentenceDist(int sentenceDist) {
            this.sentenceDist = sentenceDist;
        }

        public void setContainsSenseInSentenceEnable(boolean containsSenseInSentenceEnable) {
            this.containsSenseInSentenceEnable = containsSenseInSentenceEnable;
        }

        public void setScoreFunction(WSDSimpleCoScore.ScoreFunction scoreFunction) {
            this.scoreFunction = scoreFunction;
        }

        public void setWeightFunction(WordWeightier.WeightFunction weightFunction) {
            this.weightFunction = weightFunction;
        }

        public int getWordDist() {
            return wordDist;
        }

        public int getSentenceDist() {
            return sentenceDist;
        }

        public boolean isContainsSenseInSentenceEnable() {
            return containsSenseInSentenceEnable;
        }

        public WSDSimpleCoScore.ScoreFunction getScoreFunction() {
            return scoreFunction;
        }

        public WordWeightier.WeightFunction getWeightFunction() {
            return weightFunction;
        }
    }

    public static void main(String[] args) throws Exception {

        Properties properties = new Properties();
        properties.load(Reader.getFileBufferReader(args[0]));

        PropertyConfigurator.configure(properties.getProperty("log4j.properties"));

        WordNetTools wordNetTools = new WordNetTools(properties.get("wordNetPath").toString());

        Set<POS> posSet = Arrays.asList(properties.get("posSet").toString().split(",")).stream().map(s -> WordNetTools.getPos(s)).collect(Collectors.toSet());


        StanfordPreProcessor englishPreprocessor = new StanfordPreProcessor();

        PreprocessorInterface preprocessor;
        if(properties.getProperty("language").equals("fa")){
            preprocessor=PersianPreprocessor.getDefaultInstance(properties.getProperty("pTokenizerPath"),properties.getProperty("tokenPosFileAddress"),null);
        }
        else if(properties.getProperty("language").equals("en")){
            preprocessor=englishPreprocessor;
        }
        else{
            System.out.println("This version not support "+properties.getProperty("language")+" language.");
            return;
        }
        WSDSimpleCoScore wsdSimpleCoScore = WSDSimpleCoScore.getDBInstance(properties, properties.getProperty("tokensFreq." + properties.getProperty("language")),
                posSet, wordNetTools, englishPreprocessor);

        WSDSystem wsdSystem = new WSDSystem(wsdSimpleCoScore, wordNetTools, posSet, englishPreprocessor, preprocessor);

        Map<String, String> result = null;
        Set<String> senseSet = new HashSet<>();

        while (true) {
            senseSet.clear();

            String context = ConsuleInput.readStringUTF8("Enter sentences as a context");
            String ambiguousWord = ConsuleInput.readStringUTF8("Enter ambiguous word");
            for (String sense : ConsuleInput.readStringUTF8("Enter ambiguous word sens by \",\" splitter").split(",")) {
                senseSet.add(sense.trim());
            }
            result = wsdSystem.disambiguate(context, ambiguousWord, 1, senseSet);

            System.out.print("result: ");
            result.entrySet().forEach(r -> System.out.println(r.getKey() + " -> " + r.getValue()));
            System.out.println("________________________________________________________");
        }
    }

}
