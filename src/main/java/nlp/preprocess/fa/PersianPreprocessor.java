package nlp.preprocess.fa;

import nlp.pos.MapBasePosTagger;
import nlp.pos.PTokenizer;
import nlp.preprocess.*;
import nlp.text.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saeed on 12/30/2016.
 */
public class PersianPreprocessor implements PreprocessorInterface {

//    Pattern sentenceSplitter= Pattern.compile("\\.|\\;|\\؛|\\:|\\!|\\?|\\؟"+"|\\?");

    NormalizerInterface normalizer;
    TokenizerInterface tokenizer;
    PosTaggerInterface posTagger;
    LemmatizerInterface lemmatizer;

    public PersianPreprocessor(){}

    public static PersianPreprocessor getDefaultInstance(String pTokenizerPath, String tokenPosFileAddress, String intelligentNormalizerPath) throws Exception {
        return getDefaultInstance(new SimpleNormalizer(),pTokenizerPath,tokenPosFileAddress,intelligentNormalizerPath);
    }

    public static PersianPreprocessor getDefaultInstance(NormalizerInterface normalizer, String pTokenizerPath, String tokenPosFileAddress, String intelligentNormalizerPath) throws Exception {
        PersianPreprocessor persianPreprocessor=new PersianPreprocessor();
        persianPreprocessor.normalizer = normalizer;
        persianPreprocessor.tokenizer = PTokenizer.getInstance(pTokenizerPath);
        persianPreprocessor.lemmatizer=new BlackListBaseLemmatizer();
        persianPreprocessor.posTagger = new MapBasePosTagger(tokenPosFileAddress);
        System.out.println("\n\n****** PersianPreprocessor load complete ******\n\n");
        return persianPreprocessor;
    }

    public static PersianPreprocessor getWindowsDefaultInstance() throws Exception {
        return getDefaultInstance(new SimpleNormalizer(),"D:\\thesis\\src\\bijankhan\\trianDataPTokenizer/", "D:\\thesis\\src\\bijankhan/tokenPos",null);
    }

    public static PersianPreprocessor getLinuxDefaultInstance() throws Exception {
        return getDefaultInstance(new SimpleNormalizer(), "/ssd/bijankhan/SimpleVersionData/", "/ssd/bijankhan/SimpleVersionData/tokenPos", null);
    }

    public static PersianPreprocessor getLinuxDefaultPurePersianInstance() throws Exception {
        return getDefaultInstance(new NormalizerPurePersian(new SimpleNormalizer()), "/ssd/bijankhan/SimpleVersionData/", "/ssd/bijankhan/SimpleVersionData/tokenPos", null);
    }

    @Override
    public List<StanfordTokenizedLemmattizedPosTaggedSentence> getSTLPTS(String text) {
        List<StanfordTokenizedLemmattizedPosTaggedSentence> list=new ArrayList<>();
        for (PreprocessedSentence preprocessedSentence : preprocess(text)) {
            StringBuilder tokensStr=new StringBuilder();
            StringBuilder lemmasStr=new StringBuilder();
            StringBuilder posesStr=new StringBuilder();
            preprocessedSentence.getPreprocessedItems().forEach(s -> {
                tokensStr.append(s.getToken() + ' ');
                lemmasStr.append(s.getLemma()+' ');
                posesStr.append(s.getPos()+' ');
            });

            if(tokensStr.length()>0) {
                tokensStr.setLength(tokensStr.length()-1);
                lemmasStr.setLength(lemmasStr.length()-1);
                posesStr.setLength(posesStr.length()-1);
                list.add(new StanfordTokenizedLemmattizedPosTaggedSentence(tokensStr.toString(), lemmasStr.toString(), posesStr.toString()));
            }
        }
        return list;
    }

    @Override
    public List<PreprocessedSentence> preprocess(String text) {
        List<PreprocessedSentence> preprocessedSentences =new ArrayList<>();

        ArrayList<String> sentenceStrings = Utils.getSentences(text);
        for (String sentenceString : sentenceStrings) {
            if(sentenceString.trim().length()==0){
                continue;
            }
            PreprocessedSentence preprocessedSentence=new PreprocessedSentence();
            List<String> tokens = tokenizer.tokenize(normalizer.normalize(sentenceString));
            List<String> poses = posTagger.getPos(tokens);
            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i);
                String pos=poses.get(i);
                preprocessedSentence.addItem(new PreprocessedItem(token,lemmatizer.getLemma(token),pos));
            }
            preprocessedSentences.add(preprocessedSentence);
        }
        return preprocessedSentences;
    }

    @Override
    public String preprocess(String text, PreprocessType preprocessType) {
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

}
