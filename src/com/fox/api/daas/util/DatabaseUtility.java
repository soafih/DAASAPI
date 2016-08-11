package com.fox.api.daas.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.fox.api.daas.payload.DatabaseInfo;

public class DatabaseUtility {

	public static Map<String, String> getConnectionDetails(DatabaseInfo dbInfo) throws Exception {

		String dbType = dbInfo.getDatabaseType().toLowerCase();
		String driverClass;
		String connectionURL;
		if (dbType.equals("oracle")) {
			driverClass = "oracle.jdbc.driver.OracleDriver";
			connectionURL = "jdbc:oracle:thin:@" + dbInfo.getHostName() + ":" + dbInfo.getPort() + "/"
					+ dbInfo.getDatabaseName();

		} else if (dbType.equals("as400")) {
			driverClass = "com.ibm.as400.access.AS400JDBCDriver";
			connectionURL = "jdbc:as400://" + dbInfo.getHostName() + ";databaseName=" + dbInfo.getDatabaseName()
					+ ";prompt=false;naming=system;libraries=" + dbInfo.getSchema();

		}

		else if (dbType.equals("sqlserver")) {
			driverClass = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
			connectionURL = "jdbc:mysql://" + dbInfo.getHostName() + "/" + dbInfo.getDatabaseName();
		}

		else if (dbType.equals("mysql")) {
			driverClass = "com.mysql.jdbc.Driver";
			connectionURL = "jdbc:microsoft:sqlserver://" + dbInfo.getHostName() + ":" + dbInfo.getPort()
					+ ";databaseName=" + dbInfo.getDatabaseName();

		} else {
			throw new Exception("Database Type: My" + dbType + " not supported");
		}
		Map<String, String> dbConn = new HashMap<String, String>();
		dbConn.put("connectionURL", connectionURL);
		dbConn.put("driverClass", driverClass);
		dbConn.put("dbUser", dbInfo.getUser());
		dbConn.put("dbPassword", dbInfo.getPassword());
		return dbConn;
	}

	
	private static Connection createConnection(Map<String, String> dbConn)
			throws ClassNotFoundException, SQLException {
		

		Class.forName(dbConn.get("driverClass"));
		Connection connection = DriverManager.getConnection(dbConn.get("connectionURL"),dbConn.get("dbUser"), dbConn.get("dbPassword"));

		return connection;
	}

	
	public static JSONObject executeQuery(String query, Map<String, String> dbConn)
			throws SQLException, ClassNotFoundException, JSONException {
		Connection conn = null;
		Statement stmt  = null;
		JSONObject resultJson = null;
		
		try
		{
		conn = createConnection(dbConn);

		stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

		ResultSet resultSet = stmt.executeQuery(query);
		resultJson = DAASUtility.processResultsAsJson(resultSet);

		}
		
		finally
		{
		DAASUtility.closeConnection(conn);		
		DAASUtility.closeStatement(stmt);	
		} 
		
		return(resultJson);
	}
}
