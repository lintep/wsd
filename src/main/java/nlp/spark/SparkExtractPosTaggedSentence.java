package nlp.spark;//package nlp.spark;

import nlp.preprocess.NormalizerInterface;
import nlp.preprocess.PreprocessorInterface;
import nlp.preprocess.StanfordTokenizedLemmattizedPosTaggedSentence;
import nlp.preprocess.fa.NormalizerPurePersian;
import nlp.preprocess.fa.PersianPreprocessor;
import nlp.preprocess.fa.SimpleNormalizer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import tools.util.Sys;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saeed on 11/13/2016.
 */
public class SparkExtractPosTaggedSentence {

    public static void main(String[] args) throws Exception {
        System.out.println("Start SparkExtractPosTaggedSentence");

        if(Sys.osIsWin()) {
            System.setProperty("hadoop.home.dir", args[1]);//"c:\\hadoop\\"
        }


        SparkConf sparkConf = new SparkConf()
                .setAppName("SparkExtractPersianPosTaggedSentence")
                .set("spark.local.ip", "127.0.0.1").set("spark.driver.host", "127.0.0.1").setMaster("local[3]").set("spark.executor.memory", "4g")
                .registerKryoClasses(new Class[]{PersianPreprocessor.class})
                ;

        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

        String inputPath = args[0];//"/ssd/wiki/dewiki-20161020-pages-articles.xml.bz2_sequencefiles_splitSize_500000";//args[0];

//         This preprocessor normalizer clear all english token and characters
        NormalizerInterface normalizer = new NormalizerPurePersian(new SimpleNormalizer(), "%%%");

//        NormalizerInterface normalizer = new SimpleNormalizer();

        PersianPreprocessor persianPreprocessor= PersianPreprocessor.getDefaultInstance(normalizer,args[1],args[2],null);

        Broadcast<PersianPreprocessor> broadcastPersianPreprocessor = sparkContext.broadcast(persianPreprocessor);

        sparkContext.textFile(inputPath).flatMap(s -> {
            List<String> resultList = new ArrayList<>();
            for (PreprocessorInterface.PreprocessedSentence preprocessedSentence : broadcastPersianPreprocessor.getValue().preprocess(s)) {
                StringBuilder tokensStr = new StringBuilder();
                StringBuilder lemmasStr = new StringBuilder();
                StringBuilder posesStr = new StringBuilder();

                for (PreprocessorInterface.PreprocessedItem preprocessedItem : preprocessedSentence.getPreprocessedItems()) {
                    if(preprocessedItem.getLemma().length()==0){
                        continue;
                    }
                    tokensStr.append(preprocessedItem.getToken()+' ');
                    lemmasStr.append(preprocessedItem.getLemma()+' ');
                    posesStr.append(preprocessedItem.getPos()+' ');
                }

                if(tokensStr.length()==0){
                    continue;
                }

                tokensStr.setLength(tokensStr.length()-1);
                lemmasStr.setLength(lemmasStr.length()-1);
                posesStr.setLength(posesStr.length()-1);

                StanfordTokenizedLemmattizedPosTaggedSentence sentence =
                        new StanfordTokenizedLemmattizedPosTaggedSentence(tokensStr.toString(),lemmasStr.toString(),posesStr.toString());
                resultList.add(sentence.getTokenizedSentence()+"\t"+sentence.getLemmatizedSentence()+"\t"+sentence.getPosTagSentence());
            }
            return resultList;
        }).saveAsTextFile(inputPath+"_posTaggedSentence");

    }

}
