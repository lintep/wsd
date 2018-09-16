package nlp.preprocess.datatype;

import edu.mit.jwi.item.POS;
import nlp.preprocess.en.StanfordPreProcessor;
import nlp.wordnet.WordNetTools;

/**
 * Created by Saeed on 8/27/15.
 */
public class Word {
    String tokenValue;
    String pos;
    String lemma;

    public Word(String tokenValue, String pos, String lemma) {
        this.tokenValue = tokenValue;
        this.pos = pos;
        this.lemma = lemma;
    }

    public String get(StanfordPreProcessor.PreprocessType preprocessType) {
        if (preprocessType == StanfordPreProcessor.PreprocessType.PosTagged) {
            return getPos();
        } else if (preprocessType == StanfordPreProcessor.PreprocessType.Tokenize) {
            return getTokenValue();
        } else if (preprocessType == StanfordPreProcessor.PreprocessType.Lemmatize) {
            return getLemma();
        }
        return "";
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public String getPos() {
        return pos.toString();
    }

    public POS getPosValue() {
        return WordNetTools.getPos(pos);
    }

    public String getLemma() {
        return lemma;
    }

    @Override
    public String toString() {
        return getTokenValue() + "\t" + getPos() + "\t" + getLemma();
    }
}
