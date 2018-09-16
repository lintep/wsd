package nlp.pos.data;


public class TokenId {

	protected String token;
	protected int id;
	
	public TokenId(String token,int id) {
		this.token=token;
		this.id=id;
	}

	public String getToken() {
		return token;
	}

	public int getId() {
		return id;
	}
}
