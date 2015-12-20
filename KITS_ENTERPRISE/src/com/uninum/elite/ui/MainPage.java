package com.uninum.elite.ui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.uninum.elite.data.UpdateContacts;
import com.uninum.elite.data.UpdateMessage;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.database.MessageDBHelper;
import com.uninum.elite.image.DrawCircularImage;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.Logout;
import com.uninum.elite.system.MyContentProvider;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.utility.NetworkUtility;
import com.uninum.elite.webservice.PubnubHelper;
import com.uninum.elite.webservice.WSContact;
import com.uninum.elite.webservice.WSHistory;
import com.uninum.elite.webservice.WSMessage;
import com.uninum.elite.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainPage extends Fragment implements OnClickListener{


	private ImageView iv_mainpage_bighead;
	private TextView mainpageName, mainpagePhone;
	private Button btn_mainpage_favorite, btn_mainpage_history, btn_mainpage_contact, btn_mainpage_textmessage;
	private Dialog waitDialog;
	private PubnubHelper pubnubHelper;
	private int groupNum;
	
	public static String callbackMainPage = "MAINPAGE_UPDATE";
	public static Handler updateUserIconHandler;
	public static Handler updateContactFinishHandler, updateMsgFinishHandler, startUpdateContact;
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	
		setHasOptionsMenu(true);
		pubnubHelper = new PubnubHelper(getActivity());
		groupNum = 0;
		
		updateUserIconHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				downloadUserIcon();
			}
			
		};	
		
		updateContactFinishHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				String groupUUID;
				UpdateMessage updateMsg = new UpdateMessage(getActivity());
				MessageDBHelper msgDB = MessageDBHelper.getInstance(getActivity());
				
				Cursor cur = GroupDBHelper.getInstance(getActivity()).queryAllGroup();
				groupNum = cur.getCount();
				if (cur.moveToFirst()){
					do{
						groupUUID = cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_UUID));
						msgDB.deleteAllMessage(groupUUID);
						updateMsg.updateMsgRequest("MainPage",groupUUID,System.currentTimeMillis(),"old");	//update msg
						pubnubHelper.Subscribe(groupUUID);									//subscribe
						getActivity().getContentResolver().call(							//add uri
								MyContentProvider.CONTENT_URI, 
								"addURI", 
								groupUUID, 
								null);
					}while(cur.moveToNext());
				}else{
					if (waitDialog!=null && waitDialog.isShowing())
						waitDialog.dismiss();
				}
				SystemManager.putPreLong(getActivity(), SystemManager.MESSAGE_UPDATE_TIME, System.currentTimeMillis());
			}
			
		};
		
		updateMsgFinishHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (msg.obj == null) {
					if( (--groupNum)<=0 && waitDialog !=null && waitDialog.isShowing())
						waitDialog.dismiss();
				} else if ( (boolean) msg.obj == false) {
					KitsApplication.getInstance().cancelPendingRequests(WSMessage.GET_MESSAGE_TAG);
					if(waitDialog !=null && waitDialog.isShowing())
						waitDialog.dismiss();
					groupNum = 0;
					Toast.makeText(getActivity(), R.string.message_update_failed, Toast.LENGTH_SHORT).show();
				}
					
			}
			
		};
		
		startUpdateContact = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				SystemManager.putPreBoolean(getActivity(), SystemManager.USER_FIRST_OPEN, false);
				
				try{
					createDialog();
					UpdateContacts updateContacts = new UpdateContacts(getActivity(), callbackMainPage);
					updateContacts.getGroupsUUID();
				}catch(Exception e){
					if(waitDialog!=null && waitDialog.isShowing()){
						waitDialog.dismiss();
					}
				}
				
			}
			
		};
		
		if (SystemManager.getPreBoolean(getActivity(), SystemManager.USER_FIRST_OPEN)) // if from profile photo
			startUpdateContact.sendEmptyMessage(0);
	}
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootview = inflater.inflate(R.layout.fragment_mainpage, container, false);
		iv_mainpage_bighead = (ImageView)rootview.findViewById(R.id.iv_mainpage_bighead);		
		mainpageName = (TextView)rootview.findViewById(R.id.tv_mainpage_name);
		mainpageName.setText(SystemManager.getPreString(getActivity(), SystemManager.USER_NAME));
		mainpagePhone = (TextView)rootview.findViewById(R.id.tv_mainpage_phone);
		mainpagePhone.setText(SystemManager.getPreString(getActivity(), SystemManager.USER_ACCOUNT));
		btn_mainpage_favorite = (Button)rootview.findViewById(R.id.btn_mainpage_favorite);
		btn_mainpage_favorite.setOnClickListener(this);
		btn_mainpage_history = (Button)rootview.findViewById(R.id.btn_mainpage_history);
		btn_mainpage_history.setOnClickListener(this);
		btn_mainpage_contact = (Button)rootview.findViewById(R.id.btn_mainpage_contact);
		btn_mainpage_contact.setOnClickListener(this);
		btn_mainpage_textmessage = (Button)rootview.findViewById(R.id.btn_mainpage_textmessage);
		btn_mainpage_textmessage.setOnClickListener(this);
		return rootview;

	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		downloadUserIcon();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_main, menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
