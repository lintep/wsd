package nlp.preprocess.fa;

import nlp.preprocess.TokenizerInterface;

import java.util.ArrayList;
import java.util.List;

public class SimpleTokenizer implements TokenizerInterface {

    public List<String> tokenize(String s) {
        List<String> tokens = new ArrayList<String>(64);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                builder.append(ch);
                tokens.add(builder.toString());
                builder.setLength(0);
            } else if (builder.length() > 0) {
                tokens.add(builder.toString());
                builder.setLength(0);
            }
        }

        if (builder.length() > 0)
            tokens.add(builder.toString());

        return tokens;
    }
}
