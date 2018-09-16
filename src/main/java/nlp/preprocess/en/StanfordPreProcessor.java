package nlp.preprocess.en;

import com.google.common.base.Stopwatch;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;
import nlp.preprocess.PreprocessorInterface;
import nlp.preprocess.StanfordTokenizedLemmattizedPosTaggedSentence;
import tools.util.ConsuleInput;
import tools.util.collection.HashSertInteger;
import tools.util.collection.KeyValue;
import tools.util.file.Reader;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class StanfordPreProcessor implements PreprocessorInterface{

//    Properties props;
    StanfordCoreNLP pipeline;

    /**
     * default Constructor with default model
     */
    public StanfordPreProcessor(){
        System.out.println("Creating StanfordPreProcessor ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Properties props= new Properties();

        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");//, ner, parse, dcoref");
        props.setProperty("-props", "StanfordCoreNLP-german.properties");

        this.pipeline = new StanfordCoreNLP(props);
        System.out.println("StanfordPreProcessor created after "+stopwatch.elapsed(TimeUnit.MILLISECONDS)+" ms.");
    }

    public StanfordPreProcessor(CoreNlpModelLanguage language) {
        Properties germanProperties = StringUtils.argsToProperties(
                new String[]{"-props", "StanfordCoreNLP-"+language.getStringValue()+".properties",
                        "annotators", "tokenize, ssplit, pos, lemma"});
        pipeline = new StanfordCoreNLP(germanProperties);
    }

    public String getPreproccedText(String text,PreprocessType preprocessType){
        List<StanfordTokenizedLemmattizedPosTaggedSentence> result=new ArrayList<StanfordTokenizedLemmattizedPosTaggedSentence>();

        StringBuilder preprocessedTextSentence=new StringBuilder();;

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            int count=0;
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                String word = token.get(preprocessType.getValue());//(LemmaAnnotation.class)
                preprocessedTextSentence.append(word);
                preprocessedTextSentence.append(' ');

                count++;
            }

            if(count>0){
                preprocessedTextSentence.setLength(preprocessedTextSentence.length()-1);
            }
        }

        return preprocessedTextSentence.toString();
    }

    public List<StanfordTokenizedLemmattizedPosTaggedSentence> getTokenizedAndLemmatizedSentences(String text){
        List<StanfordTokenizedLemmattizedPosTaggedSentence> result=new ArrayList<StanfordTokenizedLemmattizedPosTaggedSentence>();

        StringBuilder tokinizedSentence=new StringBuilder();
        StringBuilder lemmatizedSentence=new StringBuilder();;
        StringBuilder posTagSentence=new StringBuilder();;

        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            tokinizedSentence.setLength(0);
            lemmatizedSentence.setLength(0);
            posTagSentence.setLength(0);
            int count=0;
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                tokinizedSentence.append(word);
                tokinizedSentence.append(' ');

                String lemma = token.get(LemmaAnnotation.class);
                lemmatizedSentence.append(lemma);
                lemmatizedSentence.append(' ');

                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                posTagSentence.append(pos);
                posTagSentence.append(' ');

                //	        // this is the NER label of the token
//	        String ne = token.get(NamedEntityTagAnnotation.class);
                count++;
            }

            if(count>0){
                tokinizedSentence.setLength(tokinizedSentence.length()-1);
                lemmatizedSentence.setLength(lemmatizedSentence.length()-1);
                posTagSentence.setLength(posTagSentence.length()-1);
            }

            result.add(new StanfordTokenizedLemmattizedPosTaggedSentence(tokinizedSentence.toString(), lemmatizedSentence.toString(), posTagSentence.toString()));

        }

        return result;
    }

    public Map<String,Integer> getLemma(String text,Set<POS> legalPosSet) {
        HashSertInteger<String> result=new HashSertInteger<String>();

        for(StanfordTokenizedLemmattizedPosTaggedSentence sentence:getTokenizedAndLemmatizedSentences(text)){
            for(KeyValue<String, POS> lemmaPos:sentence.getLemmaPos()){
                if(legalPosSet.contains(lemmaPos.getValue())) {
                    result.add(lemmaPos.getKey());
                }
            }
        }

        return result.getHashMap();
    }

    @Override
    public List<StanfordTokenizedLemmattizedPosTaggedSentence> getSTLPTS(String text) {
        return getTokenizedAndLemmatizedSentences(text);
    }

    @Override
    public List<PreprocessedSentence> preprocess(String text) {
        List<PreprocessedSentence> result=new ArrayList<>();
        getTokenizedAndLemmatizedSentences(text).forEach(item -> {
            PreprocessedSentence ps = new PreprocessedSentence();
            item.getWordList().forEach(w -> ps.addItem(new PreprocessedItem(w.getTokenValue(),w.getLemma(),w.getPos())));
            result.add(ps);
        });
        return result;
    }

    @Override
    public String preprocess(String text, PreprocessorInterface.PreprocessType preprocessType) {
        StringBuilder result = new StringBuilder();
        preprocess(text).forEach(s -> s.getPreprocessedItems().forEach(w -> {
            switch (preprocessType){
                case NORMALIZE:
                case TOKENIZE:
                    result.append(w.getToken());
                    break;
                case LEMMATIZE:
                    result.append(w.getLemma());
                    break;
                case POSTAG:
                    result.append(w.getPos());
                    break;
            }
            result.append(' ');
        }));
        if(result.length()>0) {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }

    public static enum PreprocessType{
        PosTagged(PartOfSpeechAnnotation.class),
        Tokenize(TextAnnotation.class),
        Lemmatize(LemmaAnnotation.class);

        Class<? extends edu.stanford.nlp.util.TypesafeMap.Key<String>> classType=null;

        PreprocessType(Class<? extends edu.stanford.nlp.util.TypesafeMap.Key<String>> classType){
            this.classType=classType;
        }

        public Class<? extends edu.stanford.nlp.util.TypesafeMap.Key<String>> getValue(){
            return classType;
        }
    }

    public static enum CoreNlpModelLanguage {
        ENGLISH("english"),
        GERMAN("german"),
        PERSIAN("persian"),
//        SPANISH("spanish"),
//        FRENCH("french")
        ;

        String language;

        CoreNlpModelLanguage(String v){
            language=v;
        }

        public String getStringValue() {
            return language;
        }
    }

}
