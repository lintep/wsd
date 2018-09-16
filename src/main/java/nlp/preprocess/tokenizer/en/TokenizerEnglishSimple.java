package nlp.preprocess.tokenizer.en;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import tools.util.Str;
import tools.util.collection.KeyValue;

public class TokenizerEnglishSimple {

	private static final String baseAddress = tools.util.File.getParent(tools.util.File.getParent(tools.util.File.getCurrentDirectory()))+"/NLP/src/nlp/preprocess/tokenizer/en/";
	private static final String fileAddressSmallTokens=baseAddress+"tokensSmallerThanThreeChar.dist";
	HashSet<String> tokensWithSmallerThanThreeChars;
	private static final String fileAddressTokensDist=baseAddress+"rawTermToken.dist";
	HashMap<String, Integer> tokensDistribution;//=new HashMap<String, Integer>();
	
	StringBuilder sentence;
	StringBuilder rawTerm;
	String currentToken;
	char currentChar;
	String preToken;
//	HashMap<String, Integer> tokensDistribution;
	private Integer minFrequency;

	/**
	 * 
	 * @param minFrequency used for checking to splitted token count
	 * @param tokenLengthBoundry if tokenLengthBoundry is true then if token length bigger than 25 replaced with null
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public TokenizerEnglishSimple(Integer minFrequency,boolean tokenLengthBoundry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		this.tokenLengthBoundry=tokenLengthBoundry;
		this.tokensWithSmallerThanThreeChars=new HashSet<String>(tools.util.file.Reader.getStringFromTextFile(fileAddressSmallTokens, false));
		this.tokensDistribution = new HashMap<String, Integer>();
		HashMap<String, Integer> rawTokenDist = tools.util.file.Reader
		.getKeyValueStringIntegerFromTextFile(
				fileAddressTokensDist, 0, false, "\t");
		this.minFrequency=minFrequency;
		for (Entry<String, Integer> entry : rawTokenDist.entrySet()) {
			if(entry.getKey().length()<3){
				if(this.tokensWithSmallerThanThreeChars.contains(entry.getKey()))
					this.tokensDistribution.put(entry.getKey(), entry.getValue());
			}
			else
				this.tokensDistribution.put(entry.getKey(), entry.getValue());
		}
		sentence = new StringBuilder();
		rawTerm = new StringBuilder();
		initTempVariables();
	}

	public TokenizerEnglishSimple(HashMap<String, Integer> validTokens, Integer minFrequency,boolean tokenLengthBoundry) {
		this.tokenLengthBoundry=tokenLengthBoundry;
		this.minFrequency=minFrequency;
		this.tokensDistribution = new HashMap<String, Integer>(validTokens);
		sentence = new StringBuilder();
		rawTerm = new StringBuilder();
		initTempVariables();
	}

	public String tokenize(String inSentence) {
		this.sentence.setLength(0);
		this.rawTerm.setLength(0);
		this.preToken = "<s>";
		for (int i = 0; i < inSentence.length(); i++) {
			currentChar = inSentence.charAt(i);
			if (currentChar == ' ') {
				currentToken = rawTerm.toString();
				this.rawTerm.setLength(0);
				getTokens(minFrequency);
				preToken = currentToken;
			} else {
				this.rawTerm.append(currentChar);
			}
		}
		if (this.rawTerm.length() > 0) {
			currentToken = rawTerm.toString();
			this.rawTerm.setLength(0);
			getTokens(minFrequency);
		}

		return sentence.toString();
	}

	String tempToken;
	private void getTokens(Integer minFrequency) {
		if (checkNumber(currentToken)) {
			if (!checkNumber(preToken)) {
				sentence.append('@');
				sentence.append(' ');
			}
		} else {
			switch (currentToken.length()) {
			case 1:
				if (currentToken.charAt(0) == 'a') {
					sentence.append('a');
					sentence.append(' ');
				}
				break;

			default:
				tempToken = getToken(currentToken,minFrequency);
				if(tempToken.length()>0){
					sentence.append(tempToken);
					sentence.append(' ');
				}

				break;
			}
		}
	}

	int i1 = 0;
	int i2 = 0;
	int i3 = 0;
	int i4 = 0;
	int i5 = 0;
	int i6 = 0;
	long tempPhreaseScoreArray[] = new long[6];
	StringBuilder tempTerm[] = new StringBuilder[6];
	// tempTerm[0]=new StringBuilder();
	StringBuilder tempStr = new StringBuilder();
	double tempPhreaseScore = 0;
	String tempString = "";
	int tempEndIndex;
	int tempStartIndex;
	ArrayList<KeyValue<String, Double>> candidateFrease = new ArrayList<KeyValue<String, Double>>();
	private boolean tokenLengthBoundry;

	private void initTempVariables() {
		for (int i = 0; i < 6; i++) {
			tempTerm[i] = new StringBuilder();
		}
	}

	private double getPhreaseScore(int tokenCount) {
		tempPhreaseScore = 0;
		 for (int i = 0; i < tokenCount; i++) {
		 tempPhreaseScore+=tempTerm[i].length()*Math.log(tempPhreaseScoreArray[i]);
		 }
//		for (int i = 0; i < tokenCount; i++) {
//			tempPhreaseScore += 1 / (tempTerm[i].length()
//					* tempTerm[i].length() * Math.log(tempPhreaseScoreArray[i]));
//		}
//		if(tokenCount==1)
//			return tempTerm[0].length()*tempPhreaseScore;
//		else
			return tempPhreaseScore / (1.0 * tokenCount);
	}

	private String getToken(String token, Integer minFrequency) {
		candidateFrease.clear();
		tempStr.setLength(0);
		for (i1 = 0; i1 < token.length(); i1++) {
			i2 = 0;
			i3 = 0;
			i4 = 0;
			i5 = 0;
			i6 = 0;
			tempTerm[0].setLength(0);
			tempTerm[1].setLength(0);
			tempTerm[2].setLength(0);
			tempTerm[3].setLength(0);
			tempTerm[4].setLength(0);
			tempTerm[5].setLength(0);
			tempPhreaseScoreArray[0] = 0;
			tempPhreaseScoreArray[1] = 0;
			tempPhreaseScoreArray[2] = 0;
			tempPhreaseScoreArray[3] = 0;
			tempPhreaseScoreArray[4] = 0;
			tempPhreaseScoreArray[5] = 0;
			tempStartIndex = 0;
			tempEndIndex = i1 + 1;
			tempString = token.substring(tempStartIndex, tempEndIndex);
			if (this.tokensDistribution.containsKey(tempString))
				if (this.tokensDistribution.get(tempString) >= minFrequency) {
					tempTerm[0].append(tempString);
					tempPhreaseScoreArray[0] = this.tokensDistribution.get(tempString);
					if (i1 == token.length() - 1) {
						tempStr.setLength(0);
						tempStr.append(tempTerm[0].toString());
						// tempPhreaseScore=tempTerm[0].length()*tempPhreaseScoreArray[0];
						candidateFrease.add(new KeyValue<String, Double>(
								tempStr.toString(), getPhreaseScore(1)));
						continue;
					}
					for (i2 = i1 + 1; i2 < token.length(); i2++) {
						tempStr.setLength(0);
						tempTerm[1].setLength(0);
						tempTerm[2].setLength(0);
						tempTerm[3].setLength(0);
						tempTerm[4].setLength(0);
						tempTerm[5].setLength(0);
						tempPhreaseScoreArray[1] = 0;
						tempPhreaseScoreArray[2] = 0;
						tempPhreaseScoreArray[3] = 0;
						tempPhreaseScoreArray[4] = 0;
						tempPhreaseScoreArray[5] = 0;
						tempStartIndex = i1 + 1;
						tempEndIndex = i2 + 1;
						tempString = token.substring(tempStartIndex,
								tempEndIndex);
						if (this.tokensDistribution.containsKey(tempString))
							if (this.tokensDistribution.get(tempString) >= minFrequency) {
								tempTerm[1].append(tempString);
								tempPhreaseScoreArray[1] = this.tokensDistribution
										.get(tempString);
								if (i2 == token.length() - 1) {
									tempStr.setLength(0);
									tempStr.append(tempTerm[0].toString());
									tempStr.append(' ');
									tempStr.append(tempTerm[1].toString());
									// tempPhreaseScore=tempTerm[0].length()*tempPhreaseScoreArray[0]+tempTerm[1].length()*tempPhreaseScoreArray[1];
									candidateFrease
											.add(new KeyValue<String, Double>(
													tempStr.toString(),
													getPhreaseScore(2)));
									continue;
								}
								for (i3 = i2 + 1; i3 < token.length(); i3++) {
									tempStr.setLength(0);
									tempTerm[2].setLength(0);
									tempTerm[3].setLength(0);
									tempTerm[4].setLength(0);
									tempTerm[5].setLength(0);
									tempPhreaseScoreArray[2] = 0;
									tempPhreaseScoreArray[3] = 0;
									tempPhreaseScoreArray[4] = 0;
									tempPhreaseScoreArray[5] = 0;
									tempStartIndex = i2 + 1;
									tempEndIndex = i3 + 1;
									tempString = token.substring(
											tempStartIndex, tempEndIndex);
									if (this.tokensDistribution
											.containsKey(tempString))
										if (this.tokensDistribution.get(tempString) >= minFrequency) {
											tempTerm[2].append(tempString);
											tempPhreaseScoreArray[2] = this.tokensDistribution
													.get(tempString);
											if (i3 == token.length() - 1) {
												tempStr.setLength(0);
												tempStr.append(tempTerm[0]
														.toString());
												tempStr.append(' ');
												tempStr.append(tempTerm[1]
														.toString());
												tempStr.append(' ');
												tempStr.append(tempTerm[2]
														.toString());
												// tempPhreaseScore=tempTerm[0].length()*tempPhreaseScoreArray[0]+tempTerm[1].length()*tempPhreaseScoreArray[1]+tempTerm[2].length()*tempPhreaseScoreArray[2];
												candidateFrease
														.add(new KeyValue<String, Double>(
																tempStr.toString(),
																getPhreaseScore(3)));
												continue;
											}
											for (i4 = i3 + 1; i4 < token
													.length(); i4++) {
												tempTerm[3].setLength(0);
												tempTerm[4].setLength(0);
												tempTerm[5].setLength(0);
												tempPhreaseScoreArray[3] = 0;
												tempPhreaseScoreArray[4] = 0;
												tempPhreaseScoreArray[5] = 0;
												tempStartIndex = i3 + 1;
												tempEndIndex = i4 + 1;
												tempString = token.substring(
														tempStartIndex,
														tempEndIndex);
												if (this.tokensDistribution
														.containsKey(tempString))
													if (this.tokensDistribution
															.get(tempString) >= minFrequency) {
														tempTerm[3]
																.append(tempString);
														tempPhreaseScoreArray[3] = this.tokensDistribution
																.get(tempString);
														if (i4 == token
																.length() - 1) {
															tempStr.setLength(0);
															tempStr.setLength(0);
															tempStr.append(tempTerm[0]
																	.toString());
															tempStr.append(' ');
															tempStr.append(tempTerm[1]
																	.toString());
															tempStr.append(' ');
															tempStr.append(tempTerm[2]
																	.toString());
															tempStr.append(' ');
															tempStr.append(tempTerm[3]
																	.toString());
															// tempPhreaseScore=tempTerm[0].length()*tempPhreaseScoreArray[0]+tempTerm[1].length()*tempPhreaseScoreArray[1]+tempTerm[2].length()*tempPhreaseScoreArray[2];
															// tempPhreaseScore+=tempTerm[3].length()*tempPhreaseScoreArray[3];
															candidateFrease
																	.add(new KeyValue<String, Double>(
																			tempStr.toString(),
																			getPhreaseScore(4)));
															continue;
														}
														for (i5 = i4 + 1; i5 < token
																.length(); i5++) {
															tempTerm[4]
																	.setLength(0);
															tempTerm[5]
																	.setLength(0);
															tempPhreaseScoreArray[4] = 0;
															tempPhreaseScoreArray[5] = 0;
															tempStartIndex = i4 + 1;
															tempEndIndex = i5 + 1;
															tempString = token
																	.substring(
																			tempStartIndex,
																			tempEndIndex);
															if (this.tokensDistribution
																	.containsKey(tempString))
																if (this.tokensDistribution
																		.get(tempString) >= minFrequency) {
																	tempTerm[4]
																			.append(tempString);
																	tempPhreaseScoreArray[4] = this.tokensDistribution
																			.get(tempString);
																	if (i5 == token
																			.length() - 1) {
																		tempStr.setLength(0);
																		tempStr.append(tempTerm[0]
																				.toString());
																		tempStr.append(' ');
																		tempStr.append(tempTerm[1]
																				.toString());
																		tempStr.append(' ');
																		tempStr.append(tempTerm[2]
																				.toString());
																		tempStr.append(' ');
																		tempStr.append(tempTerm[3]
																				.toString());
																		tempStr.append(' ');
																		tempStr.append(tempTerm[4]
																				.toString());
																		// tempPhreaseScore=tempTerm[0].length()*tempPhreaseScoreArray[0]+tempTerm[1].length()*tempPhreaseScoreArray[1]+tempTerm[2].length()*tempPhreaseScoreArray[2];
																		// tempPhreaseScore+=tempTerm[3].length()*tempPhreaseScoreArray[3]+tempTerm[4].length()*tempPhreaseScoreArray[4];
																		candidateFrease
																				.add(new KeyValue<String, Double>(
																						tempStr.toString(),
																						getPhreaseScore(5)));
																		continue;
																	}
																	for (i6 = i5 + 1; i6 < token
																			.length(); i6++) {
																		tempTerm[5]
																				.setLength(0);
																		tempPhreaseScoreArray[5] = 0;
																		tempStartIndex = i5 + 1;
																		tempEndIndex = i6 + 1;
																		tempString = token
																				.substring(
																						tempStartIndex,
																						tempEndIndex);
																		if (this.tokensDistribution
																				.containsKey(tempString))
																			if (this.tokensDistribution
																					.get(tempString) >= minFrequency) {
																				tempTerm[5]
																						.append(tempString);
																				tempPhreaseScoreArray[5] = this.tokensDistribution
																						.get(tempString);
																				if (i6 == token
																						.length() - 1) {
																					tempStr.setLength(0);
																					tempStr.append(tempTerm[0]
																							.toString());
																					tempStr.append(' ');
																					tempStr.append(tempTerm[1]
																							.toString());
																					tempStr.append(' ');
																					tempStr.append(tempTerm[2]
																							.toString());
																					tempStr.append(' ');
																					tempStr.append(tempTerm[3]
																							.toString());
																					tempStr.append(' ');
																					tempStr.append(tempTerm[4]
																							.toString());
																					tempStr.append(' ');
																					tempStr.append(tempTerm[5]
																							.toString());
																					// tempPhreaseScore=tempTerm[0].length()*tempPhreaseScoreArray[0]+tempTerm[1].length()*tempTerm[2].length()*tempPhreaseScoreArray[1]+tempPhreaseScoreArray[2];
																					// tempPhreaseScore+=tempTerm[3].length()*tempPhreaseScoreArray[3]+tempTerm[4].length()*tempPhreaseScoreArray[4]+tempTerm[5].length()*tempPhreaseScoreArray[5];
																					candidateFrease
																							.add(new KeyValue<String, Double>(
																									tempStr.toString(),
																									getPhreaseScore(6)));
																					continue;
																				}
																			}
																	}
																}
														}
													}
											}
										}
								}
							}
					}
				}
		}
		double maxPhreaseScore = 0;
		String bestPhrease = token;
		for (KeyValue<String, Double> keyValue : candidateFrease) {
//			 System.out.println(keyValue.getKeyValue(" -> "));
			if (keyValue.getValue() > maxPhreaseScore) {
				maxPhreaseScore = keyValue.getValue();
				bestPhrease = keyValue.getKey();
			}
		}

		if(bestPhrease.indexOf(token)==0){
			if(tokenLengthBoundry)
			if(token.length()>25)
				return "";
			else if(this.tokensDistribution.containsKey(token)){
				if(this.tokensDistribution.get(token)<minFrequency)
				return "";
			}
		}
		
		return bestPhrease;
	}

	boolean checkNumber(String token) {
		for (int i = 0; i < token.length(); i++) {
			if (token.charAt(i) < '0' || token.charAt(i) > '9')
				if (token.charAt(i) != '.')
					return false;
		}
		return true;
	}
}
