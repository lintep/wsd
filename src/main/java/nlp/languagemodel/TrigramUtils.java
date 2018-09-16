package nlp.languagemodel;

import com.carrotsearch.hppc.LongDoubleOpenHashMap;
import com.carrotsearch.hppc.LongIntOpenHashMap;
import com.carrotsearch.hppc.LongLongOpenHashMap;
import com.carrotsearch.hppc.LongOpenHashSet;
import com.carrotsearch.hppc.cursors.LongCursor;
import com.carrotsearch.hppc.cursors.LongLongCursor;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import nlp.languagemodel.filter.TrigramPosFilter;
import nlp.preprocess.NormalizerTokenizerInterface;
import tools.util.BitCodec;
import tools.util.BitCodec.ThreeInt;
import tools.util.file.SparkTextReader;
import tools.util.file.TextReader;
import tools.util.file.Write.HashMapWriteType;
import tools.util.sort.Collection.SortType;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Saeed on 1/23/15.
 */
public class TrigramUtils {

	final int BITCOUNT = 21;
	int MAXTOKENCOUNT = (int) Math.round(Math.pow(2.0, BITCOUNT)) - 2;
	final int ILEGALTOKENID = MAXTOKENCOUNT + 1;
	final int DOCUMETDISTICTTOKENCOUNT = 100;
	NormalizerTokenizerInterface normalizerTokenizer;
	LongIntOpenHashMap tokenHashToId;
	HashMap<Integer, String> tokenidToToken;
	HashFunction murmur3_128 = Hashing.murmur3_128();
	BitCodec bitCodec;

	boolean writeToMap = false;

	LongOpenHashSet legalCoOccuranceCodeSet;

	LongLongOpenHashMap legalTrigramCoOccuranceCodeMap;
	LongLongOpenHashMap legalBigramCoOccuranceCodeMap;

	LongLongOpenHashMap dfCodeCount;
	LongLongOpenHashMap coCodeCount;

	// temp variable
	LongLongOpenHashMap tempLongCodeCount1 = new LongLongOpenHashMap(100000);
	LongLongOpenHashMap tempLongCodeCount2 = new LongLongOpenHashMap(100000);
	LongDoubleOpenHashMap tempLongDoubleCodeScore = new LongDoubleOpenHashMap(
			100000);
	LongOpenHashSet tempLongOpenHashSet = new LongOpenHashSet(100000);
	HashSet<Long> tempLongHashSet = new HashSet<Long>(10000);
	private double EPS = 0.00001;

	public static TrigramUtils newTrigramUtils(String legalTokenFileAddress,
                                               String dfFileAddress, String coFileAddress, int size,
                                               NormalizerTokenizerInterface normalizerTokenizer) throws Exception {
		TrigramUtils trigramUtils = new TrigramUtils(legalTokenFileAddress,
				normalizerTokenizer);

		trigramUtils.dfCodeCount = new LongLongOpenHashMap(size);

		trigramUtils.coCodeCount = new LongLongOpenHashMap(size);

		tools.util.Time.setStartTimeForNow();
		TextReader textReaderDf = new TextReader(
				dfFileAddress);
		int counter = 0;
		while (textReaderDf.hasNext()) {
			String[] split = textReaderDf.next().split("\t");
			trigramUtils.dfCodeCount.put(Long.parseLong(split[0]),
					Long.parseLong(split[1]));
			counter++;
			if (counter % 100000 == 0)
				System.out.println("loading... " + counter + " df loaded.");
		}
		textReaderDf.close();
		System.out.println("legal Df CodeMap load complete with "
				+ trigramUtils.dfCodeCount.size() + " (duplicateCode:"
				+ (counter - trigramUtils.dfCodeCount.size()) + ") count in "
				+ tools.util.Time.getTimeLengthForNow() + " ms.");

		tools.util.Time.setStartTimeForNow();
		TextReader textReaderCo = new TextReader(
				coFileAddress);
		counter = 0;
		while (textReaderCo.hasNext()) {
			String[] split = textReaderCo.next().split("\t");
			trigramUtils.coCodeCount.put(Long.parseLong(split[0]),
					Long.parseLong(split[1]));
			counter++;
			if (counter % 100000 == 0)
				System.out.println("loading... " + counter + " co loaded.");
		}
		textReaderCo.close();
		System.out.println("legal Co CodeMap load complete with "
				+ trigramUtils.dfCodeCount.size() + " (duplicateCode:"
				+ (counter - trigramUtils.coCodeCount.size()) + ") count in "
				+ tools.util.Time.getTimeLengthForNow() + " ms.");

		return trigramUtils;
	}

	/**
	 * 
//	 * @param legalTokenFileAddress
//	 * @param legalTrigramCodeCountFileAddress
	 * @param normalizerTokenizer
	 * @throws Exception
	 */
	public TrigramUtils(BufferedReader legalTokenBufferedReader,
                        BufferedReader legalBigramCodeCountBufferedReader,
                        int legalBigramCoOccuranceCodeSize,
                        BufferedReader legalTrigramCodeCountBufferedReader,
                        int legalTrigramCoOccuranceCodeSize,
                        NormalizerTokenizerInterface normalizerTokenizer) throws Exception {
		this(legalTokenBufferedReader, normalizerTokenizer);

		this.writeToMap = true;

		int counter = 0;

		if (legalBigramCodeCountBufferedReader != null
				&& legalBigramCoOccuranceCodeSize != 0) {
			// load legal bigram co-occurance code
			this.legalBigramCoOccuranceCodeMap = new LongLongOpenHashMap(
					legalBigramCoOccuranceCodeSize);

			tools.util.Time.setStartTimeForNow();
			TextReader textReaderBigram = new TextReader(
					legalBigramCodeCountBufferedReader);
			counter = 0;
			while (textReaderBigram.hasNext()) {
				String[] split = textReaderBigram.next().split("\t");
				ThreeInt decode = bitCodec.decode(Long.parseLong(split[0]),
						BITCOUNT);
				this.legalBigramCoOccuranceCodeMap.put(
						decode.getDistinctSortedCode(BITCOUNT), 0);
				counter++;
				if (counter > legalBigramCoOccuranceCodeSize)
					break;
				if (counter % 100000 == 0)
					System.out.println("loading... " + counter
							+ " legalBigramCoOccuranceCodeMap loaded.");
			}
			textReaderBigram.close();
			System.out
					.println("legalBigramCoOccuranceCodeMap load complete with "
							+ this.legalBigramCoOccuranceCodeMap.size()
							+ " (duplicateCode:"
							+ (counter - this.legalBigramCoOccuranceCodeMap
									.size())
							+ ") count in "
							+ tools.util.Time.getTimeLengthForNow() + " ms.");
		}

		if (legalTrigramCodeCountBufferedReader != null
				&& legalTrigramCoOccuranceCodeSize != 0) {
			// load legal trigram co-occurance code
			this.legalTrigramCoOccuranceCodeMap = new LongLongOpenHashMap(
					legalTrigramCoOccuranceCodeSize);

			tools.util.Time.setStartTimeForNow();
			TextReader textReader = new TextReader(
					legalTrigramCodeCountBufferedReader);
			counter = 0;
			while (textReader.hasNext()) {
				String[] split = textReader.next().split("\t");
				ThreeInt decode = bitCodec.decode(Long.parseLong(split[0]),
						BITCOUNT);
				this.legalTrigramCoOccuranceCodeMap.put(
						decode.getDistinctSortedCode(BITCOUNT), 0);
				counter++;
				if (counter > legalTrigramCoOccuranceCodeSize)
					break;
				if (counter % 100000 == 0)
					System.out.println("loading... " + counter
							+ " legalTrigramCoOccuranceCodeMap loaded.");
			}
			textReader.close();
			System.out
					.println("legalTrigramCoOccuranceCodeMap load complete with "
							+ this.legalTrigramCoOccuranceCodeMap.size()
							+ " (duplicateCode:"
							+ (counter - this.legalTrigramCoOccuranceCodeMap
									.size())
							+ ") count in "
							+ tools.util.Time.getTimeLengthForNow() + " ms.");
		}
	}

