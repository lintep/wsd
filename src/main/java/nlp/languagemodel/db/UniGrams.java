package nlp.languagemodel.db;

import nlp.languagemodel.data.UniGram;
import tools.database.DbConnection;
import tools.database.DbDataFields;
import tools.database.DbTools;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;

public class UniGrams {
    final static String TABLEPREFNAME ="unigrams";
    String tbName = TABLEPREFNAME;
    DbConnection dbConnection;

    public UniGrams(DbConnection dbConnection, String dataSetName) {
        try {
            this.dbConnection = dbConnection;
            tbName += "_"+dataSetName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void batchInsert(HashSet<UniGram> unigrams)
            throws ClassNotFoundException {
        String query = "INSERT INTO `" + tbName + "`(`term`, `freq`) VALUES";
        for (UniGram unigram : unigrams) {
            query += "('" + DbTools.getValidateQuery(unigram.getTerm1()) + "',"
                    + unigram.getFrequency() + "),";
        }
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
     * this methods get unigram frequency
     *
     * @param term
     * @return unigram frequency
     * @throws Exception
     */
    public long getFreq(String term) throws Exception {
        Statement statement = dbConnection.getStatement(false);
        ResultSet resultSet = statement.executeQuery("select freq from "
                + tbName + " where term='" + term + "'");
        long result = 0l;
        try {
            if (resultSet.next()) {
                result = resultSet.getLong("freq");
            }
            if (resultSet.next()) {
                throw new Exception(
                        "Unigram database rows is invalid because more than one row exsist with term:"
                                + term);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }


    public static void createMySqlTable(String nodeName, int dbPort, String username, String pass, String dbName, String dataSetName, boolean addIndex) throws Exception {
        HashMap<String, DbDataFields.MySqlDataFields> fieldMap = new HashMap<>();
        fieldMap.put("term", DbDataFields.MySqlDataFields.VARCHAR256);
        fieldMap.put("freq", DbDataFields.MySqlDataFields.INT11);

        DbConnection mySqlCon = DbConnection.getInstance(DbTools.getDbUrl(nodeName, dbPort, dbName, DbTools.DbType.MYSQl), DbTools.getClass(DbTools.DbType.MYSQl), username, pass);

        try {
            DbTools.createMySqlTable(mySqlCon.getConnection(), TABLEPREFNAME + "_" + dataSetName, fieldMap);
        } catch (Exception e) {
            if (e.getMessage().indexOf("already exists") >= 0) {
                System.out.println(e.getMessage());
            } else {
                throw e;
            }
        }
        if(addIndex){
            DbTools.addMySQlIndex(mySqlCon.getConnection(), TABLEPREFNAME + "_" + dataSetName,"term");
        }

    }

}
