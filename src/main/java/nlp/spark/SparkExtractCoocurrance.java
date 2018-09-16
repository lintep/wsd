package nlp.spark;//package nlp.spark;

import edu.mit.jwi.item.POS;
import nlp.wordnet.WordNetTools;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;
import tools.util.BitCodec;
import tools.util.Sys;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Saeed on 11/13/2016.
 */
public class SparkExtractCoocurrance {

    public static void main(String[] args) {

        System.out.println("Start SparkExtractCoocurrance 34");
        if(Sys.osIsWin()) {
            System.setProperty("hadoop.home.dir", args[1]);//"c:\\hadoop\\"
        }

        SparkConf sparkConf = new SparkConf()
                .setAppName("SparkExtractCoocurrance")
               .set("spark.local.ip", "127.0.0.1").set("spark.driver.host", "127.0.0.1").setMaster("local[3]").set("spark.executor.memory", "4g")
                ;

        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

        String inputPath = args[0];

        HashSet<POS> posSet = new HashSet<>();
        StrBuilder posSetString = new StrBuilder();
        for (String pos : args[1].split(",")) {
            POS posValue = WordNetTools.getPos(pos);
            posSet.add(posValue);
            posSetString.append(posValue.toString()+"-");
        }
        posSetString.setLength(posSetString.length()-1);

        String resultPath=inputPath + "_" + posSetString.toString();


//        if(args.length==2) {
            partI(sparkContext, inputPath, posSet, resultPath);
//        }
//        else {
            for (String wStr : args[2].split(",")) {
                int w = Integer.parseInt(wStr);
                partII(sparkContext,resultPath+ "_posFilteredSentence",w,resultPath);
            }
//        }


    }

    public static JavaRDD<String[]> partI(JavaSparkContext sparkContext, String inputPath, Set<POS> posSet, String resultPath){

        final String poSkippedToken = "@-@_@-@";

        JavaRDD<String> sentences = sparkContext.textFile(inputPath);

        JavaRDD<String> filteredSentence = sentences.map(s -> {
            String[] splits = s.split("\t");
            String[] tokens = splits[0].split(" ");
            String[] lemmas = splits[1].split(" ");
            String[] poses = splits[2].split(" ");

            String[] legalLemmas = new String[poses.length];
            StringBuilder legalLemmasString = new StringBuilder();
            for (int i = 0; i < poses.length; i++) {
                if (posSet.contains(WordNetTools.getPos(poses[i])) || poses[i].equals("UNKNOWN")) {
                    legalLemmas[i] = lemmas[i];
                } else {
                    legalLemmas[i] = poSkippedToken;
                }
                legalLemmasString.append(legalLemmas[i]);
                legalLemmasString.append(' ');
            }

            if(legalLemmasString.length()>0){
                legalLemmasString.setLength(legalLemmasString.length()-1);
            }

            return splits[0] + "\t" + legalLemmasString + "\t" + splits[2];
        }).filter(s -> s.length() > 0);

        filteredSentence.saveAsTextFile(resultPath + "_posFilteredSentence");

//        JavaRDD<String> filteredSentence = sparkContext.textFile(resultPath + "_posFilteredSentence");

        JavaRDD<String[]> sentenceLemma = filteredSentence.map(s -> s.split("\t")[1].split(" "));

        JavaPairRDD<Integer, String> freqToken = sentenceLemma.flatMap(s -> Arrays.asList(s)).
                mapToPair(s -> new Tuple2<String, Integer>(s, 1)).
                reduceByKey((a, b) -> a + b).
                mapToPair(s -> s.swap()).sortByKey(false, 1);

        freqToken.persist(StorageLevel.MEMORY_ONLY());
        freqToken.saveAsTextFile(resultPath + "_tokensFreq");

        final int maxTokenCount = (int) Math.pow(2., 21) - 2;

        int minSf=1;
        while (freqToken.count()>maxTokenCount) {
            System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nminSf: "+minSf+"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
            final int finalI = minSf;
            JavaPairRDD<Integer, String> newFreqToken = freqToken.filter(s -> s._1 > finalI);
            newFreqToken.persist(StorageLevel.MEMORY_ONLY());
            freqToken.unpersist();
            freqToken=newFreqToken;
            minSf++;
        }
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nfinal minSf: "+minSf+"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        List<Tuple2<Integer, String>> tokenFreqMap = freqToken.sortByKey(false, 1).collect();//.filter(s -> s._1>Integer.parseInt(args[3]))
        int id = 0;
//        Map<Integer, String> idToken = new HashMap<>(maxTokenCount);
        Map<String, Integer> tokenId = new HashMap<>(maxTokenCount);
        for (Tuple2<Integer, String> item : tokenFreqMap) {
//            idToken.put(id, item._2());
            tokenId.put(item._2, id);
            id++;
        }

        sparkContext.parallelize(tokenId.entrySet().stream().map(s -> s.getKey() + "\t" + s.getValue())
                .collect(Collectors.toList())).
//                mapToPair(s -> {String[] splites = s.split("\t");return new Tuple2<Integer,String>(Integer.parseInt(splites[1]),splites[0]);}).
                repartition(1).saveAsTextFile(resultPath + "_tokenId");


        freqToken.unpersist();

        return sentenceLemma;
    }


