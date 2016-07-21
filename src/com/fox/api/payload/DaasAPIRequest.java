package com.fox.api.payload;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="DaasAPIRequest")
public class DaasAPIRequest {

	private String applicationName;
	private DatabaseInfo databaseInfo;
	 private String query;
	 private String organization;
	 private String space;
	 private DatabaseConnectionAttr connectionAttr;
	
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	public DatabaseInfo getDatabaseInfo() {
		return databaseInfo;
	}
	public void setDatabaseInfo(DatabaseInfo databaseInfo) {
		this.databaseInfo = databaseInfo;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String getSpace() {
		return space;
	}
	public void setSpace(String space) {
		this.space = space;
	}
	
	public DatabaseConnectionAttr getConnectionAttr() {
		return connectionAttr;
	}
	public void setConnectionAttr(DatabaseConnectionAttr connectionAttr) {
		this.connectionAttr = connectionAttr;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	
	
}
