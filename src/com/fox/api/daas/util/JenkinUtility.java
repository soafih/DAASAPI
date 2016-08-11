package com.fox.api.daas.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import com.fox.api.daas.payload.DaasAPIRequest;

public class JenkinUtility {
	
	private static String jenkinURL = DAASUtility.getProperty("JenkinURL"); 
	private static String jenkinJob = DAASUtility.getProperty("JenkinJob");
	

	public static String getBuildNumber(String jobIdentifier) throws Exception {

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse jenkinResp = null;
		String buildNumber = "0";
		String url = jenkinURL+"/api/xml?tree=jobs[name,builds[number,actions[parameters[name,value]]]]&xpath=/hudson/job[name='"+jenkinJob+"']/build[action/parameter[name='job_identifier'][value='"
				+ jobIdentifier + "']]/number";

		int i = 0;
		try {

			HttpGet request = new HttpGet(url);
			ResponseHandler<String> handler = new BasicResponseHandler();
			while (i < 15) {
				Thread.sleep(1000);
				i++;

				jenkinResp = httpClient.execute(request);
				try {
					String httpRespBody = handler.handleResponse(jenkinResp);
					buildNumber = httpRespBody.substring(httpRespBody.indexOf(">") + 1, httpRespBody.indexOf("</"));
					break;
				} catch (HttpResponseException ex) {
					continue;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error obtaining the Build URI ");
		}

		finally {
			httpClient.close();
			if (jenkinResp != null) {
				jenkinResp.close();
			}
		}
		return buildNumber;
	}

	public static JSONObject getStatus(String buildNumber) throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse jenkinResp = null;
		JSONObject rootObj = new JSONObject();
		JSONObject respObj = new JSONObject();

		try {

			HttpGet request = new HttpGet(
					jenkinURL+"/job/"+jenkinJob+"/" + buildNumber + "/consoleText");
			ResponseHandler<String> handler = new BasicResponseHandler();
			jenkinResp = httpClient.execute(request);
			String httpRespBody = handler.handleResponse(jenkinResp);

			if (httpRespBody.contains("Finished: SUCCESS")) {
				respObj.put("status", "Success");
				respObj.put("stage", "");

			}

			else if (httpRespBody.contains("Finished: FAILURE")) {
				respObj.put("status", "Failed");

				if (httpRespBody.contains("Build step 'Push to Cloud Foundry' marked build as failure")) {

					respObj.put("stage", "PushFailed");

				} else if (httpRespBody.contains("BUILD FAILURE")) {

					respObj.put("stage", "BuildFailed");

				}

				else if (httpRespBody.contains("ERROR: Error fetching remote repo 'origin'")) {

					respObj.put("stage", "CheckoutFailed");

				}

			}

			else if (httpRespBody.contains("Cloud Foundry push successful")) {
				respObj.put("status", "WIP");
				respObj.put("stage", "Pushed");

			}

			else if (httpRespBody.contains("BUILD SUCCESS")) {
				respObj.put("status", "WIP");
				respObj.put("stage", "BuildComplete");

			}

			else if (httpRespBody.contains("git checkout")) {
				respObj.put("status", "WIP");
				respObj.put("stage", "Checkedout");

			}

			else {
				respObj.put("status", "UNKNOWN");
				respObj.put("stage", "");
			}

			rootObj.putOpt("response", respObj);
			return rootObj;
		}

		catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception("Error obtaining the status for the build ");
		}
	}

	public static String initiateBuild(String jobIdentifier,String request) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse jenkinResp = null;
		try {

			InputStream is = new ByteArrayInputStream(request.getBytes());

			HttpPost httpPost = new HttpPost(jenkinURL+"/job/"+jenkinJob+"/build");

			HttpEntity entity = MultipartEntityBuilder.create()
					.addTextBody("json",
							"{\"parameter\": [{\"name\":\"app.properties\", \"file\":\"file0\"},"
									+ "{\"name\":\"job_identifier\", \"value\":\"" + jobIdentifier + "\"}]}")
					.addBinaryBody("file0", is, ContentType.create("application/octet-stream"), "app.properties")
					.build();

			httpPost.setEntity(entity);


			jenkinResp = httpclient.execute(httpPost);

			if (jenkinResp.getStatusLine().getStatusCode() != 201) {
				throw new Exception("Failed to invoke the JenkinJob : HTTP error code : "
						+ jenkinResp.getStatusLine().getStatusCode());
			}

			

		}

		finally {
			httpclient.close();
			if (jenkinResp != null) {
				jenkinResp.close();
			}
		}

		return jobIdentifier;
	}

}
