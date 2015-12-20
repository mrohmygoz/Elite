package com.uninum.elite.webservice;

import org.json.JSONException;
import org.json.JSONObject;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.uninum.elite.data.UpdateMessage;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.database.MessageDBHelper.GroupMessageEntry;
import com.uninum.elite.database.MessageDBHelper;
import com.uninum.elite.object.SingleMessage;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.MyContentProvider;
import com.uninum.elite.ui.TabMessage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class PubnubHelper {
	public final String PUBLISH_KEY = "pub-c-335f32d0-defb-4023-8991-6d0c98d7e4bd";
	public final String SUBSCRIBE_KEY = "sub-c-bc4d56b6-4030-11e5-81a9-0619f8945a4f";
	public static Callback subscribeCallback, publishCallback, statusCallback, herenowCallback;
	
	MessageDBHelper msgDB;
	GroupDBHelper groupDB;
	
	Pubnub pubnub;
	String callback;
	Context context;
	UpdateMessage updateMsg;
	
	public PubnubHelper(final Context context){
		pubnub = new Pubnub(PUBLISH_KEY, SUBSCRIBE_KEY);
		this.context = context;
		msgDB = MessageDBHelper.getInstance(context);
		groupDB = GroupDBHelper.getInstance(context);
		updateMsg = new UpdateMessage(context);
		
		publishCallback = new Callback(){

			@Override
			public void errorCallback(String arg0, PubnubError arg1) {
				// TODO Auto-generated method stub
				super.errorCallback(arg0, arg1);
				Log.i("pubnub", "activity msg: send error");
			}

			@Override
			public void successCallback(String arg0, Object arg1) {
				// TODO Auto-generated method stub
				super.successCallback(arg0, arg1);
				Log.i("pubnub", "activity msg: send success");
			}
            
        };
		
		subscribeCallback = new Callback(){

			@Override
			public void connectCallback(String groupuuid, Object object) {
				// TODO Auto-generated method stub
				super.connectCallback(groupuuid, object);
				Log.i("pubnub", "connect call back: " + groupuuid);
			}

			@Override
			public void disconnectCallback(String groupuuid, Object object) {
				// TODO Auto-generated method stub
				super.disconnectCallback(groupuuid, object);
				Log.i("pubnub", "disconnect call back: " + groupuuid);
			}

			@Override
			public void reconnectCallback(String groupuuid, Object object) {
				// TODO Auto-generated method stub
				super.reconnectCallback(groupuuid, object);
				Log.i("pubnub", "reconnect call back: " + groupuuid);
			}

			@Override
			public void errorCallback(String groupuuid, PubnubError arg1) {
				// TODO Auto-generated method stub
				super.errorCallback(groupuuid, arg1);
				Log.i("pubnub", "error call back: " + groupuuid);
			}

			@Override
			public void successCallback(String groupuuid, Object object) {
				// TODO Auto-generated method stub
				super.successCallback(groupuuid, object);
				JSONObject json = (JSONObject) object;
				Cursor cur = groupDB.queryGroupUUID(groupuuid);
				cur.moveToFirst();
				
				try {
					switch(json.getInt(SingleMessage.JSON_TAG_TYPE)){
					
					
					// ---------------------------------------------------------------- //
					// ------------------------- text message ------------------------- //
					// ---------------------------------------------------------------- //
					
					case 0:
						Log.i("pubnub", ""+ groupuuid + ": " + object.toString());
						try {
							
							if (cur!=null) { 															//if the group uuid exist (if not, update the contact)
								
								String selfUUID = cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_SELFUUID));
								if (!json.getString(SingleMessage.JSON_TAG_FROM).equals(selfUUID)) {	//if not from the user herself(himself)
									SingleMessage message = new SingleMessage(json, context);
									if (message.getFromName().equals(SingleMessage.JSON_ERROR)) 		//do something to make "fromName" right (get contact list)
										makeFromNameRight();
									else
										insertMsg(message,SingleMessage.JSON_TYPE[0]);
								} 
							}
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
						break;
						
					// ---------------------------------------------------------------- //
					// ------------------------ image  message ------------------------ //
					// ---------------------------------------------------------------- //
							
					case 1:
						Log.i("pubnub", ""+ groupuuid + ": " + object.toString());
						if (cur!=null) {
							String selfUUID = cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_SELFUUID));
							
							if (!json.getString(SingleMessage.JSON_TAG_FROM).equals(selfUUID)) { 			//if not from the user herself(himself)
								
								SingleMessage message = new SingleMessage(json, context);
								if (message.getFromName().equals(SingleMessage.JSON_ERROR))
									makeFromNameRight();
								else
									insertMsg(message,SingleMessage.JSON_TYPE[1]);
							}
						}
						break;
						
						
					// ---------------------------------------------------------------- //
					// ------------------------ video  message ------------------------ //
					// ---------------------------------------------------------------- //
								
					case 2:
						Log.i("pubnub", ""+ groupuuid + ": " + object.toString());
						
						if (cur!=null) {
							String selfUUID = cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_SELFUUID));
							
							if (!json.getString(SingleMessage.JSON_TAG_FROM).equals(selfUUID)) { 			//if not from the user herself(himself)
								
								SingleMessage message = new SingleMessage(json, context);
								if (message.getFromName().equals(SingleMessage.JSON_ERROR))
									makeFromNameRight();
								else
									insertMsg(message,SingleMessage.JSON_TYPE[2]);
							}
						}
						break;
						
						
					// ---------------------------------------------------------------- //
					// ------------------------ status message ------------------------ //
					// ---------------------------------------------------------------- //
						
					case 5:
						String selfUUID = cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_SELFUUID));
						
						//if (json.getString(SingleMessage.JSON_TAG_FROM).equals(selfUUID)){
							//Log.d("pubnub", "equals self");
							String selection = GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID + " = ?";
							String[] selectionArgs = {json.getString(SingleMessage.JSON_TAG_MSGID)};
							
							ContentValues values = new ContentValues();
							values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_NUMBER, json.getString(SingleMessage.JSON_TAG_READCOUNT));
							context.getContentResolver().update(
									Uri.parse("content://" + MyContentProvider.AUTHORITY + "/" + groupuuid), values,
									selection, selectionArgs);
						//}
						break;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
            
        };
        
        
	}
	
	private void insertMsg (SingleMessage message, int type) {
		
		if (KitsApplication.getInstance().isMsgActivityVisible()){
			
			message.setStatus(SingleMessage.STATUS_READED);
			JSONObject readObject = new JSONObject();
			try {
				readObject.put(SingleMessage.JSON_TAG_MSGID, message.getMessageUUID());
				readObject.put(SingleMessage.JSON_TAG_FROM, message.getFromUUID());
				readObject.put(SingleMessage.JSON_TAG_TO, message.getGroupUUID());
				readObject.put(SingleMessage.JSON_TAG_TYPE, 5);
				readObject.put(SingleMessage.JSON_TAG_READTIME, System.currentTimeMillis());
				
				Cursor tempcur = groupDB.queryGroupUUID(message.getGroupUUID());
				if (tempcur.moveToFirst())
					readObject.put(SingleMessage.JSON_TAG_READFROM, tempcur.getString(tempcur.getColumnIndex(GroupEntry.COLUMN_GROUP_SELFUUID)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			updateMsg.sendReadMsg(readObject);
			
		}else
			message.setStatus(SingleMessage.STATUS_UNREAD);
		
		ContentValues values = new ContentValues();
		values.put(GroupMessageEntry.COLUMN_MESSAGE_MSG, message.getMessage());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID, message.getMessageUUID());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_FROM_UUID, message.getFromUUID());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_FROM_NAME, message.getFromName());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_GROUP_UUID, message.getGroupUUID());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME, System.currentTimeMillis());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_TIME, "");
		values.put(GroupMessageEntry.COLUMN_MESSAGE_TYPE, type);
		values.put(GroupMessageEntry.COLUMN_MESSAGE_STATUS, message.getStatus());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_NUMBER, 0);
		if (type!=0) {
			values.put(GroupMessageEntry.COLUMN_MESSAGE_DOWNLOAD_URL, message.getDownloadUrl());
			values.put(GroupMessageEntry.COLUMN_MESSAGE_PREVIEW_URL, message.getPreviewUrl());
		}
		context.getContentResolver().insert(Uri.parse("content://" + MyContentProvider.AUTHORITY + "/" + message.getGroupUUID()), values);
		
		if (KitsApplication.getInstance().isTabMsgStarted())
			TabMessage.updateViewHandler.sendEmptyMessage(0);
	}
	
	private void makeFromNameRight(){
		
	}
	
	public void Subscribe(String groupUUID){
		try {
			if (!KitsApplication.getInstance().isGroupSubscribed(groupUUID)) {
				Cursor cur = groupDB.queryGroupUUID(groupUUID);
				cur.moveToFirst();
				pubnub.setUUID(cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_SELFUUID)));
				pubnub.subscribe(groupUUID, subscribeCallback);
				KitsApplication.getInstance().addGroup(groupUUID);
				Log.d("pubnub", "subscribe");
			}
        } catch (PubnubException e) {
            e.printStackTrace();
        }
		
	}
}
