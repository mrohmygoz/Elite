package com.uninum.elite.ui;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.utility.NetworkUtility;
import com.uninum.elite.webservice.VolleyWSJsonRequest;
import com.uninum.elite.webservice.WSAccount;
import com.uninum.elite.webservice.WSAccountAuthentication;
import com.uninum.elite.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Validate extends ActionBarActivity{
	private Button btnSubmit, btnResend;
	private ImageButton ibtnDelete;
	private EditText edtvNum ;
	private TextView tv_number;
	String name;
	String number;
	public static String VALIDATE = "VALIDATE";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_validate);
		getSupportActionBar().hide();
		Bundle bundle = getIntent().getExtras();
		number = bundle.getString(Register.NUMBER);
		name = bundle.getString(Register.NAME);
		Log.d("KITs",number);
		edtvNum = (EditText)findViewById(R.id.edtv_validate);
		btnSubmit = (Button)findViewById(R.id.btn_validate_submit);
		tv_number = (TextView)findViewById(R.id.tv_validate_number);
		btnResend = (Button)findViewById(R.id.btn_validate_resend);
		btnResend.setBackgroundResource(R.drawable.validate_button_resend_disable_shape);
		resendTimer();
		btnResend.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(NetworkUtility.isConnected(Validate.this)){
					resendSMS(number);
					btnResend.setEnabled(false);
					btnResend.setBackgroundResource(R.drawable.validate_button_resend_disable_shape);
					resendTimer();
				}else{
					Toast.makeText(Validate.this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
				}
				
			}
			
		});
		tv_number.setText(number);
		btnSubmit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(NetworkUtility.isConnected(Validate.this)){
				String authCode;
					validateWS(number, edtvNum.getText().toString(), SystemManager.getUniqueID( getApplicationContext()));		
				}else{
					Toast.makeText(Validate.this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		ibtnDelete = (ImageButton)findViewById(R.id.ibtn_validate_delete);
		ibtnDelete.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				edtvNum.setText("");
			}
			
		});

	}
	private void resendTimer(){
		new CountDownTimer(60000,60000){

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				btnResend.setEnabled(true);
				btnResend.setBackgroundResource(R.drawable.img_enter_phone);
			}
			
		}.start();
	}
	private void validateWS(final String number, final String validateCode, final String deviceKey){
		final ProgressDialog progressDialog = new ProgressDialog(Validate.this);
		JSONObject json = new JSONObject();
		try {
			json.put("account",number+"@pack");
			json.put("authCode", validateCode);
			json.put("deviceKey", deviceKey);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WSAccountAuthentication.Validate validateRequest = new WSAccountAuthentication.Validate(json,new Response.Listener<JSONObject>() {
			
			@Override
			public void onResponse(JSONObject response) {
				// TODO Auto-generated method stub
				try {
					if (progressDialog.isShowing()) {					
							progressDialog.dismiss();
					}
					Log.d("KITs Validation",response.toString());
					String status = response.getString("status");			
					if(status != null) {
						SystemManager.putPreString(Validate.this, SystemManager.USER_ACCOUNT, number); 
						SystemManager.putPreString(Validate.this, SystemManager.USER_NAME, name); 
						Intent intent = new Intent();
						intent.setClass(Validate.this, Activity_Main.class);
						startActivity(intent);
						finish();
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				try {
				if (progressDialog.isShowing()) {
					
						progressDialog.dismiss();
					
				}
				NetworkResponse response = error.networkResponse;
				//JSONObject json = new JSONObject();
				String json;
				if(response != null && response.data != null){
					JSONObject result = new JSONObject(new String(response.data));
					String result_status = result.getString(VolleyWSJsonRequest.JSON_STATUS);
					if( WSAccountAuthentication.Validate.errorHandle(result_status, Validate.this)){
						return ;
					}else if (result_status.equals(WSAccountAuthentication.RESPONSE_VALIDATION_INCORRECT)){
						Toast.makeText( Validate.this, getString(R.string.response_validation_incorrect ), Toast.LENGTH_SHORT).show();
						return ;
					}else{
						Toast.makeText( Validate.this, getString(R.string.network_invalid ), Toast.LENGTH_SHORT).show();
						return ;
					}
				}
				} catch (Exception e) {
					// view leak
					e.printStackTrace();
					Toast.makeText( Validate.this, getString(R.string.network_invalid ), Toast.LENGTH_SHORT).show();

				}
			}
		} );
		progressDialog.setMessage(getString(R.string.validate_sending_info));
		progressDialog.setCancelable(false);
		progressDialog.show();
		KitsApplication.getInstance().addToRequestQueue(validateRequest, WSAccountAuthentication.VALIDATE_TAG);		
	}
	
	public void resendSMS(final String number){
	JSONObject json = new JSONObject();
	try {
		json.put("account",number+"@pack");
		json.put("mediaType", "SMS");
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	final ProgressDialog progressDialog = new ProgressDialog(Validate.this);
	WSAccountAuthentication.Transfer resendRequest = new WSAccountAuthentication.Transfer(json, new Response.Listener<JSONObject>() {
		
		@Override
		public void onResponse(JSONObject response) {
			// TODO Auto-generated method stub
			try {
				if (progressDialog.isShowing()) {					
						progressDialog.dismiss();					
				}
					String status ="";
					status = response.getString("status");
					if(status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)){
						Toast.makeText( Validate.this , getString(R.string.validate_resend_message1)+ number + 
								getString(R.string.validate_resend_message2), Toast.LENGTH_LONG).show();
					}
					Log.d("KITs Transfer",status);						
					
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}, new Response.ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			// TODO Auto-generated method stub
			try {
				if (progressDialog.isShowing()) {					
						progressDialog.dismiss();
				}
				NetworkResponse response = error.networkResponse;
				//JSONObject json = new JSONObject();
				String json;
				if(response != null && response.data != null){
//					Toast.makeText(activity, new String(response.data), Toast.LENGTH_SHORT).show();
					JSONObject result = new JSONObject(new String(response.data));; 
					String status_result = result.getString("status");
					if( WSAccount.Register.errorHandle(status_result, Validate.this) ){
						return ;
					}else{
						Toast.makeText(Validate.this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
					}
				}
			}catch(Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText( Validate.this, getString(R.string.network_invalid ), Toast.LENGTH_SHORT).show();
			}		
		}
	} );
	progressDialog.setMessage(getString(R.string.register_sending_info));
	progressDialog.setCancelable(false);
	progressDialog.show();
	KitsApplication.getInstance().addToRequestQueue(resendRequest, WSAccountAuthentication.RESEND_TAG );	
}
}
