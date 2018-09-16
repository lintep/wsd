package nlp.languagemodel;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import com.google.common.base.Stopwatch;
import nlp.preprocess.tokenizer.en.CompoundWordTokenizer;
import tools.util.Directory;
import tools.util.File;
import tools.util.Str;
import tools.util.Time;
import tools.util.collection.PairValue;
import tools.util.directory.Search;
import tools.util.file.Reader;
import tools.util.file.SparkTextReader;
import tools.util.file.TextReader;
import tools.util.file.Write;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Saeed on 7/25/2016.
 */
public class SimpleBigramModel implements LanguageModelScorer , Serializable{

    //!!!!!!!!! caseSensitive lemma scorer

    final String filterTokenReplace = "@@@";
    final int windowSize = 10;

    long edgeWeight=0;

    SimpleBigramUtils simpleBigramUtils;
    IntIntOpenHashMap bigramCount;
    CompoundWordTokenizer compoundWordTokenizer;

    public SimpleBigramModel(Set<String> tokenSet, String fileAddress, CompoundWordTokenizer compoundWordTokenizer, Set<String> legalPosTag) throws Exception {
        this.simpleBigramUtils = new SimpleBigramUtils(tokenSet);
        this.simpleBigramUtils.addNewToken(filterTokenReplace);

        this.bigramCount = IntIntOpenHashMap.newInstanceWithExpectedSize(30000000);
        this.compoundWordTokenizer = compoundWordTokenizer;

        TextReader corpusFileTextReader = new TextReader(fileAddress);

        tools.util.Time.setStartTimeForNow();
        int lineCounter = 0;
        while (corpusFileTextReader.hasNext()) {
            String newLine = corpusFileTextReader.next();

            updateModel(newLine, legalPosTag);

            lineCounter++;
            if (lineCounter % 10000 == 0) {
                System.out.println("LineCounter: " + lineCounter + " \tin " + tools.util.Time.getCurrentTime() / 1000 +
                        " sec  ("+this.bigramCount.size() + " entry, and sum edge weight = \"+edgeWeight+\")\t\t" + newLine);
            }

//            if (lineCounter > 100000) {
//                break;
//            }
        }
        corpusFileTextReader.close();

        System.out.println("SimpleBigramModel load complete with " + this.bigramCount.size() + " entry (sum edge weight = "+edgeWeight+").");
    }

    private void updateModel(String newLine, Set<String> legalPosTag) throws Exception {
        String[] splits = newLine.split("\t");
        if (splits.length < 3) {
            System.out.println("\t\t\t\t\t\tHAAAAAAAAAAAAA\t\t\t" + newLine);
            return;
        }

        String newLineTokenized = compoundWordTokenizer.tokenize(splits[2], 3);

        ArrayList<String> tokenList = new ArrayList<String>();

        String[] tokens = newLineTokenized.split(" ");
        String[] poses = splits[1].split(" ");

        if (poses.length != tokens.length) {
            poses = getCorrectPosArray(tokens, poses,legalPosTag);
        }

        for (int i = 0; i < tokens.length; i++) {
            if (legalPosTag.contains(poses[i])) {
                int code = this.simpleBigramUtils.getTokensCode(tokens[i]);
                if (code > 0) {
                    edgeWeight++;
                    tokenList.add(tokens[i]);
                    if (!this.bigramCount.containsKey(code)) {
                        this.bigramCount.putIfAbsent(code, 1);
                    } else {
                        this.bigramCount.addTo(code, 1);
                    }
                } else {
                    tokenList.add(filterTokenReplace);
                }
            } else {
                tokenList.add(filterTokenReplace);
            }
        }


        HashSet<Integer> bigramCodeSet = new HashSet<Integer>();

        tokenList.remove(filterTokenReplace);

        for (int i = 0; i < tokenList.size(); i++) {
            for (int j = i + 1; j < Math.min(i + windowSize, tokenList.size()); j++) {
                int code = this.simpleBigramUtils.getTokensCodeDistinct(tokenList.get(i), tokenList.get(j));
                bigramCodeSet.add(code);
            }
        }

        for (int code : bigramCodeSet) {
            edgeWeight++;
            if (!this.bigramCount.containsKey(code)) {
                this.bigramCount.putIfAbsent(code, 1);
            } else {
                this.bigramCount.addTo(code, 1);
            }
        }

    }

