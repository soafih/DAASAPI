package com.fox.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


import com.fox.api.payload.DaasAPIRequest;



@Path("/service")
public class GenerateDAASService {
	@POST
	@Path("/generateDaasApp")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})


	public String generateService(DaasAPIRequest request) throws ClientProtocolException, IOException
			 {
	
	 CloseableHttpClient httpclient = HttpClients.createDefault();
	  CloseableHttpResponse response2 = null;
	  try{
	  HttpPost httpPost = new HttpPost("http://jenkins-06hw6.10.135.4.49.xip.io/job/JenkinsBuild/build");
     
      response2 = httpclient.execute(httpPost);

 /*    try {
         System.out.println(response2.getStatusLine());
       
         // do something useful with the response body
         // and ensure it is fully consumed
         if (response2.getStatusLine().getStatusCode() != 201) {
 			throw new RuntimeException("Failed : HTTP error code : "
 				+ response2.getStatusLine().getStatusCode());
 		}
       */
 		

 		String output;
 		System.out.println("HI");
 		System.out.println("Output from Server .... \n");
 		BufferedReader br = new BufferedReader(
               new InputStreamReader((response2.getEntity().getContent())));
 		while ((output = br.readLine()) != null) {
 			System.out.println(output);
 		}
 	
     } finally {
         response2.close();
         httpclient.close();
     }
	 
 return "Success";
}

}
