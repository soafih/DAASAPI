package com.fox.api.daas;



import java.sql.Connection;

import javax.ws.rs.Consumes;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.fox.api.daas.payload.DatabaseAPIRequest;
import com.fox.api.daas.util.DAASUtility;
import com.fox.api.daas.util.DatabaseUtility;

@Path("/DBUtility")
public class DatabaseAPI {

	@POST
	@Path("/ValidateQuery")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })

	public Response validateQuery(DatabaseAPIRequest request) throws JSONException {

		int statusCode = 200;
		String result = null;
		try {
			
			JSONObject resultJson = DatabaseUtility.executeQuery(request.getQuery(), DatabaseUtility.getConnectionDetails(request.getDatabaseInfo()));
			

			result = resultJson.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			result = DAASUtility.getErrorJson(ex).toString();
		}

		return Response.status(statusCode).entity(result).build();
	}
	
	@POST
	@Path("/ValidateConnection")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })

	public Response validateConnection(DatabaseAPIRequest request) throws JSONException {

		int statusCode = 200;
		String result = null;
		try {
			
			Connection con = DatabaseUtility.createConnection(DatabaseUtility.getConnectionDetails(request.getDatabaseInfo()));
			DAASUtility.closeConnection(con);
			JSONObject rootObj = new JSONObject();
			JSONObject respObj = new JSONObject();
			respObj.put("status", "Success");
			rootObj.put("response", respObj);
			
			result = rootObj.toString();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			result = DAASUtility.getErrorJson(ex).toString();
		}

		return Response.status(statusCode).entity(result).build();
	}

}
