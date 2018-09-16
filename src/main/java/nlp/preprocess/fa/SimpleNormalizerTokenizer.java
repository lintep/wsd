package nlp.preprocess.fa;

import nlp.preprocess.NormalizerTokenizerInterface;

import java.util.List;

public class SimpleNormalizerTokenizer implements NormalizerTokenizerInterface {

	SimpleNormalizer simpleNormalizer = new SimpleNormalizer();
	SimpleTokenizer simpleTokenizer = new SimpleTokenizer();


	@Override
	public String tokenizeAsString(String inputText, char delim) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.setLength(0);
		for (String token : tokenize(inputText)) {
			stringBuilder.append(token);
			stringBuilder.append(delim);
		}
		if (stringBuilder.length() > 0)
			stringBuilder.setLength(stringBuilder.length() - 1);
		return stringBuilder.toString();
	}

	@Override
	public List<String> tokenize(String inputText) {
		return this.simpleTokenizer.tokenize(inputText);
	}

	@Override
	public String normalize(String inputText) {
		return this.simpleNormalizer.normalize(inputText);
	}

	@Override
	public List<String> normalizeTokenize(String inputText) {
		return tokenize(normalize(inputText));
	}

	public String normalizeTokenizeAsString(String inputText, char delim) {
		// TODO Auto-generated method stub
		return tokenizeAsString(normalize(inputText), delim);
	}
}
