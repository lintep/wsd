package nlp.languagemodel.data;

import tools.database.DbConnection;
import tools.database.DbDataFields;
import tools.database.DbTools;

import java.util.HashMap;

public class UniGram {

	protected String term1;
	protected long frequency;
	
	public UniGram(String term1,long frequency) {
		this.term1=term1;
		this.frequency=frequency;
	}

	public String getTerm1() {
		return term1;
	}

	public long getFrequency() {
		return frequency;
	}
}
