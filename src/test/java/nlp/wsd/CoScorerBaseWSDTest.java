package nlp.wsd;

import edu.mit.jwi.item.POS;
import nlp.preprocess.PreprocessorInterface;
import nlp.preprocess.en.StanfordPreProcessor;
import nlp.preprocess.fa.PersianPreprocessor;
import nlp.wordnet.WordNetTools;
import nlp.wsd.co.WSDSimpleCoScore;
import org.apache.log4j.PropertyConfigurator;
import tools.util.file.Reader;

import java.util.*;
import java.util.stream.Collectors;

public class CoScorerBaseWSDTest {

    public static void main(String[] args) throws Exception {

        Properties properties=new Properties();
        properties.load(Reader.getFileBufferReader("resources/wsd.properties"));

        PropertyConfigurator.configure(properties.getProperty("log4j.properties"));

        WordNetTools wordNetTools = new WordNetTools(properties.get("wordNetPath").toString());

        Set<POS> posSet = Arrays.asList(properties.get("posSet").toString().split(",")).stream().map(s -> WordNetTools.getPos(s)).collect(Collectors.toSet());

        PreprocessorInterface persianPreprocessor = PersianPreprocessor.getWindowsDefaultInstance();
        StanfordPreProcessor englishPreprocessor = new StanfordPreProcessor();
        WSDSimpleCoScore wsdSimpleCoScore = WSDSimpleCoScore.getDBInstance(properties, properties.getProperty("tokenFreq."+properties.getProperty("language")),
                posSet, wordNetTools, englishPreprocessor);

//        System.out.println(wsd.getCoScore("ملی","تیم"));
        WSDSystem wsdSystem=new WSDSystem(wsdSimpleCoScore,wordNetTools,posSet,englishPreprocessor,persianPreprocessor);

        String text;
        Map<String, String> result = null;


        Set<String> senseSet=new HashSet<>();
        senseSet.add("حیوان");
        senseSet.add("خوراکی");
        text="حضور شیر جنگل در قفس به عنوان سلطان";
        result=wsdSystem.disambiguate(text, "شیر", 1, senseSet);
        System.out.print(text+"\t-> ");
        result.entrySet().forEach(r -> System.out.println(r.getKey()+" -> "+r.getValue()));

        senseSet.clear();
        senseSet.add("خوراکی");
        senseSet.add("سپاس");
        text = "شکر در برابر قند عوارض کمتری دارد";
        result = wsdSystem.disambiguate(text, "شکر", 1, senseSet);
        System.out.print(text+"\t-> ");
        result.entrySet().forEach(r -> System.out.println(r.getKey()+" -> "+r.getValue()));
        System.out.println("________________________________________________________");

        senseSet.clear();
        senseSet.add("فوتبال");
        senseSet.add("سیاست");

        text = "شاهرودی و علی دایی دیدار در آزادی";
        result = wsdSystem.disambiguate(text, "شاهرودی", 1, senseSet);
        System.out.print(text+"\t-> ");
        result.entrySet().forEach(r -> System.out.println(r.getValue()));
        System.out.println("________________________________________________________");

        text = "شاهرودی و درخواست عفو رهبری";
        result = wsdSystem.disambiguate(text, "شاهرودی", 1, senseSet);
        System.out.print(text+"\t-> ");
        result.entrySet().forEach(r -> System.out.println(r.getValue()));
    }
}
