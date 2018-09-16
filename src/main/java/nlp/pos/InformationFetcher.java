package nlp.pos;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import tools.util.BitCodec;
import tools.util.BitCodec.ThreeInt;
import tools.util.Time;
import tools.util.collection.HashSert;
import tools.util.collection.IncrementalTable;
import tools.util.collection.KeyValue;
import tools.util.file.Write.HashMapWriteType;
import tools.util.sort.Collection.SortType;

import com.google.common.collect.Table;

public class InformationFetcher {
	
	public static void main(String[] args) throws Exception {
		String path = "/home/rahmani/Data/bijankhan/posAnalysis/test/";		
		InformationFetcher informationFetcher = new InformationFetcher(path
				+ "sentencesWithCompletePOS.utf8", ',');
		String resultPath=path+"posFetcher/";
		informationFetcher.writeTokenCount(resultPath + "_tokenTf");
		informationFetcher.writePosCount(resultPath);
		informationFetcher.writeCharacterCount(resultPath + "_charSet");
		informationFetcher.writePosTokenStatistic(resultPath);
		informationFetcher.writeDatasetFile(resultPath+"_dataset");
		
//		String path = "/home/saeed/Data/bijankhan/posFetcher/compoundWord/";
//		HashMap<String, Integer> tokensId=tools.util.file.Reader.getKeyValueStringIntegerFromTextFile(path+"tokenId", -1, true,"\t");
//		HashMap<Long, Long> oneTwoThreeTokenCodedCount=tools.util.file.Reader.getKeyValueLongLongFromTextFile(path+"oneTwoThreeTokenCount", -1l, true,"\t");
//		HashMap<String, Long> moreThanThreeTokenCount=tools.util.file.Reader.getKeyValueStringLongFromTextFile(path+"moreThanThreeTokenCount", -1l, true,"\t");;
//		String resultFilePath=path;
//		getPrefixPostFixToken(tokensId, oneTwoThreeTokenCodedCount, moreThanThreeTokenCount, resultFilePath);
		System.out.println("operation complete.");
	}

	ArrayList<KeyValue<String, String>> postaggerDataset=new ArrayList<KeyValue<String,String>>();
	ArrayList<KeyValue<String, String>> postaggerDatasetLevel1=new ArrayList<KeyValue<String,String>>();
	ArrayList<KeyValue<String, String>> postaggerDatasetLevel2=new ArrayList<KeyValue<String,String>>();
	ArrayList<KeyValue<String, String>> postaggerDatasetLevel3=new ArrayList<KeyValue<String,String>>();
	ArrayList<KeyValue<String, String>> postaggerDatasetLevel4=new ArrayList<KeyValue<String,String>>();
	ArrayList<KeyValue<String, String>> postaggerDatasetLevel5=new ArrayList<KeyValue<String,String>>();
	
	HashSert<String> token = new HashSert<String>();
	HashSert<Character> charSet=new HashSert<Character>();

	HashSert<String> posLevelOne = new HashSert<String>();
	HashSert<String> posLevelTwo = new HashSert<String>();
	HashSert<String> posLevelThree = new HashSert<String>();
	HashSert<String> posLevelFour = new HashSert<String>();
	HashSert<String> posLevelFive = new HashSert<String>();

	IncrementalTable<String, String> posTokensTfLevelOne = new IncrementalTable<String, String>();
	IncrementalTable<String, String> posTokensTfLevelTwo = new IncrementalTable<String, String>();
	IncrementalTable<String, String> posTokensTfLevelThree = new IncrementalTable<String, String>();
	IncrementalTable<String, String> posTokensTfLevelFour = new IncrementalTable<String, String>();
	IncrementalTable<String, String> posTokensTfLevelFive = new IncrementalTable<String, String>();

