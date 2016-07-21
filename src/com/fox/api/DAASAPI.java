package com.fox.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fox.api.payload.DaasAPIRequest;



@Path("/service")
public class DAASAPI {
	@POST
	@Path("/DAASAPI")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})


	public Response generateService(DaasAPIRequest request) throws ClientProtocolException, IOException, JSONException
			 {
	int statusCode =200;
	 CloseableHttpClient httpclient = HttpClients.createDefault();
	  CloseableHttpResponse response2 = null;
	  try{
		  ;
		  InputStream is=new ByteArrayInputStream(generateInput(request).getBytes()) ;
		 
	  HttpPost httpPost = new HttpPost("http://jenkins-06hw6.10.135.4.49.xip.io/job/DAASBuild/build");
	  
	  HttpEntity entity = MultipartEntityBuilder
			    .create()
			    .addTextBody("json", "{\"parameter\": [{\"name\":\"app.properties\", \"file\":\"file0\"}]}")
			    .addBinaryBody("file0",is,ContentType.create("application/octet-stream"), "app.properties").build();
     
	  httpPost.setEntity(entity);
      response2 = httpclient.execute(httpPost);

 /*    try {
         System.out.println(response2.getStatusLine());
       
         // do something useful with the response body
         // and ensure it is fully consumed
         if (response2.getStatusLine().getStatusCode() != 201) {
 			throw new RuntimeException("Failed : HTTP error code : "
 				+ response2.getStatusLine().getStatusCode());
 		}
       
 		

 		String output;
 		System.out.println("HI");
 		System.out.println("Output from Server .... \n");
 		BufferedReader br = new BufferedReader(
               new InputStreamReader((response2.getEntity().getContent())));
 		while ((output = br.readLine()) != null) {
 			System.out.println(output);
 		}*/
 		
 		JSONObject rootObj = new JSONObject();
		
		
		
		
		JSONObject sobj= new JSONObject();
		//sobj.put("id", info.getQueryParameters().getFirst("id"));
		//sobj.put("name", info.getQueryParameters().getFirst("name"));
		
		sobj.put("result","success");
		
		rootObj.put("response", sobj);
		
		
		return Response.status(statusCode).entity(rootObj.toString()).build();
 	
     } finally {
         //response2.close();
         httpclient.close();
     }
	 
 
}
	
	private String generateInput(DaasAPIRequest request)
	{
		
		StringBuffer sb= new StringBuffer("");
		sb.append("organization="+request.getOrganization());
		sb.append("space="+request.getSpace());
		sb.append("applicationName=" +request.getApplicationName());
		sb.append("query="+request.getQuery() );
		sb.append("databaseType="+request.getDatabaseInfo().getDatabaseType() );
		sb.append("hostName="+request.getDatabaseInfo().getHostName() );
		sb.append("port="+request.getDatabaseInfo().getPort() );
		sb.append("databaseName="+request.getDatabaseInfo().getDatabaseName());
		sb.append("user="+request.getDatabaseInfo().getUser());
		sb.append("password="+request.getDatabaseInfo().getPassword());
		sb.append("maxActive="+request.getConnectionAttr().getMaxActive());
		sb.append("maxIdle="+request.getConnectionAttr().getMaxIdle());
		sb.append("maxWait="+request.getConnectionAttr().getMaxWait());
		
		return sb.toString();
	}

}
