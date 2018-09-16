package nlp.languagemodel;

import com.google.gson.Gson;
import nlp.languagemodel.data.BiGram;
import nlp.languagemodel.data.UniGram;
import nlp.languagemodel.db.BiGrams;
import nlp.languagemodel.db.UniGrams;
import nlp.preprocess.fa.SimpleNormalizerTokenizer;
import tools.database.DbConnection;
import tools.database.DbTools;
import tools.util.ConsuleInput;
import tools.util.Directory;
import tools.util.collection.PairValue;
import tools.util.file.Reader;
import tools.util.file.SparkTextReader;
import tools.util.file.TextReader;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class MapReduceToMysql {

    public static void writeMapReduceFileToDB(String fileOrPathAddress,
                                              String tokenFileAddress,
                                              DbConnection dbConnection,
                                              String dataSetName,
                                              int tablePartitionCount,
                                              int batchSize) throws Exception {
        long startTime = System.currentTimeMillis();
        final UniGrams uniGramsDB = new UniGrams(dbConnection,dataSetName);

        TrigramUtils trigramUtil = new TrigramUtils(tokenFileAddress, new SimpleNormalizerTokenizer());//???????????
        final BiGrams biGramsDB = new BiGrams(dbConnection, dataSetName, batchSize, tablePartitionCount, trigramUtil);

        final HashSet<UniGram> uniGramsSet = new HashSet<UniGram>(batchSize);
        final HashSet<BiGram> biGramsSet = new HashSet<BiGram>(batchSize);

        int uniGramsSetCounter = 0;
        int biGramsSetCounter = 0;

        int sumUniGramsSetCounter = 0;
        int sumBiGramsSetCounter = 0;
        int sumTriGramsSetCounter = 0;

        TextReader reader = null;

        List<String> fileAddresses = new ArrayList<>();
        fileAddresses.add(tokenFileAddress);
        System.out.println(tokenFileAddress + "\t added to list.");

        if (new File(fileOrPathAddress).isFile()) {
            if (isValid(Reader.getFirstLine(fileOrPathAddress))) {
                fileAddresses.add(fileOrPathAddress);
                System.out.println(fileOrPathAddress + "\t added to list.");
            }
        } else {
            File[] files = Directory.ls(fileOrPathAddress);
            for (File file : files) {
                String fileAddress = file.getAbsolutePath();
                if (isValid((Reader.getFirstLine(fileAddress)))) {
                    fileAddresses.add(fileAddress);
                    System.out.println(fileAddress + "\t added to list.");
                }
            }
        }
        reader = new TextReader(fileAddresses);

        String newLine;
        int lines = 0;
        String[] splits;
        String[] terms;
        while (reader.hasNext()) {
            newLine = reader.next();
            if(SparkTextReader.isInSparkFormat(newLine)){
                PairValue<String, String> keyVal = SparkTextReader.getLineValue(newLine);
                splits=new String[]{keyVal.getValue2(),keyVal.getValue1()};
                terms = splits[0].split(" ");
            }
            else {
                splits = newLine.split("\t");
                terms = splits[0].split(" ");
            }
            try {
                switch (terms.length) {
                    case 1:
                        if (terms[0].length() > 20)
                            break;
                        uniGramsSet.add(new UniGram(terms[0], Long
                                .parseLong(splits[1])));
                        uniGramsSetCounter++;
                        sumUniGramsSetCounter++;
                        if (uniGramsSetCounter == batchSize) {
                            try {
                                uniGramsDB.batchInsert(uniGramsSet);
                            } catch (ClassNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            uniGramsSet.clear();
                            uniGramsSetCounter = 0;
                        }
                        break;
                    case 2:
                        sumBiGramsSetCounter++;
                        if (terms[0].length() > 20 || terms[1].length() > 20)
                            break;
                        biGramsSet.add(new
                                BiGram(terms[0], terms[1], Long.parseLong(splits[1])));
                        biGramsSetCounter++;
                        if (biGramsSetCounter == batchSize) {
                            try {
                                biGramsDB.batchInsert(biGramsSet);
                            } catch (ClassNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            biGramsSet.clear();
                            biGramsSetCounter = 0;
                        }
                        break;
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            lines++;
            if (lines % 10000 == 0)
                System.out.println(lines
                        + " lines handled in "
                        + (System.currentTimeMillis() - startTime) / 1000
                        + " sec"
                        + "\t ("
                        + (int) (lines / ((System
                        .currentTimeMillis() - startTime + 1) / 1000))
                        + " line/sec)");
        }
        reader.close();
        if (uniGramsSetCounter > 0) {
            try {
                uniGramsDB.batchInsert(uniGramsSet);
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            uniGramsSet.clear();
        }
        System.out.println("Operation complete in (unigram:"
                + sumUniGramsSetCounter + ") (bigram:" + sumBiGramsSetCounter
                + ") (trigram:" + sumTriGramsSetCounter + ")"
                + (System.currentTimeMillis() - startTime) / 1000 + " s.");
    }

    private static boolean isValid(String line) {
        try {
            String[] split = line.split("\t");
            if (split.length > 0 && Integer.parseInt(split[split.length - 1]) > 0) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static String getDataSetName(String fileAddress) throws UnsupportedEncodingException {
        String dataSetName = "";
        try {
            String[] split1 = new File(fileAddress).getName().split("_");
            String[] split11 = split1[0].split("-");
            dataSetName = split11[0] + "_" + split11[1] + "_" + split1[7] + "_" + split1[8];
        } catch (Exception e) {
        }
        return dataSetName.length() > 0 ? dataSetName : ConsuleInput.readStringUTF8("Enter dataset name");
    }

    public static void main(String[] args) throws Exception {

        Properties properties=new Properties();
        properties.load(Reader.getFileBufferReader(args[0]));

        //"localhost";
        //"ppp"
        //"nlp"

        String nodeName = properties.getProperty("mysqlDbServerAddress");
        String dbName = properties.getProperty("mysqlDbName");
        String username = properties.getProperty("mysqlDbUsername");
        String pass = properties.getProperty("mysqlDbPass");
        int dbPort = properties.containsKey("mysqlDbPort")?Integer.parseInt(properties.getProperty("dbPort")):DbTools.getDefaultPort(DbTools.DbType.MYSQl);

        System.out.println(new Gson().toJson(properties));
        DbTools.createDatabase(nodeName, dbPort, username, pass, dbName, DbTools.DbType.MYSQl);

        String fileAddress = properties.getProperty("inputPath");//"C:\\thesis\\fawiki-20180701-pages-articles.xml.bz2.newLineByLine_clearedText_paragraph_posTaggedSentence_noun-adjective_w2_coCount/";
        String tokenFleAddress = properties.getProperty("tokensFreq."+properties.getProperty("language"));//"C:\\thesis\\fawiki-20180701-pages-articles.xml.bz2.newLineByLine_clearedText_paragraph_posTaggedSentence_noun-adjective_tokensFreq/part-00000";
        String dataSetName = getDataSetName(fileAddress);

        int partitionCount = Integer.parseInt(properties.getProperty("tablePartitionCount"));//5;


        BiGrams.createMySqlTable(nodeName, dbPort, username, pass, dbName,dataSetName, partitionCount, false);
        UniGrams.createMySqlTable(nodeName,dbPort,username,pass,dbName,dataSetName,false);

        DbConnection dbConnection=DbConnection.getInstance(DbTools.getDbUrl(nodeName,dbPort,dbName, DbTools.DbType.MYSQl),DbTools.getClass(DbTools.DbType.MYSQl),username,pass);

        writeMapReduceFileToDB(
                fileAddress,
                tokenFleAddress,
                dbConnection,
                dataSetName,
                partitionCount,
                1000);

        BiGrams.createMySqlTable(nodeName, dbPort, username, pass ,dbName,dataSetName,partitionCount,true);
        UniGrams.createMySqlTable(nodeName,dbPort,username,pass,dbName,dataSetName,true);

        System.out.println("Operation complete.");
    }
}
