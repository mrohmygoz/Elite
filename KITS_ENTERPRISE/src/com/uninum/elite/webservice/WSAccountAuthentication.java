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


public class WSAccountAuthentication {

	public static final String RESPONSE_NO_USER = "40902";	
	public static final String RESPONSE_VALIDATION_INCORRECT = "40904";
	public static final String RESEND_TAG = "RESEND";
	public static final String TRANSFER_TAG = "TRANSFER";
	public static final String VALIDATE_TAG = "VALIDATE";
	public static final String VALIDATE_RESEND_TAG = "VALIDATE_RESEND";
	private static String auth_url = "https://ts.kits.tw/projectLYS/v0/Account/authentication";

	public static class Transfer extends VolleyWSJsonRequest{

		public Transfer(JSONObject jsonRequest,
				Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Method.POST, auth_url, jsonRequest, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context)){
				return true;
			}else if(status.equals(RESPONSE_NO_USER)){
				Toast.makeText(context, context.getString(R.string.response_no_user)+" ("+status+") ", Toast.LENGTH_SHORT).show();
				return true;
			}else if(status.equals(RESPONSE_VALIDATION_INCORRECT)){
				Toast.makeText(context, context.getString(R.string.response_validation_incorrect)+" ("+status+") ", Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}		
	}

	public static class Validate extends VolleyWSJsonRequest{

		public Validate(JSONObject jsonRequest,
				Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Method.PUT, auth_url, jsonRequest, listener, errorListener);
			// TODO Auto-generated constructor stub
		}
		
		public static Boolean errorHandle(String status, Context context){
			if(VolleyWSJsonRequest.errorHandle(status, context)){
				return true;
			}else if(status.equals(RESPONSE_NO_USER)){
				Toast.makeText(context, context.getString(R.string.response_no_user)+" ("+status+") ", Toast.LENGTH_SHORT).show();
				return true;
			}else if(status.equals(RESPONSE_VALIDATION_INCORRECT)){
				Toast.makeText(context, context.getString(R.string.response_validation_incorrect)+" ("+status+") ", Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}
	}
	

}
