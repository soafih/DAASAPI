package com.fox.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;



@Path("/JenkinUtility")
public class JenkinUtility {
	@GET
	@Path("/GetStatus")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	
	public Response getStatus(@Context UriInfo info) throws JSONException {
		int statusCode = 200;
		String result = null;
		try
		{
		String buildNumber = info.getQueryParameters().getFirst("buildNumber");
		
		
		if(buildNumber.trim().equals("0"))
		{
			buildNumber = getBuildNumber(info.getQueryParameters().getFirst("buildIdentifier"));
		}
		
		 
		result= parsebuildLog(buildNumber).toString();
		
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			result = getErrorJson(ex).toString();
		}
		
		
		return Response.status(statusCode).entity(result).build();
	}

	@GET
	@Path("/GetBuildNumber")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	
	public Response getBuildNumber_wrapper(@Context UriInfo info) throws JSONException {
		int statusCode = 200;
		String result = null;
		try
		{
		
			String buildNumber = getBuildNumber(info.getQueryParameters().getFirst("buildIdentifier"));
			JSONObject rootObj = new JSONObject();
			JSONObject respObj = new JSONObject();

			respObj.put("buildNumber", buildNumber);
			rootObj.put("response", respObj);

			result = rootObj.toString();
		}
		
		catch(Exception ex)
		{
			ex.printStackTrace();
			result = getErrorJson(ex).toString();
		}
		
		
		return Response.status(statusCode).entity(result).build();
	}

	
	public static String getBuildNumber(String jobIdentifier) throws Exception{
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse jenkinResp = null;
		String buildNumber = "0";
		String url ="http://jenkins-06hw6.10.135.4.49.xip.io/api/xml?tree=jobs[name,builds[number,actions[parameters[name,value]]]]&xpath=/hudson/job[name='DAASBuild']/build[action/parameter[name='job_identifier'][value='"+jobIdentifier+"']]/number";
		   
		int i = 0;
		try {

			HttpGet request = new HttpGet(url);
			ResponseHandler<String> handler = new BasicResponseHandler();
			while (i < 15) {
				Thread.sleep(1000);
				i++;
				
				jenkinResp = httpClient.execute(request);
				try
				{
				String httpRespBody = handler.handleResponse(jenkinResp);
				buildNumber=httpRespBody.substring(httpRespBody.indexOf(">")+1, httpRespBody.indexOf("</"));
				break;
				}
				catch(HttpResponseException ex)
				{
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
	
	
	
	private JSONObject parsebuildLog(String buildNumber) throws Exception
	{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse jenkinResp = null;
		JSONObject rootObj = new JSONObject();
		JSONObject respObj = new JSONObject();
		
		try {		

			HttpGet request = new HttpGet("http://jenkins-06hw6.10.135.4.49.xip.io/job/DAASBuild/"+buildNumber+"/consoleText");
			ResponseHandler<String> handler = new BasicResponseHandler();
			jenkinResp = httpClient.execute(request);
			String httpRespBody = handler.handleResponse(jenkinResp);
			
			if(httpRespBody.contains("Finished: SUCCESS"))
			{
				respObj.put("status", "Success");
				respObj.put("stage", "");
				
			}
			
			else if (httpRespBody.contains("Finished: FAILURE"))
			{
				respObj.put("status", "Failed");
				
				 if(httpRespBody.contains("Build step 'Push to Cloud Foundry' marked build as failure"))
				{
					
					respObj.put("stage", "PushFailed");
					
				}
				 else if(httpRespBody.contains("BUILD FAILURE"))
					{
						
						respObj.put("stage", "BuildFailed");
						
					}
				 
				 else if(httpRespBody.contains("ERROR: Error fetching remote repo 'origin'"))
					{
						
						respObj.put("stage", "CheckoutFailed");
						
					}
				 
				
			}
			
			
			else if(httpRespBody.contains("Cloud Foundry push successful"))
			{
				respObj.put("status", "WIP");
				respObj.put("stage", "Pushed");
				
			}
			
						
			
			
			else if(httpRespBody.contains("BUILD SUCCESS"))
			{
				respObj.put("status", "WIP");
				respObj.put("stage", "BuildComplete");
				
			}
			
			
			
			else if(httpRespBody.contains("git checkout"))
			{
				respObj.put("status", "WIP");
				respObj.put("stage", "Checkedout");
				
			}
			
			else
			{
				respObj.put("status", "UNKNOWN");
				respObj.put("stage", "");
			}
			
			rootObj.putOpt("response", respObj);
			return rootObj;
	}
		
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new Exception("Error obtaining the status for the build ");
		}
	}
	
	
	private JSONObject getErrorJson(Exception ex) throws JSONException {
		JSONObject rootObj = new JSONObject();
		JSONObject respObj = new JSONObject();

		respObj.put("status", "Error");
		respObj.put("ErrorDetails", ex.getMessage());
		rootObj.put("response", respObj);

		return rootObj;
	}
}
