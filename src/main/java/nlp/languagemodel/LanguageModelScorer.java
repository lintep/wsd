package nlp.languagemodel;

/**
 * Created by Saeed on 7/28/2016.
 */
public interface LanguageModelScorer {

    public long getScore(String lemma1) throws Exception;

    public long getScore(String lemma1, String lemma2) throws Exception;

    public long getScore(String lemma1, String lemma2, String lemma3) throws Exception;
}
