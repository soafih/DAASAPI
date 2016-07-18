package com.fox.api.payload;

public class DaasAPIRequest {

	DatabaseInfo databaseInfo;
	ApplicationInfo appInfo;
	String query;
	public DatabaseInfo getDatabaseInfo() {
		return databaseInfo;
	}
	public void setDatabaseInfo(DatabaseInfo databaseInfo) {
		this.databaseInfo = databaseInfo;
	}
	public ApplicationInfo getAppInfo() {
		return appInfo;
	}
	public void setAppInfo(ApplicationInfo appInfo) {
		this.appInfo = appInfo;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	
	
}