//		case R.id.menu_main_logout:
//			Logout logout = new Logout(getActivity());
//			logout.LogoutApp();
//			getActivity().finish();
//			break;
		case R.id.menu_main_search:
			Intent intent = new Intent();
			intent.setClass(getActivity(), SearchContact.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	private void downloadUserIcon(){
		final String timeStamp = String.valueOf(SystemManager.getPreLong(getActivity(), SystemManager.USER_BIGHEAD_TIME));
		final String token = SystemManager.getToken(getActivity());
		if(SystemManager.getUserBigHead(getActivity()) != null){
			Bitmap bitmap = DrawCircularImage.DrawCircular(SystemManager.getUserBigHead(getActivity()));
			iv_mainpage_bighead.setImageBitmap(bitmap);
		}			
		Log.d("ELITE","FileId:"+SystemManager.getPreString(getActivity(), SystemManager.USER_BIGHEAD_ID));
		if(!token.equals("")&& !SystemManager.getPreString(getActivity(), SystemManager.USER_BIGHEAD_ID).equals("")){
			final String img_url = "https://ts.kits.tw/projectLYS/v2/File/"+token+"/image/"+SystemManager.getPreString(getActivity(), SystemManager.USER_BIGHEAD_ID);
			ImageRequest request = new ImageRequest(img_url,
				    new Response.Listener<Bitmap>() {
				        @Override
				        public void onResponse(Bitmap bitmap) {
				        	if(bitmap!=null)
				        		iv_mainpage_bighead.setImageBitmap(DrawCircularImage.DrawCircular(bitmap));
				        }
				    }, 0, 0, null,
				    new Response.ErrorListener() {
				        public void onErrorResponse(VolleyError error) {
				        	if(SystemManager.getUserBigHead(getActivity())!=null){
				        		Bitmap bitmap = DrawCircularImage.DrawCircular(SystemManager.getUserBigHead(getActivity()));
								iv_mainpage_bighead.setImageBitmap(bitmap);
				        	}else{
				        		iv_mainpage_bighead.setImageResource(R.drawable.contact_head_big);
				        	}				        		        	
				        }
				    });
			KitsApplication.getInstance().addToRequestQueue(request, "MAINPAGE_DOWNLOADICON");	
		}
	}
	
	private void createDialog(){
		 waitDialog = new Dialog(getActivity(), R.style.Theme_Dialog);
		 waitDialog.setContentView(R.layout.upload_loading_dialog);
		 TextView dialogTitle_tv = (TextView)waitDialog.findViewById(R.id.tv_loading_title);
		 dialogTitle_tv.setText(R.string.mainpage_loading_title);
		 waitDialog.show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setClass(getActivity(), Activity_Communication.class);
		Bundle bundle = new Bundle();
		switch(v.getId()){
		case R.id.btn_mainpage_favorite:
			bundle.putString(Activity_Communication.TAB_SELECT, Activity_Communication.TAB_FAVORITE);
			break;
		case R.id.btn_mainpage_history:
			bundle.putString(Activity_Communication.TAB_SELECT, Activity_Communication.TAB_HISTORY);
			break;
		case R.id.btn_mainpage_contact:
			bundle.putString(Activity_Communication.TAB_SELECT, Activity_Communication.TAB_CONTACT);
			break;
		case R.id.btn_mainpage_textmessage:
			bundle.putString(Activity_Communication.TAB_SELECT, Activity_Communication.TAB_MESSAGE);
			break;
		}
		intent.putExtras(bundle);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}
