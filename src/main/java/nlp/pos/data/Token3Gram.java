package nlp.pos.data;

public class Token3Gram extends Token2Gram{

	protected int token3ID;
	
	public Token3Gram(int token1ID,int token2ID,int token3ID,long frequency) {
		super(token1ID,token2ID, frequency);
		this.token3ID=token3ID;
	}

	public int getToken3ID() {
		return token3ID;
	}
	
}
