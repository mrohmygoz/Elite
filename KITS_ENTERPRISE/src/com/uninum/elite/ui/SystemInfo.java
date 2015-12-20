package com.uninum.elite.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uninum.elite.R;
import com.uninum.elite.database.HistoryDBHelper.HistoryEntry;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.utility.NetworkUtility;
import com.uninum.elite.webservice.VolleyWSJsonRequest;
import com.uninum.elite.webservice.WSAccount;
import com.uninum.elite.webservice.WSSystemMsgService;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class SystemInfo extends ActionBarActivity{

	private ListView infoList;
	private ArrayList<HashMap<String, Object>> infoListItem;
	protected static final int MENU_BUTTON_1 = Menu.FIRST;
	protected static final int MENU_BUTTON_2 = Menu.FIRST + 1;
	private SimpleAdapter simpleAdapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
		setContentView(R.layout.fragment_systeminfo);
		infoList = (ListView) findViewById(R.id.lv_systeminfo);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		registerForContextMenu(findViewById(R.id.lv_systeminfo));
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(NetworkUtility.isConnected(this)){
			loadInfo();
		}else{
			Toast.makeText(this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
		}	
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.menu_systeminfo_delete_all:
			if(NetworkUtility.isConnected(this)){
				new AlertDialog.Builder(this)
				.setTitle(R.string.systeminfo_menu_delete_all)
				.setPositiveButton(R.string.systeminfo_menu_delete_yes, new  DialogInterface.OnClickListener ()  {			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						deleteSystemMsg(true, "all", -1);
					}					
				})
				.setNegativeButton(R.string.systeminfo_menu_delete_no, new  DialogInterface.OnClickListener ()  {
			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						return ;
					}
					
				}).show();				
			}else{
				Toast.makeText(this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
			}
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.menu_systeminfo, menu);
		return true;
	}

	private void updateList(){
		simpleAdapter = new SimpleAdapter(SystemInfo.this , 
				infoListItem, R.layout.systeminfo_item, 
				new String[]{WSSystemMsgService.JSON_MESSAGE, WSSystemMsgService.JSON_TIME}, 
				new int[] {R.id.tv_systeminfo_message, R.id.tv_systeminfo_time});								
		infoList.setAdapter(simpleAdapter);
	}
	
	private void loadInfo(){
		WSSystemMsgService.WSSystemMsgServiceList wsSMS = new WSSystemMsgService.WSSystemMsgServiceList(SystemManager.getToken(this),
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						String status ="";
						setSupportProgressBarIndeterminateVisibility(false);
						try {
							status = response.getString(VolleyWSJsonRequest.JSON_STATUS);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Log.d("KITs","success"+response.toString());
						if(status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)){
							try {
								infoListItem = new ArrayList<HashMap<String, Object>>();
								JSONArray msgArray = new JSONArray();
								msgArray = response.getJSONArray("msgArray");
								if(msgArray!= null){
									for(int i=0; i < msgArray.length(); i++){
										HashMap<String, Object> map = new HashMap<String, Object>();
										String msg = msgArray.getJSONObject(i).getString(WSSystemMsgService.JSON_MESSAGE);
										msg = msg.replaceAll("\\[", " [ ");
										msg = msg.replaceAll("\\]", " ] ");
										map.put(WSSystemMsgService.JSON_MESSAGE, msg);
										map.put(WSSystemMsgService.JSON_UUID, msgArray.getJSONObject(i).getString(WSSystemMsgService.JSON_UUID));
										map.put(WSSystemMsgService.JSON_TYPE, msgArray.getJSONObject(i).getString(WSSystemMsgService.JSON_TYPE));
										//transfer ms to time String 
										String time = msgArray.getJSONObject(i).getString(WSSystemMsgService.JSON_TIME);
										SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
										Date callDate = new Date(Long.valueOf(time));
										map.put(WSSystemMsgService.JSON_TIME, format.format(callDate) );
										infoListItem.add(map);
									}
									Collections.reverse(infoListItem);
									updateList();
								}
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						try{
							NetworkResponse response = error.networkResponse;
							String json;
							setSupportProgressBarIndeterminateVisibility(false);
							if(response != null && response.data != null){
								Log.d("KITs","error:"+new String(response.data));
	//							Toast.makeText(activity, new String(response.data), Toast.LENGTH_SHORT).show();
								JSONObject result = new JSONObject(new String(response.data));
								String status_result = result.getString(VolleyWSJsonRequest.JSON_STATUS);
								if(WSSystemMsgService.WSSystemMsgServiceList.errorHandle(status_result, SystemInfo.this)){
									return ;				
								}else{
									Toast.makeText(SystemInfo.this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
								}
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				});
		KitsApplication.getInstance().addToRequestQueue(wsSMS, "SystemInfo");	
		setSupportProgressBarIndeterminateVisibility(true);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		menu.setHeaderTitle(R.string.systeminfo_context_title);	//設定長按選單的表頭
		menu.add(0, MENU_BUTTON_1, 0, R.string.systeminfo_context_delete);
		super.onCreateContextMenu(menu, v, menuInfo);
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
		case MENU_BUTTON_1:
			if(NetworkUtility.isConnected(this)){
				deleteSystemMsg(false, (String)infoListItem.get(info.position).get(WSSystemMsgService.JSON_UUID), info.position);	
			}else{
				Toast.makeText(this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
			}			
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	private void deleteSystemMsg(final Boolean isAll, String msgId, final int msgIndex){
		
		WSSystemMsgService.WSSystemMsgServiceDelete wsSystemMsgServiceDelete = new WSSystemMsgService.WSSystemMsgServiceDelete(SystemManager.getToken(this), msgId, new Response.Listener<JSONObject>(){

			@Override
			public void onResponse(JSONObject response) {
//				// TODO Auto-generated method stub
//				Toast.makeText(SystemInfo.this, response.toString(), Toast.LENGTH_SHORT).show();
				try {
					if(response.getString(VolleyWSJsonRequest.JSON_STATUS).equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)){
						if(isAll){
							infoListItem.clear();
						}else{
							infoListItem.remove(msgIndex);			
						}									
						runOnUiThread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								simpleAdapter.notifyDataSetChanged();
							}
							
						});
						
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				try{
					NetworkResponse response = error.networkResponse;
					String json;
					setSupportProgressBarIndeterminateVisibility(false);
					if(response != null && response.data != null){
						Log.d("KITs","error:"+new String(response.data));
	//							Toast.makeText(activity, new String(response.data), Toast.LENGTH_SHORT).show();
						JSONObject result = new JSONObject(new String(response.data));
						String status_result = result.getString(VolleyWSJsonRequest.JSON_STATUS);
						if(WSSystemMsgService.WSSystemMsgServiceList.errorHandle(status_result, SystemInfo.this)){
							return ;				
						}else{
							Toast.makeText(SystemInfo.this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
						}
					}else{
						Toast.makeText(SystemInfo.this, getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
					}
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		KitsApplication.getInstance().addToRequestQueue(wsSystemMsgServiceDelete, "SystemInfoDelete");	
	}
}
