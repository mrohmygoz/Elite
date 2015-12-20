package com.uninum.elite.object;

import org.json.JSONException;
import org.json.JSONObject;

import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.ContactDBHelper.ContactEntry;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.database.MessageDBHelper.GroupMessageEntry;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class SingleMessage {
	public static final String STATUS_SENDING = "sending";
	public static final String STATUS_FAILED = "failed";
	public static final String STATUS_RECEIVING = "receiving";
	public static final String STATUS_UNREAD = "unread";
	public static final String STATUS_READED = "readed";
	public static final String JSON_ERROR = "JSON_ERROR";
	
	public static final String JSON_TAG_MSGID = "msgId";
	public static final String JSON_TAG_MESSAGE = "message";
	public static final String JSON_TAG_TO = "to";
	public static final String JSON_TAG_FROM = "from";
	public static final String JSON_TAG_TYPE = "type"; 
	public static final String JSON_TAG_STATUS = "status";
	public static final String JSON_TAG_CREATETIME = "createdTime";
	public static final String JSON_TAG_READTIME = "readTime";
	public static final String JSON_TAG_READCOUNT = "readCount";
	public static final String JSON_TAG_DOWNLOAD_URL = "downloadUrl";
	public static final String JSON_TAG_PREVIEW_URL = "previewUrl";
	public static final String JSON_TAG_READFROM = "readFrom";
	public static final int[]  JSON_TYPE = {0,1,2,3,4,5}; // 0:text, 5:status msg
	
	private String messageUUID;
	private String message;
	private String fromUUID;
	private String fromName;
	private String groupUUID;
	private long createdTime;
	private long readedTime;
	private int type;
	private String status;
	private int readedNumber;
	private String downloadUrl;
	private String previewUrl;
	
	private Contact contact;
	private String tableName;
	private Context context;
	ContactDBHelper contactDB;
	GroupDBHelper groupDB;
	
	public SingleMessage(String msguuid, String msg, String fromuuid, String fromName, String groupuuid, long created_time, int type, Context context){
		this.messageUUID = msguuid;
		this.message = msg;
		this.fromUUID = fromuuid;
		this.fromName = fromName;
		this.groupUUID = groupuuid;
		this.createdTime = created_time;
		this.type = this.JSON_TYPE[0];
		
		if (fromuuid.equals(fromName)){
			this.status = this.STATUS_SENDING;
			contact = null;
		}
		else{
			this.status = this.STATUS_UNREAD;
			contactDB = ContactDBHelper.getInstance(context);
			Cursor cursor = contactDB.queryContact(fromUUID, groupUUID);
			if(cursor.moveToFirst())
				this.contact = new Contact(cursor);
			cursor.close();
		}
		
		readedNumber = 0;
		this.context = context;
	}
	
	public SingleMessage(String msguuid, String msg, Contact contact, String groupuuid, long created_time, int type, Context context){
		this.messageUUID = msguuid;
		this.message = msg;
		this.fromUUID = contact.getContactUUID();
		this.fromName = contact.getName();
		this.groupUUID = groupuuid;
		this.createdTime = created_time;
		this.type = this.JSON_TYPE[0];
		this.status = this.STATUS_UNREAD;
		readedNumber = 0;
		this.contact = contact;
	}
	
	public SingleMessage(Cursor cur, Context context){
		this.messageUUID = cur.getString(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID));
		this.message = cur.getString(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_MSG));
		this.fromUUID = cur.getString(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_FROM_UUID));
		this.fromName = cur.getString(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_FROM_NAME));
		this.groupUUID = cur.getString(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_GROUP_UUID));
		this.createdTime = cur.getLong(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME));
		this.readedTime = cur.getLong(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_READED_TIME));
		this.type = cur.getInt(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_TYPE));
		this.status = cur.getString(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_STATUS));
		this.readedNumber = cur.getInt(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_READED_NUMBER));
		this.downloadUrl = cur.getString(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_DOWNLOAD_URL));
		this.previewUrl = cur.getString(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_PREVIEW_URL));
		
		if (fromName!=null) {
			
			if (!fromName.equals(fromUUID)) {
				contactDB = ContactDBHelper.getInstance(context);
				Cursor cursor = contactDB.queryContact(fromUUID, groupUUID);
				if (cursor.moveToFirst())
					this.contact = new Contact(cursor);
				cursor.close();
			} else
				contact = null;
			
		} else {
			this.fromName = this.JSON_ERROR;
			contact = null;
		}
	}
	
	public SingleMessage(JSONObject object, Context context){
		try {
			this.messageUUID = object.getString(JSON_TAG_MSGID);
			this.message = object.getString(JSON_TAG_MESSAGE);
			this.fromUUID = object.getString(JSON_TAG_FROM);
			this.groupUUID = object.getString(JSON_TAG_TO);
			this.type = object.getInt(JSON_TAG_TYPE);
			
			groupDB = GroupDBHelper.getInstance(context);
			if (!groupDB.isSelf(groupUUID, fromUUID)) {
				contactDB = ContactDBHelper.getInstance(context);
				Cursor cur = contactDB.queryContact(fromUUID, groupUUID);
				if (cur.moveToFirst()) {
					this.fromName = cur.getString(cur.getColumnIndex(ContactEntry.COLUMN_CONTACT_NAME));
					this.contact = new Contact(cur);
				} else {
					Log.e("Jenny", "single msg: contact cursor is null, contact: " + fromUUID + ", group: " + groupUUID);
					this.fromName = this.JSON_ERROR;
				} 
			}else{
				this.fromName = fromUUID;
			}
			
			if (type!=0){
				this.downloadUrl = object.getString(JSON_TAG_DOWNLOAD_URL).replace("\\", "");
				this.previewUrl = object.getString(JSON_TAG_PREVIEW_URL).replace("\\", "");
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("Jenny", "single msg: json error");
			this.fromName = this.JSON_ERROR;
		}
		this.context = context;
	}
	
	public String getMessageUUID(){
		return this.messageUUID;
	}
	
	public String getMessage(){
		return this.message;
	}
	
	public String getFromUUID() {
		return this.fromUUID;
	}
	
	public String getFromName() {
		return this.fromName;
	}
	
	public String getGroupUUID() {
		return this.groupUUID;
	}
	
	public long getCreatedTime(){
		return this.createdTime;
	}
	
	public long getReadedTime(){
		return this.readedTime;
	}
	
	public int getType(){
		return this.type;
	}
	
	public String getStatus(){
		return this.status;
	}
	
	public int getReadedNumber(){
		return this.readedNumber;
	}
	
	public Contact getContact(){
		if (contact==null)
			return null;
		return this.contact;
	}
	
	public JSONObject getJSON(){
		JSONObject obj = new JSONObject();
		try {
			obj.put(JSON_TAG_MSGID, this.messageUUID);
			obj.put(JSON_TAG_MESSAGE, this.message);
			obj.put(JSON_TAG_FROM, this.fromUUID);
			obj.put(JSON_TAG_TO, this.groupUUID);
			obj.put(JSON_TAG_TYPE, this.JSON_TYPE[0]);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	public String getDownloadUrl(){
		return this.downloadUrl;
	}
	
	public String getPreviewUrl(){
		return this.previewUrl;
	}
	
	public String getTypeString() {
		switch (this.type){
		case 0:
			return "text";
		case 1:
			return "image";
		case 2:
			return "audio";
		case 3:
			return "video";
		case 4:
			return "file";
		default:
			return "error";	
		}
	}
	
	//set method
	public void setTime(long time){
		this.createdTime = time;
	}
	

	public void setReadTime(long time){
		this.readedTime = time;
	}
	
	public void setStatus(String sts){
		this.status = sts;
	}
	
	public void setReadedTime(long readTime){
		this.readedTime = readTime;
	}
	
	public void setReadedNumber(int num){
		this.readedNumber = num;
	}
}