    public static void partII(JavaSparkContext sparkContext, String inputPath,int w,String resultPath){

        Map<String, Integer> tokenId = sparkContext.textFile(resultPath + "_tokenId").mapToPair(s -> {
            String[] splits = s.split("\t");
            return new Tuple2<String, Integer>(splits[0], Integer.parseInt(splits[1]));
        }).collectAsMap();
        Broadcast<Map<String, Integer>> broadcastTokenId = sparkContext.broadcast(tokenId);

        JavaRDD<String[]> sentenceLemma = sparkContext.textFile(inputPath).map(s -> s.split("\t")[1].split(" ")).filter(s -> s.length>1);

        //System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nsentenceLemma.count(): "+sentenceLemma.count()+"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
//        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nsentenceLemma.count(): "+sentenceLemma.count()+"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        JavaRDD<Integer[]> sentenceEncoded = sentenceLemma.map(s -> {
            Map<String, Integer> tokenToId = broadcastTokenId.getValue();
            Integer[] encodedSentence = new Integer[s.length];
            for (int i = 0; i < s.length; i++) {
                Integer code = tokenToId.get(s[i]);
                if (code!=null) {
                    encodedSentence[i] = code;
                } else {
                    encodedSentence[i] = -1;
                }
            }
            return encodedSentence;
        });
//        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nsentenceEncoded.count(): "+sentenceEncoded.count()+"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        final int ENCODEBITCOUNT = 21;
        JavaPairRDD<Long, Integer> biCoCount = sentenceEncoded.flatMap(s -> {
            HashSet<Long> coTokenCode = new HashSet<Long>();

            if (s.length > w) {
                for (int i = 0; i <= s.length - w; i++) {
                    for (int j = i; j < i + w; j++) {
                        for (int k = j + 1; k < i + w; k++) {
                            if(s[j]>0 && s[k]>0) {
                                long biCode = BitCodec.decode(BitCodec.encode(s[j], s[k], ENCODEBITCOUNT), ENCODEBITCOUNT).getDistinctSortedCode(ENCODEBITCOUNT);
                                coTokenCode.add(biCode);
                            }
                        }
                    }
                }
            } else {
                for (int j = 0; j < s.length; j++) {
                    for (int k = j + 1; k < s.length; k++) {
                        if(s[j]>0 && s[k]>0) {
                            long biCode = BitCodec.decode(BitCodec.encode(s[j], s[k], ENCODEBITCOUNT), ENCODEBITCOUNT).
                                    getDistinctSortedCode(ENCODEBITCOUNT);
                            coTokenCode.add(biCode);
                        }
                    }
                }
            }

            return coTokenCode;
        }).mapToPair(s -> new Tuple2<Long, Integer>(s, 1)).reduceByKey((a, b) -> a + b);

        biCoCount.persist(StorageLevel.MEMORY_AND_DISK());

        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nbiCoCount.count(): "+biCoCount.count()+"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        biCoCount.saveAsTextFile(resultPath + "_w"+w+"_encoded_coCount");

        Map<Integer, String> idToken = tokenId.entrySet().stream().map(s -> new AbstractMap.SimpleEntry<Integer, String>(s.getValue(), s.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Broadcast<Map<Integer, String>> broadcastIdToken = sparkContext.broadcast(idToken);

        biCoCount.map(s -> {
            BitCodec.ThreeInt decode = BitCodec.decode(s._1, ENCODEBITCOUNT);
            Map<Integer, String> idToToken = broadcastIdToken.getValue();
            return idToToken.get(decode.getInt2()) + " " + idToToken.get(decode.getInt3()) + "\t" + s._2;
        }).saveAsTextFile(resultPath+"_w" + w + "_coCount");

    }



    public static void testEncoding() {
        int ENCODEBITCOUNT = 21;
        Integer[] s = new Integer[5];
        int w = 4;
        for (int i = 0; i < s.length; i++) {
            s[i] = i + 1;
        }

        HashSet<Long> coTokenCode = new HashSet<Long>();

        for (int i = 0; i <= s.length - w; i++) {
            for (int j = i; j < i + w; j++) {
                for (int k = j + 1; k < i + w; k++) {
                    try {
                        long biCode = BitCodec.decode(BitCodec.encode(s[j], s[k], ENCODEBITCOUNT), ENCODEBITCOUNT).
                                getDistinctSortedCode(ENCODEBITCOUNT);
                        coTokenCode.add(biCode);
                        System.out.println(s[j] + " , " + s[k] + " : " + biCode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.println();
            }
        }

        System.out.println("_____________________");
        coTokenCode.forEach(a -> {
            BitCodec.ThreeInt decode = BitCodec.decode(a, ENCODEBITCOUNT);
            System.out.println(decode.getInt1() + "\t" + decode.getInt2() + "\t" + decode.getInt3());
        });
    }


}
