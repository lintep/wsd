package nlp.pos.data;

import java.util.List;

public interface NormalizerTokenizerInterface {

	String normalize(String inputText);
	List<String> tokenize(String inputText);
	String tokenizeAsString(String inputText, char delim);
	
} 
