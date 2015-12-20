package com.uninum.elite.database;

import java.util.List;
import java.util.UUID;

import com.uninum.elite.database.ContactDBHelper.ContactEntry;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.object.ContactGroup;
import com.uninum.elite.object.SingleMessage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageDBHelper extends SQLiteOpenHelper {
	
	//for sql use
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	
	//database setting
	public 	static final String 	MESSAGE_DATABASE_NAME = "Message.db";
	public 	static final int 		MESSAGE_DATABASE_VERSION = 2;
	private static MessageDBHelper 	instance;
	private GroupDBHelper			groupDB;
	Context							context;
	
	private String SQL_CREATE_MSG_TABLE_ENTRIES_BRACKET = 
			" (" + 
			GroupMessageEntry._ID + " INTEGER PRIMARY KEY," + 
			GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID 		+ TEXT_TYPE 	+ COMMA_SEP +
			GroupMessageEntry.COLUMN_MESSAGE_MSG 			+ TEXT_TYPE 	+ COMMA_SEP +
			GroupMessageEntry.COLUMN_MESSAGE_FROM_UUID 		+ TEXT_TYPE 	+ COMMA_SEP +
			GroupMessageEntry.COLUMN_MESSAGE_FROM_NAME 		+ TEXT_TYPE 	+ COMMA_SEP +
			GroupMessageEntry.COLUMN_MESSAGE_GROUP_UUID		+ TEXT_TYPE 	+ COMMA_SEP +
			GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME 	+ INTEGER_TYPE 	+ COMMA_SEP +
			GroupMessageEntry.COLUMN_MESSAGE_TYPE 			+ INTEGER_TYPE 	+ COMMA_SEP +
			GroupMessageEntry.COLUMN_MESSAGE_READED_TIME 	+ INTEGER_TYPE 	+ COMMA_SEP +
			GroupMessageEntry.COLUMN_MESSAGE_STATUS 		+ TEXT_TYPE 	+ COMMA_SEP + 
			GroupMessageEntry.COLUMN_MESSAGE_DOWNLOAD_URL 	+ TEXT_TYPE 	+ COMMA_SEP + 
			GroupMessageEntry.COLUMN_MESSAGE_PREVIEW_URL 	+ TEXT_TYPE 	+ COMMA_SEP + 
			GroupMessageEntry.COLUMN_MESSAGE_READED_NUMBER 	+ INTEGER_TYPE 	+ " )";

	
	public MessageDBHelper(Context context) {
		super(context, MESSAGE_DATABASE_NAME, null, MESSAGE_DATABASE_VERSION);
		// TODO Auto-generated constructor stub
		this.context = context;
		groupDB = GroupDBHelper.getInstance(context);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.d("Jenny", "msg db: on create");
		Cursor cur = groupDB.queryAllGroup();
		if (cur.moveToFirst()){
			do {
				db.execSQL("CREATE TABLE IF NOT EXISTS '" + cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_UUID)) + "'" + SQL_CREATE_MSG_TABLE_ENTRIES_BRACKET);
			}while (cur.moveToNext());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion){
		case 1:
			String groupuuid;
			Cursor cur = groupDB.queryAllGroup();
			if (cur.moveToFirst()) {
				do {
					groupuuid = cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_UUID));
					db.execSQL(" ALTER TABLE '"+ groupuuid + "' ADD COLUMN "+ GroupMessageEntry.COLUMN_MESSAGE_DOWNLOAD_URL + TEXT_TYPE);
					db.execSQL(" ALTER TABLE '"+ groupuuid + "' ADD COLUMN "+ GroupMessageEntry.COLUMN_MESSAGE_PREVIEW_URL + TEXT_TYPE);
				} while (cur.moveToNext());
			}
			
			Log.d("Jenny", "msg db: alter table");
			break;
		}
	}
	
	public static synchronized MessageDBHelper getInstance(Context context){
		if(instance == null){
			instance = new MessageDBHelper(context);
		}
		return instance;
	}
	
	public synchronized void createGroupMsgTable(String tableName){						//create by String
			String sql = new String("CREATE TABLE IF NOT EXISTS '" + tableName + "'" + this.SQL_CREATE_MSG_TABLE_ENTRIES_BRACKET);
			try {
				getWritableDatabase().execSQL(sql);
				Log.d("Jenny", "msg db: create group msg table " + tableName + " success");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("Jenny", "msg db: create group msg table " + tableName + " failed");
			}
	}
	
	public synchronized void insertGroupMsg (SingleMessage message, String name){
			ContentValues values = new ContentValues();
			values.put(GroupMessageEntry.COLUMN_MESSAGE_MSG, message.getMessage());
			values.put(GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID, message.getMessageUUID());
			values.put(GroupMessageEntry.COLUMN_MESSAGE_FROM_UUID, message.getFromUUID());
			values.put(GroupMessageEntry.COLUMN_MESSAGE_FROM_NAME, message.getFromName());
			values.put(GroupMessageEntry.COLUMN_MESSAGE_GROUP_UUID, message.getGroupUUID());
			values.put(GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME, message.getCreatedTime());
			values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_TIME, message.getReadedTime());
			values.put(GroupMessageEntry.COLUMN_MESSAGE_TYPE, message.getType());
			values.put(GroupMessageEntry.COLUMN_MESSAGE_STATUS, message.getStatus());
			values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_NUMBER, message.getReadedNumber());
			values.put(GroupMessageEntry.COLUMN_MESSAGE_DOWNLOAD_URL, message.getDownloadUrl());
			values.put(GroupMessageEntry.COLUMN_MESSAGE_PREVIEW_URL, message.getPreviewUrl());
			getWritableDatabase().insert("'"+name+"'", null, values);
	}
	
	
	public synchronized Cursor queryAllMessage(String name){
		if (this.IsTableExist(name)) {
			String sortOrder = GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME + " ASC";
			return getReadableDatabase().query("'"+name+"'", group_msg_projection, null, null, null, null, sortOrder);
		}
		else
			return null;
	}
	
	public synchronized Cursor queryLastUnread(String name){
		String selection = GroupMessageEntry.COLUMN_MESSAGE_STATUS + " = ?";
		String[] selectionArgs = {SingleMessage.STATUS_UNREAD};
		return getReadableDatabase().query("'"+name+"'", group_msg_projection, selection, selectionArgs, null, null, null);
	}
	
	public synchronized Cursor querySending (String name) {
		String selection = GroupMessageEntry.COLUMN_MESSAGE_STATUS + " = ?";
		String[] selectionArgs = {SingleMessage.STATUS_SENDING};
		return getReadableDatabase().query("'"+name+"'", group_msg_projection, selection, selectionArgs, null, null, null);
	}
	
	public synchronized void updateMessage(SingleMessage msg, String name){		// for updating read count
		ContentValues values = new ContentValues();
		values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_NUMBER, msg.getReadedNumber());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_TIME, msg.getReadedTime());
		String where = GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID + " = ? ";
		String[] whereArgs = {msg.getMessageUUID()};
		getWritableDatabase().update("'"+name+"'", values, where, whereArgs);
	}
	
	public synchronized void setStatusReaded(String name, String msgid){
		String where = GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID + " = ? ";
		String[] whereArgs = {msgid};
		ContentValues values = new ContentValues();
		values.put(GroupMessageEntry.COLUMN_MESSAGE_STATUS, SingleMessage.STATUS_READED);
		getWritableDatabase().update("'"+name+"'", values, where, whereArgs);
	}
	
	public synchronized void setStatusFailed(String name){
		String where = GroupMessageEntry.COLUMN_MESSAGE_STATUS + " = ? ";
		String[] whereArgs = {SingleMessage.STATUS_SENDING};
		ContentValues values = new ContentValues();
		values.put(GroupMessageEntry.COLUMN_MESSAGE_STATUS, SingleMessage.STATUS_FAILED);
		getWritableDatabase().update("'"+name+"'", values, where, whereArgs);
	}
	
	public boolean IsTableExist(String name){
		boolean isExist;
		String sql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + name + "'";
		Cursor cur;
		try {
			cur = getReadableDatabase().rawQuery(sql, null);
			if (cur.getCount()>0)
				isExist = true;
			else
				isExist = false;
			cur.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isExist = false;
			Log.e("Jenny", "Msg db: is table exist failed");
		}
		
		return isExist;
	}
	
	public boolean isMsgExist(String name, String msguuid){
		String selection = GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID + " = ?";
		String[] selectionArgs = {msguuid};
		if (getReadableDatabase().query("'"+name+"'", new String[] {GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID}, selection, selectionArgs, null, null, null).moveToFirst())
			return true; 
		return false;
	}

	public void deleteTable(String name){
		String sql = "DROP TABLE IF EXISTS ";
		getWritableDatabase().execSQL(sql + "'" + name + "'");
	}
	
	public void deleteAllMessage(String name){
		getWritableDatabase().delete("'"+name+"'", null, null);
	}
	
	public class GroupMessageEntry implements BaseColumns{
		public static final String COLUMN_MESSAGE_MSG_UUID = "MESSAGE_UUID";
		public static final String COLUMN_MESSAGE_MSG = "MESSAGE";
		public static final String COLUMN_MESSAGE_FROM_UUID = "FROM_UUID"; //contact uuid of sender
		public static final String COLUMN_MESSAGE_FROM_NAME = "FROM_NAME";
		public static final String COLUMN_MESSAGE_GROUP_UUID = "GROUP_UUID";
		public static final String COLUMN_MESSAGE_CREATED_TIME = "CREATED_TIME";
		public static final String COLUMN_MESSAGE_READED_TIME = "READED_TIME";
		public static final String COLUMN_MESSAGE_TYPE = "COLUMN_MESSAGE_TYPE";
		public static final String COLUMN_MESSAGE_STATUS = "STATUS";
		public static final String COLUMN_MESSAGE_READED_NUMBER = "READED_NUMBER";
		public static final String COLUMN_MESSAGE_DOWNLOAD_URL = "DOWNLOAD_URL";
		public static final String COLUMN_MESSAGE_PREVIEW_URL = "PREVIEW_URL";
	}
	
	
	public static String[] group_msg_projection = {
			GroupMessageEntry._ID,
			GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID,
			GroupMessageEntry.COLUMN_MESSAGE_MSG,
			GroupMessageEntry.COLUMN_MESSAGE_FROM_UUID,
			GroupMessageEntry.COLUMN_MESSAGE_FROM_NAME,
			GroupMessageEntry.COLUMN_MESSAGE_GROUP_UUID,
			GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME,
			GroupMessageEntry.COLUMN_MESSAGE_READED_TIME,
			GroupMessageEntry.COLUMN_MESSAGE_TYPE,
			GroupMessageEntry.COLUMN_MESSAGE_STATUS,
			GroupMessageEntry.COLUMN_MESSAGE_READED_NUMBER,
			GroupMessageEntry.COLUMN_MESSAGE_DOWNLOAD_URL,
			GroupMessageEntry.COLUMN_MESSAGE_PREVIEW_URL
	};
}
