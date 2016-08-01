package com.fox.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.fox.api.payload.DaasAPIRequest;

@Path("/BuildApp")
public class DAASAPI {
	@POST
	// @Path("/DAASAPI/BuildApp")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

	public Response generateService(DaasAPIRequest request) throws JSONException {
		int statusCode = 200;
		String result = null;
		String buildIdentifier = null;

		try {

			// String buildURL = getBuildURI(initiateBuild(request));
			buildIdentifier = initiateBuild(request);
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
				respObj.put("logURL",
						"http://jenkins-06hw6.10.135.4.49.xip.io/job/DAASBuild/" + buildNumber + "/consoleText");
			}
			respObj.put("app_ep", "https://" + request.getApplicationName().toLowerCase()
					+ ".10.135.4.49.xip.io/FIH/service/" + request.getApplicationName());
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

		/*
		 * Map<String, String> dbCon = new HashMap<String, String>();
		 * dbCon.put("connectionString", connectionString); dbCon.put("driver",
		 * driverClass);
		 */
		return sb.toString();
	}

	private String initiateBuild(DaasAPIRequest request) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse jenkinResp = null;
		String queueItemURL = null;
		String jobIdentifier = request.getApplicationName() + "_"
				+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		try {

			InputStream is = new ByteArrayInputStream(generateInput(request).getBytes());

			HttpPost httpPost = new HttpPost("http://jenkins-06hw6.10.135.4.49.xip.io/job/DAASBuild/build");

			HttpEntity entity = MultipartEntityBuilder.create()
					.addTextBody("json",
							"{\"parameter\": [{\"name\":\"app.properties\", \"file\":\"file0\"},"
									+ "{\"name\":\"job_identifier\", \"value\":\"" + jobIdentifier + "\"}]}")
					.addBinaryBody("file0", is, ContentType.create("application/octet-stream"), "app.properties")
					.build();

			httpPost.setEntity(entity);

			/*
			 * Map<String, String> dbCon = generateInput(request);
			 * 
			 * List<NameValuePair> params = new ArrayList<NameValuePair>();
			 * params.add(new BasicNameValuePair("organization",
			 * request.getOrganization())); params.add(new
			 * BasicNameValuePair("space", request.getSpace())); params.add(new
			 * BasicNameValuePair("applicationName",
			 * request.getApplicationName())); params.add(new
			 * BasicNameValuePair("appHostName",
			 * request.getApplicationName().toLowerCase())); params.add(new
			 * BasicNameValuePair("query", request.getQuery())); params.add(new
			 * BasicNameValuePair("ConnectionString",
			 * dbCon.get("connectionString"))); params.add(new
			 * BasicNameValuePair("driverClassName", dbCon.get("driver")));
			 * params.add(new BasicNameValuePair("dbuser",
			 * request.getDatabaseInfo().getUser())); params.add(new
			 * BasicNameValuePair("password",
			 * request.getDatabaseInfo().getPassword())); params.add(new
			 * BasicNameValuePair("maxActive",
			 * request.getConnectionAttr().getMaxActive())); params.add(new
			 * BasicNameValuePair("maxIdle",
			 * request.getConnectionAttr().getMaxIdle())); params.add(new
			 * BasicNameValuePair("maxWait",
			 * request.getConnectionAttr().getMaxWait()));
			 * 
			 * httpPost.setEntity(new UrlEncodedFormEntity(params));
			 */

			jenkinResp = httpclient.execute(httpPost);

			if (jenkinResp.getStatusLine().getStatusCode() != 201) {
				throw new Exception("Failed to invoke the JenkinJob : HTTP error code : "
						+ jenkinResp.getStatusLine().getStatusCode());
			}

			/*
			 * Header[] headers = jenkinResp.getAllHeaders();
			 * 
			 * for (Header header : headers) { if
			 * (header.getName().equals("location")) { queueItemURL =
			 * header.getValue();
			 * 
			 * }
			 * 
			 * }
			 */

		}

		finally {
			httpclient.close();
			if (jenkinResp != null) {
				jenkinResp.close();
			}
		}

		return jobIdentifier;
	}

	/*
	 * private String getBuildURI(String queueItemURL) throws Exception {
	 * 
	 * CloseableHttpClient httpClient = HttpClients.createDefault();
	 * CloseableHttpResponse jenkinResp = null; String buildURL =
	 * "URI not found"; JSONObject executable = null; int i = 0; try {
	 * 
	 * HttpGet request = new HttpGet(queueItemURL + "api/json");
	 * ResponseHandler<String> handler = new BasicResponseHandler(); while (i <
	 * 15) { Thread.sleep(1000); i++; jenkinResp = httpClient.execute(request);
	 * String httpRespBody = handler.handleResponse(jenkinResp); JSONObject
	 * bodyObject = new JSONObject(httpRespBody);
	 * 
	 * try {
	 * 
	 * executable = bodyObject.getJSONObject("executable"); }
	 * 
	 * catch (JSONException je) { continue; }
	 * 
	 * buildURL = executable.get("url").toString() + "consoleText";
	 * 
	 * break; }
	 * 
	 * } catch (Exception e) { throw new Exception(
	 * "Error obtaining the Build URI "); }
	 * 
	 * finally { httpClient.close(); if (jenkinResp != null) {
	 * jenkinResp.close(); } } return buildURL; }
	 */

	private JSONObject getErrorJson(Exception ex, String buildIdentifier) throws JSONException {
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
