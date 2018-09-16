package nlp.wordnet;

import edu.mit.jwi.item.POS;

import java.util.List;

public class WordSenseFeature {
    String senseKey;
    POS sensePos;
    int wordNetFrequency;
    List<String> synonyms;
    List<String> glosses;
    List<String> glossesRelatedWords;

    public WordSenseFeature(String senseKey, POS sensePos, int wordNetFrequency, List<String> synonyms, List<String> glosses, List<String> glossesRelatedWords) {
        this.senseKey = senseKey;
        this.sensePos = sensePos;
        this.wordNetFrequency = wordNetFrequency;
        this.synonyms = synonyms;
        this.glosses = glosses;
        this.glossesRelatedWords = glossesRelatedWords;
    }

    public WordSenseFeature(String senseKey) {
        this.senseKey = senseKey;
    }

    public String getOriginalSenseKey() {
        return senseKey;
    }

    public String getSenseKey() {
        if(senseKey.indexOf('%')>0){
            return senseKey.substring(0,senseKey.indexOf('%'));
        }
        return senseKey;
    }

    public POS getSensePos() {
        return sensePos;
    }

    public int getWordNetFrequency() {
        return wordNetFrequency;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public List<String> getGlosses() {
        return glosses;
    }

    public List<String> getGlossesRelatedWords() {
        return glossesRelatedWords;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("senseKey: "+getOriginalSenseKey());
        result.append('\n');
        result.append("sensePOS: "+getSensePos());
        result.append('\n');
        result.append("senseWordNetFreq: "+getWordNetFrequency());
        result.append('\n');

        List<String> list = getSynonyms();
        if (list!=null && list.size() > 0) {
            result.append("synSet: ");
            for (String s : list) {
                result.append(s);
                result.append('\t');
            }
            result.setLength(result.length() - 1);
        }
        result.append('\n');

        list = getGlosses();
        if (list!=null && list.size() > 0) {
            result.append("synGlosses: ");
            for (String s : list) {
                result.append(s);
                result.append('\t');
            }
            result.setLength(result.length() - 1);
        }
        result.append('\n');

        list = getGlossesRelatedWords();
        if (list!=null && list.size() > 0) {
            result.append("synGlossesRelated: ");
            for (String s : list) {
                result.append(s);
                result.append('\t');
            }
            result.setLength(result.length() - 1);
        }
        result.append('\n');

        return result.toString();
    }

    public void setSense(String sense) {
        int index = senseKey.indexOf('%');
        if(index>0){
            this.senseKey = senseKey.substring(0,index+1)+sense;
        }
        else {
            this.senseKey = sense;
        }
    }
}
