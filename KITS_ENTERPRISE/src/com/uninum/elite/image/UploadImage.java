package com.uninum.elite.image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uninum.elite.R;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.ui.ProfilePhoto;
import com.uninum.elite.webservice.VolleyWSJsonRequest;

public class UploadImage {
	private static final int AFTER_CUT_PHOTO = 300;
	private DefaultHttpClient mHttpClient;
	private Dialog waitDialog;
	private Handler errorDialogHandler;
	private static String JSON_FILE_ID = "fileId";
	private Context context;
	public UploadImage(final Context context){
		this.context = context;
		errorDialogHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				final Dialog errorDialog = new Dialog(context, R.style.Theme_Dialog);
	   		 	errorDialog.setContentView(R.layout.upload_loading_dialog);
	   		 	TextView dialogTitle_tv = (TextView)errorDialog.findViewById(R.id.tv_loading_title);
	   		 	dialogTitle_tv.setText(R.string.myprofile_error);
	   		 	ProgressBar bar = (ProgressBar)errorDialog.findViewById(R.id.progressBar1);
	   		 	bar.setVisibility(ProgressBar.INVISIBLE);
	   		 	ImageButton dismiss_ibtn = (ImageButton)errorDialog.findViewById(R.id.ibtn_dismiss);
	   		 	dismiss_ibtn.setVisibility(ImageButton.VISIBLE);
	   		 	dismiss_ibtn.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						errorDialog.dismiss();
					}	   		 		
	   		 	});
	   		 	errorDialog.show();
	   		 	
			}
			
		};
	}
	private void createDialog(){
		 waitDialog = new Dialog(context, R.style.Theme_Dialog);
		 waitDialog.setContentView(R.layout.upload_loading_dialog);
		 TextView dialogTitle_tv = (TextView)waitDialog.findViewById(R.id.tv_loading_title);
		 dialogTitle_tv.setText(R.string.upload_waiting_title);
		 waitDialog.show();
	}
	public void uploadUserPhoto( File image, String timeStamp) {

	    try {
	    	String url = "https://ts.kits.tw/projectLYS/v1/File/"+SystemManager.getToken(context)+"/image"+"?timeStamp="+timeStamp;
	        HttpPost httppost = new HttpPost(url);
	        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
	        multipartEntity.addPart("file", new FileBody(image));
	        httppost.setEntity(multipartEntity);
	        httppost.setHeader("apiKey", "e5141ddacca87c02f80f120ea21f2fd6e2e7912e71b215d926f185380c5ccb96");
	        mHttpClient.execute(httppost, new PhotoUploadResponseHandler());
	    } catch (Exception e) {
	        Log.e(ProfilePhoto.class.getName(), e.getLocalizedMessage(), e);
	    }
	}

	private class PhotoUploadResponseHandler implements ResponseHandler<Object> {

	    @Override
	    public Object handleResponse(HttpResponse response)
	            throws ClientProtocolException, IOException {

	        HttpEntity r_entity = response.getEntity();
	        String responseString = EntityUtils.toString(r_entity);
	        Log.d("UPLOAD", responseString);
	        
	        try {
	        	JSONObject jsonResult = new JSONObject(responseString);
	        	String status = jsonResult.getString("status");
				if(status.equals("200")){
					SystemManager.putPreString(context, SystemManager.USER_BIGHEAD_ID, jsonResult.getString(JSON_FILE_ID));
				}else{
					if( waitDialog.isShowing())
						waitDialog.dismiss();	 
					VolleyWSJsonRequest.errorHandle(status, context);
					errorDialogHandler.sendEmptyMessage(0);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return null;
	    }

	}
	
	public void uploadImage(final Bitmap bitmap, final String timeStamp){
		 new AsyncTask<Void,Void,Void>(){
			 
			 final ProgressDialog progressDialog = new ProgressDialog(context);
			 
			 @Override
			 protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
				createDialog();
			 }
			
			 @Override
			 protected Void doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				final File f = new File(context.getCacheDir(), "elite_01");
				try {					  
    			  f.createNewFile();
    			  ByteArrayOutputStream bos = new ByteArrayOutputStream();
    			  bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
    			  byte[] bitmapdata = bos.toByteArray();
    	
    			  //write the bytes in file
    			  FileOutputStream fos = new FileOutputStream(f);
    			 
					fos.write(bitmapdata);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				HttpParams params = new BasicHttpParams();
	      	    params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
	      	    mHttpClient = new DefaultHttpClient(params);
	      	    uploadUserPhoto(f, timeStamp);
	      	    
	      	    if(waitDialog.isShowing()){
	      	    	waitDialog.dismiss();
	      	    }
	      	    return null;
			}
				  
		 }.execute();
	}
}