	public InformationFetcher(String fileAddress, char posDelimiter)
			throws IOException {
		tools.util.Time.setStartTimeForNow();
		int sentenceCounter = 0;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileAddress), Charset.forName("utf8")));
		String[] newLineSplit = new String[3];
		String newLine;
		String[] tokens;
		String[] cpos;
		String[] hierarchicalPos;
		int sentenceId = 0;
		String tempStr = "";
		int posDeepBiggerThan5 = 0;
		try {
			while ((newLine = reader.readLine()) != null) {
				sentenceCounter++;
				if (sentenceCounter % 100 == 0)
					System.out.println(sentenceCounter + " sentence handled on "
							+ tools.util.Time.getTimeLengthForNow()/1000 + " s.");

				newLineSplit = newLine.split("\t");
				if (newLineSplit.length > 0)
					sentenceId = Integer.parseInt(newLineSplit[0]);
				if (newLineSplit.length == 3) {
					tokens = newLineSplit[1].split(" ");
					cpos = newLineSplit[2].split(" ");
					getDatasetFileData(newLineSplit[1],newLineSplit[2],posDelimiter);
					if (tokens.length == cpos.length) {
						for (int i = 0; i < tokens.length; i++) {
							this.token.put(tokens[i]);
							for (int j = 0; j < tokens[i].length(); j++) {
								this.charSet.put(tokens[i].charAt(j));
							}
							hierarchicalPos = cpos[i].split(posDelimiter + "");
							if (hierarchicalPos.length == 1) {
								this.posLevelOne.put(hierarchicalPos[0]);
								posTokensTfLevelOne.put(hierarchicalPos[0],
										tokens[i]);
								this.posLevelTwo.put(hierarchicalPos[0]);
								posTokensTfLevelTwo.put(hierarchicalPos[0],
										tokens[i]);
								this.posLevelThree.put(hierarchicalPos[0]);
								posTokensTfLevelThree.put(hierarchicalPos[0],
										tokens[i]);
								this.posLevelFour.put(hierarchicalPos[0]);
								posTokensTfLevelFour.put(hierarchicalPos[0],
										tokens[i]);
								this.posLevelFive.put(hierarchicalPos[0]);
								posTokensTfLevelFive.put(hierarchicalPos[0],
										tokens[i]);
							} else if (hierarchicalPos.length == 2) {
								this.posLevelOne.put(hierarchicalPos[0]);
								posTokensTfLevelOne.put(hierarchicalPos[0],
										tokens[i]);
								tempStr = hierarchicalPos[0] + posDelimiter
										+ hierarchicalPos[1];
								this.posLevelTwo.put(tempStr);
								posTokensTfLevelTwo.put(tempStr, tokens[i]);
								this.posLevelThree.put(tempStr);
								posTokensTfLevelThree.put(tempStr, tokens[i]);
								this.posLevelFour.put(tempStr);
								posTokensTfLevelFour.put(tempStr, tokens[i]);
								this.posLevelFive.put(tempStr);
								posTokensTfLevelFive.put(tempStr, tokens[i]);
							} else if (hierarchicalPos.length == 3) {
								this.posLevelTwo.put(hierarchicalPos[0]);
								posTokensTfLevelOne.put(hierarchicalPos[0],
										tokens[i]);
								this.posLevelTwo.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1]);
								posTokensTfLevelTwo.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1],
										tokens[i]);
								tempStr = hierarchicalPos[0] + posDelimiter
										+ hierarchicalPos[1] + posDelimiter
										+ hierarchicalPos[2];
								this.posLevelThree.put(tempStr);
								posTokensTfLevelThree.put(tempStr, tokens[i]);
								this.posLevelFour.put(tempStr);
								posTokensTfLevelFour.put(tempStr, tokens[i]);
								this.posLevelFive.put(tempStr);
								posTokensTfLevelFive.put(tempStr, tokens[i]);
							} else if (hierarchicalPos.length == 4) {
								this.posLevelOne.put(hierarchicalPos[0]);
								posTokensTfLevelOne.put(hierarchicalPos[0],
										tokens[i]);
								this.posLevelTwo.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1]);
								posTokensTfLevelTwo.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1],
										tokens[i]);
								this.posLevelThree.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1]
										+ posDelimiter + hierarchicalPos[2]);
								posTokensTfLevelThree.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1]
										+ posDelimiter + hierarchicalPos[2],
										tokens[i]);
								tempStr = hierarchicalPos[0] + posDelimiter
										+ hierarchicalPos[1] + posDelimiter
										+ hierarchicalPos[2] + posDelimiter
										+ hierarchicalPos[3];
								this.posLevelFour.put(tempStr);
								posTokensTfLevelFour.put(tempStr, tokens[i]);
								this.posLevelFive.put(tempStr);
								posTokensTfLevelFive.put(tempStr, tokens[i]);
							} else if (hierarchicalPos.length >= 5) {
								if (hierarchicalPos.length > 5)
									posDeepBiggerThan5++;
								this.posLevelOne.put(hierarchicalPos[0]);
								posTokensTfLevelOne.put(hierarchicalPos[0],
										tokens[i]);
								this.posLevelTwo.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1]);
								posTokensTfLevelTwo.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1],
										tokens[i]);
								this.posLevelThree.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1]
										+ posDelimiter + hierarchicalPos[2]);
								posTokensTfLevelThree.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1]
										+ posDelimiter + hierarchicalPos[2],
										tokens[i]);
								this.posLevelFour.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1]
										+ posDelimiter + hierarchicalPos[2]
										+ posDelimiter + hierarchicalPos[3]);
								posTokensTfLevelFour.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1]
										+ posDelimiter + hierarchicalPos[2]
										+ posDelimiter + hierarchicalPos[3],
										tokens[i]);
								this.posLevelFive.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1]
										+ posDelimiter + hierarchicalPos[2]
										+ posDelimiter + hierarchicalPos[3]
										+ posDelimiter + hierarchicalPos[4]);
								posTokensTfLevelFive.put(hierarchicalPos[0]
										+ posDelimiter + hierarchicalPos[1]
										+ posDelimiter + hierarchicalPos[2]
										+ posDelimiter + hierarchicalPos[3]
										+ posDelimiter + hierarchicalPos[4],
										tokens[i]);
							}
						}
					} else {
						System.out.println("sentence(" + sentenceId
								+ ") not have valid pos and token count.");
					}
				} else {
					System.out.println("sentence(" + sentenceId
							+ ") not have valid lenght.");
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader.close();
		System.out.println(sentenceCounter + " sentence handled on "
				+ tools.util.Time.getTimeLengthForNow() + " ms with "
				+ posDeepBiggerThan5 + " bigger than 5 pos Token Deep.");

	}

	private void getDatasetFileData(String tokens, String completePos,char posDelimiter) {
		int offset = 0;
		for (int i = 0; i < 5; i++) {
			String poses="";
			for (String cpos : completePos.split(" ")) {
				offset=0;
				for (int j = 0; j <i; j++) {
					offset+=cpos.indexOf(posDelimiter, offset)+1;
				}
				poses+=cpos.substring(0, cpos.indexOf(posDelimiter,offset)<0?cpos.length():cpos.indexOf(posDelimiter,offset))+" ";
			}
			switch (i) {
			case 0:
				postaggerDatasetLevel1.add(new KeyValue<String, String>(tokens, poses));
				break;
			case 1:
				postaggerDatasetLevel2.add(new KeyValue<String, String>(tokens, poses));
				break;
			case 2:
				postaggerDatasetLevel3.add(new KeyValue<String, String>(tokens, poses));
				break;
			case 3:
				postaggerDatasetLevel4.add(new KeyValue<String, String>(tokens, poses));
				break;
			case 4:
				postaggerDatasetLevel5.add(new KeyValue<String, String>(tokens, poses));
				break;
			}
		}
//		postaggerDataset.add(new KeyValue<String, String>(tokens, poses));
	}

	public void writeDatasetFile(String fileAddress) throws Exception {
		for (int i = 1; i <= 5; i++) {
			writeDatasetFile(fileAddress, i);
		}
	}
	
