package nlp.analyzer.data.text;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class Filter {
	
	HashSet<String> terms;
	HashMap<String,Character> termsCode;
	
	int maxTermCount;
	Analyzer analyzer;
	CharArraySet dictionary;
	int minConvertedSize;
	
	public Filter(HashSet<String> terms,int maxTermCount,int minConvertedSize) {
		this.terms=new HashSet<String>(terms);
		this.maxTermCount=maxTermCount;
		this.minConvertedSize=minConvertedSize;
		this.dictionary = new CharArraySet(Version.LUCENE_46, terms, false);
		this.analyzer = new CompoundWordsAnalyzer(dictionary, maxTermCount);
	}
	
	
	int resultCounter=0;
	public String filter(String inText) throws IOException {
		resultCounter=0;
		TokenStream tokenStream = analyzer.tokenStream("f", inText);
		CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);
		tokenStream.reset();
		StringBuilder sb = new StringBuilder();
		while(tokenStream.incrementToken()) {
			sb.append(termAtt.toString());
			sb.append(' ');
			resultCounter++;
		}
		tokenStream.close();

		if (sb.length() >= 1 && resultCounter>=this.minConvertedSize)
			return sb.toString();
		else
			return "";
	}

	
	
	public int filteredTermsCount(String text) throws IOException {
		resultCounter=0;
		TokenStream tokenStream = analyzer.tokenStream("f", text);
		tokenStream.reset();
		while(tokenStream.incrementToken()) {
			resultCounter++;
		}
		tokenStream.close();

		return resultCounter;
	}
	
	public void close() {
		analyzer.close();
	}
	
}