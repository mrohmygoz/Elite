package com.uninum.elite.ui;

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
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Register extends ActionBarActivity{
	private Button btnSubmit, btnTransfer;
	private ImageButton ibtnDelete, ibtnNameDelete;
	private EditText edtvNum, edtvName ;
	private TextView title, tvMcc;
	public static String NUMBER = "NUMBER";
	public static String NAME = "NAME";
	private Spinner countrySpinner;
	private ArrayAdapter<String> spinnerAdapter;
	private String mcc="";
	private boolean transferMode ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		getSupportActionBar().hide();
		edtvNum = (EditText)findViewById(R.id.edtv_reg_num);
		edtvName = (EditText)findViewById(R.id.edtv_reg_name);
		countrySpinner = (Spinner)findViewById(R.id.spin_reg_mcc);
		btnTransfer = (Button) findViewById(R.id.btn_reg_alreadyReg);
		title = (TextView)findViewById(R.id.tv_reg_num_title);
		tvMcc = (TextView)findViewById(R.id.edtv_reg_mcc);
		btnSubmit = (Button)findViewById(R.id.btn_reg_submit);
		btnSubmit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub	
				String number =  edtvNum.getText().toString().trim();
				if(number.startsWith("0"))
					number = number.substring(1,number.length());
				if(edtvName.getText().toString().trim().equals("")){
					Toast.makeText(Register.this, R.string.register_submit_no_name, Toast.LENGTH_SHORT).show();
				}else if(edtvNum.getText().toString().trim().equals("")){
					Toast.makeText(Register.this, R.string.register_submit_no_phone, Toast.LENGTH_SHORT).show();
				}else if(mcc.equals("")){
					Toast.makeText(Register.this, R.string.register_submit_no_mcc, Toast.LENGTH_SHORT).show();
				}else if(!NetworkUtility.isConnected(Register.this)){
						Toast.makeText(Register.this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
				}else if( !number.equals("")){
						if(transferMode == false){
							register(mcc + number, edtvName.getText().toString());		
						}else{
							transfer(mcc + number, edtvName.getText().toString());
						}				
				}else{
					Toast.makeText(Register.this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
				}					
			}			
		});
		
		ibtnDelete = (ImageButton)findViewById(R.id.ibtn_reg_delete);
		ibtnDelete.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				edtvNum.setText("");
			}
			
		});
		ibtnNameDelete = (ImageButton)findViewById(R.id.ibtn_reg_name_delete);
		ibtnNameDelete.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				edtvName.setText("");
			}
			
		});
		
		btnTransfer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				transferMode = true;
				title.setText(getString(R.string.register_transfer_title));
				btnTransfer.setVisibility(Button.INVISIBLE);
			}
			
		});
		spinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.mobile_country_code));
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner.setAdapter(spinnerAdapter);
		countrySpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(position!=0){
					try{
						String spinnerSelected = parent.getSelectedItem().toString();				
						String[] temp = spinnerSelected.split("[(]");
						String[] temp2 = temp[1].split("[)]");
						mcc = temp2[0];
						tvMcc.setText(mcc);
						Log.d("kits spinner", "position:"+position+";"+mcc);				
						}catch(Exception e){
							e.printStackTrace();
						}
				}else{
					mcc="";
					tvMcc.setText(mcc);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});
		transferMode = false;
	}

	private void register(final String number, final String name){
		final ProgressDialog progressDialog = new ProgressDialog(Register.this);
		JSONObject json = new JSONObject();
		try {
			json.put("account", number+"@pack");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WSAccount.Register registerWS =  new WSAccount.Register(json, new Response.Listener<JSONObject>() {
			
			@Override
			public void onResponse(JSONObject response) {
				// TODO Auto-generated method stub
				try {
					if (progressDialog.isShowing()) {					
							progressDialog.dismiss();					
					}
						String status ="";
						status = response.getString("status");
						Log.d("KITs","success"+response.toString());
						if(status.equals(WSAccount.SUCCESS)){
							Intent intent = new Intent();
							Bundle bundle = new Bundle();
							bundle.putString(NUMBER, number);
							bundle.putString(NAME, name);
							intent.putExtras(bundle);
							intent.setClass(Register.this, Validate.class);
							startActivity(intent);
							Toast.makeText(Register.this, getString(R.string.validate_resend_message1)+ number + 
									getString(R.string.validate_resend_message2), Toast.LENGTH_LONG).show();
						}
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
						Log.d("KITs","error:"+new String(response.data));
//						Toast.makeText(activity, new String(response.data), Toast.LENGTH_SHORT).show();
						JSONObject result = new JSONObject(new String(response.data));
						String status_result = result.getString(VolleyWSJsonRequest.JSON_STATUS);
						if(status_result.equals(WSAccount.REGISTER_DUPLICATE)){
							transfer(number, name);							
						}else{
							Toast.makeText(Register.this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
						}
					}
				}catch(Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(Register.this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
				}		
			}
		} );
		progressDialog.setMessage(getString(R.string.register_sending_info));
		progressDialog.setCancelable(false);
		progressDialog.show();
		KitsApplication.getInstance().addToRequestQueue(registerWS, "Register");	
	}
	
	public void transfer(final String number, final String name){
	JSONObject json = new JSONObject();
	try {
		json.put("account",number+"@pack");
		json.put("mediaType", "SMS");
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	final ProgressDialog progressDialog = new ProgressDialog(Register.this);
	WSAccountAuthentication.Transfer transferRequest = new WSAccountAuthentication.Transfer(json, new Response.Listener<JSONObject>() {
		
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
						
							Intent intent = new Intent();
							Bundle bundle = new Bundle();
							bundle.putString(NUMBER, number);
							bundle.putString(NAME, name);
							intent.putExtras(bundle);
							intent.setClass(Register.this, Validate.class);
							startActivity(intent);
							Toast.makeText( Register.this , getString(R.string.validate_resend_message1)+ number + 
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
					if( WSAccount.Register.errorHandle(status_result, Register.this) ){
						return ;
					}else{
						Toast.makeText(Register.this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
					}
				}
			}catch(Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(Register.this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
			}		
		}
	} );
	progressDialog.setMessage(getString(R.string.register_sending_info));
	progressDialog.setCancelable(false);
	progressDialog.show();
	KitsApplication.getInstance().addToRequestQueue(transferRequest, "Transfer");	
}
}
