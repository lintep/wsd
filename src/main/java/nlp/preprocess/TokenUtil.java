package nlp.preprocess;

import nlp.preprocess.fa.SimpleNormalizer;
import tools.util.collection.HashSertInteger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Saeed on 9/8/2016.
 */
public class TokenUtil {
    static Set<Character> whiteSpaceCharSet=new HashSet<>();
    static Set<Character> skipCharSet=new HashSet<>();
    static Set<Character> persianCharSet=new HashSet<>();
    static Set<Character> englishCharSet=new HashSet<>();
    static Set<Character> numericCharSet=new HashSet<>();

    static {

        char[] whiteSpaceCharList = new char[]{' ', '\t', '\n', (char) 10};

        for (char ch : whiteSpaceCharList) {
            whiteSpaceCharSet.add(ch);
        }

        char[] charList = new char[]{'-', '_', '/', '?', '|', '?', '.', ':', ';', '!', '@', '#', '$', '%', '^', '&', '*',
                '(', ')', '{', '}', '~', '\\', '[', ']', '+', '=', ',', '\'', '\"'};
        for (char ch : charList) {
            skipCharSet.add(ch);
        }


        for (int charCode = '0'; charCode <= '9'; charCode++) {
            numericCharSet.add((char) charCode);
        }
        for (int charCode = '۰'; charCode <= '۹'; charCode++) {
            numericCharSet.add((char) charCode);
        }


        for (int charCode = 'a'; charCode <= 'z'; charCode++) {
            englishCharSet.add((char) charCode);
        }
        for (int charCode = 'A'; charCode <= 'Z'; charCode++) {
            englishCharSet.add((char) charCode);
        }

        charList=new char[]{'ئ','أ','ؤ','ئ','ئ','ء','أ','ا','آ','ا','ب','ب','ب','ب','پ','پ','پ','پ','ت','ت','ت','ت',
                'ث','ث','ث','ث','ج','ج','ج','ج','چ','چ','چ','چ','ح','ح','ح','ح','خ','خ','خ','خ','د','د','ذ','ذ','ر','ر',
                'ز','ز','ژ','ژ','س','س','س','س','ش','ش','ش','ش','ص','ص','ص','ص','ض','ض','ض','ض','ط','ط','ط','ط','ظ','ظ',
                'ظ','ظ','ع','ع','ع','ع','غ','غ','غ','غ','ف','ف','ف','ف','ق','ق','ق','ق','ک','ک','ک','ک','گ','گ','گ','گ',
                'ل','ل','ل','ل','م','م','م','م','ن','ن','ن','ن','و','و','ه','ه','ه','ه','ي','ي','ي','ي'};
        for (char ch : charList) {
            persianCharSet.add(ch);
        }
        for (int charCode = 'ا'; charCode <= 'ی'; charCode++) {
            persianCharSet.add((char)charCode);
        }
    }

    public static Set<Character> getCharSet(TokenType... tokenTypes) {

        HashSet<Character> result = new HashSet<Character>();

        for (TokenType tokenType : tokenTypes) {

            switch (tokenType){

                case Persian:
                    result.addAll(persianCharSet);
                    break;

                case English:
                    result.addAll(englishCharSet);
                    break;

                case Numeric:
                    result.addAll(numericCharSet);
                    break;

                case Skip:
                    result.addAll(skipCharSet);
                    break;

            }
        }
        return result;
    }

    public static TokenType getTokensType(String[] tokens){
        HashSertInteger<TokenType> tokenTypeCounter=new HashSertInteger<>();
        for (String token : tokens) {
            tokenTypeCounter.put(getTokenType(token));
        }
        return tokenTypeCounter.getHashMap().entrySet().stream().max((a,b) -> ((Integer)a.getValue()).compareTo(b.getValue())).get().getKey();
    }

