package nlp.mapred;

import nlp.preprocess.StanfordTokenizedLemmattizedPosTaggedSentence;
import nlp.preprocess.en.StanfordPreProcessor;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import tools.util.Sys;

import java.io.IOException;

public class MapredExtractPosTaggedSentence {


    //Mapper class
    public static class ExtractPosTaggedSentenceMapper extends MapReduceBase implements
            Mapper<LongWritable,Text,Text,NullWritable>{

        final Text outKey=new Text();
        final NullWritable nullValue=NullWritable.get();

        StanfordPreProcessor stanfordPreProcessor;

        public void configure(JobConf job) {
            StanfordPreProcessor.CoreNlpModelLanguage language = job.getEnum("language",
                    StanfordPreProcessor.CoreNlpModelLanguage.ENGLISH);
            System.out.println("language: "+language);
            stanfordPreProcessor=new StanfordPreProcessor(language);
        }

        //Map function
        public void map(LongWritable id, Text text,
                        OutputCollector<Text, NullWritable> output,
                        Reporter reporter) throws IOException {

            try {
                for (StanfordTokenizedLemmattizedPosTaggedSentence sentence :
                        stanfordPreProcessor.getTokenizedAndLemmatizedSentences(text.toString().replaceAll("\t", " "))) {
                    outKey.set(sentence.getTokenizedSentence()+"\t"+sentence.getLemmatizedSentence()+"\t"+sentence.getPosTagSentence());
                    output.collect(outKey,nullValue);
                }
            } catch (Exception e) {
                reporter.incrCounter("Exception","Exception",1);
            }

        }
    }



    //Main function
    public static void main(String args[]) throws Exception {

        if(Sys.osIsWin()) {
            System.setProperty("hadoop.home.dir", args[2]);//"c:\\hadoop\\"
        }


        System.out.println("Supported languages are:");
        int i=0;
        for (StanfordPreProcessor.CoreNlpModelLanguage coreNlpModelLanguage : StanfordPreProcessor.CoreNlpModelLanguage.values()) {
            System.out.println(++i +") "+ coreNlpModelLanguage);
        }

        JobConf conf = new JobConf(MapredExtractPosTaggedSentence.class);

        String jobName = "MapredExtractPosTaggedSentence";
        System.out.println(jobName);
        conf.setJobName(jobName);


        conf.setInputFormat(TextInputFormat.class);
        conf.setMapperClass(ExtractPosTaggedSentenceMapper.class);

        conf.setOutputFormat(TextOutputFormat.class);
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(NullWritable.class);

        conf.setNumReduceTasks(0);

        FileInputFormat.setInputPaths(conf, new Path(args[0]));

        FileOutputFormat.setOutputPath(conf, new Path(args[0]+"_posTaggedSentence"));

        conf.set("language", args[1]);

//        conf.set("mapred.min.split.size", String.valueOf(Long.MAX_VALUE));

        JobClient.runJob(conf);
    }

}