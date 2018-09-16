package nlp.pos.data;

public class Token2Gram extends Token1Gram{

	protected int token2ID;
	
	public Token2Gram(int token1ID,int token2ID,long frequency) {
		super(token1ID, frequency);
		this.token2ID=token2ID;
	}

	public int getToken2ID() {
		return token2ID;
	}
	
}
