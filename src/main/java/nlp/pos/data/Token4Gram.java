package nlp.pos.data;

public class Token4Gram extends Token3Gram{

	protected int token4ID;
	
	public Token4Gram(int token1ID,int token2ID,int token3ID,int token4ID,long frequency) {
		super(token1ID,token2ID,token3ID, frequency);
		this.token4ID=token4ID;
	}

	public int getToken4ID() {
		return token4ID;
	}
	
}
