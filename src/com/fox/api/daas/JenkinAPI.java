package com.fox.api.daas;



import javax.ws.rs.Consumes;
import javax.ws.rs.GET;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;



import org.json.JSONException;
import org.json.JSONObject;

import com.fox.api.daas.util.DAASUtility;
import com.fox.api.daas.util.JenkinUtility;



@Path("/JenkinUtility")
public class JenkinAPI {
	@GET
	@Path("/GetStatus")
	@Produces({ MediaType.APPLICATION_JSON})
	@Consumes({ MediaType.APPLICATION_JSON})
	
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
			result = DAASUtility.getErrorJson(ex).toString();
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
			result = DAASUtility.getErrorJson(ex).toString();
		}
		
		
		return Response.status(statusCode).entity(result).build();
	}

	
	
	
	
	
}
