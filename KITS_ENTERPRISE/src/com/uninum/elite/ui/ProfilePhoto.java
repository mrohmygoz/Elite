package com.uninum.elite.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.uninum.elite.image.BitmapProcess;
import com.uninum.elite.image.DrawCircularImage;
import com.uninum.elite.image.UploadImage;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.webservice.VolleyWSJsonRequest;
import com.uninum.elite.webservice.WSAccount;
import com.uninum.elite.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ProfilePhoto extends Fragment implements OnClickListener{
	private Button btnGallery, btnCamera, btnIgnore;
	private ImageView imgBigHead;
	private static final int REQUEST_PICK_IMAGE = 100;
	private static final int REQUEST_TAKE_PICTURE = 200;
	private static final int AFTER_CUT_PHOTO = 300;
	private static final int IMAGE_SCALE = 240;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.fragment_profilephoto, container, false);
		btnGallery = (Button)rootView.findViewById(R.id.btn_profilephoto_gallery);
		btnCamera = (Button)rootView.findViewById(R.id.btn_profilephoto_camera);
		btnIgnore = (Button)rootView.findViewById(R.id.btn_profilephoto_ignore);
		btnGallery.setOnClickListener(this);
		btnCamera.setOnClickListener(this);
		btnIgnore.setOnClickListener(this);
		imgBigHead = (ImageView)rootView.findViewById(R.id.profilephoto_iv_bighead);
		if(SystemManager.getUserBigHead(getActivity())!=null){
			 Log.d("ELITE", "Myproflie not null");
			 btnIgnore.setBackgroundResource(R.drawable.button_rectangle_shape_orange);
			 btnIgnore.setText(getActivity().getString(R.string.myprofile_finish));
			 imgBigHead.setImageBitmap(SystemManager.getUserBigHead(getActivity()));
		}
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getPhotoId();

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		FragmentManager fragmentManager = getFragmentManager();
		switch (view.getId()){
		case R.id.btn_profilephoto_gallery:			
			Intent pickImageIntent = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(pickImageIntent, REQUEST_PICK_IMAGE);
			break;
		case R.id.btn_profilephoto_camera:
			Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);			
			i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File("/sdcard/kits")));						
			startActivityForResult(i, REQUEST_TAKE_PICTURE);
			break;
		case R.id.btn_profilephoto_ignore:
