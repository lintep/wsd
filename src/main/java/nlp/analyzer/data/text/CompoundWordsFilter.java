package nlp.analyzer.data.text;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.CharsRef;

import java.io.IOException;

public final class CompoundWordsFilter extends TokenFilter {
	public static final String COMPOUND_TYPE = "compound";
	
	private final SlidingWindow window;
	
	private final CharsRef token = new CharsRef();
	
	private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
	private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

	private boolean eos = false;
	
	public CompoundWordsFilter(TokenStream input, CharArraySet dictionary, int maxCompoundWordSize) {
		super(input);
		this.window = new SlidingWindow(dictionary, maxCompoundWordSize);
	}
	
	@Override
	public boolean incrementToken() throws IOException {
		if (eos)
			return finalizeWindow();
		
		while(true) {
			if (!input.incrementToken()) {
				eos = true;
				break;
			}
			if (addTokenToWindow()) {
				compoundToken();
				return true;
			}
		}
		
		return finalizeWindow();
	}
	
	private boolean finalizeWindow() {
		while(window.finalizeWindow()) {
			if (window.isMatched()) {
				compoundToken();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void reset() throws IOException {
		super.reset();
		eos = false;
		window.reset();
	}
	
	// ================================================= Helper Methods ================================================
	
	private boolean addTokenToWindow() {
		token.chars = termAttribute.buffer();
		token.offset = 0;
		token.length = termAttribute.length();
		return window.add(token);
	}
	
	private void compoundToken() {
		CharsRef matchedTerm = window.getMatchedTerm();
		
		clearAttributes();
		
		termAttribute.copyBuffer(matchedTerm.chars, matchedTerm.offset, matchedTerm.length);
		typeAttribute.setType(COMPOUND_TYPE);
	}
}