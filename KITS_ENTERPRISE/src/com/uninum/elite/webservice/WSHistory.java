package com.uninum.elite.webservice;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;





import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.uninum.elite.R;


public class WSHistory {
	private static String url = "https://ts.kits.tw/projectLYS/v0/History/";
	private static String url2 = "https://ts.kits.tw/projectLYS/v1/History/";
	public static final String GET_HISTORY_TAG = "GET_HISTORY";
	public static final String CLEAR_HISTORY_TAG = "CLEAR_HISTORY";
	public static final String UPLOAD_HISTORY_TAG = "UPLOAD_HISTORY";
	public static final String HISTORY_NOT_FOUND = "40907";
	public static final String JSON_HISTORY_ARRAY = "hisArray";
	public static final String JSON_HISTORY_HISTORY_UUID = "hisUUID";
	public static final String JSON_HISTORY_UPLOAD_HISTORY_UUID = "hisUuid";
	public static final String JSON_HISTORY_GROUP_UUID = "unitUUID";
	public static final String JSON_HISTORY_UPLOAD_GROUP_UUID = "unitUuid";
	public static final String JSON_HISTORY_PEER_UUID = "peerUUID";
	public static final String JSON_HISTORY_STARTTIME = "startTime";
	public static final String JSON_HISTORY_ENDTIME = "endTime";
	public static final String JSON_HISTORY_TYPE = "type";
	public static final String JSON_HISTORY_ANS = "ans";
	public static final String JSON_HISTORY_CALLEE = "callee";
	public static final String HISTORY_RESPONSE_NULL = "Null";
	
	public static class WSHistoryList extends VolleyWSJsonRequest{

		public WSHistoryList(String token, String timeStamp, JSONObject jsonRequest,
				Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Method.GET, url2 + token + "/?" + "timeStamp="+ timeStamp, jsonRequest, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		
		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context)){
				return true;
			}else if(status.equals(HISTORY_NOT_FOUND)){
				Toast.makeText(context, context.getString(R.string.response_history_notfound)+" ("+status+") ", Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}
	}

	public static class WSHistoryDelete extends VolleyWSJsonRequest{

		public WSHistoryDelete(String token, Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Method.DELETE,url + token + "/?" + "hisUUID=all", null, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		
		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context)){
				return true;
			}else if(status.equals(HISTORY_NOT_FOUND)){
				Toast.makeText(context, context.getString(R.string.response_history_notfound)+" ("+status+") ", Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}
	}
	
	public static class WSHistoryUpload extends VolleyWSJsonRequest{

		public WSHistoryUpload(String token,JSONObject json, Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Method.POST, url + token , json, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		
		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context)){
				return true;
			}else if(status.equals(HISTORY_NOT_FOUND)){
				Toast.makeText(context, context.getString(R.string.response_history_notfound)+" ("+status+") ", Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}
	}
	
}
