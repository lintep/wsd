package nlp.preprocess.tokenizer.en;

import nlp.preprocess.SerializableReader;
import tools.util.collection.HashSertInteger;
import tools.util.file.FileTools;
import tools.util.file.TextReader;

import java.io.IOException;
import java.util.*;

/**
 * Created by Saeed on 7/25/2016.
 */
public class CompoundWordTokenizer {

    HashSet<String> compoundWord;
    final char delimiter ='_';//'ð';
    int kgramCount=0;

    final static String COMPOUND_SPLITTER ="_|-";

    int maxTokenCount;

    /***
     * input compound word should contacted together with '_' or '-'
     * @param compoundWords
     */
    public CompoundWordTokenizer(Set<String>... compoundWords){

        this.compoundWord=new HashSet<String>();
        HashSertInteger<Integer> kgramCounter=new HashSertInteger(2);
        for(Set<String> compoundWord:compoundWords) {
            for (String token : compoundWord) {
                int tokenCount = token.split("_|-").length;
                if(tokenCount>1) {
                    kgramCounter.add(tokenCount);
                    this.compoundWord.add(token.replaceAll(COMPOUND_SPLITTER, delimiter+""));
                }
                else{
                    tokenCount = token.split(delimiter+"").length;
                    kgramCounter.add(tokenCount);
                    if(tokenCount>1){
                        this.compoundWord.add(token);
                    }
                }
            }
        }

        for (Map.Entry<Integer, Integer> item:kgramCounter.getHashMap().entrySet()) {
            if(item.getKey()>kgramCount){
                kgramCount=item.getKey();
            }
            System.out.println(item.getKey()+"\t"+item.getValue());
        }

        System.out.println("CompoundWord load complete with "+this.compoundWord.size()+" item, and with max k-gram:"+kgramCount);
    }

    public CompoundWordTokenizer(String compoundWordFileAddress) throws IOException {
        this.compoundWord= new HashSet<>();

        Iterator<String> stringIterator = null;
        if (!FileTools.exist(compoundWordFileAddress + ".ser")) {
            stringIterator = new TextReader(compoundWordFileAddress);
        } else {
            stringIterator = new SerializableReader<>(compoundWordFileAddress + ".ser");
        }

        int skipCount = 0;
        while (stringIterator.hasNext()) {
            String newLine = stringIterator.next();
            int wordCount = newLine.split(" ").length;
            if (wordCount > 1 && wordCount <= this.maxTokenCount) {
                compoundWord.add(newLine);
            } else {
                skipCount++;
            }
        }


        System.out.println("skip count: " + skipCount);
        System.out.println("compound word statistical information:");
//        for (int i = maxTokenCount; i > 1; i--) {
//            System.out.println("\t" + i + "\t" + this.compoundWordSet.get(this.maxTokenCount - i).size());
//        }
        System.out.println("Load complete.");
    }


    public HashSet<String> getCompoundWord() {
        return compoundWord;
    }

    public String tokenize(String sentence, int kgramCount){
        StringBuilder result = new StringBuilder();

        StringBuilder compoundWordStringBuilder = new StringBuilder();

        String[] tokens = sentence.split(" ");
        int tokenIndex=0;
        while(tokenIndex<tokens.length){

            int i=0;

            for (i = kgramCount; i > 1; i--) {

                compoundWordStringBuilder.setLength(0);
                for(int j=tokenIndex;j<Math.min(i+tokenIndex,tokens.length);j++){
                    compoundWordStringBuilder.append(tokens[j]);
                    compoundWordStringBuilder.append('_');
                }

                if(compoundWordStringBuilder.length()>0) {
                    compoundWordStringBuilder.setLength(compoundWordStringBuilder.length() - 1);

                    String compoundWordString = compoundWordStringBuilder.toString();

                    if(this.compoundWord.contains(compoundWordString)){
                        result.append(compoundWordString).append(' ');
                        tokenIndex+=i;
                    }
                    else{
                        result.append(tokens[tokenIndex]).append(' ');
                        tokenIndex++;
                    }
                }
                else{
                    tokenIndex++;
                }
            }
        }

        result.setLength(result.length()-1);

        return result.toString();
    }

    public static boolean isCompound(String token) {
        return token.split(COMPOUND_SPLITTER).length>1;
    }

    public List<String> tokenize(List<String> tokenList) {

        List<String> currentTokenList = tokenList;

        StringBuilder compoundWord = new StringBuilder();

        for (int k = this.maxTokenCount; k > 1; k--) {
            HashSet<String> currentCompoundWordSet = this.compoundWord;//this.compoundWordSet.get(this.maxTokenCount - k);
            int index = 0;
            while (index < currentTokenList.size() - k) {
                compoundWord.setLength(0);
                for (int i = index; i < index + k; i++) {
                    compoundWord.append(currentTokenList.get(i));
                    compoundWord.append(' ');
                }
                compoundWord.setLength(compoundWord.length() - 1);

                if (currentCompoundWordSet.contains(compoundWord.toString())) {
                    currentTokenList.set(index, compoundWord.toString());
                    for (int i = index + k - 1; i >= index + 1; i--) {
                        currentTokenList.remove(i);
                    }
                }

                index++;
            }
        }
        return currentTokenList;
    }

}
