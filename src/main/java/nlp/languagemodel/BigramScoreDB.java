package nlp.languagemodel;

import nlp.languagemodel.db.TrigramScore;
import nlp.preprocess.en.StanfordPreProcessor;
import nlp.preprocess.fa.SimpleNormalizerTokenizer;
import tools.database.DbConnection;
import tools.util.BitCodec;
import tools.util.file.SparkTextReader;

import java.io.IOException;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Saeed on 9/4/15.
 */
public class BigramScoreDB implements LanguageModelScorer{

    int BITCOUNT=21;
    TrigramUtils trigramUtils;
    TrigramScore dbTrigramScore;
    String tableName;
    StanfordPreProcessor stanfordPreProcessor;
    Map<String, Long> termDf;

    public BigramScoreDB(String tokenFileAddress, Map<String, Long> termDf, DbConnection dbConnection, int tableCount) throws SQLException, ClassNotFoundException, IOException {
        this.dbTrigramScore=new TrigramScore(dbConnection,tableCount);
        this.trigramUtils=new TrigramUtils(tokenFileAddress,new SimpleNormalizerTokenizer());
        this.tableName="trigramScoreC0W10-minScore1";
        this.stanfordPreProcessor=new StanfordPreProcessor();
        this.termDf=termDf;
        System.out.println("BiTrigramScore() load complete.");
    }

    public BigramScoreDB(String tokenFileAddress, DbConnection dbConnection, String tableName, int tableCount) throws SQLException, ClassNotFoundException, IOException {
        termDf = SparkTextReader.getLines(tokenFileAddress)
                .stream().map(s -> new AbstractMap.SimpleEntry<String,Long>(s.getValue2(),Long.parseLong(s.getValue1()))).collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
        this.dbTrigramScore=new TrigramScore(dbConnection,tableCount);
        this.trigramUtils=new TrigramUtils(tokenFileAddress,new SimpleNormalizerTokenizer());
        this.tableName=tableName;
        this.stanfordPreProcessor=new StanfordPreProcessor();
        System.out.println("BiTrigramScore() load complete.");
    }

    public BigramScoreDB(String tokenFileAddress, DbConnection dbConnection, String tableName, int tableCount,StanfordPreProcessor stanfordPreProcessor) throws SQLException, ClassNotFoundException, IOException {
        termDf = SparkTextReader.getLines(tokenFileAddress)
                .stream().map(s -> new AbstractMap.SimpleEntry<String,Long>(s.getValue2(),Long.parseLong(s.getValue1()))).collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
        this.dbTrigramScore=new TrigramScore(dbConnection,tableCount);
        this.trigramUtils=new TrigramUtils(tokenFileAddress,new SimpleNormalizerTokenizer());
        this.tableName=tableName;
        this.stanfordPreProcessor=stanfordPreProcessor;
        System.out.println("BiTrigramScore() load complete.");
    }

    public long getLemmatizedScore(String tokens) throws Exception {
        String[] tokenizedLemma = this.stanfordPreProcessor.getPreproccedText(tokens, StanfordPreProcessor.PreprocessType.Lemmatize).split(" ");
        if(tokenizedLemma.length==1){
            System.out.println("lemma: "+tokenizedLemma[0]);
            return getScore(tokenizedLemma[0]);
        }
        else if(tokenizedLemma.length==2){
            System.out.println("lemma: "+tokenizedLemma[0]+" "+tokenizedLemma[1]);
            return getScore(tokenizedLemma[0],tokenizedLemma[1]);
        }
        else if(tokenizedLemma.length==3){
            System.out.println("lemma: "+tokenizedLemma[0]+" "+tokenizedLemma[1]+" "+tokenizedLemma[2]);
            return getScore(tokenizedLemma[0],tokenizedLemma[1],tokenizedLemma[2]);
        }
        return -2;
    }

    public long getLemmatizedScore(String token1,String token2) throws Exception {
        String token1Lemma = this.stanfordPreProcessor.getPreproccedText(token1, StanfordPreProcessor.PreprocessType.Lemmatize);
        String token2Lemma = this.stanfordPreProcessor.getPreproccedText(token2, StanfordPreProcessor.PreprocessType.Lemmatize);
        return getScore(token1Lemma,token2Lemma);
    }

    public long getLemmatizedScore(String token1,String token2,String token3) throws Exception {
        String token1Lemma = this.stanfordPreProcessor.getPreproccedText(token1, StanfordPreProcessor.PreprocessType.Lemmatize);
        String token2Lemma = this.stanfordPreProcessor.getPreproccedText(token2, StanfordPreProcessor.PreprocessType.Lemmatize);
        String token3Lemma = this.stanfordPreProcessor.getPreproccedText(token2, StanfordPreProcessor.PreprocessType.Lemmatize);
        return getScore(token1Lemma,token2Lemma,token3Lemma);
    }

    public long getScore(String lemma1) {
        if(this.termDf.containsKey(lemma1)){
            return this.termDf.get(lemma1);
        }
        else{
            return 0;
        }
    }

    public long getScore(String token1, String token2) throws Exception {
        long dbKey = BitCodec.decode(this.trigramUtils.getTokensCode(token1, token2), BITCOUNT).getDistinctSortedCode(BITCOUNT);
        return this.dbTrigramScore.getTrigramScore(this.tableName,dbKey);
    }

    public long getScore(String token1,String token2,String token3) throws Exception {
        long dbKey = BitCodec.decode(this.trigramUtils.getTokensCode(token1, token2, token3), BITCOUNT).getDistinctSortedCode(BITCOUNT);
        return this.dbTrigramScore.getTrigramScore(this.tableName,dbKey);
    }
}
