package nlp.pos;

import nlp.preprocess.TokenizerInterface;
import nlp.preprocess.fa.SimpleNormalizer;
import nlp.text.Utils;
import tools.util.BitCodec;

import java.util.*;

public class PTokenizer implements TokenizerInterface {

    BitCodec bitCodec;
    int bitCodecLimitLength;
    Map<String, Integer> tokenId;
    Map<Long, Double> posTokenizerFScore;
    double minimumValue = 0.00000000000000001;
    double threshold = 0.4;

    public static PTokenizer getInstance(String dataPath) throws Exception {
        HashMap<String, Integer> tokenId = new HashMap<String, Integer>();
        int i = 0;
        for (String token : tools.util.file.Reader.getStringFromTextFile(
                dataPath + "sentences.tokens", true)) {
            i++;
            tokenId.put(token, i);
        }

        HashMap<Long, Integer> posTaggedTTF = tools.util.file.Reader
                .getKeyValueLongIntegerFromTextFile(
                        dataPath + "lmTokenizer.lm",
                        -1, true, "\t");
        HashMap<Long, Integer> languageModelTaggedTTF = tools.util.file.Reader
                .getKeyValueLongIntegerFromTextFile(
                        dataPath + "lmForTokenizer.lm",
                        -1, true, "\t");

        return new PTokenizer(tokenId, posTaggedTTF,
                languageModelTaggedTTF, 21);

    }

    public PTokenizer(Map<String, Integer> tokenId,
                      Map<Long, Integer> posTaggedTTF,
                      Map<Long, Integer> languageModelTaggedTTF,
                      int bitCodecLimitLength) {
        initialize(tokenId, posTaggedTTF, languageModelTaggedTTF,
                bitCodecLimitLength);
    }

    private void initialize(Map<String, Integer> tokenId,
                            Map<Long, Integer> posTaggedTTF,
                            Map<Long, Integer> languageModelTaggedTTF,
                            int bitCodecLimitLength) {
        this.tokenId = tokenId;
        this.bitCodecLimitLength = bitCodecLimitLength;
        this.bitCodec = new BitCodec();
        this.posTokenizerFScore = new HashMap<Long, Double>();
        for (Long triGramId : posTaggedTTF.keySet()) {
            if (languageModelTaggedTTF.containsKey(triGramId)) {
                this.posTokenizerFScore.put(triGramId,
                        (double) posTaggedTTF.get(triGramId)
                                / languageModelTaggedTTF.get(triGramId));
            } else {
                this.posTokenizerFScore.put(triGramId, minimumValue);
            }
        }
    }

    public String tokenize2(String inputString) throws Exception {
        String result = "";
        // long uniGramCode;
        long biGramCode;
        long triGramCode;
        // double uniGramCodeFScore;
        double biGramCodeFScore;
        double triGramCodeFScore;
        String[] inTokens = inputString.split(" ");
        int i = 0;
        while (i < inTokens.length) {
            if (tokenId.containsKey(inTokens[i])) {
                // uniGramCode = this.bitCodec
                // .encode(0, 0, this.tokenId.get(inTokens[i]),
                // this.bitCodecLimitLength);
                // uniGramCodeFScore = posTokenizerFScore.get(uniGramCode);
                if (i + 1 < inTokens.length
                        && this.tokenId.containsKey(inTokens[i + 1])) {
                    biGramCode = this.bitCodec.encode(0,
                            this.tokenId.get(inTokens[i]),
                            this.tokenId.get(inTokens[i + 1]),
                            this.bitCodecLimitLength);
                    biGramCodeFScore = getPosTokenizerFScore(biGramCode);
                    if (biGramCodeFScore > 0)
                        System.out.println("biGramCodeFScore:"
                                + biGramCodeFScore);
                    if (i + 2 < inTokens.length
                            && tokenId.containsKey(inTokens[i + 2])) {
                        triGramCode = this.bitCodec.encode(
                                this.tokenId.get(inTokens[i]),
                                this.tokenId.get(inTokens[i + 1]),
                                this.tokenId.get(inTokens[i + 2]),
                                this.bitCodecLimitLength);
                        triGramCodeFScore = getPosTokenizerFScore(triGramCode);
                        if (triGramCodeFScore > 0)
                            System.out.println("triGramCodeFScore:"
                                    + triGramCodeFScore);
                        if (triGramCodeFScore > threshold) {
                            result += inTokens[i] + " " + inTokens[i + 1] + " "
                                    + inTokens[i + 2] + "\n";
                            i += 2;
                        } else {
                            if (biGramCodeFScore > threshold) {
                                result += inTokens[i] + " " + inTokens[i + 1]
                                        + "\n";
                                i += 1;
                            } else {
                                result += inTokens[i] + "\n";
                            }
                        }
                    } else {
                        if (biGramCodeFScore > threshold) {
                            result += inTokens[i] + " " + inTokens[i + 1]
                                    + "\n";
                            i += 1;
                        } else {
                            result += inTokens[i] + "\n";
                        }
                    }
                } else {
                    result += inTokens[i] + "\n";
                }
            } else {
                result += inTokens[i] + "\n";
            }
            i++;
        }
        return result;
    }

