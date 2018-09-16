package nlp.pos.tagger.hmm;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nlp.pos.data.PosTaggedInstanceSentence;
import nlp.pos.data.TokenPos;

import tools.util.collection.IncrementalTable;
import tools.util.collection.KeyValue;

public class Emission {

	IncrementalTable<String, String> tokenToPosMatrix;
	
	public Emission() {
		this.tokenToPosMatrix=new IncrementalTable<String, String>();
	}
	
	public void addTokenPosArray(String[] tokens,String[] poses) throws Exception{
		if(poses.length>0 && tokens.length==poses.length){
			for (int i = 0; i < tokens.length; i++) {
				this.tokenToPosMatrix.put(tokens[i],poses[i]);
			}
		}
		else
			throw new Exception("not equal pos size with token size");
	}
	
	public void addTokenPosArray(PosTaggedInstanceSentence posTaggedInstanceSentence) throws Exception{
		for(TokenPos tokenPos:posTaggedInstanceSentence.getTokenPosArray()){
			this.tokenToPosMatrix.put(tokenPos.token,tokenPos.pos);
		}
	}
	
	Map<String, Long> tempPosesCount=new HashMap<String, Long>(); 
	Map<String,Double> tempHashMapResult=new HashMap<String, Double>();
	
	public Map<String,Double> getTokenPosesProbability(String token) {
		tempPosesCount = this.tokenToPosMatrix.row(token);
		double sumFreq = 0l;
		for (Long count : tempPosesCount.values()) {
			sumFreq+=count;
		}
		tempHashMapResult.clear();
		for (Entry<String, Long> posCount : tempPosesCount.entrySet()) {
			tempHashMapResult.put(posCount.getKey(), posCount.getValue()/sumFreq);
		}
		return tempHashMapResult;
	}
	
	public KeyValue<String,Double> getTokenPosWithMaxProbability(String token) {
		tempPosesCount = this.tokenToPosMatrix.row(token);
		double sumFreq = 0l;
		for (Long count : tempPosesCount.values()) {
			sumFreq+=count;
		}
		double maxProb=-1;
		double prob;
		String pos="unknown";
		for (Entry<String, Long> posCount : tempPosesCount.entrySet()) {
			prob=posCount.getValue()/sumFreq;
			if(prob>maxProb){
				maxProb=prob;
				pos=posCount.getKey();
			}
		}
		return new KeyValue<String, Double>(pos,maxProb);
	}

	public void clear() {
		this.tokenToPosMatrix.clear();
	}
}
