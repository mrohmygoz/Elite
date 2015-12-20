package com.uninum.elite.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uninum.elite.R;
import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.HistoryDBHelper;
import com.uninum.elite.database.ContactDBHelper.ContactEntry;
import com.uninum.elite.object.History;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.ui.MainPage;
import com.uninum.elite.ui.TabHistory;
import com.uninum.elite.webservice.VolleyWSJsonRequest;
import com.uninum.elite.webservice.WSHistory;

public class UpdateHistory {
private Context context;
private List<History> historys ;
HistoryDBHelper historyDB;
	public UpdateHistory(Context context){
		this.context = context;
		historyDB = HistoryDBHelper.getInstance(context);
	}
	public void updateHistoryRequest(final String callTag){
		String historyTimeStamp = String.valueOf(SystemManager.getPreLong(context, SystemManager.HISTORT_UPDATE_TIME)); 
		
		//web service setting
		WSHistory.WSHistoryList getHistoryRequest = 
				new WSHistory.WSHistoryList(SystemManager.getToken(context),
						historyTimeStamp, null, new Response.Listener<JSONObject>() {
			
			@Override
			public void onResponse(JSONObject response) {
				// TODO Auto-generated method stub
				try {
						
						String status ="";
						status = response.getString(VolleyWSJsonRequest.JSON_STATUS);
						if(status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)){				
								Log.d("KITs HistoryList",response.toString());						
								JSONArray array = response.getJSONArray(WSHistory.JSON_HISTORY_ARRAY);
								if(array.length()>0){
										historys = new ArrayList<History>();
										Log.d("ELITE History", "JSON Length:"+array.length());
										for(int i = 0;i<array.length();i++)
										{
											JSONObject json = array.getJSONObject(i);
											String hisUUID = json.getString(WSHistory.JSON_HISTORY_HISTORY_UUID);
											String groupUUID = json.getString(WSHistory.JSON_HISTORY_GROUP_UUID);
											String peerUUID = json.getString(WSHistory.JSON_HISTORY_PEER_UUID);
											String type = json.getString(WSHistory.JSON_HISTORY_TYPE);
											long startTime = Long.valueOf(json.getString(WSHistory.JSON_HISTORY_STARTTIME) );
											long endTime = Long.valueOf(json.getString(WSHistory.JSON_HISTORY_ENDTIME) );
																		
											Cursor cursor = ContactDBHelper.getInstance(context).queryContact(peerUUID, groupUUID);
											cursor.moveToFirst();
											if(cursor.getCount()>0){
												SystemManager.putPreLong(context, SystemManager.HISTORT_UPDATE_TIME, System.currentTimeMillis()/1000); // long with 13 bit
												History history;	
												String name = cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_NAME));
												String phone  = cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_PHONE));
												int isPrivate = cursor.getInt(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_PRIVATE));
												cursor.close();							
												if(type.equals(History.in)){
													history = new History(hisUUID, groupUUID, peerUUID, phone, name, startTime,endTime, History.INCOMMING, true);
													historys.add(history);
												}else if(type.equals(History.out)){
													history = new History(hisUUID, groupUUID, peerUUID, phone,  name, startTime,endTime, History.OUTGOING, true);
													historys.add(history);
												}else{
													
												}
											}
																						
										}

										Log.d("ELITE History","Update History Number:"+historys.size());
										if (callTag.equals(TabHistory.GET_NEW_HISTORIES))
											historyDB.removeAll();
										for(int i=0;i<historys.size();i++){						
											historyDB.insertHistory(historys.get(i));
										}
										TabHistory.updateHistoryViewHandler.sendEmptyMessage(0);
								}else{
									Toast.makeText(context, R.string.history_fetchmore_noelse, Toast.LENGTH_SHORT).show();
									TabHistory.updateHistoryNoUpdateHandler.sendEmptyMessage(0);
									return ;
								}
						}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					TabHistory.updateHistoryCompleteLoadingHandler.sendEmptyMessage(0);
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				try {
					NetworkResponse response = error.networkResponse;
					//JSONObject json = new JSONObject();
					String json;
					if(response != null && response.data != null){
//						Toast.makeText(tab.context, new String(response.data), Toast.LENGTH_SHORT).show();
						JSONObject result = new JSONObject(new String(response.data));; 
						String result_status = result.getString(VolleyWSJsonRequest.JSON_STATUS);
						if(WSHistory.WSHistoryList.errorHandle(result_status, context)){
							return ;
						}else{
							Toast.makeText(context, context.getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
						}
					}
				}catch(Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
//					MainPage.loadingHandler.sendEmptyMessage(0);
					TabHistory.updateHistoryErrorHandler.sendEmptyMessage(0);
					TabHistory.updateHistoryCompleteLoadingHandler.sendEmptyMessage(0);
				}
			}
		} );
		KitsApplication.getInstance().addToRequestQueue(getHistoryRequest, WSHistory.GET_HISTORY_TAG);	
	}
}
