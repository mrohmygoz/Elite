package com.uninum.elite.ui;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uninum.elite.adapter.HistoryAdapter;
import com.uninum.elite.data.UpdateHistory;
import com.uninum.elite.data.UpdateMessage;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.HistoryDBHelper;
import com.uninum.elite.database.MessageDBHelper;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.database.HistoryDBHelper.HistoryEntry;
import com.uninum.elite.object.History;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.MyContentProvider;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.utility.LoginService;
import com.uninum.elite.utility.NetworkUtility;
import com.uninum.elite.webservice.PubnubHelper;
import com.uninum.elite.webservice.VolleyWSJsonRequest;
import com.uninum.elite.webservice.WSHistory;
import com.uninum.elite.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TabHistory extends Fragment implements OnRefreshListener, OnClickListener {
	private List<History> historys ;
	HistoryAdapter adapter;
	ExpandableListView expandableListView;
//	Button fetchmore_btn;
	ProgressBar fetchmore_pb;
	private PullToRefreshLayout mPullToRefreshLayout;
	TextView defaultListHint_tv;
	HistoryDBHelper historyDB ;
	public static Handler updateHistoryHandler, updateHistoryViewHandler,
							updateHistoryNoUpdateHandler, updateHistoryCompleteLoadingHandler,
							updateHistoryErrorHandler, stopService;
	public static final String GET_NEW_HISTORIES = "GET_NEW_HISTORIES";
	public static final String GET_HISTORY_LIST = "GET_HISTORY_LIST";
	View rootview, loadmoreView;
	RelativeLayout footerview_rl;
	Button fetchmore_btn;
	private boolean isFetchMoreClick =false;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	
		setHasOptionsMenu(true);
		historyDB = HistoryDBHelper.getInstance(getActivity());
		CreateHandler();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		rootview = inflater.inflate(R.layout.fragment_history, container, false);
		expandableListView = (ExpandableListView)rootview.findViewById(R.id.elsv_history);
		loadmoreView = inflater.inflate(R.layout.footerview_loadmore, null, false);
		expandableListView.addFooterView(loadmoreView);
		footerview_rl = (RelativeLayout)loadmoreView.findViewById(R.id.footerview_rl);
		fetchmore_btn = (Button)loadmoreView.findViewById(R.id.footer_fetchmore_btn);
		fetchmore_btn.setOnClickListener(this);
		fetchmore_pb = (ProgressBar)loadmoreView.findViewById(R.id.footerview_pb);
		defaultListHint_tv = (TextView)rootview.findViewById(R.id.history_default_hint_tv);
		mPullToRefreshLayout = (PullToRefreshLayout)rootview.findViewById(R.id.history_linearLayout);
		ActionBarPullToRefresh.from(getActivity())
              .allChildrenArePullable()
              .listener(this)
              .setup(mPullToRefreshLayout);	
		return rootview;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		CheckLogin();
		
		int color = getActivity().getResources().getColor(R.color.elite_theme_color_history_item_line);
		int[] colors = { color,color };
		expandableListView.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
		expandableListView.setDividerHeight(0);
		expandableListView.setChildIndicator(null);
		expandableListView.setChildDivider(getResources().getDrawable(android.R.color.transparent));       
		Log.d("KITs Elite", "minus:"+ (System.currentTimeMillis() - SystemManager.getPreLong(getActivity(), SystemManager.HISTORT_UPDATE_TIME)));
		checkNormalHistoryUpload();
	}

	public void CheckLogin(){
		if (KitsApplication.getInstance().isLogin() && KitsApplication.getInstance().isTabHistoryStarted()){
			if (KitsApplication.getInstance().getUpdateHistory()){
				getNewHistories();
				KitsApplication.getInstance().unsetUpdateHistory();
			}
			updateHistory();
			return;
		}else if (KitsApplication.getInstance().isLogin() && !KitsApplication.getInstance().isTabHistoryStarted()){
			getNewHistories();
			KitsApplication.getInstance().TabHistoryStarted();
			return;
		}
		Intent serviceIntent = new Intent(getActivity(),LoginService.class);
		Bundle serviebundle = new Bundle();
		serviebundle.putString(LoginService.CALLBACK, "TabHistory");
		serviceIntent.putExtras(serviebundle);
		getActivity().startService(serviceIntent);
		KitsApplication.getInstance().TabHistoryStarted();
	}
	
	private void CreateHandler() {
		
		stopService = new Handler(){
			@Override
			public void handleMessage(Message msg) {  //add uri, subscribe, update message, get new histories
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Intent serviceIntent = new Intent(getActivity(),LoginService.class);
				getActivity().stopService(serviceIntent);
				getNewHistories();
				
				String groupUUID;
				UpdateMessage updateMsg = new UpdateMessage(getActivity());
				PubnubHelper pubnubHelper = new PubnubHelper(getActivity());
				
				Cursor cur = GroupDBHelper.getInstance(getActivity()).queryAllGroup();
				if (cur.moveToFirst()){
					do{
						groupUUID = cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_UUID));
						MessageDBHelper.getInstance(getActivity()).deleteAllMessage(groupUUID);
						updateMsg.updateMsgRequest("TabHistory", groupUUID,System.currentTimeMillis(),"old");
						pubnubHelper.Subscribe(groupUUID);
						getActivity().getContentResolver().call(
								MyContentProvider.CONTENT_URI, 
								"addURI", 
								groupUUID,
								null);
					}while(cur.moveToNext());
				}
				SystemManager.putPreLong(getActivity(), SystemManager.MESSAGE_UPDATE_TIME, System.currentTimeMillis());
			}		
		};
		
		updateHistoryHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				updateHistory();
			}		
		};
		
		updateHistoryViewHandler = new Handler(){
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (adapter==null)
					updateHistory();
				else{
					adapter.changeCursor(historyDB.queryAllHistories());
					adapter.notifyDataSetChanged();
				}
				footerview_rl.setVisibility(LinearLayout.VISIBLE);
				fetchmore_pb.setVisibility(View.INVISIBLE);
			}					
		};
		
		updateHistoryNoUpdateHandler = new Handler(){
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				footerview_rl.setVisibility(LinearLayout.INVISIBLE);
				fetchmore_pb.setVisibility(View.INVISIBLE);
			}					
		};
		updateHistoryCompleteLoadingHandler = new Handler(){
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if(mPullToRefreshLayout!=null)
					mPullToRefreshLayout.setRefreshComplete();
				isFetchMoreClick = false;
				fetchmore_pb.setVisibility(View.INVISIBLE);
			}			
		};
		updateHistoryErrorHandler = new Handler(){
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				updateHistory();
			}
		};
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.menu_history_delete:
			if(NetworkUtility.isConnected(getActivity())){
				new AlertDialog.Builder(getActivity())
				.setTitle(R.string.history_delete_all_title)
				.setPositiveButton(R.string.history_delete_yes, new  DialogInterface.OnClickListener ()  {			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						deleteHistory();
					}					
				})
				.setNegativeButton(R.string.history_delete_no, new  DialogInterface.OnClickListener ()  {
			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						return ;
					}
					
				}).show();	
				
			}else{
				Toast.makeText(getActivity(), getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
	    inflater.inflate(R.menu.menu_history, menu);
	}

	private void deleteHistory(){
		WSHistory.WSHistoryDelete registerRequest = new WSHistory.WSHistoryDelete(SystemManager.getToken(getActivity()), new Response.Listener<JSONObject>() {
		
		@Override
		public void onResponse(JSONObject response) {
			// TODO Auto-generated method stub
			try {
					String status ="";
					status = response.getString(VolleyWSJsonRequest.JSON_STATUS);
					Log.d("KITs HistoryDelete",response.toString());				
					if(status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)){
						historyDB.removeAll();					
						updateHistoryHandler.sendEmptyMessage(0);
						updateHistoryCompleteLoadingHandler.sendEmptyMessage(0);
					}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
//				Activity_Communication.handler_progressbar_off.sendEmptyMessage(0);
			}
		}
	}, new Response.ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			// TODO Auto-generated method stub
			try {

				NetworkResponse response = error.networkResponse;
				String json;
				if(response != null && response.data != null){
//					Toast.makeText(tab.getActivity(), new String(response.data), Toast.LENGTH_SHORT).show();
					JSONObject result = new JSONObject(new String(response.data));
					String result_status = result.getString(VolleyWSJsonRequest.JSON_STATUS);
					if(VolleyWSJsonRequest.errorHandle(result_status, getActivity())){
						return ;
					}else if(WSHistory.WSHistoryDelete.errorHandle(result_status, getActivity())){
						return ;
					}else{
						Toast.makeText(getActivity(), getActivity().getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
					}
				}
			}catch(Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{

			}		
		}
	} );

	KitsApplication.getInstance().addToRequestQueue(registerRequest, WSHistory.CLEAR_HISTORY_TAG);	
	
	}
	
	//check adapter item number if is 0 then show hint
	private void updateHistory(){
		Cursor cursor = historyDB.queryAllHistories();
		adapter = new HistoryAdapter(cursor, getActivity());		
		
		if(cursor !=null && cursor.getCount()>0){			
			defaultListHint_tv.setVisibility(TextView.INVISIBLE);
			if(cursor.getCount()>=20)
				footerview_rl.setVisibility(LinearLayout.VISIBLE);
			else
				footerview_rl.setVisibility(LinearLayout.INVISIBLE);
		}else{
			footerview_rl.setVisibility(LinearLayout.INVISIBLE);
			defaultListHint_tv.setVisibility(TextView.VISIBLE);
		}
		expandableListView.setAdapter(adapter);		
	}
	
	private void checkNormalHistoryUpload(){
		try{
			if(NetworkUtility.isConnected(getActivity())){
				Cursor historyCursor = historyDB.queryHistoryUpload();
				historyCursor.moveToFirst();
				Log.d("ELITE","Check history update count:"+historyCursor.getCount());
				if(historyCursor.getCount()>0){
					for(int i=0; i<historyCursor.getCount(); i++){
						History history = new History(historyCursor);
						uploadHistory(history.getHisUUID(), history.getNumber(), 
								String.valueOf(history.getStartTime()), 
								String.valueOf(history.getEndTime()), 
								history.getGroupUUID());
						if(!historyCursor.isLast())
							historyCursor.moveToNext();
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void uploadHistory(final String hisUUID, String number, String startTime, String endTime, String unitUUID){
		JSONObject json = new JSONObject();
		try {
			json.put(WSHistory.JSON_HISTORY_UPLOAD_HISTORY_UUID, hisUUID);
			json.put(WSHistory.JSON_HISTORY_CALLEE, number);
			json.put(WSHistory.JSON_HISTORY_STARTTIME, startTime);
			json.put(WSHistory.JSON_HISTORY_ENDTIME, endTime);
			json.put(WSHistory.JSON_HISTORY_UPLOAD_GROUP_UUID, unitUUID);
			Log.d("ELITE",json.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WSHistory.WSHistoryUpload wsHistoryUpload = new WSHistory.WSHistoryUpload(
				SystemManager.getToken(getActivity()), json , new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						String status;
						try {
							status = response.getString(VolleyWSJsonRequest.JSON_STATUS);
							Log.d("KITs uploadHistory","response:"+ response.toString());
							if(status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)){
								historyDB.updateHistoryHisUuid(hisUUID, response.getString(WSHistory.JSON_HISTORY_UPLOAD_HISTORY_UUID));								
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
						NetworkResponse response = error.networkResponse;
						Log.i("KITs uploadHistory", "Error:"+ new String(response.data));
					}
				});
		KitsApplication.getInstance().addToRequestQueue(wsHistoryUpload, WSHistory.UPLOAD_HISTORY_TAG);	
	
	}

	@Override
	public void onRefreshStarted(View view) {
		// TODO Auto-generated method stub
		if ( NetworkUtility.isConnected(getActivity()) ){
			getNewHistories();
		}else{
			if(mPullToRefreshLayout!=null)
				mPullToRefreshLayout.setRefreshComplete();
			Toast.makeText(getActivity(), getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
		}		
	}
	
	//get newest histories
	private void getNewHistories(){
		if(NetworkUtility.isConnected(getActivity())){
			SystemManager.putPreLong(getActivity(), SystemManager.HISTORT_UPDATE_TIME, System.currentTimeMillis()/1000);
			if (mPullToRefreshLayout!=null && !mPullToRefreshLayout.isRefreshing())
				mPullToRefreshLayout.setRefreshing(true);
			UpdateHistory updateHistory = new UpdateHistory(getActivity());
			updateHistory.updateHistoryRequest(this.GET_NEW_HISTORIES);
		}else{
			Toast.makeText(getActivity(), getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.footer_fetchmore_btn:
				if(!isFetchMoreClick){
		            isFetchMoreClick = true;
		            getHistoryList();
				}
	            break;
		}
		
	}

	private void getHistoryList(){
		if(NetworkUtility.isConnected(getActivity())){
			Cursor cursorTemp = historyDB.queryAllHistories();
			if(cursorTemp!=null && cursorTemp.getCount()>0){
				cursorTemp.moveToFirst();
	            cursorTemp.moveToPosition(cursorTemp.getCount()-1);
	            Log.d("ELITE History", "DB count:"+cursorTemp.getCount() + "History Time:"+ cursorTemp.getString(cursorTemp.getColumnIndex(HistoryEntry.COLUMN_HISTORY_START_TIME)));	            
	            SystemManager.putPreLong(getActivity(), SystemManager.HISTORT_UPDATE_TIME,
	    	            Long.valueOf(cursorTemp.getString(cursorTemp.getColumnIndex(HistoryEntry.COLUMN_HISTORY_START_TIME))) );
	            fetchmore_pb.setVisibility(View.VISIBLE);
				UpdateHistory updateHistory = new UpdateHistory(getActivity());
				updateHistory.updateHistoryRequest(this.GET_HISTORY_LIST);
			}else{
				getNewHistories();
			}
		}else{
            isFetchMoreClick = false;
			Toast.makeText(getActivity(), getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
		}
	}
}
