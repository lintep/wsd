package nlp.languagemodel.filter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;

import nlp.preprocess.fa.SimpleNormalizerTokenizer;
import tools.util.file.TextReader;

import nlp.preprocess.NormalizerTokenizerInterface;

public class TrigramPosFilter {

	public static void posFilterTrigramResult(String fileAddress, int kgram)
			throws FileNotFoundException, IOException {
		String bigramConOrVerbOrAdverbOrPOrPostpOrPreoFileAddress = "/data/bijankhan/filter/BigramPosFilter";
		String trigramConjFileAddress = "/data/bijankhan/filter/TrigramPosFilterConj";
		String trigramAdverbPostpProSetFileAddress = "/data/bijankhan/filter/TrigramPosFilterAdverbPostpProP";
		String trigramVerbFileAddress = "/data/bijankhan/filter/TrigramPosFilterVerbTokens";

		NormalizerTokenizerInterface normalizerTokenizer = new SimpleNormalizerTokenizer();

		TrigramPosFilter trigramPosFilter = new TrigramPosFilter(
				bigramConOrVerbOrAdverbOrPOrPostpOrPreoFileAddress,
				trigramConjFileAddress, trigramAdverbPostpProSetFileAddress,
				trigramVerbFileAddress, normalizerTokenizer);

		TextReader textReader = new TextReader(
				fileAddress);
		PrintWriter writerTrsut = tools.util.file.Write.getPrintWriter(
				fileAddress + ".posTrust", false);
		PrintWriter writerFiltered = tools.util.file.Write.getPrintWriter(
				fileAddress + ".posfiltered", false);
		while (textReader.hasNext()) {
			String newLine = textReader.next();
			List<String> tokens = normalizerTokenizer
					.normalizeTokenize(newLine);
			if (kgram == 2) {
				if (trigramPosFilter
						.isTrustBigram(tokens.get(0), tokens.get(1))) {
					writerTrsut.println(newLine);
				} else {
					writerFiltered.println(newLine);
				}
			} else {
				if (trigramPosFilter.isTrustTrigram(tokens.get(0),
						tokens.get(1), tokens.get(2))) {
					writerTrsut.println(newLine);
				} else {
					writerFiltered.println(newLine);
				}
			}
		}
		writerFiltered.close();
		writerTrsut.close();
		textReader.close();

		System.out.println("operation complete");
	}

	NormalizerTokenizerInterface normalizerTokenizerInterface;

	HashSet<String> bigramConOrVerbOrAdverbOrPOrPostpOrPreoSet = new HashSet<String>();

	HashSet<String> trigramConjSet = new HashSet<String>();
	HashSet<String> trigramAdverbPostpProSet = new HashSet<String>();
	HashSet<String> trigramVerbSet = new HashSet<String>();

	public TrigramPosFilter(
			String bigramConOrVerbOrAdverbOrPOrPostpOrPreoFileAddress,
			String trigramConjFileAddress,
			String trigramAdverbPostpProSetFileAddress,
			String trigramVerbFileAddress,
			NormalizerTokenizerInterface normalizerTokenizerInterface) {

		for (String bigramFilterToken : tools.util.file.Reader
				.getTextLinesString(
						bigramConOrVerbOrAdverbOrPOrPostpOrPreoFileAddress,
						false)) {
			bigramConOrVerbOrAdverbOrPOrPostpOrPreoSet
					.add(normalizerTokenizerInterface
							.normalize(bigramFilterToken.split("\t")[0]));
		}

		for (String conj : tools.util.file.Reader.getTextLinesString(
				trigramConjFileAddress, false)) {
			trigramConjSet.add(normalizerTokenizerInterface.normalize(conj
					.split("\t")[0]));
		}

		for (String adverb : tools.util.file.Reader.getTextLinesString(
				trigramAdverbPostpProSetFileAddress, false)) {
			trigramAdverbPostpProSet.add(normalizerTokenizerInterface
					.normalize(adverb.split("\t")[0]));
		}

		for (String verb : tools.util.file.Reader.getTextLinesString(
				trigramVerbFileAddress, false)) {
			trigramVerbSet.add(normalizerTokenizerInterface.normalize(verb
					.split("\t")[0]));
		}
	}

