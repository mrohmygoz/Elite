package com.uninum.elite.webservice;

import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.uninum.elite.R;

public class WSAccountAccessToken extends VolleyWSJsonRequest{
	public static final String LOGIN_INTERNAL_ERROR = "500";
	public static final String RESPONSE_NO_USER = "40902";
	public static final String RESPONSE_INCORRECT_KEY = "40102";
	public static final String RESPONSE_NOT_VALIDATION = "40905";
	public static final String RESPONSE_SIP_SERVER_ERROR = "50001";
	private static String loginMVPN_url = "https://ts.kits.tw/projectLYS/v0/Account/accessToken";	
	public WSAccountAccessToken( JSONObject jsonRequest,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(Method.POST, loginMVPN_url, jsonRequest, listener, errorListener);
		// TODO Auto-generated constructor stub		
	}
	public static Boolean errorHandle(String status, Context context){
		if(VolleyWSJsonRequest.errorHandle(status, context)){
			return true;
		}else if(status.equals(RESPONSE_SIP_SERVER_ERROR)){
			Toast.makeText(context, context.getString(R.string.response_sip_error)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}else if(status.equals(RESPONSE_NOT_VALIDATION)){
			Toast.makeText(context, context.getString(R.string.response_not_validate)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}else if(status.equals(RESPONSE_INCORRECT_KEY)){
			Toast.makeText(context, context.getString(R.string.response_device_change)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}else if(status.equals(RESPONSE_NO_USER)){
			Toast.makeText(context, context.getString(R.string.response_no_user)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}
}
