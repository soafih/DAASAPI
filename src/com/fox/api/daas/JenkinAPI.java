package com.fox.api.daas;

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

import com.fox.api.daas.util.JenkinUtility;



@Path("/JenkinUtility")
public class JenkinAPI {
	@GET
	@Path("/GetStatus")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	
	public Response getStatus_wrapper(@Context UriInfo info) throws JSONException {
		int statusCode = 200;
		String result = null;
		try
		{
		String buildNumber = info.getQueryParameters().getFirst("buildNumber");
		
		
		if(buildNumber.trim().equals("0"))
		{
			buildNumber = JenkinUtility.getBuildNumber(info.getQueryParameters().getFirst("buildIdentifier"));
		}
		
		 
		result= JenkinUtility.getStatus(buildNumber).toString();
		
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
		
			String buildNumber = JenkinUtility.getBuildNumber(info.getQueryParameters().getFirst("buildIdentifier"));
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

	
	
	
	
	private JSONObject getErrorJson(Exception ex) throws JSONException {
		JSONObject rootObj = new JSONObject();
		JSONObject respObj = new JSONObject();

		respObj.put("status", "Error");
		respObj.put("ErrorDetails", ex.getMessage());
		rootObj.put("response", respObj);

		return rootObj;
	}
}
