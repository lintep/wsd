package nlp.wordnet;

import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import tools.util.ConsuleInput;
import tools.util.Time;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Saeed on 8/30/15.
 */
public class WordNetTools implements Serializable{

    final File wordnetDirectory;
    final IRAMDictionary ramDictionary;

    public WordNetTools(String wordnetDirectory) throws IOException {
        long startTime = System.currentTimeMillis();
        System.out.println("wordnet loading ...");
        this.wordnetDirectory = new File(wordnetDirectory);
        this.ramDictionary = new RAMDictionary(this.wordnetDirectory, ILoadPolicy.IMMEDIATE_LOAD);
        this.ramDictionary.open();
        System.out.println("wordnet load complete after " + Time.getMilliSecondDistanceTime(startTime) + " ms");
    }

    public String getWordLemmaDaba(String word, String posString) {
        POS pos = getPos(posString);
        if (pos != null) {
            return getWordLemmaDaba(word, pos);
        }
        return "";
    }

    public String getWordLemmaDaba(String word, POS pos) {
        String lemma = null;
        try {
            IIndexWord indexWord = this.ramDictionary.getIndexWord(word, pos);
            try {
                lemma = indexWord.getLemma();
            } catch (NullPointerException e) {
            }
        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        return lemma == null ? "" : lemma;
    }

    public List<WordSenseFeature> getWordSenseFeatures(String word){
        List<WordSenseFeature> result;
        result = getWordSenseFeatures(word,POS.NOUN);
        if(result==null){
            result = getWordSenseFeatures(word,POS.ADJECTIVE);
            if(result==null){
                result = getWordSenseFeatures(word,POS.VERB);
                if(result==null){
                    result = getWordSenseFeatures(word,POS.ADVERB);
                }
            }
        }
        return result;
    }

    public List<WordSenseFeature> getWordAllSenseFeatures(String word){
        List<WordSenseFeature> result=new ArrayList<>();
        for (POS pos : POS.values()) {
            List<WordSenseFeature> tempResult = getWordSenseFeatures(word, pos);
            if(tempResult!=null) {
                result.addAll(tempResult);
            }
        }
        return result;
    }

    public List<WordSenseFeature> getWordSenseFeatures(String word, String posString) {
        POS pos = getPos(posString);
        if (pos != null) {
            return getWordSenseFeatures(word, pos);
        }
        return null;
    }

    public List<WordSenseFeature> getWordSenseFeatures(String word, POS pos) {
        ArrayList<WordSenseFeature> result = new ArrayList<>();
        try {
            IIndexWord indexWord = this.ramDictionary.getIndexWord(word, pos);
            List<IWordID> wrdIDs = indexWord.getWordIDs();
            for (IWordID iWordID : wrdIDs) {
                IWord iword = this.ramDictionary.getWord(iWordID);
                String senseKey = iword.getSenseKey().toString();
                int freq = this.ramDictionary.getSenseEntry(iword.getSenseKey()).getTagCount();
                List<String> sysnsets = new ArrayList<String>();
                for (IWord innerIword : iword.getSynset().getWords()) {
                    sysnsets.add(innerIword.getLemma());
                }

                List<String> glosses = new ArrayList<String>();
                List<String> glossesRelatedWords = new ArrayList<String>();
                String glossString = iword.getSynset().getGloss();
                for (String string : glossString.split(";")) {
                    String trimmed = string.trim();
                    if (trimmed.length() == 0) {
                        continue;
                    }
                    if (trimmed.charAt(0) == '"' && trimmed.charAt(trimmed.length() - 1) == '"') {
                        glosses.add(trimmed.substring(1, trimmed.length() - 1));
                    } else {
                        for (String strings2 : trimmed.split(",")) {
                            String trimmedString2 = strings2.trim();
                            if (trimmedString2.length() == 0) {
                                continue;
                            }
                            if (trimmed.indexOf("or ") == 0) {
                                trimmedString2 = trimmedString2.substring(3);
                            }
                            glossesRelatedWords.add(trimmedString2);
                        }
                    }
                }
                WordSenseFeature s = new WordSenseFeature(senseKey, pos, freq, sysnsets, glosses, glossesRelatedWords);
                result.add(s);
            }
        } catch (NullPointerException e) {
            return null;
        }

        return result;
    }

    public static String getFineGrain(String originalSenseKey) {
        if (originalSenseKey.lastIndexOf(":00") == originalSenseKey.length() - 3) {
            String fineGrainedSenseString = originalSenseKey.substring(originalSenseKey.lastIndexOf(':', originalSenseKey
                    .length() - 4) + 1, originalSenseKey.length() - 3);
            return fineGrainedSenseString;
        }
        return "";
    }


    static HashMap<String, POS> staticPosMap;

    static {
        staticPosMap = new HashMap<String, POS>();

        staticPosMap.put("NN", POS.NOUN);
        staticPosMap.put("NNS", POS.NOUN);
        staticPosMap.put("N", POS.NOUN);//MLE
        staticPosMap.put("UNKNOWN", POS.NOUN);//MLE

        staticPosMap.put("JJ", POS.ADJECTIVE);
        staticPosMap.put("AJ", POS.ADJECTIVE);//MLE

        staticPosMap.put("VB", POS.VERB);
        staticPosMap.put("VBD", POS.VERB);
        staticPosMap.put("VBG", POS.VERB);
        staticPosMap.put("VBN", POS.VERB);
        staticPosMap.put("VBZ", POS.VERB);
        staticPosMap.put("VBP", POS.VERB);

        staticPosMap.put("RB", POS.ADVERB);

        //_________________________________

        staticPosMap.put("NNP", POS.NOUN);
        staticPosMap.put("NNPS", POS.NOUN);

        staticPosMap.put("RBR", POS.ADVERB);
        staticPosMap.put("RBS", POS.ADVERB);

        staticPosMap.put("JJR", POS.ADJECTIVE);
        staticPosMap.put("JJS", POS.ADJECTIVE);

        staticPosMap.put(POS.NOUN.name(), POS.NOUN);
        staticPosMap.put(POS.NOUN.name().toLowerCase(), POS.NOUN);
        staticPosMap.put(POS.ADJECTIVE.name(), POS.ADJECTIVE);
        staticPosMap.put(POS.ADJECTIVE.name().toLowerCase(), POS.ADJECTIVE);
        staticPosMap.put(POS.VERB.name(), POS.VERB);
        staticPosMap.put(POS.VERB.name().toLowerCase(), POS.VERB);
        staticPosMap.put(POS.ADVERB.name(), POS.ADVERB);
        staticPosMap.put(POS.ADVERB.name().toLowerCase(), POS.ADVERB);

    }

    public static POS getPos(String pos) {
        if (staticPosMap.containsKey(pos)) {
            return staticPosMap.get(pos);
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("getPos(VBG):"+getPos("VBG"));
        String wordnetDictDir = "/data/backup/dataset/wordnet/WordNet-2.1/dict/";

        WordNetTools wordNetTools = new WordNetTools(wordnetDictDir);

////        wordNetTools.getWordSenseFeatures("think", POS.VERB);
//        String term = "filled";
//        POS pos = POS.ADJECTIVE;
//        String lemma = wordNetTools.getWordLemmaDaba(term, pos);
//        System.out.println("lemma: "+lemma);
//        System.out.println("termFsize: "+wordNetTools.getWordSenseFeatures(term, pos).size());
//        System.out.println("lemmaFsize: "+wordNetTools.getWordSenseFeatures(lemma, pos).size());
//        List<WordSenseFeature> featureSet = wordNetTools.getWordSenseFeatures(term, pos);
//        for (WordSenseFeature wordSenseFeature : featureSet) {
//            System.out.println(wordSenseFeature);
//            System.out.println("___________________");
//        }

        while (true){
            String token = ConsuleInput.readStringUTF8("Enter token");
            try {
                wordNetTools.getWordAllSenseFeatures(token).forEach(s ->
                        System.out.println(s.getSensePos() + "\t" + s.getOriginalSenseKey()));
            }
            catch (Exception e){
                e.printStackTrace();
            }

            if("q".equals(token)){
                break;
            }
        }
    }
}

