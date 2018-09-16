package nlp.analyzer.data.text;

import nlp.preprocess.normalizer.en.NormalizerEnglishSimple;
import tools.util.collection.HashSert;
import tools.util.collection.HashSertInteger;
import tools.util.file.Write.HashMapWriteType;
import tools.util.sort.Collection.SortType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class Distribution {


	static void test1(String baseAddress) throws IOException {
		String splitRegularExpression = "(\\s)+|(\\t)+";
		HashMap<String, Long> pathTokenDistribution = getPathEnglishSimpleNormalizeTokenDistribution(
				baseAddress, 0, splitRegularExpression, true);
		tools.util.file.Write.mapToTextFileSortedByValue(pathTokenDistribution,
				tools.util.File.getParent(baseAddress) + "/allToken.dist",
				SortType.DECREMENTAL, HashMapWriteType.KEYVALUE);
		int minCount = 1;
	}

	static public HashSet<Character> getFileCharSet(String fileAddress)
			throws IOException {
		HashSet<Character> charSet = new HashSet<Character>(100000);
		int lineCounter = 0;
		BufferedReader fileBufferReader = tools.util.file.Reader
				.getFileBufferReader(fileAddress);
		String newLine;
		while ((newLine = fileBufferReader.readLine()) != null) {
			lineCounter++;
			for (int i = 0; i < newLine.length(); i++) {
				charSet.add(newLine.charAt(i));
			}
			if (lineCounter % 100000 == 0)
				System.out.println(lineCounter + " line handled.");
		}
		fileBufferReader.close();
		System.out.println("getCharSet operation complete with "
				+ charSet.size() + " for " + lineCounter + " handled line.");
		return charSet;
	}

	static public HashSet<Character> getPathCharSet(String filesPath,
			int directorySearchDeepLevel) throws IOException {
		HashSet<Character> charSet = new HashSet<Character>(100000);
		int lineCounter = 0;
		int fileCounter = 0;
		for (File file : tools.util.directory.Search.getFilesForPath(filesPath,
				false, new HashSet<String>(), directorySearchDeepLevel)) {
			fileCounter++;
			BufferedReader fileBufferReader = tools.util.file.Reader
					.getFileBufferReader(file);
			String newLine;
			while ((newLine = fileBufferReader.readLine()) != null) {
				lineCounter++;
				for (int i = 0; i < newLine.length(); i++) {
					charSet.add(newLine.charAt(i));
				}
				if (lineCounter % 100000 == 0)
					System.out.println(lineCounter + " lines for "
							+ fileCounter + " file.");
			}
			fileBufferReader.close();
		}
		System.out.println("getCharSet operation complete with "
				+ charSet.size() + " for " + fileCounter
				+ " handled file of with " + lineCounter + " lines.");
		return charSet;
	}

	static public HashMap<Character, Long> getFileCharDistribution(
			String fileAddress) throws IOException {
		HashSert<Character> charDistribution = new HashSert<Character>();
		int lineCounter = 0;
		BufferedReader fileBufferReader = tools.util.file.Reader
				.getFileBufferReader(fileAddress);
		String newLine;
		while ((newLine = fileBufferReader.readLine()) != null) {
			lineCounter++;
			for (int i = 0; i < newLine.length(); i++) {
				charDistribution.put(newLine.charAt(i));
			}
			if (lineCounter % 100000 == 0)
				System.out.println(lineCounter + " line handled.");
		}
		fileBufferReader.close();
		System.out.println("getCharSet operation complete with "
				+ charDistribution.size() + " for " + lineCounter
				+ " handled line.");
		return charDistribution.getHashMap();
	}

	static public void printFileCharDistribution(String fileAddress)
			throws IOException {
		System.out.println("The " + tools.util.File.getName(fileAddress)
				+ " character distribution:");
		int i = 0;
		for (Entry<Character, Long> entry : tools.util.sort.Collection
				.mapSortedByValuesDecremental(getFileCharDistribution(fileAddress))) {
			System.out.println(++i + ") " + entry.getKey() + "\t\t"
					+ entry.getValue());
		}
	}

	static public HashMap<Character, Long> getPathCharDistribution(
			String filesPath, int directorySearchDeepLevel) throws IOException {
		HashSert<Character> charDistribution = new HashSert<Character>();
		int lineCounter = 0;
		int fileCounter = 0;
		for (File file : tools.util.directory.Search.getFilesForPath(filesPath,
				false, new HashSet<String>(), directorySearchDeepLevel)) {
			fileCounter++;
			BufferedReader fileBufferReader = tools.util.file.Reader
					.getFileBufferReader(file);
			String newLine;
			while ((newLine = fileBufferReader.readLine()) != null) {
				lineCounter++;
				for (int i = 0; i < newLine.length(); i++) {
					charDistribution.add(newLine.charAt(i));
				}
				if (lineCounter % 100000 == 0)
					System.out.println(lineCounter + " lines for "
							+ fileCounter + " file.");
			}
			fileBufferReader.close();
		}
		System.out.println("getCharSet operation complete with "
				+ charDistribution.size() + " for " + fileCounter
				+ " handled file of with " + lineCounter + " lines.");
		return charDistribution.getHashMap();
	}

	static public HashSet<String> getFileTokenSet(String fileAddress,
			String splitRegularExpression) throws IOException {
		HashSet<String> tokenSet = new HashSet<String>(100000);
		int lineCounter = 0;
		BufferedReader fileBufferReader = tools.util.file.Reader
				.getFileBufferReader(fileAddress);
		String newLine;
		while ((newLine = fileBufferReader.readLine()) != null) {
			lineCounter++;
			for (String token : newLine.split(splitRegularExpression)) {
				tokenSet.add(token.trim());
			}
			if (lineCounter % 100000 == 0)
				System.out.println(lineCounter + " line handled.");
		}
		fileBufferReader.close();
		System.out.println("getCharSet operation complete with "
				+ tokenSet.size() + " for " + lineCounter + " handled line.");
		return tokenSet;
	}

	static public HashSet<String> getPathTokenSet(String filesPath,
			int directorySearchDeepLevel, String splitRegularExpression)
			throws IOException {
		HashSet<String> tokenSet = new HashSet<String>(100000);
		int lineCounter = 0;
		int fileCounter = 0;
		for (File file : tools.util.directory.Search.getFilesForPath(filesPath,
				false, new HashSet<String>(), directorySearchDeepLevel)) {
			fileCounter++;
			BufferedReader fileBufferReader = tools.util.file.Reader
					.getFileBufferReader(file);
			String newLine;
			while ((newLine = fileBufferReader.readLine()) != null) {
				lineCounter++;
				for (String token : newLine.split(splitRegularExpression)) {
					tokenSet.add(token.trim());
				}
				if (lineCounter % 100000 == 0)
					System.out.println(lineCounter + " lines for "
							+ fileCounter + " file.");
			}
			fileBufferReader.close();
		}
		System.out.println("getCharSet operation complete with "
				+ tokenSet.size() + " for " + fileCounter
				+ " handled file of with " + lineCounter + " lines.");
		return tokenSet;
	}

	static public HashMap<String, Long> getFileTokenDistribution(
			String fileAddress, String splitRegularExpression)
			throws IOException {
		HashSert<String> tokenDistribution = new HashSert<String>();
		int lineCounter = 0;
		BufferedReader fileBufferReader = tools.util.file.Reader
				.getFileBufferReader(fileAddress);
		String newLine;
		while ((newLine = fileBufferReader.readLine()) != null) {
			lineCounter++;
			for (String token : newLine.split(splitRegularExpression)) {
				tokenDistribution.add(token.trim());
			}
			if (lineCounter % 100000 == 0)
				System.out.println("getFileTokenDistribution "+lineCounter + " line handled.");
		}
		fileBufferReader.close();
		System.out.println("getCharSet operation complete with "
				+ tokenDistribution.size() + " for " + lineCounter
				+ " handled line.");
		return tokenDistribution.getHashMap();
	}

	static public HashMap<String, Long> getFileEnglishSimpleNormalizeTokenDistribution(
			String fileAddress, String splitRegularExpression,
			boolean caseSensitive) throws IOException {
		nlp.preprocess.normalizer.en.NormalizerEnglishSimple normalizerEnglishSimple = new NormalizerEnglishSimple();
		HashSert<String> tokenDistribution = new HashSert<String>();
		int lineCounter = 0;
		BufferedReader fileBufferReader = tools.util.file.Reader
				.getFileBufferReader(fileAddress);
		String newLine;
		while ((newLine = fileBufferReader.readLine()) != null) {
			lineCounter++;
			for (String token : normalizerEnglishSimple
					.normalize(newLine, caseSensitive).trim()
					.split(splitRegularExpression)) {
				tokenDistribution.add(token.trim());
			}
			if (lineCounter % 100000 == 0)
				System.out.println(lineCounter + " line handled.");
		}
		fileBufferReader.close();
		System.out.println("getCharSet operation complete with "
				+ tokenDistribution.size() + " for " + lineCounter
				+ " handled line.");
		return tokenDistribution.getHashMap();
	}

	static public HashMap<String, Long> getFileTokenTokenDistribution(
			String fileAddress, String splitRegularExpression)
			throws IOException {
		tools.util.Time.setStartTimeForNow();
		HashSert<String> tokenDistribution = new HashSert<String>();
		int lineCounter = 0;
		BufferedReader fileBufferReader = tools.util.file.Reader
				.getFileBufferReader(fileAddress);
		String newLine;
		while ((newLine = fileBufferReader.readLine()) != null) {
			lineCounter++;
			String[] split = newLine.split(splitRegularExpression);
			for (int i = 0; i < split.length - 1; i++) {
				tokenDistribution.add(split[i] + " " + split[i + 1]);
			}
			if (lineCounter % 100000 == 0)
				System.out.println(lineCounter + " line handled.");
		}
		fileBufferReader.close();
		System.out.println("getTokenTonen operation complete with "
				+ tokenDistribution.size() + " for " + lineCounter
				+ " handled line in " + tools.util.Time.getTimeLengthForNow()
				+ " ms.");
		return tokenDistribution.getHashMap();
	}

	static public HashMap<String, Integer> getPathTokenDistribution(
			String filesPath, int directorySearchDeepLevel,
			String splitRegularExpression) throws IOException {
		HashSertInteger<String> tokenDistribution = new HashSertInteger<String>();
		int lineCounter = 0;
		int fileCounter = 0;
		for (File file : tools.util.directory.Search.getFilesForPath(filesPath,
				false, new HashSet<String>(), directorySearchDeepLevel)) {
			fileCounter++;
			BufferedReader fileBufferReader = tools.util.file.Reader
					.getFileBufferReader(file);
			String newLine;
			while ((newLine = fileBufferReader.readLine()) != null) {
				lineCounter++;
				for (String token : newLine.split(splitRegularExpression)) {
					tokenDistribution.add(token.trim());
				}
				if (lineCounter % 100000 == 0)
					System.out.println(lineCounter + " lines for "
							+ fileCounter + " file.");
			}
			fileBufferReader.close();
		}
		System.out.println("getCharSet operation complete with "
				+ tokenDistribution.size() + " for " + fileCounter
				+ " handled file of with " + lineCounter + " lines.");
		return tokenDistribution.getHashMap();
	}

	static public HashMap<String, Long> getPathEnglishSimpleNormalizeTokenDistribution(
			String filesPath, int directorySearchDeepLevel,
			String splitRegularExpression, boolean caseSensitive)
			throws IOException {
		nlp.preprocess.normalizer.en.NormalizerEnglishSimple normalizerEnglishSimple = new NormalizerEnglishSimple();
		HashSert<String> tokenDistribution = new HashSert<String>();
		int lineCounter = 0;
		int fileCounter = 0;
		for (File file : tools.util.directory.Search.getFilesForPath(filesPath,
				false, new HashSet<String>(), directorySearchDeepLevel)) {
			fileCounter++;
			BufferedReader fileBufferReader = tools.util.file.Reader
					.getFileBufferReader(file);
			String newLine;
			while ((newLine = fileBufferReader.readLine()) != null) {
				lineCounter++;
				for (String token : normalizerEnglishSimple
						.normalize(newLine, caseSensitive).trim()
						.split(splitRegularExpression)) {
					tokenDistribution.add(token.trim());
				}
				if (lineCounter % 100000 == 0)
					System.out.println(lineCounter + " lines for "
							+ fileCounter + " file.");
			}
			fileBufferReader.close();
		}
		System.out.println("getCharSet operation complete with "
				+ tokenDistribution.size() + " for " + fileCounter
				+ " handled file of with " + lineCounter + " lines.");
		return tokenDistribution.getHashMap();
	}
}