//	public void writeDatasetFile(String fileAddress) throws Exception {
//		Time.setStartTimeForNow();
//		PrintWriter out = new PrintWriter(fileAddress,"UTF-8");
//		int counter=0;
//		KeyValue<String, String> newSentence;
//		Iterator<KeyValue<String, String>> iterator = this.postaggerDataset.iterator();
//		while (iterator.hasNext()) {
//			newSentence = iterator.next();
//			out.println(newSentence.getKeyValue());
//			counter++;
//			if(counter%10000==0)
//				System.out.println(counter+" line writed.");
//		}
//		out.close();
//		if(counter!=this.postaggerDataset.size())
//			throw new Exception("enumerated line count not equal with loaded line count loadedLineCount:"+this.postaggerDataset.size()+" "+" writedFileLineCount:"+counter);
//		System.out.println("write dataset "+counter+" line to file("+fileAddress+") on "+Time.getTimeLengthForNow()+" ms.");
//	}
//	
	
	private void writeDatasetFile(String fileAddress,int level) throws Exception {
		Time.setStartTimeForNow();
		PrintWriter out = new PrintWriter(fileAddress+"_level_"+level,"UTF-8");
		int counter=0;
		KeyValue<String, String> newSentence;
		Iterator<KeyValue<String, String>> iterator = null;
		switch (level) {
		case 1:
			iterator= this.postaggerDatasetLevel1.iterator();
			break;
		case 2:
			iterator= this.postaggerDatasetLevel2.iterator();
			break;
		case 3:
			iterator= this.postaggerDatasetLevel3.iterator();
			break;
		case 4:
			iterator= this.postaggerDatasetLevel4.iterator();
			break;
		case 5:
			iterator= this.postaggerDatasetLevel5.iterator();
			break;
		}
		
		while (iterator.hasNext()) {
			newSentence = iterator.next();
			out.println(newSentence.getKeyValue());
			counter++;
			if(counter%10000==0)
				System.out.println(counter+" level "+level+" line writed.");
		}
		out.close();
		if(counter!=this.postaggerDatasetLevel1.size())
			throw new Exception("enumerated line count not equal with loaded line count loadedLineCount:"+this.postaggerDataset.size()+" "+" writedFileLineCount:"+counter);
		System.out.println("write dataset  level "+level+": "+counter+" line to file("+fileAddress+" level "+level+") on "+Time.getTimeLengthForNow()+" ms.");
	}
	
	
	public void writeTokenCount(String fileAddress) {
		tools.util.file.Write.mapToTextFileSortedByValue(
				this.token.getHashMap(), fileAddress, SortType.DECREMENTAL,
				HashMapWriteType.KEYVALUE);
	}

	public void writeCharacterCount(String fileAddress) {
		tools.util.file.Write.mapToTextFileSortedByValue(
				this.charSet.getHashMap(), fileAddress, SortType.DECREMENTAL,
				HashMapWriteType.KEYVALUE);
	}
	
	public void writePosCount(String filePath) {
		tools.util.Directory.create(filePath + "//PosStatisticInformation");
		tools.util.file.Write.mapToTextFileSortedByValue(
				this.posLevelOne.getHashMap(), filePath
						+ "//PosStatisticInformation//PoslevelOne",
				SortType.DECREMENTAL, HashMapWriteType.KEYVALUE);
		tools.util.file.Write.mapToTextFileSortedByValue(
				this.posLevelTwo.getHashMap(), filePath
						+ "//PosStatisticInformation//PoslevelTwo",
				SortType.DECREMENTAL, HashMapWriteType.KEYVALUE);
		tools.util.file.Write.mapToTextFileSortedByValue(
				this.posLevelThree.getHashMap(), filePath
						+ "//PosStatisticInformation//PoslevelThree",
				SortType.DECREMENTAL, HashMapWriteType.KEYVALUE);
		tools.util.file.Write.mapToTextFileSortedByValue(
				this.posLevelFour.getHashMap(), filePath
						+ "//PosStatisticInformation//PoslevelFour",
				SortType.DECREMENTAL, HashMapWriteType.KEYVALUE);
	}

	public void writePosTokenStatistic(String filePath) {
		String resultPath = filePath + "PosStatisticInformation";
		tools.util.Directory.create(resultPath);
		resultPath+="//";
		savePOSInformation(resultPath, "levelOne",
				this.posTokensTfLevelOne.getTable());
		savePOSInformation(resultPath, "levelTwo",
				this.posTokensTfLevelTwo.getTable());
		savePOSInformation(resultPath, "levelThree",
				this.posTokensTfLevelThree.getTable());
		savePOSInformation(resultPath, "levelFour",
				this.posTokensTfLevelFour.getTable());
		savePOSInformation(resultPath, "levelBiggerThanFour",
				this.posTokensTfLevelFive.getTable());
	}

	private void savePOSInformation(String resultPath, String level,
			Table<String, String, Long> table) {
		HashMap<String, Integer> posCount = new HashMap<String, Integer>();
		int counter = 0;
		String tempPosString;
		String tempCountString;
		// extract term <POS,Frequency> for level argument of hierarchical POS
		tools.util.Directory.create(resultPath + level + "//");
		tools.util.Time.setStartTimeForNow();
		for (Entry<String, Map<String, Long>> pos : table.rowMap().entrySet()) {
			for (Entry<String, Long> entry : tools.util.sort.Collection
					.mapSortedByValuesDecremental(pos.getValue())) {
				tools.util.file.Write.stringToTextFile(entry.getKey() + "\t"
						+ entry.getValue(), resultPath + "//" + level + "//"
						+ pos.getKey(), true);
			}
		}
		System.out.println("write pos " + level + " information on "
				+ tools.util.Time.getTimeLengthForNow() + " ms.");
		// extract term <PosCount,Token,POS[]> for level argument of
		// hierarchical POS
		tools.util.Time.setStartTimeForNow();
		for (Entry<String, Map<String, Long>> token : table.columnMap()
				.entrySet()) {
			tempPosString = "";
			tempCountString = "";
			counter=0;
			for (Entry<String, Long> entry : tools.util.sort.Collection
					.mapSortedByValuesDecremental(token.getValue())) {
				tempPosString += entry.getKey() + " ";
				tempCountString += entry.getValue() + " ";
				counter++;
			}
			posCount.put(token.getKey() + "\t" + tempPosString + "\t"
					+ tempCountString,counter);
		}
		tools.util.file.Write.mapToTextFileSortedByValue(posCount,
				resultPath + "//tokenPos_" + level, SortType.DECREMENTAL,
				HashMapWriteType.VALUEKEY);
		System.out.println("write token (pos " + level + ") information on "
				+ tools.util.Time.getTimeLengthForNow() + " ms.");
	}

	public static void getPrefixPostFixToken(HashMap<String, Integer> tokensId,HashMap<Long, Long> oneTwoThreeTokenCodedCount,HashMap<String, Long> moreThanThreeTokenCount,String resultFilePath){
		HashMap<Integer,Long> token1gramHashMap=new HashMap<Integer,Long>();
		HashSert<Integer> token2gramPrefix=new HashSert<Integer>();
		HashSert<Integer> token2gramPostfix=new HashSert<Integer>();
		HashSert<Integer> token3gramPrefix=new HashSert<Integer>();
		HashSert<Integer> token3gramMiddlefix=new HashSert<Integer>();
		HashSert<Integer> token3gramPostfix=new HashSert<Integer>();
		HashSert<Integer> tokenMoreGramPrefix=new HashSert<Integer>();
		HashSert<Integer> tokenMoreGramMiddlefix=new HashSert<Integer>();
		HashSert<Integer> tokenMoreGramPostfix=new HashSert<Integer>();
		BitCodec bitCodec=new BitCodec();
		ThreeInt threeInt;
		long count = 0l;
		for (Entry<Long, Long> entry : oneTwoThreeTokenCodedCount.entrySet()) {
			count=entry.getValue();
			threeInt = bitCodec.decode(entry.getKey(),21);
//			threeInt.println();
			if(threeInt.getInt2()==0){
				token1gramHashMap.put(threeInt.getInt1(), count);
			}
			else if(threeInt.getInt3()==0){
				token2gramPrefix.add(threeInt.getInt1(),count);
				token2gramPostfix.add(threeInt.getInt2(),count);
			}
			else{
				token3gramPrefix.add(threeInt.getInt1(),count);
				token3gramMiddlefix.add(threeInt.getInt2(),count);
				token3gramPostfix.add(threeInt.getInt3(),count);
			}
		}
		for (Entry<String, Long> entry : moreThanThreeTokenCount.entrySet()) {
			count=entry.getValue();
			String[] split = entry.getKey().split("(\\s)+");
			tokenMoreGramPrefix.add(tokensId.get(split[0]), count);
			for (int i = 1; i < split.length-1; i++) {
				tokenMoreGramMiddlefix.add(tokensId.get(split[i]), count);
			}
			tokenMoreGramPostfix.add(tokensId.get(split[split.length-1]), count);
		}
		
		HashSert<Integer> token1gramHashSert=new HashSert<Integer>(token1gramHashMap);
		HashMap<String, Double> tokenPrefixScore=new HashMap<String, Double>(tokensId.size());
		HashMap<String, Double> tokenMiddlefixScore=new HashMap<String, Double>(tokensId.size());
		HashMap<String, Double> tokenPostfixScore=new HashMap<String, Double>(tokensId.size());
		HashMap<String, Double> tokenPrefixScoreMulLogTTF=new HashMap<String, Double>(tokensId.size());
		HashMap<String, Double> tokenMiddlefixScoreMulLogTTF=new HashMap<String, Double>(tokensId.size());
		HashMap<String, Double> tokenPostfixScoreMulLogTTF=new HashMap<String, Double>(tokensId.size());
		String tokenString="";
		int tokenId=0;
		long sumPrefix = 0l;
		long sumMiddlefix = 0l;
		long sumPostfix = 0l;
		for (Entry<String, Integer> entry : tokensId.entrySet()) {
			tokenId=entry.getValue();
			tokenString=entry.getKey();
			sumPrefix=token2gramPrefix.get(tokenId)+token3gramPrefix.get(tokenId)+tokenMoreGramPrefix.get(tokenId);
			sumMiddlefix=token3gramMiddlefix.get(tokenId)+tokenMoreGramMiddlefix.get(tokenId);
			sumPostfix=token2gramPostfix.get(tokenId)+token3gramPostfix.get(tokenId)+tokenMoreGramPostfix.get(tokenId);
			if(sumPrefix>0){
				tokenPrefixScore.put(tokenString, ((double)sumPrefix)/(sumPrefix+token1gramHashSert.get(tokenId)));
				tokenPrefixScoreMulLogTTF.put(tokenString, ((double)sumPrefix)/(sumPrefix+token1gramHashSert.get(tokenId))*Math.log((sumPrefix+token1gramHashSert.get(tokenId))));
			}
			if(sumMiddlefix>0){
				tokenMiddlefixScore.put(tokenString, ((double)sumMiddlefix)/(sumMiddlefix+token1gramHashSert.get(tokenId)));
				tokenMiddlefixScoreMulLogTTF.put(tokenString, ((double)sumMiddlefix)/(sumMiddlefix+token1gramHashSert.get(tokenId))*Math.log((sumMiddlefix+token1gramHashSert.get(tokenId))));
			}
			if(sumPostfix>0){
				tokenPostfixScore.put(tokenString, ((double)sumPostfix)/(sumPostfix+token1gramHashSert.get(tokenId)));
				tokenPostfixScoreMulLogTTF.put(tokenString, ((double)sumPostfix)/(sumPostfix+token1gramHashSert.get(tokenId))*Math.log((sumPostfix+token1gramHashSert.get(tokenId))));
			}
		}
		tools.util.file.Write.mapToTextFileSortedByValue(tokenPrefixScore, resultFilePath+"tokenPrefixScore", SortType.DECREMENTAL, HashMapWriteType.KEYVALUE);
		tools.util.file.Write.mapToTextFileSortedByValue(tokenMiddlefixScore, resultFilePath+"tokenMiddlefixScore", SortType.DECREMENTAL, HashMapWriteType.KEYVALUE);
		tools.util.file.Write.mapToTextFileSortedByValue(tokenPostfixScore, resultFilePath+"tokenPostfixScore", SortType.DECREMENTAL, HashMapWriteType.KEYVALUE);
		tools.util.file.Write.mapToTextFileSortedByValue(tokenPrefixScoreMulLogTTF, resultFilePath+"tokenPrefixScoreMulLogTTF", SortType.DECREMENTAL, HashMapWriteType.KEYVALUE);
		tools.util.file.Write.mapToTextFileSortedByValue(tokenMiddlefixScoreMulLogTTF, resultFilePath+"tokenMiddlefixScoreMulLogTTF", SortType.DECREMENTAL, HashMapWriteType.KEYVALUE);
		tools.util.file.Write.mapToTextFileSortedByValue(tokenPostfixScoreMulLogTTF, resultFilePath+"tokenPostfixScoreMulLogTTF", SortType.DECREMENTAL, HashMapWriteType.KEYVALUE);
	}
	
}
