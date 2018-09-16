package nlp.languagemodel;

import com.google.common.collect.MinMaxPriorityQueue;
import tools.util.BitCodec;
import tools.util.collection.HashSertInteger;
import tools.util.collection.KeyValue;
import tools.util.file.BufferedIterator;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map.Entry;

public class BaseSimpleLanguageModel {

	private static void getPostFixPrefix(String languageModelPath, String token, int count) throws Exception {
		BaseSimpleLanguageModel languageModel=new BaseSimpleLanguageModel(languageModelPath);
		MinMaxPriorityQueue<KeyValue<String, Integer>> sortedTopTrigramTokenTTF= MinMaxPriorityQueue.create();
		
		languageModel.getBigramPrefix(token, count, sortedTopTrigramTokenTTF);
		int i=0;
		double tdf=languageModel.getBigramPrefixCount(token);
		System.out.println(token+" Prefix tdf:"+tdf);
		while (sortedTopTrigramTokenTTF.size()>0) {
			i++;
			KeyValue<String, Integer> termTDF = sortedTopTrigramTokenTTF.removeLast();
			System.out.println(i+":\t"+termTDF.getKey()+"\t\t"+termTDF.getValue()/tdf);
		}
		
		System.out.println("\n\n");
		
		i=0;
		languageModel.getBigramPostfix(token, count, sortedTopTrigramTokenTTF);
		tdf=languageModel.getBigramPostfixCount(token);
		System.out.println(token+" Postfix tdf:"+tdf);
		while (sortedTopTrigramTokenTTF.size()>0) {
			i++;
			KeyValue<String, Integer> termTDF = sortedTopTrigramTokenTTF.removeLast();
			System.out.println(i+":\t"+termTDF.getKey()+"\t\t"+termTDF.getValue()/tdf);
		}
	}
	
	
	
	HashMap<String, Integer> tokensId;
	HashMap<Integer, String> idsToken;
	HashSertInteger<Long> encodedOneTwoTriGram;
	HashSertInteger<Integer> nullStarToken;
	HashSertInteger<Integer> nullTokenStar;
	private int bitCount = 21;
	int token1Id = 0;
	int token2Id = 0;
	int token3Id = 0;
	BitCodec bitCodec = new BitCodec();
	double SUMTTF;

	private BaseSimpleLanguageModel() {
		this.tokensId=new HashMap<String, Integer>();
		this.idsToken=new HashMap<Integer, String>();
		this.encodedOneTwoTriGram=new HashSertInteger<Long>();
		this.nullTokenStar=new HashSertInteger<Integer>();
		this.nullStarToken=new HashSertInteger<Integer>();
	}

	public BaseSimpleLanguageModel(String languageModelPath) {
		this.idsToken=new HashMap<Integer, String>(tools.util.file.Reader.getKeyValueIntegerStringFromTextFile(languageModelPath+"/id.token", -1, true, "\t"));
		this.tokensId=new HashMap<String, Integer>(this.idsToken.size());
		for (Entry<Integer, String> idTerm : this.idsToken.entrySet()) {
			this.tokensId.put(idTerm.getValue(),idTerm.getKey());
		}
		this.encodedOneTwoTriGram=new HashSertInteger<Long>(tools.util.file.Reader.getKeyValueLongIntegerFromTextFile(languageModelPath+"/lm", -1, true, "\t"));
		this.nullTokenStar=new HashSertInteger<Integer>(tools.util.file.Reader.getKeyValueIntegerIntegerFromTextFile(languageModelPath+"/nullTokenStar", -1, true));
		this.nullStarToken=new HashSertInteger<Integer>(tools.util.file.Reader.getKeyValueIntegerIntegerFromTextFile(languageModelPath+"/nullStarToken", -1, true));
//		for (Entry<Long, Integer> encodedOneTwoTriGramTTF : this.encodedOneTwoTriGram.getHashMap().entrySet()) {
//			this.reversEncodedOneTwoTriGram.put(this.bitCodec.revers(encodedOneTwoTriGramTTF.getKey(),bitCount),encodedOneTwoTriGramTTF.getValue());
//		}
	}
	
	public static void exportBigramLanguageModel(String bigramFileAddress , String languageModelPath) throws NumberFormatException, Exception{
		BaseSimpleLanguageModel baseSimpleLanguageModel=new BaseSimpleLanguageModel();
		baseSimpleLanguageModel.loadBigramLanguageModel(bigramFileAddress);
		baseSimpleLanguageModel.exportLanguageModel(languageModelPath);
	}
	
	public static BaseSimpleLanguageModel getLanguageModelFromBigram(String bigramFileAddress) throws NumberFormatException, Exception{
		BaseSimpleLanguageModel baseSimpleLanguageModel=new BaseSimpleLanguageModel();
		baseSimpleLanguageModel.loadBigramLanguageModel(bigramFileAddress);
		return baseSimpleLanguageModel;
	}
	
