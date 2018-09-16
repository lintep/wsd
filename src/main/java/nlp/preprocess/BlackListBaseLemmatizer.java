package nlp.preprocess;

import nlp.text.Utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Saeed on 12/31/2016.
 */
public class BlackListBaseLemmatizer implements LemmatizerInterface {

    Set<String> blackListTokenSet;

    public BlackListBaseLemmatizer(){
        blackListTokenSet=new HashSet<>();
        blackListTokenSet.addAll(Utils.TokenJoinerAdjective);
        blackListTokenSet.addAll(Utils.TokenJoinerNoun);
        blackListTokenSet.addAll(Utils.TokenJoinerVerb);
    }

    @Override
    public String getLemma(String token) {
        StringBuilder lemmaStringBuilder = new StringBuilder();
        String[] splits = token.split(" |_");
        if(splits.length>1) {
            for (String split : splits) {
                if (!blackListTokenSet.contains(split)) {
                    lemmaStringBuilder.append(split);
                    lemmaStringBuilder.append('_');
                }
            }
            if(lemmaStringBuilder.length() == 0){
                lemmaStringBuilder.append(getMaxLenSplit(splits));
            }
            else {
                lemmaStringBuilder.setLength(lemmaStringBuilder.length() - 1);
            }
            return lemmaStringBuilder.toString();
        }
        else{
            return token;
        }
    }

    private String getMaxLenSplit(String[] splits) {
        String result = splits[0];
        for (int i = 1; i < splits.length; i++) {
            if(splits[i].length()>result.length()){
                result=splits[i];
            }
        }
        return result;
    }
}