	/**
	 * 
//	 * @param legalTokenFileAddress
//	 * @param legalTrigramCodeCountFileAddress
	 * @param normalizerTokenizer
	 * @throws Exception
	 */
	public TrigramUtils(BufferedReader legalTokenBufferedReader,
                        BufferedReader legalTrigramCodeCountBufferedReader,
                        int legalCoOccuranceCodeSetSize, int minDf,
                        NormalizerTokenizerInterface normalizerTokenizer) throws Exception {
		this(legalTokenBufferedReader, normalizerTokenizer);

		// load legal co-occurance code
		this.legalCoOccuranceCodeSet = new LongOpenHashSet(
				legalCoOccuranceCodeSetSize);

		tools.util.Time.setStartTimeForNow();
		TextReader textReader = new TextReader(
				legalTrigramCodeCountBufferedReader);
		int counter = 0;
		int skippedCode = 0;
		while (textReader.hasNext()) {
			String[] split = textReader.next().split("\t");
			if (Long.parseLong(split[1]) < minDf) {
				skippedCode++;
				continue;
			}
			ThreeInt decode = bitCodec.decode(Long.parseLong(split[0]),
					BITCOUNT);
			this.legalCoOccuranceCodeSet.add(decode
					.getDistinctSortedCode(BITCOUNT));
			counter++;
			if (counter > legalCoOccuranceCodeSetSize)
				break;
			if (counter % 100000 == 0)
				System.out.println("loading... " + counter
						+ " legalCoOccuranceCodeSet loaded(skippedCode:"
						+ skippedCode + ").");
		}
		textReader.close();
		System.out.println("legalCoOccuranceCodeSet load complete with "
				+ this.legalCoOccuranceCodeSet.size() + " (skippedCode:"
				+ skippedCode + ") count in "
				+ tools.util.Time.getTimeLengthForNow() + " ms.");

	}

	/**
	 * 
//	 * @param legalTokenFileAddress
//	 * @param legalTrigramCodeCountFileAddress
	 * @param normalizerTokenizer
	 * @throws Exception
	 */
	public TrigramUtils(BufferedReader legalTokenBufferedReader,
                        LongOpenHashSet legalCoOccuranceCodeSet,
                        NormalizerTokenizerInterface normalizerTokenizer) throws Exception {
		this(legalTokenBufferedReader, normalizerTokenizer);
		this.legalCoOccuranceCodeSet = legalCoOccuranceCodeSet;
		System.out.println("legalCoOccuranceCodeSet load complete with "
				+ this.legalCoOccuranceCodeSet.size() + " count in "
				+ tools.util.Time.getTimeLengthForNow() + " ms.");
	}

	/**
	 * 
	 * @param legalTokenFileAddress
	 * @param normalizerTokenizer
	 * @throws IOException
	 */
	public TrigramUtils(BufferedReader legalTokenFileAddress,
                        NormalizerTokenizerInterface normalizerTokenizer)
			throws IOException {
		this.tokenHashToId = new LongIntOpenHashMap(MAXTOKENCOUNT);
		this.tokenidToToken = new HashMap<Integer, String>(MAXTOKENCOUNT);
		this.normalizerTokenizer = normalizerTokenizer;
		TextReader textReader = new TextReader(
				legalTokenFileAddress);
		int lineNumber = 0;
		while (textReader.hasNext()) {
			lineNumber++;
			String line=textReader.next();
			String token= SparkTextReader.isInSparkFormat(line)? SparkTextReader.getLineValue(line).getValue2():line.split("\t")[1];
			token = this.normalizerTokenizer.normalize(
					token).replace("(\\s)+",
					"");
			if (lineNumber <= MAXTOKENCOUNT) {
				long tokenHashId = this.murmur3_128.hashString(token,Charset.forName("UTF-8")).asLong();
				if (!this.tokenHashToId.containsKey(tokenHashId)) {
					this.tokenHashToId.addTo(tokenHashId, lineNumber);
					this.tokenidToToken.put(lineNumber, token);
				}
			}
			if (lineNumber%1000000==0)
				System.out.println(lineNumber + " lines handled.");
		}
		textReader.close();
		this.bitCodec = new BitCodec();
		System.out.println("Trigram load complete with " + tokenHashToId.size()
				+ " legal tokens.");
		if (lineNumber > tokenHashToId.size()) {
			int duplicateTokenCount = lineNumber - tokenHashToId.size();
			System.out.println("Duplicate token count: " + duplicateTokenCount);
		}
		if (lineNumber > MAXTOKENCOUNT) {
			int skippedTokenCount = lineNumber - MAXTOKENCOUNT;
			System.out.println("Tokens after line " + MAXTOKENCOUNT
					+ " skipped(" + skippedTokenCount + ")");
		}
	}

	/**
	 *
	 * @param legalTokenFileAddress
	 * @param normalizerTokenizer
	 * @throws IOException
	 */
	public TrigramUtils(String legalTokenFileAddress,
                        NormalizerTokenizerInterface normalizerTokenizer)
			throws IOException {
		this(tools.util.file.Reader.getFileBufferReader(legalTokenFileAddress),
				normalizerTokenizer);
	}

	// / get DF methods
	private void getStringUnigramBigramTrigramCodeCountToLongHashSet(
			String string, HashSet<Long> hashSet) throws Exception {
		List<String> tokens = this.normalizerTokenizer
				.tokenize(this.normalizerTokenizer.normalize(string));
		// get unigrams
		for (int i = 0; i < tokens.size(); i++) {
			long tokenId1 = getTokenId(tokens.get(i));
			if (tokenId1 == ILEGALTOKENID)
				continue;
			hashSet.add(this.bitCodec.encode(0, 0, tokenId1, BITCOUNT));
		}

		// get bigrams
		for (int i = 0; i < tokens.size() - 1; i++) {
			long tokenId1 = getTokenId(tokens.get(i));
			if (tokenId1 == ILEGALTOKENID)
				continue;
			long tokenId2 = getTokenId(tokens.get(i + 1));
			if (tokenId2 == ILEGALTOKENID)
				continue;
			hashSet.add(this.bitCodec.encode(0, tokenId1, tokenId2, BITCOUNT));
		}

		// get trigrams
		for (int i = 0; i < tokens.size() - 2; i++) {
			long tokenId1 = getTokenId(tokens.get(i));
			if (tokenId1 == ILEGALTOKENID)
				continue;
			long tokenId2 = getTokenId(tokens.get(i + 1));
			if (tokenId2 == ILEGALTOKENID)
				continue;
			long tokenId3 = getTokenId(tokens.get(i + 2));
			if (tokenId3 == ILEGALTOKENID)
				continue;
			hashSet.add(this.bitCodec.encode(tokenId1, tokenId2, tokenId3,
					BITCOUNT));
		}
	}

	/**
	 *
	 * @param string
	 * @param longCodeOpenHashSet
	 * @throws Exception
	 */
	private void getStringUnigramBigramTrigramCodeCountToLongOpenHashSet(
			String string, LongOpenHashSet longCodeOpenHashSet)
			throws Exception {
		List<String> tokens = this.normalizerTokenizer
				.tokenize(this.normalizerTokenizer.normalize(string));

		// get unigrams
		for (int i = 0; i < tokens.size(); i++) {
			long tokenId1 = getTokenId(tokens.get(i));
			if (tokenId1 == ILEGALTOKENID)
				continue;
			longCodeOpenHashSet.add(this.bitCodec.encode(0, 0, tokenId1,
					BITCOUNT));
		}

		// get bigrams
		for (int i = 0; i < tokens.size() - 1; i++) {
			long tokenId1 = getTokenId(tokens.get(i));
			if (tokenId1 == ILEGALTOKENID)
				continue;
			long tokenId2 = getTokenId(tokens.get(i + 1));
			if (tokenId2 == ILEGALTOKENID)
				continue;
			longCodeOpenHashSet.add(this.bitCodec.encode(0, tokenId1, tokenId2,
					BITCOUNT));
		}

		// get trigrams
		for (int i = 0; i < tokens.size() - 2; i++) {
			long tokenId1 = getTokenId(tokens.get(i));
			if (tokenId1 == ILEGALTOKENID)
				continue;
			long tokenId2 = getTokenId(tokens.get(i + 1));
			if (tokenId2 == ILEGALTOKENID)
				continue;
			long tokenId3 = getTokenId(tokens.get(i + 2));
			if (tokenId3 == ILEGALTOKENID)
				continue;
			longCodeOpenHashSet.add(this.bitCodec.encode(tokenId1, tokenId2,
					tokenId3, BITCOUNT));
		}
	}

