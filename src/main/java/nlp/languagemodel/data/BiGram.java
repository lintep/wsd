package nlp.languagemodel.data;

public class BiGram extends UniGram{

	protected String term2;
	
	public BiGram(String term1,String term2,long frequency) {
		super(term1, frequency);
		this.term2=term2;
	}

	public String getTerm2() {
		return term2;
	}
	
}
