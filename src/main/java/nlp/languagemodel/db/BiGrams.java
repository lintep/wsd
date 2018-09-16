package nlp.languagemodel.db;

import nlp.languagemodel.TrigramUtils;
import nlp.languagemodel.data.BiGram;
import tools.database.DbConnection;
import tools.database.DbDataFields;
import tools.database.DbTools;
import tools.util.Time;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BiGrams {

    String dbName;

    final static String TABLEPREFNAME = "bigrams";

    String tableName = TABLEPREFNAME;

    int tableParts = 1;

    TrigramUtils trigramUtils;

    int batchSize;

    Map<Integer, Map<Long, BiGram>> batchBuffer;

    DbConnection dbConnection;

    public BiGrams(DbConnection dbConnection, String dataSetName, int batchSize, int tableParts, TrigramUtils trigramUtils) {
        try {
            this.dbConnection = dbConnection;
            tableName += "_" + dataSetName;
            this.tableParts = tableParts;
            this.trigramUtils = trigramUtils;
            this.batchSize = batchSize;
            batchBuffer = new HashMap<>();
            for (int i = 0; i < tableParts; i++) {
                batchBuffer.put(i, new HashMap<>());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void createMySqlTable(String nodeName, int dbPort, String username, String pass, String dbName, String dataSetName, int partitionCount, boolean addIndex) throws Exception {
        HashMap<String, DbDataFields.MySqlDataFields> fieldMap = new HashMap<>();
        fieldMap.put("term1", DbDataFields.MySqlDataFields.VARCHAR256);
        fieldMap.put("term2", DbDataFields.MySqlDataFields.VARCHAR256);
        fieldMap.put("bigramCode", DbDataFields.MySqlDataFields.BIGINT22);
        fieldMap.put("freq", DbDataFields.MySqlDataFields.INT11);

        DbConnection mySqlCon = DbConnection.getInstance(DbTools.getDbUrl(nodeName,dbPort,dbName, DbTools.DbType.MYSQl), DbTools.getClass(DbTools.DbType.MYSQl), username, pass);

        if (partitionCount <= 1) {
            try {
                DbTools.createMySqlTable(mySqlCon.getConnection(),TABLEPREFNAME + "_" + dataSetName, fieldMap);
            } catch (Exception e) {
                if (e.getMessage().indexOf("already exists") >= 0) {
                    System.out.println(e.getMessage());
                } else {
                    throw e;
                }
            }
            if (addIndex) {
                DbTools.addMySQlIndex(mySqlCon.getConnection(),TABLEPREFNAME + "_" + dataSetName, "bigramCode");
                DbTools.addMySQlIndex(mySqlCon.getConnection(),TABLEPREFNAME + "_" + dataSetName, "term1");
                DbTools.addMySQlIndex(mySqlCon.getConnection(),TABLEPREFNAME + "_" + dataSetName, "term2");
            }
        } else {
            for (int i = 0; i < partitionCount; i++) {
                try {
                    DbTools.createMySqlTable(mySqlCon.getConnection(),TABLEPREFNAME + "_" + dataSetName + "_" + i, fieldMap);
                } catch (Exception e) {
                    if (e.getMessage().indexOf("already exists") >= 0) {
                        System.out.println(e.getMessage());
                    } else {
                        e.printStackTrace();
                    }
                }
                if (addIndex) {
                    DbTools.addMySQlIndex(mySqlCon.getConnection(),TABLEPREFNAME + "_" + dataSetName + "_" + i, "bigramCode");
                    DbTools.addMySQlIndex(mySqlCon.getConnection(),TABLEPREFNAME + "_" + dataSetName + "_" + i, "term1");
                    DbTools.addMySQlIndex(mySqlCon.getConnection(),TABLEPREFNAME + "_" + dataSetName + "_" + i, "term2");
                }
            }
        }
    }

    public static int createMySqlTable(String nodeName, int dbPort, String username, String pass, String dbName, String dataSetName) throws ClassNotFoundException {
        try {
            createMySqlTable(nodeName, dbPort, username, pass, dbName, dataSetName, 0, true);
            return 1;
        } catch (SQLException e) {
            if (e.getMessage().indexOf("already exists") > 0) {
                System.out.println(e.getMessage() + ".");
                return 0;
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void batchInsertLowPerformance(HashSet<BiGram> bigrams)
            throws SQLException, ClassNotFoundException {
        PreparedStatement PS = dbConnection.getConnection().prepareStatement("INSERT INTO `"
                + tableName + "`(`term1`, `term2`,`freq`) VALUES (?,?,?)");
        for (BiGram bigram : bigrams) {
            PS.setString(1, bigram.getTerm1());
            PS.setString(2, bigram.getTerm2());
            PS.setLong(3, bigram.getFrequency());
            PS.addBatch();
        }
        PS.executeBatch();
        PS.close();
    }

    String query = "";

    public void batchInsert(HashSet<BiGram> bigrams)
            throws ClassNotFoundException {
        if (tableParts <= 1) {
            query = "INSERT INTO `" + tableName
                    + "`(`term1`, `term2`,`bigramCode`,`freq`) VALUES";
            for (BiGram bigram : bigrams) {
                try {
                    query += "('" + DbTools.getValidateQuery(bigram.getTerm1()) + "','"
                            + DbTools.getValidateQuery(bigram.getTerm2()) + "',"
                            + trigramUtils.getDistinctTokensCode(bigram.getTerm1(), bigram.getTerm2()) + ","
                            + bigram.getFrequency() +
                            "),";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            query = query.substring(0, query.length() - 1);

            runQuery(query);
        } else {
            String[] queries = new String[tableParts];

            for (int i = 0; i < tableParts; i++) {
                queries[i] = "INSERT INTO `" + tableName + "_" + i
                        + "`(`term1`, `term2`,`bigramCode`,`freq`) VALUES";
            }

            for (BiGram bigram : bigrams) {
                try {
                    long bigramCode = trigramUtils.getDistinctTokensCode(bigram.getTerm1(), bigram.getTerm2());
                    queries[(int) (bigramCode % tableParts)] += "('" + DbTools.getValidateQuery(bigram.getTerm1()) + "','"
                            + DbTools.getValidateQuery(bigram.getTerm2()) + "',"
                            + bigramCode + ","
                            + bigram.getFrequency() +
                            "),";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < tableParts; i++) {
                runQuery(queries[i].substring(0, queries[i].length() - 1));
            }
        }
    }

    private void runQuery(String query) {
        PreparedStatement PS = dbConnection.getPreparedStatement(query);
        try {
            PS.execute();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            System.out.println("EXCEPTION:\t" + query);
            e.printStackTrace();
        }
        try {
            PS.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void batchInsert(BiGram bigrams)//////????????????????????????!!!!!!!!!!!!!!!!!!
            throws ClassNotFoundException {
//		query = "INSERT INTO `" + tbName
//				+ "`(`term1`, `term2`,`bigramCode`,`freq`) VALUES";
//		for (BiGram bigram : bigrams.) {
//			try {
//				query += "('" + validateQuery(bigram.getTerm1()) + "','"
//						+ validateQuery(bigram.getTerm2()) + "',"
//						+ trigramUtils.getDistinctTokensCode(bigram.getTerm1(),bigram.getTerm2()) + ","
//						+ bigram.getFrequency() + "),";
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
        query = query.substring(0, query.length() - 1);
        PreparedStatement PS = dbConnection.getPreparedStatement(query);
        try {
            PS.execute();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            System.out.println("EXCEPTION:\t" + query);
            e.printStackTrace();
        }
        try {
            PS.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * this methods get second terms with bigram frequency for first term1 as
     * input
     *
     * @param term1
     * @return
     * @throws SQLException
     */
    public HashMap<String, Long> getRows(String term1) throws SQLException {
        HashMap<String, Long> result = new HashMap<String, Long>();
        Statement statement = dbConnection.getStatement(false);
        tools.util.Time.setStartTimeForNow();
        ResultSet resultSet = statement.executeQuery("select term2,freq from "
                + tableName + " where term1='" + term1 + "'");
        int counter = 0;
        try {
            while (resultSet.next()) {
                counter++;
                result.put(resultSet.getString("term2"),
                        resultSet.getLong("freq"));
                if (counter % 100 == 0)
                    System.out.println("fetched " + counter + " term in "
                            + Time.getTimeLengthForNow() + "ms.");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("fetched " + counter + " term in "
                + Time.getTimeLengthForNow() + "ms.");
        return result;
    }

    /**
     * this methods get bigram frequency
     *
     * @param term1
     * @param term2
     * @return bigram frequency
     * @throws Exception
     */


    public long getFreq(String term1, String term2) throws Exception {
        Statement statement = dbConnection.getStatement(false);
        ResultSet resultSet = statement.executeQuery("select freq from "
                + tableName + " where term1='" + term1 + "' and term2='"
                + term2 + "'");
        long result = 0l;
        try {
            if (resultSet.next()) {
                result = resultSet.getLong("freq");
            }
            if (resultSet.next()) {
                throw new Exception(
                        "Bigram database rows is invalid because more than one row exsist with term1:"
                                + term1 + " term2:" + term2);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

}