	/**
	 *
	 * @param inputfileAddress
	 * @param longCodeCountOpenHashMap
	 * @throws FileNotFoundException
	 * @throws Exception
	 * @throws IOException
	 */
	private void getFileUnigramBigramTrigramCodeAsDfToLongLongHashMap(
			String inputfileAddress,
			LongLongOpenHashMap longCodeCountOpenHashMap)
			throws FileNotFoundException, Exception, IOException {

		TextReader textReader = new TextReader(
				inputfileAddress);

		int j = 0;
		while (textReader.hasNext()) {
			String string = textReader.next();
			// tempLongOpenHashSet.clear();
			// getStringUnigramBigramTrigramCodeCountToLongOpenHashSet(string,
			// tempLongOpenHashSet);
			// for (LongCursor longLongCursor : tempLongOpenHashSet) {
			// addLongCodeToOpenHashMap(longLongCursor.value,
			// longCodeCountOpenHashMap);
			// }

			tempLongHashSet.clear();
			getStringUnigramBigramTrigramCodeCountToLongHashSet(string,
					tempLongHashSet);
			for (Long code : tempLongHashSet) {
				addLongCodeToOpenHashMap(code, longCodeCountOpenHashMap);
			}

			j++;
			if (j % 10000 == 0)
				System.out.println("FileUnigramBigramTrigramCodeAsDF " + j
						+ " lines handled with " + tempLongCodeCount1.size()
						+ " distict count.");
		}
		textReader.close();
	}

	/**
	 * 
	 * @param inputText
	 * @return
	 * @throws Exception
	 */
	public long[] getUnigramBigramTrigramCode(String inputText)
			throws Exception {
		tempLongOpenHashSet.clear();
		getStringUnigramBigramTrigramCodeCountToLongOpenHashSet(inputText,
				tempLongOpenHashSet);
		return tempLongOpenHashSet.keys;
	}

	/**
	 * 
	 * @param inputText
	 * @throws Exception
	 */
	public void writeUnigramBigramTrigramCodeAsDF(String inputText)
			throws Exception {
		// ///////// implementation 1
		// tempLongOpenHashSet.clear();
		// getStringUnigramBigramTrigramCodeCountToLongOpenHashSet(inputText,
		// tempLongOpenHashSet);
		// writeLongLongOpenHashSet(tempLongOpenHashSet);

		// ///////// implementation 2
		tempLongHashSet.clear();
		getStringUnigramBigramTrigramCodeCountToLongHashSet(inputText,
				tempLongHashSet);
		for (Long code : tempLongHashSet) {
			write(code);
		}

	}

	/**
	 * 
	 * @param inputfileAddress
	 * @param outputFileAddress
	 * @throws Exception
	 */
	public void writeFileUnigramBigramTrigramCodeAsDF(String inputfileAddress,
			String outputFileAddress) throws Exception {
		tempLongCodeCount1.clear();
		getFileUnigramBigramTrigramCodeAsDfToLongLongHashMap(inputfileAddress,
				tempLongCodeCount1);
		int j = writeCountedHashMapToFiles(tempLongCodeCount1,
				outputFileAddress);
		System.out
				.println("write to FileUnigramBigramTrigramCodeAsDF  complete with "
						+ tempLongCodeCount1.size() + "[" + j + "] code.");
	}

	// / get TTF methods

	/**
	 * 
	 * @param string
	 * @param longCodeCountOpenHashMap
	 * @throws Exception
	 */
	private void getStringUnigramBigramTrigramTtfToLongLongHashMap(
			String string, LongLongOpenHashMap longCodeCountOpenHashMap)
			throws Exception {
		List<String> tokens = this.normalizerTokenizer
				.tokenize(this.normalizerTokenizer.normalize(string));

		// get unigrams
		for (int i = 0; i < tokens.size(); i++) {
			long tokenId1 = getTokenId(tokens.get(i));
			if (tokenId1 == ILEGALTOKENID)
				continue;
			addLongCodeToOpenHashMap(
					this.bitCodec.encode(0, 0, tokenId1, BITCOUNT),
					longCodeCountOpenHashMap);
		}

		// get bigrams
		for (int i = 0; i < tokens.size() - 1; i++) {
			long tokenId1 = getTokenId(tokens.get(i));
			if (tokenId1 == ILEGALTOKENID)
				continue;
			long tokenId2 = getTokenId(tokens.get(i + 1));
			if (tokenId2 == ILEGALTOKENID)
				continue;
			addLongCodeToOpenHashMap(
					this.bitCodec.encode(0, tokenId1, tokenId2, BITCOUNT),
					longCodeCountOpenHashMap);
		}

		// get trigrams
		for (int i = 0; i < tokens.size() - 2; i++) {
			long tokenId1 = getTokenId(tokens.get(i));
			if (tokenId1 == ILEGALTOKENID)
				continue;
			long tokenId2 = getTokenId(tokens.get(i + 1));
			if (tokenId2 == ILEGALTOKENID)
				continue;
			long tokenId3 = getTokenId(tokens.get(i + 2));
			if (tokenId3 == ILEGALTOKENID)
				continue;
			addLongCodeToOpenHashMap(this.bitCodec.encode(tokenId1, tokenId2,
					tokenId3, BITCOUNT), longCodeCountOpenHashMap);
		}
	}

	/**
	 * 
	 * @param inputText
	 * @return
	 * @throws Exception
	 */
	public HashMap<Long, Long> getUnigramBigramTrigramCodeAsTTF(String inputText)
			throws Exception {
		HashMap<Long, Long> result = new HashMap<Long, Long>();
		this.tempLongCodeCount1.clear();
		getStringUnigramBigramTrigramTtfToLongLongHashMap(inputText,
				this.tempLongCodeCount1);
		for (LongLongCursor codeTTf : this.tempLongCodeCount1) {
			result.put(codeTTf.key, codeTTf.value);
		}
		return result;
	}

	/**
	 * 
	 * @param inputText
	 * @throws Exception
	 */
	public void writeUnigramBigramTrigramCodeAsTTF(String inputText)
			throws Exception {
		this.tempLongCodeCount1.clear();
		getStringUnigramBigramTrigramTtfToLongLongHashMap(inputText,
				this.tempLongCodeCount1);
		for (LongLongCursor codeTtf : this.tempLongCodeCount1) {
			write(codeTtf.key, codeTtf.value);
		}
	}

	// / get CO methods

	/**
	 * 
	 * @param string
//	 * @param HashSet
	 *            <Long>
	 * @throws Exception
	 */
	private void getStringAllCoOccuranceCodeCountToLongHashSet(String string,
			HashSet<Long> longCodeHashSet) throws Exception {
		int[] distinctSortedToeknsId = getDistinctSortedToeknsId(this.normalizerTokenizer
				.tokenize(this.normalizerTokenizer.normalize(string)));

		// get bi co-occurrence code
		for (int i = 0; i < distinctSortedToeknsId.length; i++) {
			for (int j = i + 1; j < distinctSortedToeknsId.length; j++) {
				longCodeHashSet.add(this.bitCodec.encode(0,
						distinctSortedToeknsId[i], distinctSortedToeknsId[j],
						BITCOUNT));
			}
		}

		// get tri co-occurrence code
		for (int i = 0; i < distinctSortedToeknsId.length; i++) {
			for (int j = i + 1; j < distinctSortedToeknsId.length; j++) {
				for (int k = j + 1; k < distinctSortedToeknsId.length; k++) {
					longCodeHashSet.add(this.bitCodec.encode(
							distinctSortedToeknsId[i],
							distinctSortedToeknsId[j],
							distinctSortedToeknsId[k], BITCOUNT));
				}
			}
		}
	}