//		        Log.i("MainActivity", "nothing on backstack, calling super");
//				Intent intent = new Intent();
//				intent.setClass(getActivity(), Activity_Main.class);
//				Bundle bundle = new Bundle();
//				bundle.putString(Activity_Main.MAIN_SELECT, Activity_Main.MAIN_SELECT_MENU);
//				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				intent.putExtras(bundle);
//				startActivity(intent);
//				getActivity().finish();
			
				FragmentManager manager = getActivity().getSupportFragmentManager();
				Fragment mainpageFragment = new MainPage();
				manager.beginTransaction()
						.replace(R.id.content_frame, mainpageFragment)
						.commit();
			
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		Uri imageUri=null;
		Bitmap bitmap=null;
		try {
		switch(requestCode){
		case REQUEST_TAKE_PICTURE:
			if (getActivity().RESULT_OK == resultCode) {
	                File fi = new File("/sdcard/kits");
	                    imageUri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), fi.getAbsolutePath(), null, null));
	                    if (!fi.delete()) {
	                        Log.i("logMarker", "Failed to delete " + fi);
	                    }
	                cutImage(imageUri, IMAGE_SCALE);    
			}
			break;
		case REQUEST_PICK_IMAGE:
			if (getActivity().RESULT_OK == resultCode) {
					cutImage(intent.getData (), IMAGE_SCALE);        
			}
			break;
		case AFTER_CUT_PHOTO:
			if(getActivity().RESULT_OK == resultCode)
    		{  
    			Bitmap bitmapCut = intent.getParcelableExtra("data");
    			if(bitmapCut!=null){
    				 SystemManager.putUserBigHead(getActivity(), bitmapCut);  				 
    				 imgBigHead.setImageBitmap(DrawCircularImage.DrawCircular(bitmapCut));
    				 String uploadTime = String.valueOf(System.currentTimeMillis());
    				 UploadImage uploadImage = new UploadImage(getActivity());
    				 uploadImage.uploadImage(bitmapCut, uploadTime);
    				 btnIgnore.setBackgroundResource(R.drawable.button_rectangle_shape_orange);
    				 btnIgnore.setText(getActivity().getString(R.string.myprofile_finish));
    				 Log.d("kits new", "time:"+uploadTime);
    				 SystemManager.putPreLong(getActivity(), SystemManager.USER_BIGHEAD_TIME, Long.valueOf(uploadTime));
    			}
    			break;
    		}
			break;
			}
		  } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             Toast.makeText(getActivity() , "選取圖片異常", Toast.LENGTH_SHORT);
	      } catch (IOException e) {
	             // TODO Auto-generated catch block
	             e.printStackTrace();
	             Toast.makeText(getActivity() , "選取圖片異常", Toast.LENGTH_SHORT);
	      } catch (Exception e){
	      	e.printStackTrace();
	          Toast.makeText(getActivity() , "選取圖片異常", Toast.LENGTH_SHORT);
	      }
	}


	private  void  cutImage ( Uri  uri ,  int  size )  { 
        Intent  intent  =  new  Intent ( "com.android.camera.action.CROP" ); 
        intent.setDataAndType (uri, "image/*"); 
        //
        intent.putExtra ("crop", "true");
        // aspectX
        intent.putExtra ( "aspectX", 1); 
        intent.putExtra ( "aspectY", 1);
        // outputX
        intent.putExtra ("outputX", size); 
        intent.putExtra ("outputY", size); 
        intent.putExtra ("return-data", true);
        startActivityForResult (intent, AFTER_CUT_PHOTO); 
 
    }
	
	private void getPhotoId(){
		WSAccount.WSGetPhotoID wsPhotoID = new WSAccount.WSGetPhotoID(SystemManager.getToken(getActivity()), new Response.Listener<JSONObject>() {
			
			@Override
			public void onResponse(JSONObject response) {
				// TODO Auto-generated method stub
			try{	
					String status = response.getString("status");									
				if(status != null && status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)) {
					SystemManager.putPreString(getActivity(), SystemManager.USER_BIGHEAD_ID, response.getString(WSAccount.JSON_IMAGEID));
					downloadUserIcon();
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
					Log.d("ELITE","Get User PhotoID Error:"+ new String(errorResponse.data) );	
				}
			}
		} );
		KitsApplication.getInstance().addToRequestQueue(wsPhotoID, WSAccount.GET_USER_PHOTOID);	
	}
	
	private void downloadUserIcon(){
		final String timeStamp = String.valueOf(SystemManager.getPreLong(getActivity(), SystemManager.USER_BIGHEAD_TIME));
		final String token = SystemManager.getToken(getActivity());
		
		if(SystemManager.getUserBigHead(getActivity()) != null){
			Bitmap bitmap = DrawCircularImage.DrawCircular(SystemManager.getUserBigHead(getActivity()));
			imgBigHead.setImageBitmap(bitmap);
		}				
		Log.d("ELITE","FileId:"+SystemManager.getPreString(getActivity(), SystemManager.USER_BIGHEAD_ID));
		if(!token.equals("")&& !SystemManager.getPreString(getActivity(), SystemManager.USER_BIGHEAD_ID).equals("")){
			final String img_url = "https://ts.kits.tw/projectLYS/v2/File/"+token+"/image/"+SystemManager.getPreString(getActivity(), SystemManager.USER_BIGHEAD_ID);
			ImageRequest request = new ImageRequest(img_url,
				    new Response.Listener<Bitmap>() {
				        @Override
				        public void onResponse(Bitmap bitmap) {
				        	if(bitmap!=null)
				        		SystemManager.putUserBigHead(getActivity(), bitmap);
				        		imgBigHead.setImageBitmap(DrawCircularImage.DrawCircular(bitmap));
				        }
				    }, 0, 0, null,
				    new Response.ErrorListener() {
				        public void onErrorResponse(VolleyError error) {
				        	if(SystemManager.getUserBigHead(getActivity())!=null){
				        		Bitmap bitmap = DrawCircularImage.DrawCircular(SystemManager.getUserBigHead(getActivity()));
				        		imgBigHead.setImageBitmap(bitmap);
				        	}else{
				        		imgBigHead.setImageResource(R.drawable.contact_head_big);
				        	}				        		        	
				        }
				    });
			KitsApplication.getInstance().addToRequestQueue(request, "MAINPAGE_DOWNLOADICON");	
		}
	}
}

