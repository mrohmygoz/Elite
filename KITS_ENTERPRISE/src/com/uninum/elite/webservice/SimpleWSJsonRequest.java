package com.uninum.elite.webservice;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class SimpleWSJsonRequest {
	public static final String RESPONSE_PARAMS_ERROR = "40001";
	public static final String RESPONSE_TOKEN_INCORRECT = "40101";
	public static final String RESPONSE_ILLIGAL_APIKEY = "40104";
	public static final String RESPONSE_SUCCESS = "200";
	public static final String RESPONSE_INTERNAL_ERROR = "500";
	public static final String JSON_STATUS = "status";
	
	private String apiKey = "e5141ddacca87c02f80f120ea21f2fd6e2e7912e71b215d926f185380c5ccb96";

	public String WSJsonRequestDELETE(String url){
		HttpClient client = new DefaultHttpClient();
		try{
			HttpDelete delete = new HttpDelete(url);
			delete.addHeader("apiKey", apiKey);
			delete.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(delete);
			String response_str = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);			
			Log.d("KITs HttpDelete", response_str);
			return URLDecoder.decode(response_str,"utf-8");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public String WSJsonRequestGET(String url) throws JSONException, ClientProtocolException, IOException{
		Log.d("ELITE","Get GroupContact: "+url);
		HttpClient client = new DefaultHttpClient();
			HttpGet delete = new HttpGet(url);
			delete.addHeader("apiKey", apiKey);
			delete.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(delete);
			if(response.getEntity()!=null){
				return EntityUtils.toString(response.getEntity(), HTTP.UTF_8);				
			}else{
				JSONObject json = new JSONObject();
				json.put(JSON_STATUS, response.getStatusLine().getStatusCode());
				Log.d("ELITE","NOT_MODIFY:"+response.getStatusLine().getStatusCode());
				return json.toString();
			}
		
	}
	
	public String WSJsonRequestPOST(String url, JSONObject json_params){
		
		HttpClient client = new DefaultHttpClient();
		try{
			HttpPost post = new HttpPost(url);
			post.addHeader("apiKey", apiKey);
	        post.setHeader("Content-type", "application/json");
			StringEntity se= new StringEntity(json_params.toString());
			post.setEntity(se);		
			ResponseHandler< String > responseHandler = new BasicResponseHandler();
			HttpResponse response = client.execute(post);
			String response_str = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);			
			Log.d("KITs HttpPost", response_str);
			return URLDecoder.decode(response_str,"utf-8");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
	}
	
	public String WSJsonRequestPUT(String url, JSONObject json_params){
		
		HttpClient client = new DefaultHttpClient();
		try{
			HttpPut put = new HttpPut(url);
			put.addHeader("apiKey", apiKey);
	        put.setHeader("Content-type", "application/json");
			StringEntity se= new StringEntity(json_params.toString());
			put.setEntity(se);		
			ResponseHandler< String > responseHandler = new BasicResponseHandler();
			HttpResponse response = client.execute(put);
			String response_str = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);			
			Log.d("KITs HttpPUT", response_str);
			return URLDecoder.decode(response_str,"utf-8");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
	}
}