    private String[] getCorrectPosArray(String[] tokens, String[] poses, Set<String> legalPosSet) {
        int tokenCounter = -1;
        ArrayList<String> posList = new ArrayList<String>();
        for (String token:tokens){
            tokenCounter++;
            String[] compoundWordTokens = token.split("ð");
            if(compoundWordTokens.length>1){
                String pos="NULL";
                if(legalPosSet.contains(poses[tokenCounter])) {
                    pos = poses[tokenCounter];
                }
                else {
                    for (int i = 1; i < compoundWordTokens.length; i++) {
                        tokenCounter++;
                        if (legalPosSet.contains(poses[tokenCounter])) {
                            pos = poses[tokenCounter];
                            break;
                        }
                    }
                }
                posList.add(pos);
            }
            else {
                if(tokenCounter>=poses.length){
                    System.out.println();
                }
                posList.add(poses[tokenCounter]);
            }
        }

        String[] result = new String[posList.size()];
        for (int i = 0; i <posList.size() ; i++) {
            result[i]=posList.get(i);
        }

        return result;
    }

    public long getScore(String lemma) throws Exception {
        int code = this.simpleBigramUtils.getTokensCode(lemma);
        return getScore(code);
    }

    public long getScore(String lemma1, String lemma2) throws Exception {
        int code = this.simpleBigramUtils.getTokensCodeDistinct(lemma1, lemma2);
        return getScore(code);
    }

//    public EdgescoredGraph(){
//
//    }

    public long getScore(String lemma1, String lemma2, String lemma3) throws Exception {
        return -1;
    }

    int getScore(int code) {
        if (!this.bigramCount.containsKey(code)) {
            return 0;
        } else {
            return this.bigramCount.get(code);
        }
    }

    public void saveModel(String path) throws IOException {
        Directory.create(path);

        PrintWriter printWriter = Write.getPrintWriter(path + "IntIntOpenHash", false);
        PrintWriter printWriterWithToken = Write.getPrintWriter(path + "BigramIntOpenHash", false);

        Iterator<IntIntCursor> iter = this.bigramCount.iterator();
        int counter = 0;
        while (iter.hasNext()) {
            IntIntCursor item = iter.next();
            printWriter.println(item.key + '\t' + item.value);
            printWriterWithToken.println(simpleBigramUtils.getToken(item.key) + '\t' + item.value);
            counter++;
            if (counter % 10000 == 0) {
                System.out.println("counter:" + counter);
            }
        }
        printWriter.close();
        printWriterWithToken.close();

        PrintWriter printWriterCompoundWord = Write.getPrintWriter(path + "CompoundWord", false);
        for (String item : this.compoundWordTokenizer.getCompoundWord()) {
            printWriterCompoundWord.println(item);
        }
        printWriterCompoundWord.close();

        PrintWriter printWriterSimpleBigramUtils = Write.getPrintWriter(path + "SimpleBigramUtils", false);
        for (Map.Entry<String, Integer> item : this.simpleBigramUtils.getTokenToId().entrySet()) {
            printWriterSimpleBigramUtils.println(item.getKey() + "\t" + item.getValue());
        }
        printWriterSimpleBigramUtils.close();

        System.out.println("save complete.\t\t counter:" + counter);
    }

