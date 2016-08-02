package com.fox.api.daas;



import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONException;
import org.json.JSONObject;

import com.fox.api.daas.payload.DaasAPIRequest;
import com.fox.api.daas.util.DAASUtility;
import com.fox.api.daas.util.JenkinUtility;

@Path("/BuildApp")
public class DAASAPI {
	@POST
	@Produces({ MediaType.APPLICATION_JSON})
	@Consumes({ MediaType.APPLICATION_JSON})

	public Response generateService(DaasAPIRequest request) throws JSONException {
		int statusCode = 200;
		String result = null;
		String buildIdentifier = null;

		try {

			String jobIdentifier = request.getApplicationName() + "_"
					+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			buildIdentifier = JenkinUtility.initiateBuild(jobIdentifier,generateInput(request) );
			String buildNumber = JenkinUtility.getBuildNumber(buildIdentifier);

			JSONObject rootObj = new JSONObject();
			JSONObject respObj = new JSONObject();

			if (buildNumber.equals("0")) {
				respObj.put("status", "Queued");
				respObj.put("stage", "");
				respObj.put("logURL", "log URI not found");
			}

			else {
				respObj.put("status", "WIP");
				respObj.put("stage", "Triggered");
				respObj.put("logURL",DAASUtility.getProperty("JenkinURL")+"/job/"+DAASUtility.getProperty("JenkinJob")+"/"+buildNumber + "/consoleText");
						
			}
			
			respObj.put("app_ep", "https://" + request.getApplicationName().toLowerCase()
					+ "."+DAASUtility.getProperty("StackatoDomain")+"/FIH/service/" + request.getApplicationName());
			respObj.put("buildNumber", buildNumber);
			respObj.put("buildIdentifier", buildIdentifier);

			rootObj.put("response", respObj);
			result = rootObj.toString();

		}

		catch (Exception ex) {
			ex.printStackTrace();
			JSONObject resultJson = getErrorJson(ex, buildIdentifier);
			result = resultJson.toString();
			// statusCode = 501;

		}

		return Response.status(statusCode).entity(result).build();

	}

	private String generateInput(DaasAPIRequest request) throws Exception {

		String dbType = request.getDatabaseInfo().getDatabaseType().toLowerCase();
		String driverClass;
		String connectionString;

		if (dbType.equals("oracle")) {
			driverClass = "oracle.jdbc.driver.OracleDriver";
			connectionString = "jdbc:oracle:thin:@" + request.getDatabaseInfo().getHostName() + ":"
					+ request.getDatabaseInfo().getPort() + "/" + request.getDatabaseInfo().getDatabaseName();

		} else if (dbType.equals("as400")) {
			driverClass = "com.ibm.as400.access.AS400JDBCDriver";
			connectionString = "jdbc:as400://" + request.getDatabaseInfo().getHostName() + ";databaseName="
					+ request.getDatabaseInfo().getDatabaseName() + ";prompt=false;naming=system;libraries="
					+ request.getDatabaseInfo().getSchema();

		}

		else if (dbType.equals("sqlserver")) {
			driverClass = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
			connectionString = "jdbc:mysql://" + request.getDatabaseInfo().getHostName() + "/"
					+ request.getDatabaseInfo().getDatabaseName();
		}

		else if (dbType.equals("mysql")) {
			driverClass = "com.mysql.jdbc.Driver";
			connectionString = "jdbc:microsoft:sqlserver://" + request.getDatabaseInfo().getHostName() + ":"
					+ request.getDatabaseInfo().getPort() + ";databaseName="
					+ request.getDatabaseInfo().getDatabaseName();

		} else {
			throw new Exception("Database Type: My" + dbType + " not supported");
		}

		StringBuffer sb = new StringBuffer("");
		sb.append("organization=" + request.getOrganization() + "\n");
		sb.append("space=" + request.getSpace() + "\n");
		sb.append("applicationName=" + request.getApplicationName() + "\n");
		sb.append("appHostName=" + request.getApplicationName().toLowerCase() + "\n");
		sb.append("query=" + request.getQuery() + "\n");
		sb.append("driverClassName=" + driverClass + "\n");
		sb.append("ConnectionString=" + connectionString + "\n");
		sb.append("dbuser=" + request.getDatabaseInfo().getUser() + "\n");
		sb.append("password=" + request.getDatabaseInfo().getPassword() + "\n");
		sb.append("maxActive=" + request.getConnectionAttr().getMaxActive() + "\n");
		sb.append("maxIdle=" + request.getConnectionAttr().getMaxIdle() + "\n");
		sb.append("maxWait=" + request.getConnectionAttr().getMaxWait() + "\n");

		
		return sb.toString();
	}

	
	public JSONObject getErrorJson(Exception ex, String buildIdentifier) throws JSONException {
		JSONObject rootObj = new JSONObject();
		JSONObject respObj = new JSONObject();

		respObj.put("status", "Error");
		respObj.put("ErrorDetails", ex.getMessage());
		if (buildIdentifier != null) {
			respObj.put("buildIdentifier", buildIdentifier);
		}
		rootObj.put("response", respObj);

		return rootObj;
	}

	

}
