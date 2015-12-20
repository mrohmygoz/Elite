package com.uninum.elite.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.uninum.elite.adapter.MainDrawerAdapter;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.MessageDBHelper;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.database.MessageDBHelper.GroupMessageEntry;
import com.uninum.elite.object.MainDrawerItem;
import com.uninum.elite.object.SingleMessage;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.MyContentProvider;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.utility.LoginService;
import com.uninum.elite.utility.NetworkUtility;
import com.uninum.elite.webservice.PubnubHelper;
import com.uninum.elite.webservice.VolleyWSJsonRequest;
import com.uninum.elite.webservice.WSAccount;
import com.uninum.elite.webservice.WSAccountAccessToken;
import com.uninum.elite.R;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class Activity_Main extends ActionBarActivity{

	public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String SENDER_ID = "105419119720";
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private LinearLayout mLlvDrawerContent;
	private ListView mLsvDrawerMenu;
	private MainDrawerAdapter drawerAdapter;
	private FragmentManager fragmentManager ;
	public static Handler backFragmentHandler, getPhotoHandler, stopServiceHandler;

	public static final String MAIN_CALLBACK = "MAIN_CALLBACK";
	public static final String CALLBACK_MAIN = "CALLBACK_MAIN";
	public static final String CALLBACK_COMM = "CALLBACK_COMM";
	public static final String MAIN_SELECT = "MAIN_SELECT";
	public static final String MAIN_SELECT_MENU = "MAIN_MENU";
	public static final String MAIN_SELECT_PROFILEPHOTO = "PROFILE_PHOTO";
	public static final String MAIN_SELECT_INFORMATION ="MAIN_INFORMATION";
	public static final String MAIN_SELECT_USERPROFILE = "USER_PROFILE";
	public static final int MAIN_MENU = 0;
	public static final int USER_PROFILE = 1;
	public static final int INFORMATION_MENU = 2;
	public static final int SYSTEM_INFO = 3;
	public static final int LOGOUT_MENU = 3;
	public static final int SEARCH_MENU = 4;
	public static final int PROFILEPHOTO = 5;
	
	private boolean doubleBackToExitPressedOnce = false;
	private int mCurrentMenuItemPosition = -1;
	private ProgressDialog progressDialog;
	private int backPressState; //0 for exit, 1 for main menu, 2 for communication
	private String backToCommSelect;
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d("Jenny", "activity main on stop, device key: " + SystemManager.getUniqueID(this));
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d("Jenny", "activity main on destroy");
		
		if (KitsApplication.getInstance().isMsgActivityStarted()) 
			Activity_Message.timerStop.sendEmptyMessage(0);
		
		String groupUUID;
		Cursor cur = GroupDBHelper.getInstance(this).queryAllGroup();
		if (cur.moveToFirst()) {
			do {
				groupUUID = cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_UUID));
//				MessageDBHelper.getInstance(this).setStatusFailed( groupUUID );
				MessageDBHelper.getInstance(this).deleteAllMessage(groupUUID);
				SystemManager.putPreLong(this, SystemManager.MESSAGE_UPDATE_TIME, 0);
			}while(cur.moveToNext());
		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUpNavigationDrawer();
		setDrawerMenu();
		KitsApplication.getInstance().MainStarted();
		backPressState = 0;
		
		
		Bundle bundle = getIntent().getExtras();		
		if(bundle != null){
			String main_select = bundle.getString(MAIN_SELECT);
			if(main_select.equals(MAIN_SELECT_MENU)){
				transformFragment(MAIN_MENU,null);
			}else if(main_select.equals(MAIN_SELECT_PROFILEPHOTO)){
				transformFragment(PROFILEPHOTO,null);
			}else if(main_select.equals(MAIN_SELECT_INFORMATION)){
				transformFragment(INFORMATION_MENU,bundle);
			}else if(main_select.equals(MAIN_SELECT_USERPROFILE)){
				transformFragment(USER_PROFILE,bundle);
			}else{ 
				transformFragment(MAIN_MENU,null);
			}
		}else{			
			if(!SystemManager.getPreBoolean(this, SystemManager.USER_FIRST_OPEN))
				transformFragment(MAIN_MENU,null);
			
			progressDialog = new ProgressDialog(Activity_Main.this);
			progressDialog.setMessage( getString(R.string.main_login_sync));
			progressDialog.setCancelable(false);
			progressDialog.show();
			
			Intent serviceIntent = new Intent(this,LoginService.class);
			Bundle serviebundle = new Bundle();
			serviebundle.putString(LoginService.CALLBACK, "Activity_Main");
			serviceIntent.putExtras(serviebundle);
			startService(serviceIntent);
		}
		
		getPhotoHandler = new Handler(){
			
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Intent serviceIntent = new Intent(Activity_Main.this,LoginService.class);
				stopService(serviceIntent);
				if (progressDialog!=null && progressDialog.isShowing()) {					
					progressDialog.dismiss();
				}
				getPhotoId();
			}
			
		};
		
		stopServiceHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Intent serviceIntent = new Intent(Activity_Main.this,LoginService.class);
				stopService(serviceIntent);
				if (progressDialog!=null && progressDialog.isShowing()) {					
					progressDialog.dismiss();
				}
			}
			
		};
		
		backFragmentHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				getSupportFragmentManager().popBackStackImmediate();
			}
			
		};

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
	    //Enable Up Button 
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

	}
	
	private void getPhotoId(){
		WSAccount.WSGetPhotoID wsPhotoID = new WSAccount.WSGetPhotoID(SystemManager.getToken(this), new Response.Listener<JSONObject>() {
			
			@Override
			public void onResponse(JSONObject response) {
				// TODO Auto-generated method stub
			try{	
					String status = response.getString("status");									
				if(status != null && status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)) {
					SystemManager.putPreString(Activity_Main.this, SystemManager.USER_BIGHEAD_ID, response.getString(WSAccount.JSON_IMAGEID));
					transformFragment(PROFILEPHOTO,null);
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
				if(errorResponse != null && errorResponse.data != null){
					Log.d("ELITE","Get User PhotoID Error:"+ new String(errorResponse.data) );	
				}
			}
		} );
		KitsApplication.getInstance().addToRequestQueue(wsPhotoID, WSAccount.GET_USER_PHOTOID);	
	}

	private void setUpNavigationDrawer(){
		
		mDrawerLayout = (DrawerLayout)findViewById(R.id.drw_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		mDrawerToggle = new ActionBarDrawerToggle(
	            this, 
	            mDrawerLayout,    // 讓 Drawer Toggle 知道母體介面是誰
	            R.drawable.btn_more, // Drawer 的 Icon
	            R.string.main_open_left_drawer, // Drawer 被打開時的描述
	            R.string.main_close_left_drawer // Drawer 被關閉時的描述
	            ) {
	                //被打開後要做的事情
	                @Override
	                public void onDrawerOpened(View drawerView) {
	                    // 將 Title 設定為自定義的文字
	                    getSupportActionBar().setTitle(R.string.main_open_left_drawer);
	                }
	
	                //被關上後要做的事情
	                @Override
	                public void onDrawerClosed(View drawerView) {
	                	String[] title_array = getResources().getStringArray(R.array.menu_items);
	                	if(mCurrentMenuItemPosition > -1){
	                		getSupportActionBar().setTitle(title_array[mCurrentMenuItemPosition]);
	                	}else {
	                		// 將 Title 設定回 APP 的名稱
	                        getSupportActionBar().setTitle(R.string.app_name);
	                	}
	                }
	    };
	    mDrawerLayout.setDrawerListener(mDrawerToggle);
	    //display Up Button ( on left of LOGO)
	   
	
	}
	
	@Override
	public void onBackPressed() {
	    if (backPressState == 0) {
			if (doubleBackToExitPressedOnce) {
				super.onBackPressed();
				KitsApplication.imageRequestRecord.clear();
				return;
			}
			this.doubleBackToExitPressedOnce = true;
			Toast.makeText(this, R.string.main_backpress, Toast.LENGTH_SHORT).show();
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					doubleBackToExitPressedOnce = false;
				}
			}, 2000);
		} else if (backPressState==1) {
			backPressState = 0;
			transformFragment(MAIN_MENU,null);
		} else {
			backPressState = 0;
			Bundle bundle = new Bundle();
			bundle.putString(Activity_Communication.TAB_SELECT, backToCommSelect);
			Intent intent = new Intent();
			intent.putExtras(bundle);
			intent.setClass(Activity_Main.this, Activity_Communication.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}
	
	private void setDrawerMenu(){
		mLsvDrawerMenu = (ListView)findViewById(R.id.lsv_drawer_menu);
		mLlvDrawerContent = (LinearLayout) findViewById(R.id.llv_left_drawer);
		mLsvDrawerMenu.setOnItemClickListener(new OnItemClickListener(){
	
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				selectMenuItem(position);
			}
			
		});
		String[] menu_items = getResources().getStringArray(R.array.menu_items);
		List<MainDrawerItem> itemList;
		itemList = new ArrayList<MainDrawerItem>();
		itemList.add(new MainDrawerItem(menu_items[0], R.drawable.ic_main_home));
		itemList.add(new MainDrawerItem(menu_items[1], R.drawable.ic_main_edit));
		itemList.add(new MainDrawerItem(menu_items[2], R.drawable.ic_main_info));
		itemList.add(new MainDrawerItem(menu_items[3], R.drawable.ic_main_info));
	
		drawerAdapter = new MainDrawerAdapter(this, R.layout.drawer_menu_item, itemList);
		mLsvDrawerMenu.setAdapter(drawerAdapter);
	
	}
	
	public void selectMenuItem(int position){
		mCurrentMenuItemPosition = position;
		mLsvDrawerMenu.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mLlvDrawerContent);
		Bundle bundle = new Bundle();
		switch (position){
		case INFORMATION_MENU:
			bundle.putString(MAIN_CALLBACK, CALLBACK_MAIN);
			transformFragment(position,bundle);
			break;
		case USER_PROFILE:
			bundle.putString(MAIN_CALLBACK, CALLBACK_MAIN);
			transformFragment(position,bundle);
			break;
		default:
			transformFragment(position,null);
			break;
		}
	}
	
	private void transformFragment(int select, Bundle bundle){
		fragmentManager = getSupportFragmentManager();
		switch (select){
		case MAIN_MENU:
			Fragment mainpageFragment = new MainPage();
			backPressState = 0;
			fragmentManager.beginTransaction()
							.replace(R.id.content_frame, mainpageFragment)
							.commit();
			break;
		case PROFILEPHOTO:
			Fragment profilePhotoFragment = new ProfilePhoto();
			backPressState = 0;
			fragmentManager.beginTransaction()
							.replace(R.id.content_frame, profilePhotoFragment)
							.commit();
			break;
		case INFORMATION_MENU:
			Fragment SystemSetting = new SystemSetting();
			if(bundle.getString(MAIN_CALLBACK).equals(CALLBACK_MAIN))
				backPressState = 1;
			else if (bundle.getString(MAIN_CALLBACK).equals(CALLBACK_COMM)){
				backPressState = 2;
				backToCommSelect = bundle.getString(Activity_Communication.TAB_SELECT);
			}
			fragmentManager.beginTransaction()
							.replace(R.id.content_frame, SystemSetting)
							.commit();
			break;
		case USER_PROFILE:
			Fragment userProfile = new MyProfile();
			if(bundle.getString(MAIN_CALLBACK).equals(CALLBACK_MAIN))
				backPressState = 1;
			else if (bundle.getString(MAIN_CALLBACK).equals(CALLBACK_COMM)){
				backPressState = 2;
				backToCommSelect = bundle.getString(Activity_Communication.TAB_SELECT);
			}
			fragmentManager.beginTransaction()
							.replace(R.id.content_frame, userProfile)
							.commit();
			break;
		case SYSTEM_INFO:
			Intent systemInfoIntent = new Intent();
			systemInfoIntent.setClass(Activity_Main.this, SystemInfo.class);
			systemInfoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(systemInfoIntent);
			break;
		default:
			
		}
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if(mDrawerToggle.onOptionsItemSelected(item)){
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
