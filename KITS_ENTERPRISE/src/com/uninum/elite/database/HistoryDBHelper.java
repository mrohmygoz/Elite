package com.uninum.elite.database;


import com.uninum.elite.database.ContactDBHelper.ContactEntry;
import com.uninum.elite.object.History;
import com.uninum.elite.system.SystemManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.util.Log;

public class HistoryDBHelper extends SQLiteOpenHelper{
	private static HistoryDBHelper instance;
	private Context context;
	public static final int HISTORY_DATABASE_VERSION = 2;
	public static final String HISTORY_DATABASE_NAME = "History.db";	
	
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
			+ HistoryEntry.TABLE_NAME + " (" 
			+ HistoryEntry._ID + " INTEGER PRIMARY KEY," 
			+ HistoryEntry.COLUMN_HISTORY_UUID +" TEXT,"
			+ HistoryEntry.COLUMN_HISTORY_NUMBER+ " TEXT,"
			+ HistoryEntry.COLUMN_HISTORY_NAME+ " TEXT,"
			+ HistoryEntry.COLUMN_HISTORY_UNITUUID + " TEXT,"
			+ HistoryEntry.COLUMN_HISTORY_PEERUUID + " TEXT,"
			+ HistoryEntry.COLUMN_HISTORY_START_TIME + " INTEGER,"
			+ HistoryEntry.COLUMN_HISTORY_END_TIME + " INTEGER,"
			+ HistoryEntry.COLUMN_HISTORY_DURATION + " INTEGER,"
			+ HistoryEntry.COLUMN_HISTORY_STATUS + " INTEGER, "
			+ HistoryEntry.COLUMN_HISTORY_UPLOAD + " INTEGER)";
	
