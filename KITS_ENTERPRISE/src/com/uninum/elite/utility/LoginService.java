package com.uninum.elite.utility;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.uninum.elite.R;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.ui.Activity_Main;
import com.uninum.elite.ui.Activity_Message;
import com.uninum.elite.ui.MainPage;
import com.uninum.elite.ui.TabHistory;
import com.uninum.elite.webservice.VolleyWSJsonRequest;
import com.uninum.elite.webservice.WSAccount;
import com.uninum.elite.webservice.WSAccountAccessToken;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LoginService extends Service {
	
	private GoogleCloudMessaging gcm;
	private String regid;
	public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String SENDER_ID = "105419119720";
    public static final String CALLBACK = "CALLBACK";

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent!=null) {
			Bundle bundle = intent.getExtras();
			String callback = bundle.getString(CALLBACK);
			login(callback);
		}
		return super.onStartCommand(intent, Service.START_FLAG_REDELIVERY, startId);
	}


	private void login(final String callback){
		
		JSONObject json = new JSONObject();
		Log.d("kits new","deviceKey:"+SystemManager.getUniqueID(this.getApplicationContext()) + " account:"+ SystemManager.getPreString(this.getApplicationContext(), SystemManager.USER_ACCOUNT));
		try {
			String account = SystemManager.getPreString(this.getBaseContext(), SystemManager.USER_ACCOUNT);
			Log.d("Jenny","Login: "+account);
			if(account.startsWith("+")){
				// do nothing
			}else{		    
			       if(account.startsWith("0")){
			    	   account = account.substring(1, account.length());
			       }
			       account = "+886" + account;
				Log.d("Jenny","Login: "+account);
				SystemManager.putPreString(this.getApplicationContext(), SystemManager.USER_ACCOUNT, account);
			}
			json.put("account",account +"@pack");
			json.put("deviceKey", SystemManager.getUniqueID(this.getApplicationContext()));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WSAccountAccessToken loginRequest = new WSAccountAccessToken(json,new Response.Listener<JSONObject>() {
			
			@Override
			public void onResponse(JSONObject response) {
			try{	
				String status = response.getString("status");
				Log.i("Jenny","Access token success: " + response.toString());					
					
				if(status != null && status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)) {
					String token = response.getString("token");
					SystemManager.setToken(getApplicationContext(), token);
			        if (checkPlayServices()) {
			        	
			        	registerInBackground();
			        	KitsApplication.getInstance().Login();
			        	

			        	//-----------------------------------//
			        	//------------ Main Task ------------//
			        	//-----------------------------------//
			        	
		                switch (callback) {
		                
		                case "Activity_Main":
							if (SystemManager.getPreBoolean(getApplicationContext(), SystemManager.USER_FIRST_OPEN))
								Activity_Main.getPhotoHandler.sendEmptyMessage(0);
							else {
								Activity_Main.stopServiceHandler.sendEmptyMessage(0);
								MainPage.startUpdateContact.sendEmptyMessage(0);
							}
							break;
							
		                case "Activity_Message":
		                	Activity_Message.stopService.sendEmptyMessage(0);
		                	break;
		                
		                case "TabHistory":
		                	TabHistory.stopService.sendEmptyMessage(0);
		                	break;
						}    
			                
		                //-----------------------------------//
			        	//------------ Main Task ------------//
			        	//-----------------------------------//  
		                
		                
		                
			                
			        } else {
			            Log.i("ELITE", "No valid Google Play Services APK found.");
			        }				
			        
				}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				NetworkResponse errorResponse = error.networkResponse;
				if(errorResponse != null && errorResponse.data != null){					
					try {
						JSONObject response = new JSONObject(new String(errorResponse.data));
						Log.e("Jenny","Error Login:"+response);
						if (WSAccountAccessToken.errorHandle(response.getString(VolleyWSJsonRequest.JSON_STATUS), getBaseContext()))
							return ;
						else
							Toast.makeText(getBaseContext(), R.string.response_connect_error, Toast.LENGTH_SHORT).show();
					} catch (JSONException e) {
						e.printStackTrace();
					}								
				}else{
					Toast.makeText(getBaseContext(), R.string.response_connect_error, Toast.LENGTH_SHORT).show();
				}
				
				
				
				//-----------------------------------//
	        	//------------ Main Task ------------//
	        	//-----------------------------------//  
				
				switch (callback) {
                
                case "Activity_Main":
					if (SystemManager.getPreBoolean(getApplicationContext(), SystemManager.USER_FIRST_OPEN))
						Activity_Main.getPhotoHandler.sendEmptyMessage(0);
					else {
						Activity_Main.stopServiceHandler.sendEmptyMessage(0);
						MainPage.startUpdateContact.sendEmptyMessage(0);
					}
					break;
					
                case "Activity_Message":
                	Activity_Message.stopService.sendEmptyMessage(0);
                	break;
                
                case "TabHistory":
                	TabHistory.stopService.sendEmptyMessage(0);
                	break;
				}  
				
				//-----------------------------------//
	        	//------------ Main Task ------------//
	        	//-----------------------------------//
			}
		} );
		KitsApplication.getInstance().addToRequestQueue(loginRequest, "LOGIN");	
	}
	
	private void registerInBackground() {
	    new AsyncTask<Void, String, String>() {
	        @Override
	        protected String doInBackground(Void... params) {
	            String msg = "";
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
	                }
	                regid = gcm.register(SENDER_ID);
	                msg = "Device registered, registration ID=" + regid;
	
	                // You should send the registration ID to your server over HTTP,
	                // so it can use GCM/HTTP or CCS to send messages to your app.
	                // The request to your server should be authenticated if your app
	                // is using accounts.
	                sendRegistrationIdToBackend(regid);
	
	                // For this demo: we don't need to send it because the device
	                // will send upstream messages to a server that echo back the
	                // message using the 'from' address in the message.
	
	                // Persist the regID - no need to register again.
	                storeRegistrationId(getApplicationContext(), regid);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	                // If there is an error, don't just keep trying to register.
	                // Require the user to click a button again, or perform
	                // exponential back-off.
	            }
	            return msg;
	        }
	
	        @Override
	        protected void onPostExecute(String msg) {
//	            Log.i("Jenny","login register ID:"+ msg);
	        }
	    }.execute(null, null, null);
	}
	
	private boolean checkPlayServices(){
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	            Log.i("ELITE", "This device is not supported.");
	        return false;
	    }
	    return true;
	}
	
	private void sendRegistrationIdToBackend(String regid) {
	    // Your implementation here.
		if(!regid.equals("") &&  !SystemManager.getToken(this).equals("")){
			JSONObject json = new JSONObject();
			try {
				json.put(WSAccount.JSON_REGISTER_PUSHID, regid);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			WSAccount.WSRegisterPushID wsRegisterPushID = new WSAccount.WSRegisterPushID(SystemManager.getToken(this), json, new Response.Listener<JSONObject>() {
				
				@Override
				public void onResponse(JSONObject response) {
					// TODO Auto-generated method stub
				try{	
					String status = response.getString("status");									
					if(status != null && status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)) {
						Log.i("Jenny", "login service: Send register id to back end");
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					// TODO Auto-generated method stub
	
					NetworkResponse errorResponse = error.networkResponse;
					//JSONObject json = new JSONObject();
					String json;
					if(errorResponse != null && errorResponse.data != null){
						Log.e("Jenny", "login service: WSRegisterPushID error");
						try {
							JSONObject response = new JSONObject(new String(errorResponse.data));
							if (WSAccount.WSRegisterPushID.errorHandle(response.getString(VolleyWSJsonRequest.JSON_STATUS), getApplicationContext()))
								return ;
							else
								Toast.makeText(getBaseContext(), R.string.response_connect_error, Toast.LENGTH_SHORT).show();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}								
					}else{
						Toast.makeText(getBaseContext(), R.string.response_connect_error, Toast.LENGTH_SHORT).show();
					}
				}
			} );
			KitsApplication.getInstance().addToRequestQueue(wsRegisterPushID, WSAccount.REGISTER_PUSHID);	
		}
	}
	
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    int appVersion = getAppVersion(context);
	    Log.i("ELITE", "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}
	
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return getSharedPreferences(Activity_Main.class.getSimpleName(),Context.MODE_PRIVATE);
	}
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
}
