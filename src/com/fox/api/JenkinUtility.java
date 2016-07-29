package com.fox.api;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

public class JenkinUtility {

	public static int getBuildNumber(String jobIdentifier) throws Exception{
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse jenkinResp = null;
		int buildNumber = 0;
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
				buildNumber=Integer.parseInt(httpRespBody.substring(httpRespBody.indexOf(">")+1, httpRespBody.indexOf("</")));
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
}
