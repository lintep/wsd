package nlp.preprocess.fa;

import nlp.pos.PTokenizer;
import nlp.preprocess.NormalizerInterface;
import nlp.preprocess.TokenUtil;

import java.util.Set;

public class NormalizerPurePersian implements NormalizerInterface {

    NormalizerInterface normalizer;
    TokenUtil tokenUtil;
    String replaceToken;

    final static String defaultReplaceToken="%%%";
    public NormalizerPurePersian(NormalizerInterface normalizer) {
        this(normalizer,defaultReplaceToken);
    }

    public NormalizerPurePersian(NormalizerInterface normalizer, String replaceToken) {
        this.normalizer=normalizer;
        this.tokenUtil=new TokenUtil();
        this.replaceToken=replaceToken;
    }

    @Override
    public String normalize(String text) {
        String[] tokens = normalizer.normalize(text).split("\\s+");
        StringBuilder result=new StringBuilder();
        for (String token : tokens) {
            TokenUtil.TokenType tokenType = TokenUtil.getTokenType(token);
            if(tokenType== TokenUtil.TokenType.Persian){
                result.append(token);
                result.append(' ');
            }
            else if(TokenUtil.hasPersianChar(tokenType)) {
                result.append(replaceUnPersianCharWithSpace(token));
                result.append(' ');
            }
            else{
                result.append(replaceToken);
                result.append(' ');
            }
        }
        if(result.length()>0){
            result.setLength(result.length()-1);
        }
        return result.toString();
    }

    public static String replaceUnPersianCharWithSpace(String token){
        StringBuilder result=new StringBuilder();
        Set<Character> charSet=TokenUtil.getCharSet(TokenUtil.TokenType.Persian);
        for (int i = 0; i <token.length() ; i++) {
            char ch = token.charAt(i);
            if (charSet.contains(ch)) {
                result.append(ch);
            }
            else{
                result.append(' ');
            }
        }
        return result.toString();
    }

}