    public List<String> tokenize(String inputString) {
        List<String> result = new ArrayList<String>();
        // long uniGramCode;
        long biGramCode;
        long triGramCode;
        // double uniGramCodeFScore;
        double biGramCodeFScore;
        double triGramCodeFScore;
        List<String> inTokens = Utils.split(inputString);
        int i = 0;
        while (i < inTokens.size()) {
            if (tokenId.containsKey(inTokens.get(i))) {
                // uniGramCode = this.bitCodec
                // .encode(0, 0, this.tokenId.get(inTokens[i]),
                // this.bitCodecLimitLength);
                // uniGramCodeFScore = posTokenizerFScore.get(uniGramCode);
                try {
                    if (i + 1 < inTokens.size()
                            && this.tokenId.containsKey(inTokens.get(i + 1))) {
                        biGramCode = this.bitCodec.encode(0,
                                this.tokenId.get(inTokens.get(i)),
                                this.tokenId.get(inTokens.get(i + 1)),
                                this.bitCodecLimitLength);
                        biGramCodeFScore = getPosTokenizerFScore(biGramCode);
                        // if(biGramCodeFScore>0)
                        // System.out.println("biGramCodeFScore:"+biGramCodeFScore);
                        if (i + 2 < inTokens.size()
                                && tokenId.containsKey(inTokens.get(i + 2))) {
                            triGramCode = this.bitCodec.encode(
                                    this.tokenId.get(inTokens.get(i)),
                                    this.tokenId.get(inTokens.get(i + 1)),
                                    this.tokenId.get(inTokens.get(i + 2)),
                                    this.bitCodecLimitLength);
                            triGramCodeFScore = getPosTokenizerFScore(triGramCode);
                            // if(triGramCodeFScore>0)
                            // System.out.println("triGramCodeFScore:"+triGramCodeFScore);
                            if (triGramCodeFScore > threshold) {
                                result.add(inTokens.get(i) + " " + inTokens.get(i + 1)
                                        + " " + inTokens.get(i + 2));
                                i += 2;
                            } else {
                                if (biGramCodeFScore > threshold) {
                                    result.add(inTokens.get(i) + " " + inTokens.get(i + 1));
                                    i += 1;
                                } else {
                                    result.add(inTokens.get(i));
                                }
                            }
                        } else {
                            if (biGramCodeFScore > threshold) {
                                result.add(inTokens.get(i) + " " + inTokens.get(i + 1));
                                i += 1;
                            } else {
                                result.add(inTokens.get(i));
                            }
                        }
                    } else {
                        result.add(inTokens.get(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result.add(inTokens.get(i));
                }
            } else {
                result.add(inTokens.get(i));
            }
            i++;
        }
        return result;
    }

    private double getPosTokenizerFScore(long triGramCode) {
        return this.posTokenizerFScore.containsKey(triGramCode) ? this.posTokenizerFScore
                .get(triGramCode) : 0;
    }
}
