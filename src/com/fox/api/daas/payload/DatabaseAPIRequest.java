package com.fox.api.daas.payload;

public class DatabaseAPIRequest {
	
	private DatabaseInfo databaseInfo;
	private String query;
	
	public DatabaseInfo getDatabaseInfo() {
		return databaseInfo;
	}
	public void setDatabaseInfo(DatabaseInfo databaseInfo) {
		this.databaseInfo = databaseInfo;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	

}
