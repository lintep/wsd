package nlp.pos.tagger.hmm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import tools.util.Time;
import tools.util.collection.KeyValue;

public class HMM {

	Emission emission;
	Transmision transmision;
	
	public HMM(Set<KeyValue<String, String>> dataset) throws Exception {
		this.emission=new Emission();
		this.transmision=new Transmision();
		Time.setStartTimeForNow();
		Iterator<KeyValue<String, String>> iteratorDataset = dataset.iterator();
		KeyValue<String, String> tokensPoses;
		String[] tokens;
		String[] poses;
		while (iteratorDataset.hasNext()) {
			tokensPoses = iteratorDataset.next();
			tokens=tokensPoses.getKey().split(" ");
			poses=tokensPoses.getValue().split(" ");
			this.emission.addTokenPosArray(tokens, poses);
			this.transmision.addPoses(poses);
		}
		System.out.println("HMM load complete in "+Time.getTimeLengthForNow()+" ms.");
	}
	
	public List<String> getPos(String[] tokens){
		return getViterbiPos(tokens);
	}

	
//	ArrayList<KeyValue<String, Double>>[] tempBeforLevelPosScore=new ArrayList<KeyValue<String,Double>>()[10];
//	ArrayList<KeyValue<String, Double>> tempCurrentLevelPosScore=new ArrayList<KeyValue<String,Double>>();
//	double tempMaxScore=0;
//	double tempScore=0;
	int TOKENMAXPOSCOUNT=0;
	private List<String> getViterbiPos(String[] tokens) {
		ArrayList<KeyValue<String, Double>>[] viterbiArray= (ArrayList<KeyValue<String, Double>>[]) new ArrayList[TOKENMAXPOSCOUNT];
		int beforTokenPosCounter = 0;
		int currentTokenPosCounter = 0;
		for (int i = 0; i < TOKENMAXPOSCOUNT; i++) {
			viterbiArray[i]=new ArrayList<KeyValue<String,Double>>();
		}
		for (Entry<String, Double> posProbability : this.emission.getTokenPosesProbability(tokens[0]).entrySet()) {
			viterbiArray[currentTokenPosCounter].add(0,new KeyValue<String, Double>(posProbability.getKey(), score(0,posProbability.getValue(),this.transmision.posToPosProbability("<s>", posProbability.getKey()))));
			currentTokenPosCounter++;
		}
		beforTokenPosCounter=currentTokenPosCounter;
		currentTokenPosCounter=0;
		for (int i = 1; i < tokens.length; i++) {
			for (Entry<String, Double> posProbability : this.emission.getTokenPosesProbability(tokens[i]).entrySet()) {
				
				currentTokenPosCounter++;
				for (String string : tokens) {
					
				}
			}
//			tempTable.put(tokens[0], posProbability.getKey(), score(posProbability.getValue(),this.transmision.posToPosProbability("<s>", posProbability.getKey())));
		}
		List<String> resulttokensPos=new ArrayList<String>();
		return resulttokensPos;
	}
	
	private double score(double beforScore,double emission,double transmision){
		return beforScore+emission*transmision;
	}
}
