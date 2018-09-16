package nlp.preprocess;

import java.util.List;

public interface NormalizerTokenizerInterface {

	String normalize(String inputText);

	List<String> tokenize(String inputText);

	List<String> normalizeTokenize(String inputText);

	String tokenizeAsString(String inputText, char delim);

}
