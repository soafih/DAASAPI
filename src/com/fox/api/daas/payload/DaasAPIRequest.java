package com.fox.api.daas.payload;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="DaasAPIRequest")
public class DaasAPIRequest {

	private String applicationName;
	private DatabaseInfo databaseInfo;
	 private String query;
	 private String organization;
	 private String space;
	 private String domain;
	 private String resultCaching;
	private String cacheExpiry;
	private String cacheCounter;

	public String getCacheCounter() {
		return cacheCounter;
	}

	public void setCacheCounter(String cacheCounter) {
		this.cacheCounter = cacheCounter;
	}

	public String getResultCaching() {
		return resultCaching;
	}

	public void setResultCaching(String resultCaching) {
		this.resultCaching = resultCaching;
	}

	public String getCacheExpiry() {
		return cacheExpiry;
	}

	public void setCacheExpiry(String cacheExpiry) {
		this.cacheExpiry = cacheExpiry;
	}

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
	 public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	
}