    SimpleBigramModel(String path) throws Exception {
        Time.setStartTimeForNow();
        HashMap<String, Integer> tokenIdMap = Reader.getKeyValueStringIntegerFromTextFile(path+"SimpleBigramUtils", -1, true, "\t");
        simpleBigramUtils=new SimpleBigramUtils(tokenIdMap);

//        if(tools.util.File.exist(path + "BigramIntOpenHash.map.obj")){
//            loadBigramCountFromSavedMap(path + "BigramIntOpenHash.map.obj");
//        }
//        else {
            this.bigramCount = IntIntOpenHashMap.newInstanceWithExpectedSize(1000000);

            TextReader intIntOpenHashIterator = new TextReader(path + "BigramIntOpenHash");

            int counter = 0;
            while (intIntOpenHashIterator.hasNext()) {
                String newLine = intIntOpenHashIterator.next();
//                if(newLine.indexOf("category film")>=0){
//                    System.out.println(newLine);
//                }
                String[] splites = newLine.split("\t");
                String[] tokens = splites[0].split(" ");
                int code = -1;
                if (tokens.length == 1) {
                    code = simpleBigramUtils.getTokensCode(tokens[0]);
                } else {
                    code = simpleBigramUtils.getTokensCodeDistinct(tokens[0], tokens[1]);//??????????????????????????!!!!!!!!!!!!!!!!!!!!!!!
                }
                int score = Integer.parseInt(splites[1]);
                this.bigramCount.put(code, score);
                counter++;
                if (counter % 10000 == 0) {
                    System.out.println("counter:" + counter);
                }
            }
            intIntOpenHashIterator.close();
//        }

        this.compoundWordTokenizer=new CompoundWordTokenizer(new HashSet<String>(Reader.getTextLinesString(path + "CompoundWord",true)));


        if(File.exist(path + "tokenCount")) {
            TextReader tokenCount = new TextReader(path + "tokenCount");

            counter = 0;
            while (tokenCount.hasNext()) {
                String newLine = tokenCount.next();
                String[] splites = newLine.split("\t");
                int code = simpleBigramUtils.getTokensCode(splites[0]);
                int score = Integer.parseInt(splites[1]);
                this.bigramCount.put(code, score);
                counter++;
                if (counter % 10000 == 0) {
                    System.out.println("tokenCount counter:" + counter);
                }
            }
            tokenCount.close();
        }
        System.out.println("load complete on "+ Str.getFormatedLong(Time.getTimeLengthForNow())+" ms.");
    }

    public CompoundWordTokenizer getCompoundWordTokenizer() {
        return compoundWordTokenizer;
    }

    private Map<Integer,Integer> getMap(){
        Map<Integer,Integer> result=new HashMap<Integer, Integer>(this.bigramCount.size());
        Iterator<IntIntCursor> iterator = this.bigramCount.iterator();
        while (iterator.hasNext()){
            IntIntCursor item = iterator.next();
            result.put(item.key,item.value);
        }
        return result;
    }

    public void saveMap(String fileAddress) throws IOException {
        new ObjectOutputStream(new FileOutputStream(fileAddress+".map.obj")).writeObject(getMap());
        System.out.println("save map complete.");
    }

    void loadBigramCountFromSavedMap(String savedMapObjectFileAddress) throws IOException, ClassNotFoundException {
        System.out.println("loading saved map ...");
        boolean hasPostfix = savedMapObjectFileAddress.indexOf(".map.obj") == savedMapObjectFileAddress.length() -
                (".map.obj").length();
        savedMapObjectFileAddress=hasPostfix?savedMapObjectFileAddress:savedMapObjectFileAddress+".map.obj";
        Map<Integer,Integer> map= (Map<Integer, Integer>) new ObjectInputStream(new FileInputStream(savedMapObjectFileAddress)).readObject();
        this.bigramCount=new IntIntOpenHashMap(map.size());
        map.entrySet().forEach(item -> this.bigramCount.put(item.getKey(),item.getValue()));
        System.out.println("load from saved map obj complete.");
    }

    public static SimpleBigramModel newInstance(String path) throws Exception {
        SimpleBigramModel simpleBigramModel=new SimpleBigramModel(path);
        return simpleBigramModel;
    }

    private SimpleBigramModel(){

    }

    public static SimpleBigramModel newInstance(String tokenFileAddress,
                                                String compoundWordFileAddress,
                                                String bigramTokenCountFileAddress,
                                                String tokenCountFileAddress) throws IOException {
        SimpleBigramUtils simpleBigramUtils = new SimpleBigramUtils(new HashSet<String>(Reader.getTextLinesString(tokenFileAddress,true)));
        return newInstance(simpleBigramUtils,compoundWordFileAddress,bigramTokenCountFileAddress,tokenCountFileAddress);
    }

