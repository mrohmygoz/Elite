package com.uninum.elite.data;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uninum.elite.R;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.MessageDBHelper;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.database.MessageDBHelper.GroupMessageEntry;
import com.uninum.elite.object.SingleMessage;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.MyContentProvider;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.ui.Activity_Message;
import com.uninum.elite.ui.MainPage;
import com.uninum.elite.webservice.PubnubHelper;
import com.uninum.elite.webservice.VolleyWSJsonRequest;
import com.uninum.elite.webservice.WSContact;
import com.uninum.elite.webservice.WSMessage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class UpdateMessage {
	
	private Context context;
	private MessageDBHelper msgDB;
	private GroupDBHelper groupDB;
	private Timer timer;
	
	public UpdateMessage(Context context){
		this.context = context;
		msgDB = MessageDBHelper.getInstance(context);
		groupDB = GroupDBHelper.getInstance(context);
	}
	
	public void sendMsgRequest(final JSONObject object){
		
		WSMessage.WSMessageSend sendMessageRequest = new WSMessage.WSMessageSend( SystemManager.getToken(context), object, new Response.Listener<JSONObject>(){

			@Override
			public void onResponse(JSONObject response) {
				try {
					Activity_Message.timerStop.sendEmptyMessage(0);
					
					String status = response.getString(VolleyWSJsonRequest.JSON_STATUS);
					Log.i("Jenny", "send msg: on response, " + response.toString());
					
					if(status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)){
						String selection = GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID + " = ?";
						String msgId = response.getString(SingleMessage.JSON_TAG_MSGID);
						String[] selectionArgs = {msgId};
						String table =  object.getString(SingleMessage.JSON_TAG_TO);
						ContentValues values = new ContentValues();
						values.put(GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME, response.getString(SingleMessage.JSON_TAG_CREATETIME));
						values.put(GroupMessageEntry.COLUMN_MESSAGE_STATUS, SingleMessage.STATUS_RECEIVING);
						context.getContentResolver().update(
								Uri.parse("content://" + MyContentProvider.AUTHORITY + "/" + table), 
								values, selection, selectionArgs);
						Log.i("Jenny", "send msg table: " + table);
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		},new Response.ErrorListener(){

			@Override
			public void onErrorResponse(VolleyError error) {
				
				try {
					
					//show error log
					NetworkResponse response = error.networkResponse;
					if(response != null && response.data != null){
						JSONObject result = new JSONObject(new String(response.data)); 
						String status = result.getString(WSContact.STATUS);
						Log.e("Jenny", "send msg: on error: " + status);
						if(WSMessage.WSMessageSend.errorHandle(status, context)){
							return ;
						}else {
							Toast.makeText(context, R.string.internal_error + "("+ status +")", Toast.LENGTH_SHORT).show();
						}
					} else
						Log.e("Jenny", "send msg: on error ");
					
				}catch(Exception e) {
					e.printStackTrace();
				}
				
			}
			
		});
		KitsApplication.getInstance().addToRequestQueue(sendMessageRequest, WSMessage.SEND_MESSAGE_TAG);
	}
	
	public void updateMsgRequest(final String callback, final String groupUUID, long timestamp, String type){ //for single group
		
		WSMessage.WSMessageUpdate getMessageRequest = 
				new WSMessage.WSMessageUpdate(SystemManager.getToken(context), type, timestamp, groupUUID, null, new Response.Listener<JSONObject>(){

				// ------------------------------------------------------- //
				// --------------------- on response --------------------- //
				// ------------------------------------------------------- //
					
					@Override
					public void onResponse(JSONObject response) {
						try {
							
							String status = response.getString(VolleyWSJsonRequest.JSON_STATUS);
							Log.d("Jenny", "update msg: on response, "+response.toString());
							
							/* --- status = 200--- */
							if(status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)){
								SystemManager.putPreLong(context, SystemManager.MESSAGE_UPDATE_TIME, System.currentTimeMillis());
								JSONArray array = response.getJSONArray(WSMessage.MESSAGE_LIST);
								
								// -------------------------------- //
								// if there is(are) new message(s)  //
								// -------------------------------- //
								
								if(array.length()>0){
									for (int i=0; i<array.length(); i++){
										JSONObject json = array.getJSONObject(i);
										SingleMessage msg = new SingleMessage(json, context);
										
										Cursor cur = groupDB.queryGroupUUID(msg.getGroupUUID());
										// if group not exist
										if (!cur.moveToFirst()) {
											
										// if contact not exist
										} else if (msg.getFromName().equals(SingleMessage.JSON_ERROR)){
											
										// if we can query contact by both groupUUID and contactUUID correctly
										} else {
											msg.setStatus(json.getString(SingleMessage.JSON_TAG_STATUS));
											msg.setTime(json.getLong(SingleMessage.JSON_TAG_CREATETIME));
											msg.setReadedTime(json.getLong(SingleMessage.JSON_TAG_READTIME));
											msg.setReadedNumber(json.getInt(SingleMessage.JSON_TAG_READCOUNT));
											
											//if msg from myself
											if ( msg.getFromUUID().equals( msg.getFromName() )) {
												msg.setStatus(SingleMessage.STATUS_RECEIVING);
											}
											
											String name = msg.getGroupUUID();
											if (!msgDB.isMsgExist(name, msg.getMessageUUID()))
												msgDB.insertGroupMsg(msg, name);
											else
												msgDB.updateMessage(msg, name);
										}
									}
								}
							}
							
							
						} catch (JSONException e) {
							e.printStackTrace();
						} finally {

							// --------------------------------------------------------------------------- //
							// This function is called in case of first opening the application,		   //
							//  it may be called by opening application, or by clicking on notification    //
							//   of msg or incoming call. So we have to subscribe to pubnub first,		   //
							//	  and call back to the callee function to inform the end of update msg.	   //
							// --------------------------------------------------------------------------- //
							
							switch (callback) {
							case "MainPage":
								MainPage.updateMsgFinishHandler.sendEmptyMessage(0);
								break;
							case "Activity_Message":
//								Activity_Message.updateMessage.sendEmptyMessage(0);
								break;
							case "TabHistory":
								break;
							}
							
						}
							
					}
					
					
					
				// ------------------------------------------------------- //
				// ----------------------- on error ---------------------- //
				// ------------------------------------------------------- //
					
				}, new Response.ErrorListener(){

					@Override
					public void onErrorResponse(VolleyError error) {
						try {
							
							NetworkResponse response = error.networkResponse;
							if(response != null && response.data != null){
								JSONObject result = new JSONObject(new String(response.data)); 
								String status = result.getString(WSContact.STATUS);
								Log.e("Jenny", "update msg on error: " + status);
								if(WSMessage.WSMessageUpdate.errorHandle(status, context)){
									return ;
								}else {
									Toast.makeText(context, R.string.internal_error + "("+ status +")", Toast.LENGTH_SHORT).show();
								}
							} else
								Log.e("Jenny", "update msg on error ");
							
						}catch(Exception e) {
							e.printStackTrace();
						}finally{
							Message msg = new Message();
							msg.obj = false;
							
							switch (callback) {
							case "MainPage":
								MainPage.updateMsgFinishHandler.sendMessage(msg);
								break;
							case "Activity_Message":
								msg.obj = groupUUID;
								Activity_Message.updateMessage.sendMessage(msg);
								break;
							case "TabHistory":
								break;
							}
						}
					}
					
				});
		
		KitsApplication.getInstance().addToRequestQueue(getMessageRequest, WSMessage.GET_MESSAGE_TAG);
	}
	
	public void updateOldMsgRequest(long timeStamp, String groupUUID){
		WSMessage.WSMessageFetchMore getMessageRequest = 
				new WSMessage.WSMessageFetchMore(SystemManager.getToken(context), timeStamp, groupUUID, null, new Response.Listener<JSONObject>(){

					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						try {
							
							String status = response.getString(VolleyWSJsonRequest.JSON_STATUS);
							Log.i("Jenny", "update msg: on response, "+response.toString());
							/* --- status = 200--- */
							if(status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)){
								JSONArray array = response.getJSONArray(WSMessage.MESSAGE_LIST);
								if(array.length()>0){
									for (int i=0; i<array.length(); i++){
										JSONObject json = array.getJSONObject(i);
										SingleMessage msg = new SingleMessage(json, context);
										
										if (!msg.getFromName().equals(SingleMessage.JSON_ERROR)){
											msg.setStatus(json.getString(SingleMessage.JSON_TAG_STATUS));
											msg.setReadedNumber(json.getInt(SingleMessage.JSON_TAG_READCOUNT));
											msg.setTime(json.getLong(SingleMessage.JSON_TAG_CREATETIME));
											msg.setReadedTime(json.getLong(SingleMessage.JSON_TAG_READTIME));
											
											String name = msg.getGroupUUID();
											if (!msgDB.isMsgExist(name, msg.getMessageUUID())){
												
												if (!msg.getFromName().equals(msg.getFromUUID())) {
													JSONObject object = new JSONObject();
													object.put(SingleMessage.JSON_TAG_MSGID, msg.getMessageUUID());
													object.put(SingleMessage.JSON_TAG_FROM, msg.getFromUUID());
													object.put(SingleMessage.JSON_TAG_TO, msg.getGroupUUID());
													object.put(SingleMessage.JSON_TAG_TYPE, 5);
													object.put(SingleMessage.JSON_TAG_READTIME,
															System.currentTimeMillis());
													Cursor cur = groupDB.queryGroupUUID(msg.getGroupUUID());
													if (cur.moveToFirst())
														object.put(SingleMessage.JSON_TAG_READFROM, cur.getString(
																cur.getColumnIndex(GroupEntry.COLUMN_GROUP_SELFUUID)));
													sendReadMsg(object);
												} else
													msg.setStatus(SingleMessage.STATUS_RECEIVING);
												
												msgDB.insertGroupMsg(msg, name);
												
											}
											
										// if contact not exist 
										} else {
											
										}
									}
									
									Activity_Message.fetchMoreHandler.sendEmptyMessage(0);
								}else
									Activity_Message.fetchNoMore.sendEmptyMessage(0);
							}
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}, new Response.ErrorListener(){

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						try {
							NetworkResponse response = error.networkResponse;
							Log.e("Jenny", "update msg: on error ");
							
							if(response != null && response.data != null){
								JSONObject result = new JSONObject(new String(response.data)); 
								String status = result.getString(WSContact.STATUS);
								Log.e("Jenny", "update msg: on error: " + status);
								if(WSMessage.WSMessageFetchMore.errorHandle(status, context)){
									return ;
								}else {
									Toast.makeText(context, R.string.internal_error + "("+ status +")", Toast.LENGTH_SHORT).show();
								}
							}
						}catch(Exception e) {
							e.printStackTrace();
						}finally{
							Activity_Message.fetchError.sendEmptyMessage(0);
						}
					}
					
				});
		
		KitsApplication.getInstance().addToRequestQueue(getMessageRequest, WSMessage.GET_MESSAGE_TAG);
	}
	
	public void sendReadMsg(final JSONObject object) {
		WSMessage.WSMessageReadCount request = new WSMessage.WSMessageReadCount(SystemManager.getToken(context),object,new Response.Listener<JSONObject>(){

			@Override
			public void onResponse(JSONObject response) {
				try {
					String status = response.getString(VolleyWSJsonRequest.JSON_STATUS);
					if(status.equals(VolleyWSJsonRequest.RESPONSE_SUCCESS)){
						Log.i("Jenny", "read count api response: " + status);
						String msgid = object.getString(SingleMessage.JSON_TAG_MSGID);
						String groupuuid = object.getString(SingleMessage.JSON_TAG_TO);
						msgDB.setStatusReaded(groupuuid, msgid);
					}else{
						Log.e("Jenny", "read count api response: " + status);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		}, new Response.ErrorListener(){

			@Override
			public void onErrorResponse(VolleyError error) {
				try {
					NetworkResponse response = error.networkResponse;
					Log.e("Jenny", "read count api on error");
					
					if(response != null && response.data != null){
						JSONObject result = new JSONObject(new String(response.data)); 
						String status = result.getString(WSContact.STATUS);
						Log.e("Jenny", "status: " + status);
						if(WSMessage.WSMessageReadCount.errorHandle(status, context)){
							return ;
						}else {
							Toast.makeText(context, R.string.internal_error + "("+ status +")", Toast.LENGTH_SHORT).show();
						}
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
		});
		
		KitsApplication.getInstance().addToRequestQueue(request, WSMessage.GET_MESSAGE_TAG);
	}
}