    /***
     * This method used for recognizing token type
     * numeric, persian, english or any combination of them
     * @param token input token
     * @return token type as enum {@link TokenType}
     */
    public static TokenType getTokenType(String token){
        boolean hasEnglishCharTokenLevel=false;
        boolean hasPersianCharTokenLevel=false;
        boolean hasNumericCharTokenLevel=false;
        boolean hasOtherCharTokenLevel=false;

        int skipCounter=0;
        int whiteSpaceCounter=0;
        for (int i = 0; i <token.length() ; i++) {
            char ch = token.charAt(i);

            if(skipCharSet.contains(ch)){
                skipCounter++;
                continue;
            }
            if(whiteSpaceCharSet.contains(ch)){
                whiteSpaceCounter++;
                continue;
            }

            boolean hasEnglishChar=englishCharSet.contains(ch);
            boolean hasNumericChar=numericCharSet.contains(ch);
            boolean hasPersianChar=persianCharSet.contains(ch);
            boolean hasOtherChar=false;
            if(!hasEnglishChar && !hasNumericChar && !hasPersianChar){
                hasOtherChar=true;
            }

            if(!hasEnglishCharTokenLevel)hasEnglishCharTokenLevel=hasEnglishChar;
            if(!hasNumericCharTokenLevel)hasNumericCharTokenLevel=hasNumericChar;
            if(!hasPersianCharTokenLevel)hasPersianCharTokenLevel=hasPersianChar;
            if(!hasOtherCharTokenLevel)hasOtherCharTokenLevel=hasOtherChar;
        }

        if(skipCounter+whiteSpaceCounter==token.length()){
            return TokenType.Skip;
        }

        if(hasOtherCharTokenLevel){
            if(skipCounter==0) {
                return TokenType.Other;
            }
            else{
                return TokenType.OtherSkip;
            }
        }

        if(hasEnglishCharTokenLevel && hasNumericCharTokenLevel && hasPersianCharTokenLevel){
            if(skipCounter==0) {
                return TokenType.NumericEnglishPersian;
            }
            else{
                return TokenType.NumericEnglishPersianSkip;
            }
        }

        if(hasEnglishCharTokenLevel && hasNumericCharTokenLevel){
            if(skipCounter==0) {
                return TokenType.NumericEnglish;
            }
            else{
                return TokenType.NumericEnglishSkip;
            }
        }

        if(hasEnglishCharTokenLevel && hasPersianCharTokenLevel){
            if(skipCounter==0) {
                return TokenType.EnglishPersian;
            }
            else{
                return TokenType.EnglishPersianSkip;
            }
        }

        if(hasPersianCharTokenLevel && hasNumericCharTokenLevel){
            if(skipCounter==0) {
                return TokenType.NumericPersian;
            }
            else{
                return TokenType.NumericPersianSkip;
            }
        }

        if(hasEnglishCharTokenLevel){
            if(skipCounter==0) {
                return TokenType.English;
            }
            else{
                return TokenType.EnglishSkip;
            }
        }

        if(hasNumericCharTokenLevel){
            if(skipCounter==0) {
                return TokenType.Numeric;
            }
            else{
                return TokenType.NumericSkip;
            }
        }

        if(hasPersianCharTokenLevel){
            if(skipCounter==0) {
                return TokenType.Persian;
            }
            else{
                return TokenType.PersianSkip;
            }
        }

        throw new NullPointerException("Please check your logic");
    }

    public static boolean hasPersianChar(TokenType tokenType) {
        if(tokenType== TokenType.Persian ||
                tokenType== TokenUtil.TokenType.EnglishPersian ||
                tokenType== TokenUtil.TokenType.PersianSkip ||
                tokenType== TokenUtil.TokenType.NumericPersian ||
                tokenType== TokenUtil.TokenType.EnglishPersianSkip ||
                tokenType== TokenUtil.TokenType.NumericEnglishPersian ||
                tokenType== TokenUtil.TokenType.NumericPersianSkip ||
                tokenType== TokenUtil.TokenType.NumericEnglishPersianSkip)
        {
            return true;
        }
        return false;

    }


