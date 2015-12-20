package com.uninum.elite.webservice;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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


public class WSAccount {
	public static final String REGISTER_DUPLICATE = "40901";
	public static final String SUCCESS  ="200";	
	public static final String REGISTER_SUCCESS = "SUCCESS";
	public static final String REGISTER_TAG = "RESIGTER";
	public static final String REGISTER_PUSHID = "REGISTER_PUSHID";
	public static final String GET_USER_PHOTOID = "USER_PHOTOID";
	public static final String JSON_REGISTER_PUSHID = "pushID";
	public static final String JSON_IMAGEID = "imgId";
	
	private static String account_url = "https://ts.kits.tw/projectLYS/v0/Account/";
	
	public static class Register extends VolleyWSJsonRequest{

		public Register(JSONObject jsonRequest,
				Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Method.POST, account_url, jsonRequest, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context)){
				return true;
			}
			else if(status.equals(REGISTER_DUPLICATE)){
				Toast.makeText(context, context.getString(R.string.response_account_duplicate)+" ("+status+") ", Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}
	}
	
	public static class WSRegisterPushID extends VolleyWSJsonRequest{

		public WSRegisterPushID(String token, JSONObject jsonRequest,
				Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Method.PUT, account_url  + token + "/" + "pushID", jsonRequest, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context)){
				return true;
			}
			return false;
		}
	}
	
	public static class WSGetPhotoID extends VolleyWSJsonRequest{

		public WSGetPhotoID(String token, Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Method.GET, account_url  + token + "/" + "imageId", null, listener, errorListener);
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
