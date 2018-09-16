package nlp.analyzer.data.text;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.Reader;

public class CompoundWordsAnalyzer extends Analyzer{
	private final CharArraySet dictionary;
	private final int maxCompoundWordSize;
	
	public CompoundWordsAnalyzer(CharArraySet dictionary, int maxCompoundWordSize) {
		this.dictionary = dictionary;
		this.maxCompoundWordSize = maxCompoundWordSize;
	}
	
	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		final StandardTokenizer src = new StandardTokenizer(Version.LUCENE_46, reader);
		TokenStream tok = new CompoundWordsFilter(src, dictionary, maxCompoundWordSize);
		return new TokenStreamComponents(src, tok);
	}
}