	// /bigram filters

	public boolean isTrustBigram(String token1, String token2) {
		if (isInBigramConjOrVerbOrAdverbOrPOrPostpOrPreoFilter(token1, token2))
			return false;

		if (isInBigramNumberFilter(token1, token2))
			return false;

		if (isInBigramMiiiJamFilter(token1, token2))
			return false;

		if (isInBigramEyHaHayeHayiiFromFilter(token1, token2))
			return false;

		return true;
	}

	private boolean isInBigramConjOrVerbOrAdverbOrPOrPostpOrPreoFilter(
			String token1, String token2) {

		if (bigramConOrVerbOrAdverbOrPOrPostpOrPreoSet.contains(token1))
			return true;

		if (bigramConOrVerbOrAdverbOrPOrPostpOrPreoSet.contains(token2))
			return true;

		return false;
	}

	private boolean isInBigramNumberFilter(String token1, String token2) {
		if (!isNubmer(token1))
			return false;
		if (!isNubmer(token2))
			return false;
		return true;
	}

	private boolean isInBigramMiiiJamFilter(String token1, String token2) {
		if ("می".equals(token1))
			return true;
		if ("می".equals(token2)) {
			if (!"جام".equals(token1))
				return true;
		}
		return false;
	}

	private boolean isInBigramEyHaHayeHayiiFromFilter(String token1,
													  String token2) {
		if ("ای".equals(token1) || "های".equals(token1) || "ها".equals(token1)
				|| "هایی".equals(token1))
			return true;
		if ("ای".equals(token2) || "های".equals(token2) || "ها".equals(token2)
				|| "هایی".equals(token2)) {
			return true;
		}
		return false;
	}

	public boolean isNubmer(String token) {
		for (int i = 0; i < token.length(); i++) {
			if (token.charAt(i) > '9')
				return false;
			if (token.charAt(i) < '0')
				return false;
		}
		return true;
	}

	// /trigram filters

	public boolean isTrustTrigram(String token1, String token2, String token3) {

		if (isInTrigramConjFilter(token1, token2, token3))
			return false;

		if (isInTrigramNumberFilter(token1, token2, token3))
			return false;

		if (isInTrigramVerbFilterinAll(token1, token2, token3))
			return false;

		if (isInTrigramAdverbPostpProFilter(token1, token2, token3))
			return false;

		if (isInTrigramMiiiJamFilter(token1, token2, token3))
			return false;

		if (isInTrigramEyHaHayeHayiiFromFilter(token1, token2, token3))
			return false;

		return true;

	}

	private boolean isInTrigramConjFilter(String token1, String token2,
										  String token3) {
		if (trigramConjSet.contains(token1))
			return true;
		if (trigramConjSet.contains(token3))
			return true;
		return false;
	}

	private boolean isInTrigramAdverbPostpProFilter(String token1,
													String token2, String token3) {
		if (trigramAdverbPostpProSet.contains(token3))
			return true;
		return false;
	}

	private boolean isInTrigramVerbFilter(String token1, String token2,
										  String token3) {
		if (trigramVerbSet.contains(token1))
			return true;
		if (trigramVerbSet.contains(token2))
			return true;
		return false;
	}

	private boolean isInTrigramVerbFilterinAll(String token1, String token2,
											   String token3) {
		if (trigramVerbSet.contains(token1))
			return true;
		if (trigramVerbSet.contains(token2))
			return true;
		if (trigramVerbSet.contains(token3))
			return true;
		return false;
	}