	public void exportLanguageModel(String languageModelPath){
		tools.util.Directory.create(languageModelPath);
		tools.util.file.Write.mapToTextFile(this.encodedOneTwoTriGram.getHashMap(), languageModelPath+"/lm");
		tools.util.file.Write.mapToTextFile(this.idsToken, languageModelPath+"/id.token");
		tools.util.file.Write.mapToTextFile(this.nullStarToken.getHashMap(), languageModelPath+"/nullStarToken");
		tools.util.file.Write.mapToTextFile(this.nullTokenStar.getHashMap(), languageModelPath+"/nullTokenStar");
		System.out.println("export operation complete.");
	}
	
	private void loadBigramLanguageModel(String bigramFileAddress) throws NumberFormatException, Exception{
		getTokenIdes(bigramFileAddress);
		loadTwoGram(bigramFileAddress);
		System.out.println("load bigram operation complete.");
	}

	private void loadTwoGram(String bigramFileAddress) throws NumberFormatException, Exception {
		Reader reader=tools.util.file.Reader.getFileBufferReader(bigramFileAddress);
		tools.util.file.BufferedIterator bufferedIterator=new BufferedIterator(reader);
		while(bufferedIterator.hasNext()){
			String newLine = bufferedIterator.next();
			if(newLine.length()<=0)
				break;
			String[] splite1 = newLine.split("\t");
			String[] tokens = splite1[0].split(" ");
			int ttf=Integer.valueOf(splite1[1]);
			int token1 = this.tokensId.get(tokens[0]);
			int token2 = this.tokensId.get(tokens[1]);
			encodedOneTwoTriGram.add(this.bitCodec.encode(0, token1, token2,21), ttf);
			encodedOneTwoTriGram.add(this.bitCodec.encode(0, 0, token1,21), ttf);
			encodedOneTwoTriGram.add(this.bitCodec.encode(0, 0, token2,21), ttf);
			nullTokenStar.add(token1,ttf);
			nullStarToken.add(token2,ttf);
		}
		reader.close();
		bufferedIterator.close();
	}

	
	void getTokenIdes(String bigramFileAddress) throws IOException{
		HashSertInteger<String> termsTTF=new HashSertInteger<String>();
		Reader reader=tools.util.file.Reader.getFileBufferReader(bigramFileAddress);
		tools.util.file.BufferedIterator bufferedIterator=new BufferedIterator(reader);
		while(bufferedIterator.hasNext()){
			String newLine = bufferedIterator.next();
			if(newLine.length()<=0)
				break;
			String[] splite1 = newLine.split("\t");
			String[] tokens = splite1[0].split(" ");
			int ttf=Integer.valueOf(splite1[1]);
			termsTTF.add(tokens[0], ttf);
			termsTTF.add(tokens[1], ttf);
		}
		reader.close();
		bufferedIterator.close();
		int id=1;
		for (Entry<String, Integer> tokens : tools.util.sort.Collection.mapSortedByValuesDecremental(termsTTF.getHashMap())) {
			this.idsToken.put(id, tokens.getKey());
			this.tokensId.put(tokens.getKey(),id);
			id++;
		}
	}

	public HashMap<String, Integer> getTokenTTF() throws Exception{
		HashMap<String, Integer> termTTF = new HashMap<String, Integer>(this.idsToken.size());
		for (int i = 1; i <= this.idsToken.size(); i++) {
			String term = this.idsToken.get(i);
			termTTF.put(term,getUnigramTTF(term));
		}
		return termTTF;
	}
	
	public void getTopTrigramToken(
			String token1,
			String token2,
			int resultCount,
			MinMaxPriorityQueue<KeyValue<String, Integer>> sortedTopTrigramTokenTTF) {
		sortedTopTrigramTokenTTF.clear();
		this.bitCodec.setEncodeToken1Token2InTrigram(this.tokensId.get(token1),
				this.tokensId.get(token2), bitCount);
		long trigramcode;
		int ttf;
		for (int tokenId = 1; tokenId <= this.tokensId.size(); tokenId++) {
			trigramcode = this.bitCodec.getTrigramCodeForToken3(tokenId,
					bitCount);
			if (this.encodedOneTwoTriGram.containsKey(trigramcode)) {
				ttf = this.encodedOneTwoTriGram.get(trigramcode);
				if (sortedTopTrigramTokenTTF.size() < resultCount) {
					KeyValue<String, Integer> tokenTTF = new KeyValue<String, Integer>(
							idsToken.get(tokenId), ttf);
					sortedTopTrigramTokenTTF.add(tokenTTF);
					// System.out.println(sortedTopTrigramTokenTTF.size()+"\t"+tokenTTF.getKeyValue(" : "));
				} else {
					if (sortedTopTrigramTokenTTF.peekFirst().getValue() < ttf) {
						sortedTopTrigramTokenTTF.removeFirst();
						KeyValue<String, Integer> tokenTTF = new KeyValue<String, Integer>(
								idsToken.get(tokenId), ttf);
						sortedTopTrigramTokenTTF.add(tokenTTF);
						// System.out.println(sortedTopTrigramTokenTTF.size()+"\t"+tokenTTF.getKeyValue(" : "));
					}
				}
			}
		}
	}

