package com.fox.api.daas.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DAASUtility {

	private static Properties props = new Properties();
	private static final String fileLoc = "DAASBuildAPI.properties";

	static {
		loadProperties();
	}

	private static void loadProperties() {
		try {
			InputStream file = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileLoc);
			props.load(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static String getProperty(String key){
		return props.getProperty(key);
	}
	
	
	public static JSONObject processResultsAsJson(ResultSet rs) throws SQLException, JSONException {
		String colVal;
		JSONObject rootObj = new JSONObject();
		JSONObject respObj = new JSONObject();
		rootObj.put("response", respObj);
		respObj.put("status", "Success");
	
		if (null != rs) {
			int totalColumns = rs.getMetaData().getColumnCount();
			JSONArray resultRows = new JSONArray();
			JSONObject rootCollectionObj = new JSONObject();

			respObj.put("DataCollection", rootCollectionObj);
			rootCollectionObj.put("Data", resultRows);
			while (rs.next()) {
				JSONObject rowDataJsonObj = new JSONObject();
				for (int columnIndex = 1; columnIndex <= totalColumns; columnIndex++) {

					colVal = rs.getString(columnIndex);
					if (colVal != null) {
						rowDataJsonObj.put(rs.getMetaData().getColumnName(columnIndex), colVal.trim());
					}
					else
					{
						rowDataJsonObj.put(rs.getMetaData().getColumnName(columnIndex), JSONObject.NULL);
					}
					
				}
				resultRows.put(rowDataJsonObj);
			}
		}

		return rootObj;
	}
	
	public static JSONObject getErrorJson(Exception ex) throws JSONException {
		JSONObject rootObj = new JSONObject();
		JSONObject respObj = new JSONObject();

		respObj.put("status", "Error");
		respObj.put("errorDetails", ex.getMessage());
		rootObj.put("response", respObj);

		return rootObj;
	}
	
	
	public static void closeConnection(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void closeStatement(Statement stmt) {

		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
