package nlp.wsd.co;

/**
 * Created by Saeed on 7/3/15.
 */
public interface PreprocessorTemp {
    String normalize(String inString);
    String[] tokenize(String inString);
    String[] normalizeTokrnize(String inString);
}
