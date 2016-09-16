package com.fox.api.daas;



import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

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
import com.fox.api.daas.util.DatabaseUtility;
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
				respObj.put("logURL",System.getenv("JenkinURL")+"/job/"+System.getenv("JenkinJob")+"/"+buildNumber + "/consoleText");
						
			}
			
			respObj.put("app_ep", "https://" + request.getApplicationName().toLowerCase()
					+ "."+request.getDomain()+"/FIH/service/" + request.getApplicationName());
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

		Map<String, String> dbCon = DatabaseUtility.getConnectionDetails(request.getDatabaseInfo());

		StringBuffer sb = new StringBuffer("");
		sb.append("organization=" + request.getOrganization() + "\n");
		sb.append("space=" + request.getSpace() + "\n");
		sb.append("domain=" + request.getDomain() + "\n");
		sb.append("applicationName=" + request.getApplicationName() + "\n");
		sb.append("appHostName=" + request.getApplicationName().toLowerCase() + "\n");
		sb.append("query=" + request.getQuery() + "\n");
		sb.append("resultCaching=" + request.getResultCaching() + "\n");
		sb.append("cacheExpiry=" + request.getCacheExpiry() + "\n");
		sb.append("driverClassName=" + dbCon.get("driverClass") + "\n");
		sb.append("ConnectionString=" + dbCon.get("connectionURL") + "\n");
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
		respObj.put("errorDetails", ex.getMessage());
		if (buildIdentifier != null) {
			respObj.put("buildIdentifier", buildIdentifier);
		}
		rootObj.put("response", respObj);

		return rootObj;
	}

	

}