	String[] history_projection = { HistoryEntry._ID,
			HistoryEntry.COLUMN_HISTORY_UUID,
			HistoryEntry.COLUMN_HISTORY_NUMBER,
			HistoryEntry.COLUMN_HISTORY_NAME,
			HistoryEntry.COLUMN_HISTORY_UNITUUID, 
			HistoryEntry.COLUMN_HISTORY_PEERUUID,
			HistoryEntry.COLUMN_HISTORY_START_TIME,
			HistoryEntry.COLUMN_HISTORY_END_TIME,
			HistoryEntry.COLUMN_HISTORY_DURATION,
			HistoryEntry.COLUMN_HISTORY_STATUS,
			HistoryEntry.COLUMN_HISTORY_UPLOAD				
			};
	
	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
			+ HistoryEntry.TABLE_NAME;
	public static synchronized HistoryDBHelper getInstance(Context context){
		if(instance==null){
			instance = new HistoryDBHelper(context, HISTORY_DATABASE_NAME, null, HISTORY_DATABASE_VERSION);
		}
		return instance;
	}
	public HistoryDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL(SQL_DELETE_ENTRIES);
		SystemManager.putPreLong(context, SystemManager.HISTORT_UPDATE_TIME, 0);//reset history update time
		onCreate(db);
	}

	/**
	 * Query all the history calling records.
	 * 
	 * @return
	 */
	public synchronized Cursor queryAllHistories() {

		String sortOrder = HistoryEntry.COLUMN_HISTORY_START_TIME + " DESC";
		Cursor cursor = getReadableDatabase().query(HistoryEntry.TABLE_NAME, history_projection,
				null, null, null, null, sortOrder);		
//		while(!cursor.isLast()){
//			Log.d("kits new","history:"+cursor.getInt(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_START_TIME)));
//			cursor.moveToNext();
//		}
		if(cursor.getCount()>0)
			return cursor;
		else 
			return null;
	}

	public synchronized Cursor queryHistoryUpload(){
		String selection = HistoryEntry.COLUMN_HISTORY_UPLOAD + " = 0 ";
		String sortOrder = HistoryEntry.COLUMN_HISTORY_START_TIME + " ASC";
		return getReadableDatabase().query(HistoryEntry.TABLE_NAME, history_projection,
				selection, null, null, null, sortOrder);
	}
	
	public synchronized Cursor queryHistory(String uuid){

		String selection = HistoryEntry.COLUMN_HISTORY_UUID + " =? ";
		String[] selectionArgs={uuid};
		String sortOrder = HistoryEntry.COLUMN_HISTORY_START_TIME + " DESC";
		return getReadableDatabase().query(HistoryEntry.TABLE_NAME, history_projection,
				selection, selectionArgs, null, null, sortOrder);
	}
	
	public synchronized long queryHistoryTimeStamp(String uuid){

		String selection = HistoryEntry.COLUMN_HISTORY_UUID + " =? ";
		String[] selectionArgs={uuid};
		String sortOrder = HistoryEntry.COLUMN_HISTORY_START_TIME + " DESC";
		Cursor cursor = getReadableDatabase().query(HistoryEntry.TABLE_NAME, history_projection,
				selection, selectionArgs, null, null, sortOrder);
		if(cursor.getCount()>0){
			return cursor.getInt(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_START_TIME));
		}else{
			return System.currentTimeMillis()/1000;
		}
	}
	
	public synchronized long queryLastHistoryTimeStamp() {

		String sortOrder = HistoryEntry.COLUMN_HISTORY_START_TIME + " ASC";
		Cursor cursor = getReadableDatabase().query(HistoryEntry.TABLE_NAME, history_projection,
				null, null, null, null, sortOrder);
		
//		while(!cursor.isLast()){
//			Log.d("kits new","history:"+cursor.getInt(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_START_TIME)));
//			cursor.moveToNext();
//		}
		if(cursor.getCount()>0){
			cursor.moveToFirst();
			return cursor.getInt(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_START_TIME));
		}else 
			return System.currentTimeMillis()/1000;
	}
	/**
	 * Insert a history entry.
	 * 
	 * @param history
	 * @return
	 */
	public synchronized long insertHistory(History history) {
		ContentValues values = new ContentValues();
//		Log.d("kits new", "startTime:"+history.getStartTime());
		values.put(HistoryEntry.COLUMN_HISTORY_UUID, history.getHisUUID());
		values.put(HistoryEntry.COLUMN_HISTORY_NUMBER, history.getNumber());
		values.put(HistoryEntry.COLUMN_HISTORY_NAME, history.getName());
		values.put(HistoryEntry.COLUMN_HISTORY_START_TIME, history.getStartTime());
		values.put(HistoryEntry.COLUMN_HISTORY_END_TIME, history.getEndTime());
		values.put(HistoryEntry.COLUMN_HISTORY_DURATION, history.getDuration());
		values.put(HistoryEntry.COLUMN_HISTORY_STATUS, history.getStatus());
		values.put(HistoryEntry.COLUMN_HISTORY_PEERUUID, history.getPeerUUID());
		values.put(HistoryEntry.COLUMN_HISTORY_UNITUUID, history.getGroupUUID());
		values.put(HistoryEntry.COLUMN_HISTORY_UPLOAD, history.isUpload()?1:0);
		//check exist before insert
		String selection = HistoryEntry.COLUMN_HISTORY_UUID + " = ? ";
		String[] selectionArgs = {history.getHisUUID()};
		String sortOrder = HistoryEntry.COLUMN_HISTORY_START_TIME + " ASC";
		Cursor historyCursor = getReadableDatabase().query(HistoryEntry.TABLE_NAME, history_projection,
				selection, selectionArgs, null, null, sortOrder);
		if(historyCursor.getCount()<=0)
			return getWritableDatabase().insert(HistoryEntry.TABLE_NAME, null,
					values);
		else
			return -1;
	}

	public synchronized int updateHistoryHisUuid(String oldHisUuid, String newHisUuid){
		if(oldHisUuid.equals(newHisUuid)){
				String where = HistoryEntry.COLUMN_HISTORY_UUID + " = ? ";
				String[] whereArgs = {oldHisUuid};
				ContentValues values = new ContentValues();
				values.put(HistoryEntry.COLUMN_HISTORY_UPLOAD, 1);
				int row = getWritableDatabase().update(HistoryEntry.TABLE_NAME, values, where, whereArgs);	
				return row;
		}else{
				String where = HistoryEntry.COLUMN_HISTORY_UUID + " = ? ";
				String[] whereArgs = {oldHisUuid};
				ContentValues values = new ContentValues();
				values.put(HistoryEntry.COLUMN_HISTORY_UUID, newHisUuid);
				values.put(HistoryEntry.COLUMN_HISTORY_UPLOAD, 1);
				int row = getWritableDatabase().update(HistoryEntry.TABLE_NAME, values, where, whereArgs);	
				return row;
		}
	}
	public synchronized long getNewestHistoryTime(){
		String[] projection = { HistoryEntry.COLUMN_HISTORY_START_TIME};
		String sortOrder = HistoryEntry.COLUMN_HISTORY_START_TIME + " DESC";
		Cursor cursor = getReadableDatabase().query(HistoryEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);
		if(cursor.getCount()>0)
		{
			cursor.moveToFirst();
			long date = cursor.getLong(cursor
					.getColumnIndex(HistoryEntry.COLUMN_HISTORY_START_TIME));
			return date;
		}
			return 0;
	}
	/**
	 * Remove all contacts.
	 */
	
	public synchronized void removeAll() {
		getWritableDatabase().delete(HistoryEntry.TABLE_NAME, null, null);
//		getWritableDatabase().execSQL(SQL_DELETE_ENTRIES);
	}
	
	public static class HistoryEntry implements BaseColumns {
		public static final String TABLE_NAME = "historyTable";
		public static final String COLUMN_HISTORY_UUID = "HISTORYUUID";
		public static final String COLUMN_HISTORY_UNITUUID = "UNITUUID";
		public static final String COLUMN_HISTORY_PEERUUID = "PEERUUID";
		public static final String COLUMN_HISTORY_NUMBER = "NUMBER";
		public static final String COLUMN_HISTORY_NAME = "NAME";
		public static final String COLUMN_HISTORY_START_TIME = "STARTTIME";
		public static final String COLUMN_HISTORY_END_TIME = "ENDTIME";
		public static final String COLUMN_HISTORY_DURATION = "DURATION";
		public static final String COLUMN_HISTORY_STATUS = "STATUS";
		public static final String COLUMN_HISTORY_UPLOAD = "UPLOAD";
		
	}
}
