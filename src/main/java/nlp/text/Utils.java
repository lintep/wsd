package nlp.text;

import java.util.*;

/**
 * Created by Saeed on 1/1/2017.
 */
public class Utils {

    public final static Set<Character> SentenceFixSplitterChar = new HashSet<>(Arrays.asList(new Character[]{'.', ';' , '؛', ':', '!', '?', '؟'}));
    public final static Set<Character> TokenFixSplitterChar = new HashSet<Character>(Arrays.asList(new Character[]
            {'،' , '(' , ')' , '[' ,']','\'' , '\"' , '|' ,'\\','«','»','«','ˈ',','}));
    public final static Set<Character> TokenSplitterNumChar = new HashSet<Character>(Arrays.asList(new Character[]
            {',' , '/' , '+' , '-'}));

    public final static Set<String> TokenJoinerVerb = new HashSet<String>(Arrays.asList(new String[]
            {"می" , "نمی" , "بر" , "خواهد" , "نخواهد" , "بر" , "اند", "شده", "نشده"}));
    public final static Set<String> TokenJoinerNoun = new HashSet<String>(Arrays.asList(new String[]
            {"ها" , "های" , "ی" , "هایی", "ای"}));
    public final static Set<String> TokenJoinerAdjective = new HashSet<String>(Arrays.asList(new String[]
            {"تر" , "ترین" }));


    public static List<String> split(String text){
        Set<Character> keepDelimiterChars=new HashSet<>();
        keepDelimiterChars.addAll(TokenFixSplitterChar);
        keepDelimiterChars.addAll(SentenceFixSplitterChar);

        String[] tokens = text.split("\\s+");
        if(keepDelimiterChars==null){
            return Arrays.asList(tokens);
        }
        else{
            ArrayList<String> resultTokens = new ArrayList<>();
            StringBuilder tokenStringBuilder = new StringBuilder();
            for (String token : tokens) {
                tokenStringBuilder.setLength(0);
                for (int i = 0; i < token.length(); i++) {
                    char ch = token.charAt(i);
                    if(keepDelimiterChars.contains(ch)){
                        if(tokenStringBuilder.length()>0){
                            resultTokens.add(tokenStringBuilder.toString());
                        }
                        resultTokens.add(ch+"");
                        tokenStringBuilder.setLength(0);
                    }
                    else{
                        tokenStringBuilder.append(ch);
                    }
                }
                if(tokenStringBuilder.length()>0){
                    resultTokens.add(tokenStringBuilder.toString());
                }
            }
            return resultTokens;
        }
    }

    public static ArrayList<String> getSentences(String text) {
        ArrayList<String> resultSentences=new ArrayList<String>();
        StringBuilder sentenceStringBuilder=new StringBuilder();
        sentenceStringBuilder.setLength(0);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            sentenceStringBuilder.append(ch);
            if(SentenceFixSplitterChar.contains(ch)){
                if(sentenceStringBuilder.length()>0){
                    resultSentences.add(sentenceStringBuilder.toString());
                }
                sentenceStringBuilder.setLength(0);
            }
        }
        if(sentenceStringBuilder.length()>0){
            resultSentences.add(sentenceStringBuilder.toString());
        }
        return resultSentences;
    }
}
