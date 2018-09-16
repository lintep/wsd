package nlp.languagemodel.db;

import tools.database.DbConnection;

import java.sql.SQLException;

public class TrigramScore {
	
	int tableCount=0;
	
	DbConnection dbConnection;
	
	public TrigramScore(DbConnection dbConnection, int tableCount) throws ClassNotFoundException, SQLException {
		this.dbConnection=dbConnection;
		this.tableCount=tableCount;
		System.out.println("TrigramScore connection ok."); 
	}
	
	public long getTrigramScore(String tableName,long triramCoCode) throws SQLException{
		int tableId = (int) (triramCoCode % tableCount);
		String query="select freq from `"+tableName+"_"+tableId+"` where bigramcode="+triramCoCode;
		String result = this.dbConnection.selectUniqueValue(query);
		if(result.length()>0 && result!=null){
			return Long.parseLong(result);
		}
		return -1;
	}
}
