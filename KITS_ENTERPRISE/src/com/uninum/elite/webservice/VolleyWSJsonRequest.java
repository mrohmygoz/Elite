package com.uninum.elite.webservice;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.uninum.elite.R;

public class VolleyWSJsonRequest extends JsonObjectRequest{
	public static final String RESPONSE_PARAMS_ERROR = "40001";
	public static final String RESPONSE_TOKEN_INCORRECT = "40101";
	public static final String RESPONSE_ILLIGAL_APIKEY = "40104";
	public static final String RESPONSE_SUCCESS = "200";
	public static final String RESPONSE_INTERNAL_ERROR = "500";
	public static final String JSON_STATUS = "status";
	@Override
	public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
		// TODO Auto-generated method stub
		retryPolicy =(RetryPolicy) new DefaultRetryPolicy(10000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		return super.setRetryPolicy(retryPolicy);
	}

	public VolleyWSJsonRequest(int method, String url, JSONObject jsonRequest,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(method, url, jsonRequest, listener, errorListener);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
		// TODO Auto-generated method stub
		 try {
	            // solution 1:
	            String jsonString = new String(response.data, "UTF-8");
	            return Response.success(new JSONObject(jsonString),
	                    HttpHeaderParser.parseCacheHeaders(response));
	        } catch (UnsupportedEncodingException e) {
	            return Response.error(new ParseError(e));
	        } catch (JSONException je) {
	            return Response.error(new ParseError(je));
	        }
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		// TODO Auto-generated method stub
		HashMap<String, String>headers = new HashMap<String, String>();
		headers.put("apiKey", "e5141ddacca87c02f80f120ea21f2fd6e2e7912e71b215d926f185380c5ccb96");
		return headers;
	}

	public static Boolean errorHandle(String status, Context context){
		if(status.equals(RESPONSE_PARAMS_ERROR)){
			Toast.makeText(context, context.getString(R.string.response_params_error)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}else if(status.equals(RESPONSE_TOKEN_INCORRECT)){
			Log.e("Jenny", "volley error: " + context.toString());
//			Toast.makeText(context, context.getString(R.string.response_token_incorrect)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}else if(status.equals(RESPONSE_ILLIGAL_APIKEY)){
			Toast.makeText(context, context.getString(R.string.response_illigal_apikey)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}else if(status.equals(RESPONSE_INTERNAL_ERROR)){
			Toast.makeText(context, context.getString(R.string.response_internal_error)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}
}
