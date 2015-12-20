package com.uninum.elite.webservice;

import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import android.content.Context;

public class WSMessage {
	private static final String url = "https://ts.kits.tw/projectLYS/v0/Message/";
	public static final String GET_MESSAGE_TAG = "GET_MESSAGE_TAG";
	public static final String SEND_MESSAGE_TAG = "SEND_MESSAGE_TAG";
	public static final String MESSAGE_LIST = "messageList";
	
	public static class WSMessageSend extends VolleyWSJsonRequest{

		public WSMessageSend(String token, JSONObject jsonRequest, Listener<JSONObject> listener,
				ErrorListener errorListener) {
			super(Method.POST, url+token, jsonRequest, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		
		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context))
				return true;
			return false;
		}
		
	}
	
	public static class WSMessageUpdate extends VolleyWSJsonRequest{

		public WSMessageUpdate(String token, String when, long timeStamp, String groupUUID, JSONObject jsonRequest, Listener<JSONObject> listener,
				ErrorListener errorListener) {
			super(Method.GET, url + token + "/getMessageRequest/" + when + "/" + timeStamp + "/?group=" + groupUUID, jsonRequest, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		
		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context))
				return true;
			return false;
		}
	}
	
	public static class WSMessageFetchMore extends VolleyWSJsonRequest{
		
		public WSMessageFetchMore(String token, long timeStamp, String groupUUID, JSONObject jsonRequest, Listener<JSONObject> listener,
				ErrorListener errorListener) {
			super(Method.GET, url + token + "/getMessageRequest/old/" + timeStamp + "/?group=" + groupUUID, jsonRequest, listener, errorListener);
			// TODO Auto-generated constructor stub
		}

		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context))
				return true;
			return false;
		}
	}
	
	public static class WSMessageReadCount extends VolleyWSJsonRequest{

		public WSMessageReadCount(String token, JSONObject jsonRequest, Listener<JSONObject> listener,
				ErrorListener errorListener) {
			super(Method.PUT, url+token+"/readCount", jsonRequest, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		
		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context))
				return true;
			return false;
		}
	}
}
