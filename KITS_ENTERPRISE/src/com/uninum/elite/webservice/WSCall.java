package com.uninum.elite.webservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.uninum.elite.R;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.utility.CallPhoneStateListener;
import com.uninum.elite.utility.NetworkUtility;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;




public class WSCall {
	public static final String CALL_INCORRECT_NUMBER_FORMAT = "40001";
	public static final String CALL_CALLEE_NOT_MVPN = "40906";
	public static final String INTERNAL_ERROR = "500";
	public static final String SIP_SERVER_ERROR = "50001";
	public static final String CALL_POINT_NOT_ENOUGH = "40913";
	public static final String CARDID = "cardID";
	public static final String CALLEE = "callee";
	public static final String TOKEN = "token";
	public static final String STATUS = "status";
	static String url = "https://ts.kits.tw/projectLYS/v0/Call/";
	public static CountDownTimer callCountDownTimer;
	private static CallPhoneStateListener callListener;
	public static class CallingWithTimer{
		
		public void Calling(final Context context, final String... args){
			try{
				if(NetworkUtility.isConnected(context)){
					callAsyncTask(context, args);

				}else{
					Toast.makeText(context, R.string.network_invalid, Toast.LENGTH_SHORT).show();
				}
			}catch(Exception e){
				e.printStackTrace();
				Toast.makeText(context, R.string.network_invalid, Toast.LENGTH_SHORT).show();
			}

		}
		
		private void callAsyncTask(final Context context, final String[] args){
			new AsyncTask<Void, Void, String>(){
				Dialog toastDialog ;
				String callee;
				String name;
				@Override
				protected void onPreExecute() {
					// TODO Auto-generated method stub
					super.onPreExecute();
					final Builder busyDialog = new AlertDialog.Builder(context);
					callCountDownTimer = new CountDownTimer(10000, 10000){
						@Override
						public void onFinish() {
							// TODO Auto-generated method stub
							busyDialog.setTitle(R.string.call_no_response_title);
							busyDialog.setMessage(context.getString(R.string.call_no_response));
							busyDialog.setCancelable(true);
							busyDialog.show();
							// stop listen phone state;
							TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
							telephonyManager.listen(callListener, PhoneStateListener.LISTEN_NONE);
						}
	
						@Override
						public void onTick(long arg0) {
							// TODO Auto-generated method stub
	
						}
						
					}.start();
					callListener = new CallPhoneStateListener(context);
					TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
					telephonyManager.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
			
				}

				@Override
				protected String doInBackground(Void... arg0) {
					// TODO Auto-generated method stub
					String token = (String)args[0];
					callee = (String)args[1];
					String cardID = (String)args[2];
					name = (String)args[3];
					String call_url = url + token + "/callRequest";		
					HashMap<String, String> params = new HashMap<String, String>();
					params.put(CALLEE, callee);
					params.put(TOKEN, token);
					params.put(CARDID, cardID);
					JSONObject json = new JSONObject(params);
					SimpleWSJsonRequest wsRequest = new SimpleWSJsonRequest();
					String result =  wsRequest.WSJsonRequestPOST(call_url, json);
					Log.d("KITs calling", "call:"+result);
					try {
						JSONObject json_result = new JSONObject(result);
						return json_result.getString("status");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}
				}
				
				@Override
				protected void onPostExecute(String result) {
					// TODO Auto-generated method stub
					super.onPostExecute(result);
					try{
						if(result.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)){
							toastDialog = new Dialog(context, R.style.Theme_Dialog);
							toastDialog.setContentView(R.layout.toast_message_dialog);
							TextView dialogToast_tv = (TextView)toastDialog.findViewById(R.id.toast__message_tv);
							dialogToast_tv.setText(R.string.call_waiting);
							LinearLayout dialog_ll = (LinearLayout)toastDialog.findViewById(R.id.ll_toast_message);
							dialog_ll.setOnClickListener(new OnClickListener(){

								@Override
								public void onClick(View arg0) {
									// TODO Auto-generated method stub
									if(toastDialog.isShowing())
										toastDialog.dismiss();
								}
								
							});
							toastDialog.setCanceledOnTouchOutside(true);
							toastDialog.setCancelable(true);
							toastDialog.show();		
							new CountDownTimer(6000, 1000){

								@Override
								public void onFinish() {
									// TODO Auto-generated method stub
									Log.i("KITs countdown","finish");
									if (toastDialog.isShowing()) {
										toastDialog.dismiss();
									}
								}

								@Override
								public void onTick(long millisUntilFinished) {
									// TODO Auto-generated method stub
									Log.i("KITs countdown","tick");
								}
								
							}.start();
							return ;
						}else if(errorHandle(result, context)){
							callCountDownTimer.cancel();
							return ;
						}else{
							callCountDownTimer.cancel();
							Toast.makeText(context, context.getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
						}
					}catch(Exception e){
						e.printStackTrace();
						Toast.makeText(context, R.string.network_invalid, Toast.LENGTH_SHORT).show();
					}finally{
						
					}
					
				}

				@Override
				protected void onCancelled() {
					// TODO Auto-generated method stub
					super.onCancelled();
					if (toastDialog.isShowing()) {
						toastDialog.dismiss();
					}
				}			
			}.execute();
			
		}
	}
	public static Boolean errorHandle(String status, Context context){
		if(VolleyWSJsonRequest.errorHandle(status, context)){
			return true;
		}else if(status.equals(CALL_INCORRECT_NUMBER_FORMAT)){
			Toast.makeText(context, context.getString(R.string.response_incorrect_numberformat)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}else if(status.equals(CALL_CALLEE_NOT_MVPN)){
			Toast.makeText(context, context.getString(R.string.response_not_mvpn)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}else if(status.equals(INTERNAL_ERROR)){
			Toast.makeText(context, context.getString(R.string.response_internal_error)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}else if(status.equals(SIP_SERVER_ERROR)){
			Toast.makeText(context, context.getString(R.string.response_sip_error)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}else if(status.equals(CALL_POINT_NOT_ENOUGH)){
			Toast.makeText(context, context.getString(R.string.response_point_not_enough)+" ("+status+") ", Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}
	
	public static void deleteCalling(final String session, final String callee){
		new AsyncTask<Void, Void, String>(){

			@Override
			protected String doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				String url = "https://ts.kits.tw/projectLYS/v0/Call/" + session + "/callRequest" + "?callee=" + callee;		
				SimpleWSJsonRequest wsRequest = new SimpleWSJsonRequest();
				String result =  wsRequest.WSJsonRequestDELETE(url);
				Log.d("KITs cancel calling", "cancel:"+result);
				try {
					JSONObject json_result = new JSONObject(result);
					return json_result.getString("status");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}		
			}
			
		}.execute();
		
	}
	
}