    public static SimpleBigramModel newInstance(SimpleBigramUtils simpleBigramUtils,
                                                String compoundWordFileAddress,
                                                String bigramTokenCountFileAddress,
                                                String tokenCountFileAddress
    ) throws IOException {
        Stopwatch stopwatch= Stopwatch.createStarted();

        stopwatch.start();

        SimpleBigramModel simpleBigramModel=new SimpleBigramModel();

        simpleBigramModel.simpleBigramUtils = simpleBigramUtils;

        simpleBigramModel.compoundWordTokenizer=new CompoundWordTokenizer(new HashSet<String>(Reader.getTextLinesString(compoundWordFileAddress,true)));

        simpleBigramModel.bigramCount = IntIntOpenHashMap.newInstanceWithExpectedSize(1000000);

        int counter = 0;
        int skipCounter = 0;
        if(Directory.isDirectory(bigramTokenCountFileAddress)){
            for (java.io.File file : Search.getFilesForPath(bigramTokenCountFileAddress,false,null,0)){
                TextReader intIntOpenHashIterator = new TextReader(file.getAbsolutePath());
                while (intIntOpenHashIterator.hasNext()) {
                    String[] splites = intIntOpenHashIterator.next().split("\t");
                    String[] tokens = splites[0].split(" ");
                    int code = -1;
                    int score = -1;
                    try {
                        if (tokens.length == 1) {
                            code = simpleBigramModel.simpleBigramUtils.getTokensCode(tokens[0]);
                        } else {
                            code = simpleBigramModel.simpleBigramUtils.getTokensCodeDistinct(tokens[0], tokens[1]);
                        }
                        if (code < 0) {
                            skipCounter++;
                            continue;
                        }
                        score = Integer.parseInt(splites[1]);
                    } catch (Exception e) {
                        skipCounter++;
                        continue;
                    }

                    simpleBigramModel.bigramCount.put(code, score);
                    counter++;
                    if (counter % 10000 == 0) {
                        System.out.println("counter:" + counter + "\t\tskipCounter: " + skipCounter);
                    }
                }
                intIntOpenHashIterator.close();
            }
        }
        else {
            TextReader intIntOpenHashIterator = new TextReader(bigramTokenCountFileAddress);
            while (intIntOpenHashIterator.hasNext()) {
                String[] splites = intIntOpenHashIterator.next().split("\t");
                String[] tokens = splites[0].split(" ");
                int code = -1;
                int score = -1;
                try {
                    if (tokens.length == 1) {
                        code = simpleBigramModel.simpleBigramUtils.getTokensCode(tokens[0]);
                    } else {
                        code = simpleBigramModel.simpleBigramUtils.getTokensCodeDistinct(tokens[0], tokens[1]);
                    }
                    if (code < 0) {
                        skipCounter++;
                        continue;
                    }
                    score = Integer.parseInt(splites[1]);
                } catch (Exception e) {
                    skipCounter++;
                    continue;
                }

                simpleBigramModel.bigramCount.put(code, score);
                counter++;
                if (counter % 10000 == 0) {
                    System.out.println("counter:" + counter + "\t\tskipCounter: " + skipCounter);
                }
            }
            intIntOpenHashIterator.close();
        }
        System.out.println("load complete with counter:" + counter + "\t\tskipCounter: " + skipCounter);

        TextReader tokenCountIterator = new TextReader(tokenCountFileAddress);
        while (tokenCountIterator.hasNext()) {
            PairValue<String, String> countToken = SparkTextReader.getLineValue(tokenCountIterator.next());
//            String[] splites = tokenCountIterator.next().split("\t");
            String[] tokens = countToken.getValue2().split(" ");
            int code = -1;
            int score= -1;
            try {
                if (tokens.length == 1) {
                    code = simpleBigramModel.simpleBigramUtils.getTokensCode(tokens[0]);
                } else {
                    continue;
                }
                if(code<0){
                    skipCounter++;
                    continue;
                }
                score = Integer.parseInt(countToken.getValue1());
            } catch (Exception e) {
                skipCounter++;
                continue;
            }

            simpleBigramModel.bigramCount.put(code, score);
            counter++;
            if (counter % 10000 == 0) {
                System.out.println("counter:" + counter+"\t\tskipCounter: "+skipCounter);
            }
        }
        tokenCountIterator.close();

        System.out.println("load complete in "+stopwatch.elapsed(TimeUnit.SECONDS)+" ms with (counter:" + counter+"\t\tskipCounter: "+skipCounter+").");

        return simpleBigramModel;
    }
}

