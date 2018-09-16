package nlp.pos.data;

public class Token1Gram {

	protected int token1ID;
	protected long frequency;
	
	public Token1Gram(int token1ID,long frequency) {
		this.token1ID=token1ID;
		this.frequency=frequency;
	}

	public int getToken1ID() {
		return token1ID;
	}

	public long getFrequency() {
		return frequency;
	}
}
