package com.uninum.elite.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.uninum.elite.image.DrawCircularImage;
import com.uninum.elite.image.UploadImage;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.utility.NetworkUtility;
import com.uninum.elite.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MyProfile extends Fragment implements OnClickListener, OnFocusChangeListener{
	private TextView uploadBigHead_tv;
	private EditText userName_et, userPhone_et;
	private ImageView imgBigHead;
	private static final int REQUEST_PICK_IMAGE = 100;
	private static final int REQUEST_TAKE_PICTURE = 200;
	private static final int AFTER_CUT_PHOTO = 300;
	private static final int IMAGE_SCALE = 240;
	private Bitmap bitmapTemp ;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.fragment_myprofile, container, false);
		userName_et = (EditText)rootView.findViewById(R.id.myprofile_name_tv);
		userPhone_et = (EditText)rootView.findViewById(R.id.myprofile_phone_tv);
		imgBigHead = (ImageView)rootView.findViewById(R.id.myprofile_bighead_iv);
		uploadBigHead_tv = (TextView)rootView.findViewById(R.id.myprofile_upload_tv);
		if(SystemManager.getUserBigHead(getActivity())!=null){
			 Log.d("ELITE", "Myproflie not null");
			 imgBigHead.setImageBitmap(DrawCircularImage.DrawCircular(SystemManager.getUserBigHead(getActivity())));
		}
		uploadBigHead_tv.setOnClickListener(this);
		userName_et.setOnFocusChangeListener(this);
		userName_et.setText(SystemManager.getPreString(getActivity(), SystemManager.USER_NAME));
		userPhone_et.setText(SystemManager.getPreString(getActivity(), SystemManager.USER_ACCOUNT));
		imgBigHead.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_myprofile, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()){
		case R.id.menu_myprofile_complete:
			SystemManager.putPreString(getActivity(), SystemManager.USER_NAME, userName_et.getText().toString());
			if(NetworkUtility.isConnected(getActivity())){
				if(bitmapTemp!=null){
					 SystemManager.putUserBigHead(getActivity(), bitmapTemp);  				 
					 String uploadTime = String.valueOf(System.currentTimeMillis());
					 UploadImage uploadImage = new UploadImage(getActivity());
					 uploadImage.uploadImage(bitmapTemp, uploadTime);
					 Log.d("kits new", "time:"+uploadTime);
					 SystemManager.putPreLong(getActivity(), SystemManager.USER_BIGHEAD_TIME, Long.valueOf(uploadTime));
				}
				Toast.makeText(getActivity(), R.string.myprofile_save, Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getActivity(), R.string.network_invalid, Toast.LENGTH_SHORT).show();
			}
			
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		FragmentManager fragmentManager = getFragmentManager();
		switch (view.getId()){
		case R.id.myprofile_upload_tv:
			showDialog();
			break;
		case R.id.myprofile_bighead_iv:
			showDialog();
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		Uri imageUri=null;
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
					bitmapTemp = intent.getParcelableExtra("data");
					imgBigHead.setImageBitmap(DrawCircularImage.DrawCircular(bitmapTemp));
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
	
	private void showDialog(){
		
		new AlertDialog.Builder(getActivity())
		.setTitle(R.string.myprofile_title)
		.setPositiveButton(R.string.myprofile_takepicture, new  DialogInterface.OnClickListener ()  {
	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);			
				i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File("/sdcard/kits")));						
				startActivityForResult(i, REQUEST_TAKE_PICTURE);
			}
			
		})
		.setNegativeButton(R.string.myprofile_gallery, new  DialogInterface.OnClickListener ()  {
	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent pickImageIntent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(pickImageIntent, REQUEST_PICK_IMAGE);
			}
			
		}).show();

	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		//auto cancel keyboard 
		if(v.getId() == R.id.myprofile_name_tv && !hasFocus) {
            InputMethodManager imm =  (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        }
	}
}