	private boolean isInTrigramNumberFilter(String token1, String token2,
											String token3) {
		if (!isNubmer(token1))
			return false;
		if (isNubmer(token2))
			return false;
		if (isNubmer(token3))
			return false;
		return true;
	}

	private boolean isInTrigramMiiiJamFilter(String token1, String token2,
											 String token3) {
		if (isInBigramMiiiJamFilter(token1, token2))
			return true;
		if ("می".equals(token3)) {
			if (!"جام".equals(token2))
				return true;
		}
		return false;
	}

	private boolean isInTrigramEyHaHayeHayiiFromFilter(String token1,
													   String token2, String token3) {
		if (isInBigramEyHaHayeHayiiFromFilter(token1, token2))
			return true;
		if ("ای".equals(token3) || "های".equals(token3) || "ها".equals(token3)
				|| "هایی".equals(token3)) {
			return true;
		}
		return false;
	}

	/*
	 * public static void filterEyHayeFromTrigram(String fileAddress, String
	 * resultFileAddress) throws IOException { // -* // *- PrintWriter writer =
	 * tools.util.file.Write.getPrintWriter( resultFileAddress, false);
	 * tools.util.file.TextReader bufferedIterator = new TextReader(
	 * tools.util.file.Reader.getFileBufferReader(fileAddress)); while
	 * (bufferedIterator.hasNext()) { String line = bufferedIterator.next();
	 * String[] split1 = line.split("\t"); String[] tokens =
	 * split1[0].split(" "); if ("ای".equals(tokens[0]) ||
	 * "های".equals(tokens[0]) || "ها".equals(tokens[0])) continue; if
	 * ("ای".equals(tokens[1]) || "های".equals(tokens[1]) ||
	 * "ها".equals(tokens[0])) { continue; } if ("ای".equals(tokens[2]) ||
	 * "های".equals(tokens[2]) || "ها".equals(tokens[0])) { continue; }
	 * writer.println(line); } writer.close(); bufferedIterator.close(); }
	 *
	 * public static void filterVerbAllTrigram(String fileAddress, String
	 * verbFileAddress, String resultFileAddress) throws IOException { // -**
	 * PrintWriter writer = tools.util.file.Write.getPrintWriter(
	 * resultFileAddress, false); SoftNormalizer softNormalizer = new
	 * SoftNormalizer(); HashSet<String> verbSet = new HashSet<String>(); for
	 * (String verbPreo : tools.util.file.Reader.getTextLinesString(
	 * verbFileAddress, false)) {
	 * verbSet.add(softNormalizer.normalize(verbPreo.split("\t")[0])); }
	 * tools.util.file.TextReader bufferedIterator = new TextReader(
	 * tools.util.file.Reader.getFileBufferReader(fileAddress)); while
	 * (bufferedIterator.hasNext()) { String line = bufferedIterator.next();
	 * String[] split1 = line.split("\t"); String[] tokens =
	 * split1[0].split(" "); if
	 * (verbSet.contains(softNormalizer.normalize(tokens[0])) ||
	 * verbSet.contains(softNormalizer.normalize(tokens[1])) ||
	 * verbSet.contains(softNormalizer.normalize(tokens[2]))) continue;
	 * writer.println(line); } writer.close(); bufferedIterator.close(); }
	 *
	 * // // public static void
	 * filterConjOrVerbOrAdverbOrPOrPostpOrPreoFromBigram( String fileAddress,
	 * String conjOrVerbOrAdverbOrPOrPostpOrPreoFileAddress, String
	 * resultFileAddress) throws IOException { // -* // *- PrintWriter writer =
	 * tools.util.file.Write.getPrintWriter( resultFileAddress, false);
	 * SoftNormalizer softNormalizer = new SoftNormalizer(); HashSet<String>
	 * conOrVerbOrAdverbOrPOrPostpOrPreoSet = new HashSet<String>(); for (String
	 * conjOrVerbOrAdverbOrPOrPostpOrPreo : tools.util.file.Reader
	 * .getTextLinesString( conjOrVerbOrAdverbOrPOrPostpOrPreoFileAddress,
	 * false)) { conOrVerbOrAdverbOrPOrPostpOrPreoSet.add(softNormalizer
	 * .normalize(conjOrVerbOrAdverbOrPOrPostpOrPreo)); }
	 * tools.util.file.TextReader bufferedIterator = new TextReader(
	 * tools.util.file.Reader.getFileBufferReader(fileAddress)); while
	 * (bufferedIterator.hasNext()) { String line = bufferedIterator.next();
	 * String[] split1 = line.split("\t"); String[] tokens =
	 * split1[2].split(" "); if
	 * (conOrVerbOrAdverbOrPOrPostpOrPreoSet.contains(softNormalizer
	 * .normalize(tokens[0]))) continue; if
	 * (conOrVerbOrAdverbOrPOrPostpOrPreoSet.contains(softNormalizer
	 * .normalize(tokens[1]))) continue; writer.println(line); } writer.close();
	 * bufferedIterator.close(); }
	 *
	 * // public static void filterNumberFromBigram(String fileAddress, String
	 * resultFileAddress) throws IOException { // -* // *- PrintWriter writer =
	 * tools.util.file.Write.getPrintWriter( resultFileAddress, false);
	 * SoftNormalizer softNormalizer = new SoftNormalizer();
	 * tools.util.file.TextReader bufferedIterator = new TextReader(
	 * tools.util.file.Reader.getFileBufferReader(fileAddress)); while
	 * (bufferedIterator.hasNext()) { String line = bufferedIterator.next();
	 * String[] split1 = line.split("\t"); String[] tokens =
	 * split1[2].split(" "); if (isNubmer(softNormalizer.normalize(tokens[0]))
	 * && isNubmer(softNormalizer.normalize(tokens[1]))) continue;
	 * writer.println(line); } writer.close(); bufferedIterator.close(); }
	 *
	 * // public static void filterConjTrigram(String fileAddress, String
	 * conjOrVerbOrAdverbOrPOrPostpOrPreoFileAddress, String resultFileAddress)
	 * throws IOException { // -* // *- PrintWriter writer =
	 * tools.util.file.Write.getPrintWriter( resultFileAddress, false);
	 * SoftNormalizer softNormalizer = new SoftNormalizer(); HashSet<String>
	 * conjSet = new HashSet<String>(); for (String
	 * conjOrVerbOrAdverbOrPOrPostpOrPreo : tools.util.file.Reader
	 * .getTextLinesString( conjOrVerbOrAdverbOrPOrPostpOrPreoFileAddress,
	 * false)) { conjSet.add(softNormalizer
	 * .normalize(conjOrVerbOrAdverbOrPOrPostpOrPreo.split("\t")[0])); }
	 * tools.util.file.TextReader bufferedIterator = new TextReader(
	 * tools.util.file.Reader.getFileBufferReader(fileAddress)); while
	 * (bufferedIterator.hasNext()) { String line = bufferedIterator.next();
	 * String[] split1 = line.split("\t"); String[] tokens =
	 * split1[0].split(" "); if
	 * (conjSet.contains(softNormalizer.normalize(tokens[0]))) continue; if
	 * (conjSet.contains(softNormalizer.normalize(tokens[2]))) continue;
	 * writer.println(line); } writer.close(); bufferedIterator.close(); }
	 *
	 * // public static void filterAdverbPostpProTrigram(String fileAddress,
	 * String adverbPostpProFileAddress, String resultFileAddress) throws
	 * IOException { // -** PrintWriter writer =
	 * tools.util.file.Write.getPrintWriter( resultFileAddress, false);
	 * SoftNormalizer softNormalizer = new SoftNormalizer(); HashSet<String>
	 * adverbPostpProSet = new HashSet<String>(); for (String
	 * conjOrVerbOrAdverbOrPOrPostpOrPreo : tools.util.file.Reader
	 * .getTextLinesString(adverbPostpProFileAddress, false)) {
	 * adverbPostpProSet .add(softNormalizer
	 * .normalize(conjOrVerbOrAdverbOrPOrPostpOrPreo .split("\t")[0])); }
	 * tools.util.file.TextReader bufferedIterator = new TextReader(
	 * tools.util.file.Reader.getFileBufferReader(fileAddress)); while
	 * (bufferedIterator.hasNext()) { String line = bufferedIterator.next();
	 * String[] split1 = line.split("\t"); String[] tokens =
	 * split1[0].split(" "); if
	 * (adverbPostpProSet.contains(softNormalizer.normalize(tokens[2])))
	 * continue; writer.println(line); } writer.close();
	 * bufferedIterator.close(); }
	 *
	 * // public static void filterVerbTrigram(String fileAddress, String
	 * verbFileAddress, String resultFileAddress) throws IOException { // -**
	 * PrintWriter writer = tools.util.file.Write.getPrintWriter(
	 * resultFileAddress, false); SoftNormalizer softNormalizer = new
	 * SoftNormalizer(); HashSet<String> verbSet = new HashSet<String>(); for
	 * (String verbPreo : tools.util.file.Reader.getTextLinesString(
	 * verbFileAddress, false)) {
	 * verbSet.add(softNormalizer.normalize(verbPreo.split("\t")[0])); }
	 * tools.util.file.TextReader bufferedIterator = new TextReader(
	 * tools.util.file.Reader.getFileBufferReader(fileAddress)); while
	 * (bufferedIterator.hasNext()) { String line = bufferedIterator.next();
	 * String[] split1 = line.split("\t"); String[] tokens =
	 * split1[0].split(" "); if
	 * (verbSet.contains(softNormalizer.normalize(tokens[0]))) continue; if
	 * (verbSet.contains(softNormalizer.normalize(tokens[1]))) continue;
	 * writer.println(line); } writer.close(); bufferedIterator.close(); }
	 *
	 * public static void filterNumberFromTrigram(String fileAddress, String
	 * resultFileAddress) throws IOException { // -* // *- PrintWriter writer =
	 * tools.util.file.Write.getPrintWriter( resultFileAddress, false);
	 * SoftNormalizer softNormalizer = new SoftNormalizer();
	 * tools.util.file.TextReader bufferedIterator = new TextReader(
	 * tools.util.file.Reader.getFileBufferReader(fileAddress)); while
	 * (bufferedIterator.hasNext()) { String line = bufferedIterator.next();
	 * String[] split1 = line.split("\t"); String[] tokens =
	 * split1[0].split(" "); if (isNubmer(softNormalizer.normalize(tokens[0]))
	 * && isNubmer(softNormalizer.normalize(tokens[1])) &&
	 * isNubmer(softNormalizer.normalize(tokens[2]))) continue;
	 * writer.println(line); } writer.close(); bufferedIterator.close(); }
	 *
	 * public static void filterMiiiJamFromTrigram(String fileAddress, String
	 * resultFileAddress) throws IOException { // -* // *- PrintWriter writer =
	 * tools.util.file.Write.getPrintWriter( resultFileAddress, false);
	 * tools.util.file.TextReader bufferedIterator = new TextReader(
	 * tools.util.file.Reader.getFileBufferReader(fileAddress)); while
	 * (bufferedIterator.hasNext()) { String line = bufferedIterator.next();
	 * String[] split1 = line.split("\t"); String[] tokens =
	 * split1[0].split(" "); if ("می".equals(tokens[0])) continue; if
	 * ("می".equals(tokens[1])) { if (!"جام".equals(tokens[0])) continue; } if
	 * ("می".equals(tokens[2])) { if (!"جام".equals(tokens[1])) continue; }
	 * writer.println(line); } writer.close(); bufferedIterator.close(); }
	 */
}
