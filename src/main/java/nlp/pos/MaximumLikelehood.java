package nlp.pos;

import com.google.gson.Gson;
import nlp.pos.data.PosTaggedInstanceSentence;
import nlp.pos.tagger.hmm.Emission;
import tools.util.Time;
import tools.util.collection.KeyValue;

import java.util.*;
import java.util.Map.Entry;

public class MaximumLikelehood {


	private static String getPosDescription(String key) {
		if("V".equals(key))
			return "فعل";
		if("N".equals(key))
			return "اسم";
		if("P".equals(key))
			return "حرف اضافه";
		if("ADV".equals(key))
			return "قید";		
		if("CONJ".equals(key))
			return "حرف ربط";
		if("NUM".equals(key))
			return "عدد";
		if("AJ".equals(key))
			return "صفت";
		if("DET".equals(key))
			return "اشاره";
		if("RES".equals(key))
			return "پسوند اضافه";
		if("PRO".equals(key))
			return "ضمیر";
		if("POSTP".equals(key))
			return "حرف اضافه مفعولی";		
		return key;
	}


	private SortedSet<Entry<String, Double>> getTokenPosList(String token) {
		return tools.util.sort.Collection.mapSortedByValues(this.emission.getTokenPosesProbability(token));
	}


	Emission emission;
	
	public MaximumLikelehood(Set<KeyValue<String, String>> trainDataTokenPos,String logName) throws Exception{
		this.emission=new Emission();
		int i=0;
		for (KeyValue<String, String> sentencePos : trainDataTokenPos) {
			emission.addTokenPosArray(sentencePos.getKey().split("(\\s)+"), sentencePos.getValue().split("(\\s)+"));
			i++;
			if(i%10000==0)
				System.out.println(i+" "+logName+" sentence with pos added to emmision matrix.");
		}
		System.out.println("load complete: "+i+" "+logName+" sentence with pos added to MaximumLikelehood.");
	}
	
//	public MaximumLikelehood(String dataSetFileAddress) throws Exception {
//		this.emission=new Emission();
//		int i=0;
//		for (String string : tools.util.file.Reader.getTextLinesString(dataSetFileAddress, false)) {
//			String[] split = string.split("\t");
//			emission.addTokenPosArray(split[1].split("(\\s)+"), split[2].split("(\\s)+"));
//			i++;
//			if(i%10000==0)
//				System.out.println(i+" sentence with pos added to emmision matrix.");
//		}
//		System.out.println("load complete: "+i+" sentence with pos added to MaximumLikelehood.");
//	}
	
	public MaximumLikelehood(String dataSetJsonFileAddress) throws Exception {
		Gson gson=new Gson(); 
		this.emission=new Emission();
		int i=0;
		for (String json : tools.util.file.Reader.getTextLinesString(dataSetJsonFileAddress, false)) {
			PosTaggedInstanceSentence posTaggedInstanceSentence = gson.fromJson(json, PosTaggedInstanceSentence.class);
			emission.addTokenPosArray(posTaggedInstanceSentence);
			i++;
			if(i%10000==0)
				System.out.println(i+" sentence with pos added to emmision matrix.");
		}
		System.out.println("load complete: "+i+" sentence with pos added to MaximumLikelehood.");
	}

	public void reload(Set<KeyValue<String, String>> trainDataTokenPos,String logName) throws Exception{
		this.emission.clear();
		int i=0;
		for (KeyValue<String, String> sentencePos : trainDataTokenPos) {
			emission.addTokenPosArray(sentencePos.getKey().split("(\\s)+"), sentencePos.getKeyValue().split("(\\s)+"));
			i++;
			if(i%10000==0)
				System.out.println(i+" "+logName+" sentence with pos added to emmision matrix.");
		}
		System.out.println("load complete: "+i+" "+logName+" sentence with pos added to MaximumLikelehood.");
	}
	
	
	public KeyValue<String, Double>[] getPos(String[] sentenceTokens){
		ArrayList<KeyValue<String, Double>> list=getPosArrayList(sentenceTokens);
		return list.toArray(new KeyValue[list.size()]);
	}
	
	private ArrayList<KeyValue<String, Double>> getPosArrayList(String[] sentenceTokens){
		ArrayList<KeyValue<String, Double>> result=new ArrayList<KeyValue<String,Double>>(sentenceTokens.length);
		for (int i = 0; i < sentenceTokens.length; i++) {
			result.add(this.emission.getTokenPosWithMaxProbability(sentenceTokens[i]));
		}
		return result;
	}
	
	private ArrayList<KeyValue<String,KeyValue<String, Double>>> getPosArrayList(List<String> sentenceTokens){
		ArrayList<KeyValue<String,KeyValue<String, Double>>> result=new ArrayList<KeyValue<String,KeyValue<String,Double>>>(sentenceTokens.size()); 
		for (String token:sentenceTokens) {
			result.add(new KeyValue<String, KeyValue<String,Double>>(token, this.emission.getTokenPosWithMaxProbability(token)));
		}
		return result;
	}
	public void getPosArrayList(Set<String> testSentences,HashMap<String, ArrayList<KeyValue<String, Double>>> testSentencesMapResult){
		int testSize = testSentences.size();
		System.out.println("start get pos for "+testSize+" sentences. ");
		Time.setStartTimeForNow();
		testSentencesMapResult.clear();
		int i = 0;
		for (String sentence : testSentences) {
			i++;
			testSentencesMapResult.put(sentence,getPosArrayList(sentence.split("(\\s)+")));
			if(i%10000==0)
				System.out.println("get "+i+"("+(i*100/testSize)+"%) sentence pos in "+(Time.getTimeLengthForNow())+" ms");
		}
		System.out.println("Get sentence pos complete for "+i+" sentence in "+(Time.getTimeLengthForNow())+" ms");
	}
	
}
