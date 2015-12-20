package com.uninum.elite.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.uninum.elite.R;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;


public class WSContact {

	public static final String GET_GROUP_UUID = "GROUP_UNIT_UUID";
	public static final String GET_CONTACT_GROUP = "GROUP_CONTACT_LIST";
	public static final String SET_PRIVACY = "SET_PRIVACY";
	public static final String DELETE_GROUP = "DELETE_GROUP";
	public static final String ERROR_GROUPUUID = "40914";
	public static final String ERROR_POINTCARD = "40910";
	public static final String GET_CONTACT_SUCCESS = "200";
	public static final String STATUS = "status";
	public static final String JSON_PRIVACY = "privacy";
	private static String[] unitUUID;
	private static String[] unitName;
	private static String getContactUUID_url = "https://ts.kits.tw/projectLYS/v0/Contact/";
	
	public static class WSContactGroup extends VolleyWSJsonRequest{
		public WSContactGroup( String token, Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Method.GET, getContactUUID_url+token, null, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		public static Boolean errorHandle(String status, Context context){			
			return VolleyWSJsonRequest.errorHandle(status, context);
		}
	}
	public static class WSContactGroupContactList extends VolleyWSJsonRequest{
		public WSContactGroupContactList(String token, String uuid, Listener<JSONObject> listener,
				ErrorListener errorListener) {
			super(Method.GET, getContactUUID_url+token +"/" + uuid, null, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context)){
				return true;
			}else if(status.equals(ERROR_GROUPUUID)){
				Toast.makeText(context, context.getString(R.string.response_params_error)+" ("+status+") ", Toast.LENGTH_SHORT).show();
				return true;
			}else if(status.equals(ERROR_POINTCARD)){
				Toast.makeText(context, context.getString(R.string.response_params_error)+" ("+status+") ", Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}
	}
	
	public static class WSContactGroupPrivacy extends VolleyWSJsonRequest{

		public WSContactGroupPrivacy(String token, String groupUUID, JSONObject jsonRequest,
				Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Method.PUT, getContactUUID_url + token + "/" + groupUUID , jsonRequest, listener, errorListener);			
			// TODO Auto-generated constructor stub
		}
		public static Boolean errorHandle(String status, Context context){			
			return VolleyWSJsonRequest.errorHandle(status, context);
		}		
	}
	
	public static class WSContactGroupDelete extends VolleyWSJsonRequest{
		public WSContactGroupDelete(String token, String uuid, Listener<JSONObject> listener,
				ErrorListener errorListener) {
			super(Method.DELETE, getContactUUID_url+token +"/" + uuid, null, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		public static Boolean errorHandle(String status, Context context){			
			return VolleyWSJsonRequest.errorHandle(status, context);
		}
	}
}
