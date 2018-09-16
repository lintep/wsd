package nlp.languagemodel;

import tools.util.BitCodec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Saeed on 1/23/15.
 */
public class SimpleBigramUtils {

    final int BITCOUNT = 16;
    int MAXTOKENCOUNT = (int) Math.round(Math.pow(2.0, BITCOUNT)) - 2;

    // final int ILEGALTOKENID = MAXTOKENCOUNT + 1;

    HashMap<Integer, String> tokenidToToken;
    HashMap<String, Integer> tokenToId;


    public SimpleBigramUtils(Set<String> tokens)
            throws IOException {

        this.tokenToId = new HashMap<String, Integer>(MAXTOKENCOUNT);
        this.tokenidToToken = new HashMap<Integer, String>(MAXTOKENCOUNT);

        int lineNumber = 0;

        for (String token : tokens) {

            lineNumber++;

            if (this.tokenToId.size() <= MAXTOKENCOUNT) {
                if (!this.tokenToId.containsKey(token)) {
                    this.tokenToId.put(token, lineNumber);
                    this.tokenidToToken.put(lineNumber, token);
                }
            }
        }

        System.out
                .println("Trigram load complete with "
                        + tokenToId.size()
                        + " legal tokens (skipped tokens: "
                        + ((lineNumber - MAXTOKENCOUNT) > 0 ? (lineNumber - MAXTOKENCOUNT)
                        : 0) + ").");

    }

    public SimpleBigramUtils(Map<String,Integer> tokenIdMap)
            throws IOException {

        this.tokenToId = new HashMap<String, Integer>(MAXTOKENCOUNT);
        this.tokenidToToken = new HashMap<Integer, String>(MAXTOKENCOUNT);

        int lineNumber = 0;

        for (Map.Entry<String, Integer> tokenId : tokenIdMap.entrySet()) {

            lineNumber++;

            this.tokenToId.put(tokenId.getKey(), tokenId.getValue());
            this.tokenidToToken.put(tokenId.getValue(), tokenId.getKey());
        }

        System.out
                .println("Trigram load complete with "
                        + tokenToId.size()
                        + " legal tokens (skipped tokens: "
                        + ((lineNumber - MAXTOKENCOUNT) > 0 ? (lineNumber - MAXTOKENCOUNT)
                        : 0) + ").");

    }

    public String getToken(int code) {
        return getCodeToken(BitCodec.decodeInt(code, BITCOUNT));
    }


    public HashMap<String, Integer> getTokenToId() {
        return tokenToId;
    }

    private String getCodeToken(BitCodec.TwoInt twoInt) {
        String twoToken = "";
        if (this.tokenidToToken.containsKey(twoInt.getInt1())) {
            twoToken += this.tokenidToToken.get(twoInt.getInt1()) + " ";
        } else {
            if (twoInt.getInt1() != 0) {
                System.out.println("***1* " + twoInt.getInt1() + " for " + twoInt);
                return null;
            }
        }

        if (this.tokenidToToken.containsKey(twoInt.getInt2())) {
            twoToken += this.tokenidToToken.get(twoInt.getInt2());
        } else {
            if (twoInt.getInt2() != 0) {
                System.out.println("***2* " + twoInt.getInt2() + " for " + twoInt);
                return null;
            }
        }

        return twoToken;
    }

    /**
     *
     * @param tokens
     * @return -1 more than 3 token -2 exist illegal token
     * @throws Exception
     */
    public int getMultiTokenCode(String[] tokens) throws Exception {

        int tokenCount = tokens.length;
        if (tokenCount == 0) {
            return 0;
        }



        for (String token : tokens) {
            if (!this.tokenToId.containsKey(token)) {
                return -2;
            }
        }

        if (tokenCount == 1) {
            return this.getTokensCode(tokens[0]);
        } else if (tokenCount == 2) {
            return this.getTokensCode(tokens[0], tokens[1]);
        }else {
            return -1;
        }
    }

    public int getTokenId(String token) {
        if (this.tokenToId.containsKey(token)) {
            return this.tokenToId.get(token);
        } else {
            return -1;
        }
    }

    public int getTokensCode(String token1) throws Exception {
        return BitCodec.encodeInt(0, getTokenId(token1), BITCOUNT);
    }

    public int getTokensCode(String token1, String token2) throws Exception {
        return BitCodec.encodeInt(getTokenId(token1), getTokenId(token2),
                BITCOUNT);
    }

    public int getTokensCodeDistinct(String token1, String token2) throws Exception {
        int code1=getTokenId(token1);
        int code2=getTokenId(token2);
        if(code1<0 || code2<0){
            return -1;
        }
        return new BitCodec.TwoInt(code1, code2).getDistinctSortedCode(BITCOUNT);
    }

    public boolean addNewToken(String token) {
        if (!this.tokenToId.containsKey(token)) {
            int newId = this.tokenToId.size()+1;
            if(newId>MAXTOKENCOUNT){
                return false;
            }
            else{
                this.tokenToId.put(token,newId);
            }
        }
        return true;
    }

}