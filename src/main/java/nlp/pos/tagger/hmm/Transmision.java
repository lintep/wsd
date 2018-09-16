package nlp.pos.tagger.hmm;

import tools.util.collection.IncrementalTable;

public class Transmision {
	IncrementalTable<String, String> posToPosMatrix;
	
	public void addPoses(String[] poses){
		if(poses.length>0){
			this.posToPosMatrix.put("<s>", poses[0]);
			for (int i = 0; i < poses.length-1; i++) {
				this.posToPosMatrix.put(poses[i],poses[i+1]);
			}
			this.posToPosMatrix.put(poses[poses.length-1],"</s>");
		}
	}
	
	public double posToPosProbability(String fromPos,String toPos) {
		double sumCount=0;
		for (Long count : this.posToPosMatrix.row(fromPos).values()) {
			sumCount+=count;
		}
		return this.posToPosMatrix.get(fromPos, toPos)/sumCount;
	}
}