	/**
	 * 
	 * @param string
//	 * @param HashSet
	 *            <Long>
	 * @throws Exception
	 */
	public void writeStringCoOccuranceCodeCount(String string) throws Exception {
		int[] distinctSortedToeknsId = getDistinctSortedToeknsId(this.normalizerTokenizer
				.tokenize(this.normalizerTokenizer.normalize(string)));

		long code;
		// get bi co-occurrence code
		for (int i = 0; i < distinctSortedToeknsId.length; i++) {
			for (int j = i + 1; j < distinctSortedToeknsId.length; j++) {
				code = this.bitCodec.encode(0, distinctSortedToeknsId[i],
						distinctSortedToeknsId[j], BITCOUNT);
				if (this.legalCoOccuranceCodeSet.contains(code)) {
					write(code);
				}
			}
		}

		// get tri co-occurrence code
		for (int i = 0; i < distinctSortedToeknsId.length; i++) {
			for (int j = i + 1; j < distinctSortedToeknsId.length; j++) {
				for (int k = j + 1; k < distinctSortedToeknsId.length; k++) {
					code = this.bitCodec.encode(distinctSortedToeknsId[i],
							distinctSortedToeknsId[j],
							distinctSortedToeknsId[k], BITCOUNT);
					if (this.legalCoOccuranceCodeSet.contains(code)) {
						write(code);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param string
//	 * @param HashSet
	 *            <Long>
	 * @throws Exception
	 */
	public void writeStringCoOccuranceCodeCountToMap(String string,
			int windowSize) throws Exception {

		if (!this.writeToMap)
			throw new Exception("Map not loaded");

		List<String> tokens = this.normalizerTokenizer
				.tokenize(this.normalizerTokenizer.normalize(string));

		bufferBigramCoOcuranceCountSet.clear();
		bufferTrigramCoOcuranceCountSet.clear();
		if (windowSize >= tokens.size()) {
			int[] distinctSortedToeknsId = getDistinctSortedToeknsId(tokens);
			if (this.legalBigramCoOccuranceCodeMap != null) {
				addStringBigramCoOccuranceCountToBufferSet(distinctSortedToeknsId);
				writeBufferBigramCoOcuranceCountSetToMapAndClearThat();
			}

			if (this.legalTrigramCoOccuranceCodeMap != null) {
				addStringTrigramCoOccuranceCountToBufferSet(distinctSortedToeknsId);
				writeBufferTrigramCoOcuranceCountSetToMapAndClearThat();
			}

		} else {

			ArrayList<String> tempTokens = new ArrayList<String>();

			for (int i = 0; i < tokens.size() - windowSize; i++) {

				tempTokens.clear();

				for (int j = i; j < (windowSize); j++) {
					tempTokens.add(tokens.get(j));
				}

				int[] distinctSortedToeknsId = getDistinctSortedToeknsId(tempTokens);

				if (this.legalBigramCoOccuranceCodeMap != null) {
					addStringBigramCoOccuranceCountToBufferSet(distinctSortedToeknsId);
				}

				if (this.legalTrigramCoOccuranceCodeMap != null) {
					addStringTrigramCoOccuranceCountToBufferSet(distinctSortedToeknsId);
				}
			}

			writeBufferBigramCoOcuranceCountSetToMapAndClearThat();
			writeBufferTrigramCoOcuranceCountSetToMapAndClearThat();
		}

	}

	// /**
	// *
	// * @param string
	// * @param HashSet
	// * <Long>
	// * @throws Exception
	// */
	// public void writeStringBigramCoOccuranceCodeCountToMap(String string)
	// throws Exception {
	//
	// if (!this.writeToMap)
	// throw new Exception("Map not loaded");
	//
	// int[] distinctSortedToeknsId =
	// getDistinctSortedToeknsId(this.normalizerTokenizer
	// .tokenize(this.normalizerTokenizer.normalize(string)));
	//
	// writeStringBigramCoOccuranceCountToMap(distinctSortedToeknsId);
	//
	// }

	// /**
	// *
	// * @param string
	// * @param HashSet
	// * <Long>
	// * @throws Exception
	// */
	// public void writeStringTrigramCoOccuranceCodeCountToMap(String string)
	// throws Exception {
	//
	// if (!this.writeToMap)
	// throw new Exception("Map not loaded");
	//
	// int[] distinctSortedToeknsId =
	// getDistinctSortedToeknsId(this.normalizerTokenizer
	// .tokenize(this.normalizerTokenizer.normalize(string)));
	//
	// writeStringTrigramCoOccuranceCountToMap(distinctSortedToeknsId);
	//
	// }

	// private void writeStringTrigramCoOccuranceCountToMap(
	// int[] distinctSortedToeknsId) throws Exception {
	// long code;
	// // get tri co-occurrence code
	// for (int i = 0; i < distinctSortedToeknsId.length; i++) {
	// for (int j = i + 1; j < distinctSortedToeknsId.length; j++) {
	// for (int k = j + 1; k < distinctSortedToeknsId.length; k++) {
	// code = this.bitCodec.encode(distinctSortedToeknsId[i],
	// distinctSortedToeknsId[j],
	// distinctSortedToeknsId[k], BITCOUNT);
	// if (this.legalTrigramCoOccuranceCodeMap.containsKey(code)) {
	// this.legalTrigramCoOccuranceCodeMap.addTo(code, 1);
	// }
	// }
	// }
	// }
	// }

	private void addStringTrigramCoOccuranceCountToBufferSet(
			int[] distinctSortedToeknsId) throws Exception {
		long code;
		// get tri co-occurrence code
		for (int i = 0; i < distinctSortedToeknsId.length; i++) {
			for (int j = i + 1; j < distinctSortedToeknsId.length; j++) {
				for (int k = j + 1; k < distinctSortedToeknsId.length; k++) {
					code = this.bitCodec.encode(distinctSortedToeknsId[i],
							distinctSortedToeknsId[j],
							distinctSortedToeknsId[k], BITCOUNT);
					if (this.legalTrigramCoOccuranceCodeMap.containsKey(code)) {
						this.bufferTrigramCoOcuranceCountSet.add(code);
					}
				}
			}
		}
	}

	private void writeBufferBigramCoOcuranceCountSetToMapAndClearThat() {
		for (long code : this.bufferBigramCoOcuranceCountSet) {
			this.legalBigramCoOccuranceCodeMap.addTo(code, 1);
		}
		this.bufferBigramCoOcuranceCountSet.clear();
	}

	private void writeBufferTrigramCoOcuranceCountSetToMapAndClearThat() {
		for (long code : this.bufferTrigramCoOcuranceCountSet) {
			this.legalTrigramCoOccuranceCodeMap.addTo(code, 1);
		}
		this.bufferTrigramCoOcuranceCountSet.clear();
	}

	// private void writeStringBigramCoOccuranceCountToMap(
	// int[] distinctSortedToeknsId) throws Exception {
	// long code;
	// // get bi co-occurrence code
	// for (int i = 0; i < distinctSortedToeknsId.length; i++) {
	// for (int j = i + 1; j < distinctSortedToeknsId.length; j++) {
	// code = this.bitCodec.encode(0, distinctSortedToeknsId[i],
	// distinctSortedToeknsId[j], BITCOUNT);
	// if (this.legalBigramCoOccuranceCodeMap.containsKey(code)) {
	// this.legalBigramCoOccuranceCodeMap.addTo(code, 1);
	// }
	// }
	// }
	// }

	// HashSet<Long> bufferCoOcuranceCountSet=new HashSet<Long>();

	HashSet<Long> bufferBigramCoOcuranceCountSet = new HashSet<Long>();
	HashSet<Long> bufferTrigramCoOcuranceCountSet = new HashSet<Long>();

	private void addStringBigramCoOccuranceCountToBufferSet(
			int[] distinctSortedToeknsId) throws Exception {
		long code;
		// get bi co-occurrence code
		for (int i = 0; i < distinctSortedToeknsId.length; i++) {
			for (int j = i + 1; j < distinctSortedToeknsId.length; j++) {
				code = this.bitCodec.encode(0, distinctSortedToeknsId[i],
						distinctSortedToeknsId[j], BITCOUNT);
				if (this.legalBigramCoOccuranceCodeMap.containsKey(code)) {
					this.bufferBigramCoOcuranceCountSet.add(code);
				}
			}
		}
	}

	/**
	 * 
	 * @param string
	 * @param longCodeOpenHashSet
	 * @throws Exception
	 */
	private void getStringAllCoOccuranceCodeCountToLongOpenHashSet(
			String string, LongOpenHashSet longCodeOpenHashSet)
			throws Exception {
		int[] distinctSortedToeknsId = getDistinctSortedToeknsId(this.normalizerTokenizer
				.tokenize(this.normalizerTokenizer.normalize(string)));

		// get bi co-occurrence code
		for (int i = 0; i < distinctSortedToeknsId.length; i++) {
			for (int j = i + 1; j < distinctSortedToeknsId.length; j++) {
				longCodeOpenHashSet.add(this.bitCodec.encode(0,
						distinctSortedToeknsId[i], distinctSortedToeknsId[j],
						BITCOUNT));
			}
		}

		// get tri co-occurrence code
		for (int i = 0; i < distinctSortedToeknsId.length; i++) {
			for (int j = i + 1; j < distinctSortedToeknsId.length; j++) {
				for (int k = j + 1; k < distinctSortedToeknsId.length; k++) {
					longCodeOpenHashSet.add(this.bitCodec.encode(
							distinctSortedToeknsId[i],
							distinctSortedToeknsId[j],
							distinctSortedToeknsId[k], BITCOUNT));
				}
			}
		}
	}

	/**
	 * 
	 * @param inputText
	 * @return
	 * @throws Exception
	 */
	public long[] getAllCoOccurrenceCode(String inputText) throws Exception {
		this.tempLongOpenHashSet.clear();
		getStringAllCoOccuranceCodeCountToLongOpenHashSet(inputText,
				this.tempLongOpenHashSet);
		return this.tempLongOpenHashSet.keys;
	}

	/**
	 * 
	 * @param inputText
	 * @throws Exception
	 */
	public void writeAllCoOccurrenceCode(String inputText) throws Exception {
		this.tempLongHashSet.clear();
		getStringAllCoOccuranceCodeCountToLongHashSet(inputText,
				this.tempLongHashSet);
		writeLongHashSet(this.tempLongHashSet);
	}

	/**
	 * 
	 * @param inputfileAddress
	 * @param legalTrigramsCodeCountFileAddress
	 * @param outputFileAddress
	 * @throws Exception
	 */
	public void writeFileCoOccurrenceCodeAsDF(String inputfileAddress,
			String legalTrigramsCodeCountFileAddress, String outputFileAddress)
			throws Exception {

		this.tempLongCodeCount1.clear();
		loadCodedFile(legalTrigramsCodeCountFileAddress,
				this.tempLongCodeCount1);

		writeFileCoOccurrenceCodeAsDF(inputfileAddress,
				this.tempLongCodeCount1, outputFileAddress);
	}

	/**
	 * 
	 * @param inputfileAddress
	 * @param legalTrigramsLongLongOpenHashMap
	 * @param outputFileAddress
	 * @throws Exception
	 */
	private void writeFileCoOccurrenceCodeAsDF(String inputfileAddress,
			LongLongOpenHashMap legalTrigramsLongLongOpenHashMap,
			String outputFileAddress) throws Exception {
		tools.util.Time.setStartTimeForNow();
		tempLongOpenHashSet.clear();
		for (long code : legalTrigramsLongLongOpenHashMap.keys) {
			ThreeInt decode = bitCodec.decode(code, BITCOUNT);
			tempLongOpenHashSet.add(decode.getDistinctSortedCode(BITCOUNT));
		}
		System.out.println("all code count : "
				+ legalTrigramsLongLongOpenHashMap.size());
		System.out.println("all code.getDistictSortedCode count : "
				+ tempLongOpenHashSet.size() + " in "
				+ tools.util.Time.getTimeLengthForNow() + " ms");
		writeFileCoOccurrenceCodeAsDF(inputfileAddress,
				this.tempLongOpenHashSet, outputFileAddress);
	}

	/**
	 * 
	 * @param inputfileAddress
	 * @param outputFileAddress
	 * @throws Exception
	 */
	private void writeFileCoOccurrenceCodeAsDF(String inputfileAddress,
			LongOpenHashSet legalTrigramsDistinctSortedCodelongOpenHashSet,
			String outputFileAddress) throws Exception {
		tools.util.Time.setStartTimeForNow();
		tempLongCodeCount2.clear();

		HashSet<Long> tempResult = new HashSet<Long>();

		TextReader textReader = new TextReader(
				inputfileAddress);
		int counter = 0;
		long code = 0;
		while (textReader.hasNext()) {
			tempResult.clear();
			counter++;
			int[] distinctSortedToeknsId = getDistinctSortedToeknsId(this.normalizerTokenizer
					.tokenize(this.normalizerTokenizer
							.normalize(textReader.next())));

			// get bi co-occurrence code
			for (int i = 0; i < distinctSortedToeknsId.length; i++) {
				for (int j = i + 1; j < distinctSortedToeknsId.length; j++) {
					code = this.bitCodec.encode(0, distinctSortedToeknsId[i],
							distinctSortedToeknsId[j], BITCOUNT);
					if (legalTrigramsDistinctSortedCodelongOpenHashSet
							.contains(code))
						tempResult.add(code);
				}
			}

			// get tri co-occurrence code
			for (int i = 0; i < distinctSortedToeknsId.length; i++) {
				for (int j = i + 1; j < distinctSortedToeknsId.length; j++) {
					for (int k = j + 1; k < distinctSortedToeknsId.length; k++) {
						code = this.bitCodec.encode(distinctSortedToeknsId[i],
								distinctSortedToeknsId[j],
								distinctSortedToeknsId[k], BITCOUNT);
						if (legalTrigramsDistinctSortedCodelongOpenHashSet
								.contains(code))
							tempResult.add(code);
					}
				}
			}

			for (Long distinctCode : tempResult) {
				addLongCodeToOpenHashMap(distinctCode, tempLongCodeCount2);
			}

			if (counter % 1000 == 0)
				System.out.println("FileCoOccurrenceCodeAsDF " + counter
						+ " lines handled with " + tempLongCodeCount2.size()
						+ " distict count.");
		}
		textReader.close();

		counter = writeCountedHashMapToFiles(tempLongCodeCount2,
				outputFileAddress);
		System.out.println("write to FileCoOccurrenceCodeAsDF  complete with "
				+ tempLongCodeCount2.size() + " code in "
				+ tools.util.Time.getTimeLengthForNow() + " ms.");
	}

	/**
	 * 
	 * @param longOpenHashSet
	 */
	private void writeLongLongOpenHashSet(LongOpenHashSet longOpenHashSet) {
		for (LongCursor code : longOpenHashSet) {
			write(code.value);
		}
	}

	/**
	 * 
//	 * @param longOpenHashSet
	 */
	private void writeLongHashSet(HashSet<Long> longHashSet) {
		for (long code : longHashSet) {
			write(code);
		}
	}

	/**
	 * 
	 * @param code
	 * @param longCodeCountOpenHashMap
	 */
	private void addLongCodeToOpenHashMap(long code,
			LongLongOpenHashMap longCodeCountOpenHashMap) {
		if (longCodeCountOpenHashMap.containsKey(code)) {
			longCodeCountOpenHashMap.put(code,
					longCodeCountOpenHashMap.get(code) + 1);
		} else {
			longCodeCountOpenHashMap.put(code, 1);
		}
	}

	public void decodeFile(String codedFileAddress) throws IOException {
		tempLongCodeCount1.clear();
		loadCodedFile(codedFileAddress, tempLongCodeCount1);
		PrintWriter printWriter = tools.util.file.Write.getPrintWriter(
				codedFileAddress + ".decoded", false);

		TextReader textReader = new TextReader(
				codedFileAddress);
		while (textReader.hasNext()) {
			String[] splites = textReader.next().split("\t");
			ThreeInt threeInt = bitCodec.decode(Long.parseLong(splites[0]),
					BITCOUNT);

			String triToken = getCodeToken(threeInt);

			printWriter.println(triToken + "\t" + splites[1]);
		}
		textReader.close();
		printWriter.close();
		System.out.println("decodeFile " + codedFileAddress + " complete.");
	}

	public void decodeFileResultSortedByValue(String codedFileAddress)
			throws IOException {
		tempLongCodeCount1.clear();
		loadCodedFile(codedFileAddress, tempLongCodeCount1);

		HashMap<String, Double> tempResult = new HashMap<String, Double>();

		TextReader textReader = new TextReader(
				codedFileAddress);
		while (textReader.hasNext()) {
			String[] splites = textReader.next().split("\t");
			ThreeInt threeInt = bitCodec.decode(Long.parseLong(splites[0]),
					BITCOUNT);

			String triToken = getCodeToken(threeInt);

			tempResult.put(triToken, Double.parseDouble(splites[1]));
		}
		textReader.close();
		tools.util.file.Write.mapToTextFileSortedByValue(tempResult,
				codedFileAddress + ".decoded", SortType.DECREMENTAL,
				HashMapWriteType.KEYVALUE, " ");
		System.out.println("decodeFile " + codedFileAddress + " complete.");
	}

	public String getCodeToken(long code) {
		return getCodeToken(this.bitCodec.decode(code, BITCOUNT));
	}

	private String getCodeToken(ThreeInt threeInt) {
		String triToken = "";
		if (this.tokenidToToken.containsKey(threeInt.getInt1())) {
			triToken += this.tokenidToToken.get(threeInt.getInt1()) + " ";
		} else {
			if (threeInt.getInt1() != 0)
				System.out.println(threeInt.getInt1());
		}

		if (this.tokenidToToken.containsKey(threeInt.getInt2())) {
			triToken += this.tokenidToToken.get(threeInt.getInt2()) + " ";
		} else {
			if (threeInt.getInt2() != 0)
				System.out.println(threeInt.getInt2());
		}
		triToken += this.tokenidToToken.get(threeInt.getInt3());
		return triToken;
	}

	private void loadCodedFile(String codedFileAddress,
			LongLongOpenHashMap longCodeCount) throws IOException {
		loadCodedFile(codedFileAddress, longCodeCount, 0);
	}

	private void loadCodedFile(String codedFileAddress,
			LongLongOpenHashMap longCodeCount, int limitDf) throws IOException {
		tools.util.Time.setStartTimeForNow();
		TextReader textReader = new TextReader(
				codedFileAddress);
		int i = 0;
		int r = 0;
		int errorCount = 0;
		while (textReader.hasNext()) {
			String[] splites = textReader.next().split("\t");

			if (splites.length < 2) {
				errorCount++;
				continue;
			}

			long df = Long.parseLong(splites[1]);
			if (df < limitDf) {
				r++;
				continue;
			}
			longCodeCount.put(Long.parseLong(splites[0]), df);
			i++;
			if (i % 100000 == 0)
				System.out.println("loading... " + i + " item loaded (skipped:"
						+ r + ") (longCodeCount:" + longCodeCount.size()
						+ ") (errorCount:" + errorCount + ") from \n"
						+ codedFileAddress);
		}
		textReader.close();
		System.out.println("load " + codedFileAddress + " complete in "
				+ tools.util.Time.getTimeLengthForNow() + " ms.");
	}

	private void loadCodedFile(String codedFileAddress,
			LongLongOpenHashMap longCodeCount, int minDf, int maxDf)
			throws IOException {
		tools.util.Time.setStartTimeForNow();
		TextReader textReader = new TextReader(
				codedFileAddress);
		int i = 0;
		int r = 0;
		int errorCount = 0;
		while (textReader.hasNext()) {
			String[] splites = textReader.next().split("\t");

			if (splites.length < 2) {
				errorCount++;
				continue;
			}

			long df = Long.parseLong(splites[1]);
			if (df <= minDf) {
				r++;
				continue;
			}
			if (df > maxDf) {
				r++;
				continue;
			}
			longCodeCount.put(Long.parseLong(splites[0]), df);
			i++;
			if (i % 100000 == 0)
				System.out.println("loading... " + i + " item loaded (skipped:"
						+ r + ") (longCodeCount:" + longCodeCount.size()
						+ ") (errorCount:" + errorCount + ") from \n"
						+ codedFileAddress);
		}
		textReader.close();
		System.out.println("load " + codedFileAddress + " complete in "
				+ tools.util.Time.getTimeLengthForNow() + " ms.");
	}

	// write temp variable
	private int writeCountedHashMapToFiles(LongLongOpenHashMap countedHashMap,
			String outputFileAddress) throws IOException {
		int j;
		PrintWriter printWriter = tools.util.file.Write.getPrintWriter(
				outputFileAddress, false);
		PrintWriter printWriter1 = tools.util.file.Write.getPrintWriter(
				outputFileAddress + ".1", false);
		PrintWriter printWriter2 = tools.util.file.Write.getPrintWriter(
				outputFileAddress + ".2", false);
		PrintWriter printWriter3 = tools.util.file.Write.getPrintWriter(
				outputFileAddress + ".3", false);
		PrintWriter printWriter4 = tools.util.file.Write.getPrintWriter(
				outputFileAddress + ".4", false);
		PrintWriter printWriter5 = tools.util.file.Write.getPrintWriter(
				outputFileAddress + ".5", false);
		PrintWriter printWriter10 = tools.util.file.Write.getPrintWriter(
				outputFileAddress + ".10", false);
		PrintWriter printWriter100 = tools.util.file.Write.getPrintWriter(
				outputFileAddress + ".100", false);
		PrintWriter printWriter1000 = tools.util.file.Write.getPrintWriter(
				outputFileAddress + ".1000", false);
		PrintWriter printWriterBiggerThan1000 = tools.util.file.Write
				.getPrintWriter(outputFileAddress + ".BiggerThan100", false);
		j = 0;
		for (LongLongCursor codeCount : countedHashMap) {
			printWriter.println(codeCount.key + "\t" + codeCount.value);

			if (codeCount.value == 1) {
				printWriter1.println(codeCount.key + "\t" + codeCount.value);
			} else if (codeCount.value == 2) {
				printWriter2.println(codeCount.key + "\t" + codeCount.value);
			} else if (codeCount.value == 3) {
				printWriter3.println(codeCount.key + "\t" + codeCount.value);
			} else if (codeCount.value == 4) {
				printWriter4.println(codeCount.key + "\t" + codeCount.value);
			} else if (codeCount.value == 5) {
				printWriter5.println(codeCount.key + "\t" + codeCount.value);
			} else if (codeCount.value <= 10) {
				printWriter10.println(codeCount.key + "\t" + codeCount.value);
			} else if (codeCount.value <= 100) {
				printWriter100.println(codeCount.key + "\t" + codeCount.value);
			} else if (codeCount.value <= 1000) {
				printWriter1000.println(codeCount.key + "\t" + codeCount.value);
			} else {
				printWriterBiggerThan1000.println(codeCount.key + "\t"
						+ codeCount.value);
			}

			j++;
			if (j % 10000 == 0)
				System.out.println("write to FileUnigramBigramTrigramCodeAsDF "
						+ j + " code writed.");
		}
		printWriter.close();
		printWriter1.close();
		printWriter2.close();
		printWriter3.close();
		printWriter4.close();
		printWriter5.close();
		printWriter10.close();
		printWriter100.close();
		printWriter1000.close();
		printWriterBiggerThan1000.close();
		return j;
	}

	protected void write(long code, long count) {
		System.out.println(code + "\t" + count);
	}

	protected void write(long code) {
		System.out.println(code);
	}

	private int[] getDistinctSortedToeknsId(List<String> tokens) {
		SortedSet<Integer> tokensId = new TreeSet<Integer>();

		for (String token : tokens) {
			if (getTokenId(token) != ILEGALTOKENID)
				tokensId.add(getTokenId(token));
		}

		int[] result = new int[tokensId.size()];

		int i = 0;
		for (Integer tokenId : tokensId) {
			result[i] = tokenId;
			i++;
		}

		return result;
	}

	private int getTokenId(String token) {
		Charset charset=Charset.forName("UTF-8");
		if (this.tokenHashToId.containsKey(this.murmur3_128.hashString(token,charset)
				.asLong()))
			return this.tokenHashToId.get(this.murmur3_128.hashString(token,charset)
					.asLong());
		else
			return ILEGALTOKENID;
	}

	public long getTokensCode(String token1) throws Exception {
		return this.bitCodec.encode(0, 0, getTokenId(token1), BITCOUNT);
	}

	public long getTokensCode(String token1, String token2) throws Exception {
		return this.bitCodec.encode(0, getTokenId(token1), getTokenId(token2),
				BITCOUNT);
	}

	public long getDistinctTokensCode(String token1, String token2) throws Exception {
		return this.bitCodec.decode(getTokensCode(token1,token2), BITCOUNT).getDistinctSortedCode(BITCOUNT);
	}

	public long getTokensCode(String token1, String token2, String token3)
			throws Exception {
		return this.bitCodec.encode(getTokenId(token1), getTokenId(token2),
				getTokenId(token3), BITCOUNT);
	}

	public void getNgramnessScore(String codedDfFileAddress,
			String codedCoFileAddress, String resultFileAddress, int minDf,
			int maxDf, double ultraNgramScoreMinLimit,
			double ultraNgramScoreMaxLimit) throws Exception {
		tempLongCodeCount1.clear();
		loadCodedFile(codedDfFileAddress, tempLongCodeCount1, minDf, maxDf);

		tempLongCodeCount2.clear();
		loadCodedFile(codedCoFileAddress, tempLongCodeCount2, minDf, maxDf);

		// HashMap<String, Double> tempResult1 = new HashMap<String, Double>(
		// tempLongCodeCount1.size() / 3);
		// HashMap<String, Double> tempResult2 = new HashMap<String, Double>(
		// tempLongCodeCount1.size() / 3);
		// HashMap<String, Double> tempResult3 = new HashMap<String, Double>(
		// tempLongCodeCount1.size() / 3);

		// HashMap<String, PairValue<Double,Long>> tempResult2u = new
		// HashMap<String, PairValue<Double,Long>>(
		// tempLongCodeCount1.size() / 3);
		// HashMap<String, PairValue<Double,Long>> tempResult3u = new
		// HashMap<String, PairValue<Double,Long>>(
		// tempLongCodeCount1.size() / 3);

		HashMap<String, Long> tempResult2u = new HashMap<String, Long>(
				tempLongCodeCount1.size() / 3);
		HashMap<String, Long> tempResult3u = new HashMap<String, Long>(
				tempLongCodeCount1.size() / 3);

		int counter = 0;
		long co = 0;
		for (LongLongCursor codeDf : tempLongCodeCount1) {
			counter++;

			if (counter % 10000 == 0)
				System.out.println(counter + " trigram handled");

			long distinctSortedCode = bitCodec.decode(codeDf.key, BITCOUNT)
					.getDistinctSortedCode(BITCOUNT);

			if (!tempLongCodeCount2.containsKey(distinctSortedCode))
				continue;

			co = tempLongCodeCount2.get(distinctSortedCode);

			if (co > 0 && co < codeDf.value)
				System.out.println(getCodeToken(codeDf.key) + "\tdf:"
						+ codeDf.value + "\tco:" + co);

			String trigram = getCodeToken(codeDf.key);
			String[] split = trigram.split(" ");
			String token = getCodeToken(codeDf.key);

			// token+="\t"+codeDf.value;

			double score = (codeDf.value + 0.01) / (co + 0.01);

			if (score > ultraNgramScoreMinLimit
					&& score <= ultraNgramScoreMaxLimit) {
				// PairValue<Double, Long> pairValue = new
				// PairValue(score,codeDf.value);
				if (split.length == 3) {
					tempResult3u.put(token, codeDf.value);
				} else if (split.length == 2) {
					tempResult2u.put(token, codeDf.value);
				}
			}

			// if (split.length == 3) {
			// tempResult3.put(token, score);
			// } else if (split.length == 2) {
			// tempResult2.put(token, score);
			// }
			// else if (split.length == 1) {
			// tempResult1.put(token, score);
			// }
		}
		String postFix = ".nscore" + ".minDf" + minDf + ".maxDf" + maxDf;
		// tools.util.file.Write.mapToTextFileSortedByValue(tempResult1,
		// resultFileAddress + ".1"+postFix, SortType.DECREMENTAL,
		// HashMapWriteType.KEYVALUE, "\t");
		// tools.util.file.Write.mapToTextFileSortedByValue(tempResult2,
		// resultFileAddress + ".2"+postFix, SortType.DECREMENTAL,
		// HashMapWriteType.KEYVALUE, "\t");
		// tools.util.file.Write.mapToTextFileSortedByValue(tempResult3,
		// resultFileAddress + ".3"+postFix, SortType.DECREMENTAL,
		// HashMapWriteType.KEYVALUE, "\t");

		String temResultAddress = resultFileAddress + ".2u.limitMinScore"
				+ ultraNgramScoreMinLimit + "limMaxScore"
				+ ultraNgramScoreMaxLimit + postFix;
		tools.util.file.Write.mapToTextFileSortedByValue(tempResult2u,
				temResultAddress, SortType.DECREMENTAL,
				HashMapWriteType.KEYVALUE, "\t");
		TrigramPosFilter.posFilterTrigramResult(temResultAddress, 2);

		temResultAddress = resultFileAddress + ".3u.limitMinScore"
				+ ultraNgramScoreMinLimit + "limMaxScore"
				+ ultraNgramScoreMaxLimit + postFix;
		tools.util.file.Write.mapToTextFileSortedByValue(tempResult3u,
				temResultAddress, SortType.DECREMENTAL,
				HashMapWriteType.KEYVALUE, "\t");
		TrigramPosFilter.posFilterTrigramResult(temResultAddress, 3);

		System.out.println("operation complete with " + counter
				+ " scored trigram.");
	}

	public void getLogDfNgramnessScore(String codedDfFileAddress,
			String codedCoFileAddress, String resultFileAddress, int minDf,
			int maxDf) throws Exception {
		tempLongCodeCount1.clear();
		loadCodedFile(codedDfFileAddress, tempLongCodeCount1, minDf, maxDf);

		tempLongCodeCount2.clear();
		loadCodedFile(codedCoFileAddress, tempLongCodeCount2, minDf, maxDf);

		HashMap<String, Double> tempResult1 = new HashMap<String, Double>();
		HashMap<String, Double> tempResult2 = new HashMap<String, Double>();
		HashMap<String, Double> tempResult3 = new HashMap<String, Double>();
		int counter = 0;
		long co = 0;
		for (LongLongCursor codeDf : tempLongCodeCount1) {
			counter++;

			if (counter % 10000 == 0)
				System.out.println(counter + " trigram handled");

			long distinctSortedCode = bitCodec.decode(codeDf.key, BITCOUNT)
					.getDistinctSortedCode(BITCOUNT);

			if (!tempLongCodeCount2.containsKey(distinctSortedCode))
				continue;

			co = tempLongCodeCount2.get(distinctSortedCode);

			if (co > 0 && co < codeDf.value)
				System.out.println(getCodeToken(codeDf.key) + "\tdf:"
						+ codeDf.value + "\tco:" + co);

			String trigram = getCodeToken(codeDf.key);
			String[] split = trigram.split(" ");
			String token = getCodeToken(codeDf.key);

			token += "\t" + codeDf.value;

			double score = Math.log(codeDf.value + 0.01)
					* (codeDf.value + 0.01) / (co + 0.01);
			if (split.length == 3) {
				tempResult3.put(token, score);
			} else if (split.length == 2) {
				tempResult2.put(token, score);
			} else if (split.length == 1) {
				tempResult1.put(token, score);
			}
		}
		String postFix = ".lgnscore" + ".minDf" + minDf + ".maxDf" + maxDf;
		tools.util.file.Write.mapToTextFileSortedByValue(tempResult1,
				resultFileAddress + ".1" + postFix, SortType.DECREMENTAL,
				HashMapWriteType.KEYVALUE, "\t");
		tools.util.file.Write.mapToTextFileSortedByValue(tempResult2,
				resultFileAddress + ".2" + postFix, SortType.DECREMENTAL,
				HashMapWriteType.KEYVALUE, "\t");
		tools.util.file.Write.mapToTextFileSortedByValue(tempResult3,
				resultFileAddress + ".3" + postFix, SortType.DECREMENTAL,
				HashMapWriteType.KEYVALUE, "\t");
		System.out.println("operation complete with " + counter
				+ " scored trigram.");
	}

	public LongLongOpenHashMap getLegalBigramCoOccuranceCodeMap() {
		return legalBigramCoOccuranceCodeMap;
	}

	public LongLongOpenHashMap getLegalTrigramCoOccuranceCodeMap() {
		return legalTrigramCoOccuranceCodeMap;
	}

	// LM methods
	public long getTokenCount(String token) {
		int tokenId = getTokenId(token);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}

		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		return this.dfCodeCount.get(tokenId);
	}

	// df
	public long getDfCount(String token1) throws Exception {
		int tokenId = getTokenId(token1);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		return this.dfCodeCount.get(getTokensCode(token1));
	}

	public long getDfCount(String token1, String token2) throws Exception {
		int tokenId = getTokenId(token1);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		tokenId = getTokenId(token2);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		return this.dfCodeCount.get(getTokensCode(token1, token2));
	}

	public long getDfCount(String token1, String token2, String token3)
			throws Exception {
		int tokenId = getTokenId(token1);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		tokenId = getTokenId(token2);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		tokenId = getTokenId(token3);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}
		return this.dfCodeCount.get(getTokensCode(token1, token2, token3));
	}

	// co
	public long getCoCount(String token1) throws Exception {
		int tokenId = getTokenId(token1);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		return this.coCodeCount.get(getCoCode(getTokensCode(token1)));
	}

	public long getCoCount(String token1, String token2) throws Exception {
		int tokenId = getTokenId(token1);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		tokenId = getTokenId(token2);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		return this.coCodeCount.get(getCoCode(getTokensCode(token1, token2)));
	}

	public long getCoCount(String token1, String token2, String token3)
			throws Exception {
		int tokenId = getTokenId(token1);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		tokenId = getTokenId(token2);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		tokenId = getTokenId(token3);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		return this.coCodeCount.get(getCoCode(getTokensCode(token1, token2,
				token3)));
	}

	public double getLmScore(String token1, String token2) throws Exception {
		int tokenId = getTokenId(token1);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		tokenId = getTokenId(token2);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		double a = (this.dfCodeCount.get(getTokensCode(token1, token2)) + EPS);
		double b = (this.dfCodeCount.get(getTokensCode(token1)) + 1);
		return a / b;
	}

	public double getLmScore(String token1, String token2, String token3)
			throws Exception {
		int tokenId = getTokenId(token1);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		tokenId = getTokenId(token2);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		tokenId = getTokenId(token3);
		if (tokenId == ILEGALTOKENID) {
			return -1;
		}
		if (!this.dfCodeCount.containsKey(tokenId)) {
			return 0;
		}

		double score = 0;

		double a = (this.dfCodeCount.get(getTokensCode(token1, token2)) + EPS);
		double b = (this.dfCodeCount.get(getTokensCode(token1)) + 1.0);
		double c = (this.dfCodeCount.get(getTokensCode(token1, token2, token3)) + EPS);
		double d = (this.dfCodeCount.get(getTokensCode(token1, token2)) + 1.0);
		score = (a / b) * (c / d);

		return score;
	}

	// string scoring methods

	public double getScoreDfBigramTrigram(String inString) throws Exception {
		double score = 0;
		List<String> normalizeTokenize = this.normalizerTokenizer
				.normalizeTokenize(inString);

		double dfBigramScore = getScoreBigramDf(normalizeTokenize);
		double dfTrigramScore = getScoreTrigramDf(normalizeTokenize);

		double coBigramScore = getScoreBigramCo(normalizeTokenize);
		double coTrigramScore = getScoreTrigramCo(normalizeTokenize);

		double dfDivCoBigramScore = getScoreBigramDfDivCo(normalizeTokenize);
		double dfDivCoTrigramScore = getScoreTrigramDfDivCo(normalizeTokenize);

		double lmTrigramScore = getScoreTrigramLm(normalizeTokenize);

		score = lmTrigramScore;

		return score;
	}

	public double getScoreBigramDf(List<String> tokens) throws Exception {
		double sumDf = 0;
		for (int i = 0; i < tokens.size() - 1; i++) {
			sumDf += getDfCount(tokens.get(i), tokens.get(i + 1));
		}
		return sumDf;
	}

	public double getScoreTrigramDf(List<String> tokens) throws Exception {
		double sumDf = 0;
		for (int i = 0; i < tokens.size() - 2; i++) {
			sumDf += getDfCount(tokens.get(i), tokens.get(i + 1),
					tokens.get(i + 2));
		}
		return sumDf;
	}

	public double getScoreBigramCo(List<String> tokens) throws Exception {
		double sumCo = 0;
		for (int i = 0; i < tokens.size() - 1; i++) {
			sumCo += getCoCount(tokens.get(i), tokens.get(i + 1));
		}
		return sumCo;
	}

	public double getScoreTrigramCo(List<String> tokens) throws Exception {
		double sumCo = 0;
		for (int i = 0; i < tokens.size() - 2; i++) {
			sumCo += getCoCount(tokens.get(i), tokens.get(i + 1),
					tokens.get(i + 2));
		}
		return sumCo;
	}

	public double getScoreBigramDfDivCo(List<String> tokens) throws Exception {
		double score = 0;
		for (int i = 0; i < tokens.size() - 1; i++) {
			score += getDfCount(tokens.get(i), tokens.get(i + 1))
					/ (getCoCount(tokens.get(i), tokens.get(i + 1)) + 1.0);
		}
		return score;
	}

	public double getScoreTrigramDfDivCo(List<String> tokens) throws Exception {
		double score = 0;
		for (int i = 0; i < tokens.size() - 2; i++) {
			score += getDfCount(tokens.get(i), tokens.get(i + 1),
					tokens.get(i + 2))
					/ (getCoCount(tokens.get(i), tokens.get(i + 1),
							tokens.get(i + 2)) + 1.0);
		}
		return score;
	}

	public double getScoreTrigramLm(List<String> tokens) throws Exception {
		double score = 0;
		if (tokens.size() == 2) {
			score += getLmScore(tokens.get(0), tokens.get(1));
		} else {
			for (int i = 0; i < tokens.size() - 2; i++) {
				score += getLmScore(tokens.get(i), tokens.get(i + 1),
						tokens.get(i + 2));
			}
		}
		return score;
	}

	public List<String> normalizeTokenize(String inString) {
		return this.normalizerTokenizer.normalizeTokenize(inString);
	}

	public String normalizeTokenizeAsString(String inString) {
		return this.normalizerTokenizer.tokenizeAsString(inString, ' ');
	}

	private long getCoCode(long code) throws Exception {
		return bitCodec.decode(code, BITCOUNT).getDistinctSortedCode(BITCOUNT);
	}
}