    public enum TokenType{
        English,
        Persian,
        Numeric,
        EnglishPersian,
        NumericPersian,
        NumericEnglish,
        NumericEnglishPersian,
        Other,
        EnglishSkip,
        PersianSkip,
        NumericSkip,
        EnglishPersianSkip,
        NumericPersianSkip,
        NumericEnglishSkip,
        NumericEnglishPersianSkip,
        OtherSkip,
        Skip;

        public boolean islegal(){
            switch (this){
                case English: return true;
                case Persian: return true;
                case EnglishPersian: return true;
                case NumericEnglish: return true;
                case NumericEnglishPersian: return true;
                default:return false;
            }
        }
    }


    public static void main(String[] args) throws IOException {

        PrintWriter[] printerAll=new PrintWriter[4];
        for (int i = 0; i < 4; i++) {
            printerAll[i]= new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(args[0] + "_" + i, false), StandardCharsets.UTF_8), true);
        }

        SimpleNormalizer normalizer=new SimpleNormalizer();
        //persian 0-gram
        ArrayList<Character> charListPersian = new ArrayList<Character>(TokenUtil.persianCharSet.stream().map(s -> normalizer.normalize(s + "").charAt(0)).collect(Collectors.toSet()));
        for (Character character : charListPersian) {
            printerAll[0].println(character);
        }
        for (Character character1 : charListPersian) {
            for (Character character2 : charListPersian) {
                printerAll[0].println(character1+""+character2);
            }
        }
        for (Character character : charListPersian) {
            for (Character character1 : charListPersian) {
                for (Character character2 : charListPersian) {
                    printerAll[0].println(character+""+character1 + "" + character2);
                }
            }
        }

        //english 0-gram
        ArrayList<Character> charListEnglish = new ArrayList<Character>(TokenUtil.englishCharSet.stream().map(s -> normalizer.normalize(s + "").charAt(0)).collect(Collectors.toSet()));
        for (Character character : charListEnglish) {
            printerAll[0].println(character);
        }
        for (Character character1 : charListEnglish) {
            for (Character character2 : charListEnglish) {
                printerAll[0].println(character1+""+character2);
            }
        }
        for (Character character : charListEnglish) {
            for (Character character1 : charListEnglish) {
                for (Character character2 : charListEnglish) {
                    printerAll[0].println(character+""+character1 + "" + character2);
                }
            }
        }

        //persian 1-gram
        final int[] counter = {0,0,0};
        Files.readAllLines(Paths.get(args[1])).forEach(s -> {

            counter[0]++;
            if(counter[0]%1000==0) {
                printerAll[1].flush();
                System.out.println("unigram "+counter[0]+" line handled.");
            }

            String split = s.split("\t")[1];
            if(TokenUtil.getTokenType(split)== TokenType.Persian){
                printerAll[1].println(split);
            }

        });

        //persian 2-gram
        Files.readAllLines(Paths.get(args[2])).forEach(s -> {

            counter[1]++;
            if(counter[1]%1000==0) {
                printerAll[2].flush();
                System.out.println("bigram "+counter[1]+" line handled.");
            }

            String split = s.split("\t")[1].trim();
            if(TokenUtil.getTokenType(split)== TokenType.Persian){
                printerAll[2].println(split);

                int index = split.indexOf(' ');
                printerAll[2].println(split.substring(0,index+2));
            }

        });

        //persian 3-gram
        Files.readAllLines(Paths.get(args[3])).forEach(s -> {

            counter[2]++;
            if(counter[2]%1000==0) {
                printerAll[3].flush();
                System.out.println("trigram "+counter[2]+" line handled.");
            }

            String split = s.split("\t")[1].trim();
            if(TokenUtil.getTokenType(split)== TokenType.Persian){
                printerAll[3].println(split);

                int index = split.lastIndexOf(' ');
                printerAll[3].println(split.substring(0,index+2));
            }

        });

        for (PrintWriter printWriter : printerAll) {
            printWriter.close();
        }

        System.out.println("Operation complete with "+counter+".");
    }
}
