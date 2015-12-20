package com.uninum.elite.webservice;

import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.uninum.elite.R;

public class WSSystemMsgService {
	private final static String URL = "https://ts.kits.tw/projectLYS/v0/System/";
	public final static String JSON_MESSAGE = "message";
	public final static String JSON_TIME = "timeStamp";
	public final static String JSON_TYPE = "type";
	public final static String JSON_UUID = "uuid";
	public static class WSSystemMsgServiceList extends VolleyWSJsonRequest{

		public WSSystemMsgServiceList(String token, Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Method.GET, URL + token , null, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		
		public static Boolean errorHandle(String status, Context context){

			return false;
		}
	}
	
	public static class WSSystemMsgServiceDelete extends VolleyWSJsonRequest{

		public WSSystemMsgServiceDelete(String token, String msgId, Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Method.DELETE, URL + token + "/"+ msgId , null, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		
		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context)){
				return true;
			}
			return false;
		}
	}

}