	public void getBigramPostfix(
			String token1,
			int resultCount,
			MinMaxPriorityQueue<KeyValue<String, Integer>> sortedTopTrigramTokenTTF) {
		sortedTopTrigramTokenTTF.clear();
//		System.out.println(sortedTopTrigramTokenTTF.size());
		this.bitCodec.setEncodeToken1Token2InTrigram(0,
				this.tokensId.get(token1), bitCount);
		long trigramcode;
		int ttf;
		for (int tokenId = 1; tokenId <= this.tokensId.size(); tokenId++) {
			trigramcode = this.bitCodec.getTrigramCodeForToken3(tokenId,
					bitCount);
			if (this.encodedOneTwoTriGram.containsKey(trigramcode)) {
				ttf = this.encodedOneTwoTriGram.get(trigramcode);
				if (sortedTopTrigramTokenTTF.size() < resultCount) {
					KeyValue<String, Integer> tokenTTF = new KeyValue<String, Integer>(
							idsToken.get(tokenId), ttf);
					sortedTopTrigramTokenTTF.add(tokenTTF);
				} else {
					if (sortedTopTrigramTokenTTF.peekFirst().getValue() < ttf) {
						sortedTopTrigramTokenTTF.removeFirst();
						KeyValue<String, Integer> tokenTTF = new KeyValue<String, Integer>(
								idsToken.get(tokenId), ttf);
						sortedTopTrigramTokenTTF.add(tokenTTF);
					}
				}
			}
		}
	}
	
	public void getBigramPrefix(
			String token2,
			int resultCount,
			MinMaxPriorityQueue<KeyValue<String, Integer>> sortedTopTrigramTokenTTF) {
		sortedTopTrigramTokenTTF.clear();
//		System.out.println(sortedTopTrigramTokenTTF.size());
		this.bitCodec.setEncodeToken1Token3InTrigram(0,
				this.tokensId.get(token2), bitCount);
		long trigramcode;
		int ttf;
		for (int tokenId = 1; tokenId <= this.tokensId.size(); tokenId++) {
			trigramcode = this.bitCodec.getTrigramCodeForToken2(tokenId,
					bitCount);
			if (this.encodedOneTwoTriGram.containsKey(trigramcode)) {
				ttf = this.encodedOneTwoTriGram.get(trigramcode);
				if (sortedTopTrigramTokenTTF.size() < resultCount) {
					KeyValue<String, Integer> tokenTTF = new KeyValue<String, Integer>(
							idsToken.get(tokenId), ttf);
					sortedTopTrigramTokenTTF.add(tokenTTF);
				} else {
					if (sortedTopTrigramTokenTTF.peekFirst().getValue() < ttf) {
						sortedTopTrigramTokenTTF.removeFirst();
						KeyValue<String, Integer> tokenTTF = new KeyValue<String, Integer>(
								idsToken.get(tokenId), ttf);
						sortedTopTrigramTokenTTF.add(tokenTTF);
					}
				}
			}
		}
	}

	public int getUnigramTTF(String token1) throws Exception {
		long trigramcode = this.bitCodec.encode(0, 0,
				this.tokensId.get(token1), bitCount);
		if (this.encodedOneTwoTriGram.containsKey(trigramcode))
			return this.encodedOneTwoTriGram.get(trigramcode);
		else
			return 0;
	}

	public int getBigramTTF(String token1, String token2) throws Exception {
		long trigramcode = this.bitCodec.encode(0, this.tokensId.get(token1),
				this.tokensId.get(token2), bitCount);
		if (this.encodedOneTwoTriGram.containsKey(trigramcode))
			return this.encodedOneTwoTriGram.get(trigramcode);
		else
			return 0;
	}
	
	public int getBigramPostfixCount(String token) throws Exception {
		int tokenId = this.tokensId.get(token);
		if(this.nullTokenStar.containsKey(tokenId))
			return this.nullTokenStar.get(tokenId);
		else
			return 0;
	}

	public int getBigramPrefixCount(String token) throws Exception {
		int tokenId = this.tokensId.get(token);
		if(this.nullStarToken.containsKey(tokenId))
			return this.nullStarToken.get(tokenId);
		else
			return 0;
	}	

	public int getTrigramTTF(String token1, String token2, String token3)
			throws Exception {
		long trigramcode = this.bitCodec.encode(this.tokensId.get(token1),
				this.tokensId.get(token2), this.tokensId.get(token3), bitCount);
		if (this.encodedOneTwoTriGram.containsKey(trigramcode))
			return this.encodedOneTwoTriGram.get(trigramcode);
		else
			return 0;
	}

//	public int getBigramTTFPrefix(String string) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
}
