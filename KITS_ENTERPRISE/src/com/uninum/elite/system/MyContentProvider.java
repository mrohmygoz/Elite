package com.uninum.elite.system;

import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.database.MessageDBHelper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class MyContentProvider extends ContentProvider {

	public static final String AUTHORITY = "com.uninum.elite.system";
	private static UriMatcher mUriMatcher;
	public static Uri CONTENT_URI;
	private MessageDBHelper msgDB;
	private GroupDBHelper groupDB;
	
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		CONTENT_URI = Uri.parse("content://" + AUTHORITY);
		mUriMatcher.addURI(AUTHORITY, "", 0);
		mUriMatcher.addURI(AUTHORITY, GroupEntry.TABLE_NAME, 100);
		Log.d("Jenny", "content provider: create");
		msgDB = MessageDBHelper.getInstance(getContext());
		groupDB = GroupDBHelper.getInstance(getContext());
		
//		Cursor cur = groupDB.queryAllGroup();
//		String groupuuid;
//		if (cur.moveToFirst()) {
//			do {
//				groupuuid = cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_UUID));
//				msgDB.deleteAllMessage(groupuuid);
//			} while (cur.moveToNext());
//		}
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		switch (mUriMatcher.match(uri)){
		case 100:
			Log.d("Jenny", "content provider: query");
			Cursor cursor = groupDB.queryAllGroup();
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		default:
			Log.d("Jenny", "content provider: query");
			int id = mUriMatcher.match(uri);
//			String table = "Table" + id;
			String table = groupDB.queryGroupUUID(id);
			if (table!=null) {
				Cursor cur = msgDB.queryAllMessage(table);
				cur.setNotificationUri(getContext().getContentResolver(), uri);
				return cur;
			}
			Log.e("Jenny", "query uri null");
			return null;
		}
	}

	@Override
	public Bundle call(String method, String groupUUID, Bundle extras) {
		// TODO Auto-generated method stub
		switch (method){
		case "addURI":
			//int id = Integer.valueOf(tableName.substring(5));
			int id = groupDB.queryGroupID(groupUUID);
			if (id != -1)
				mUriMatcher.addURI(AUTHORITY, groupUUID, id);
			else
				Log.e("Jenny", "add uri: id = -1");
			break;
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		Log.d("Jenny", "content provider: get type");
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (mUriMatcher.match(uri)) {
		case 100:
			long groupid = groupDB.getWritableDatabase().insert(GroupEntry.TABLE_NAME, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse("content://" + AUTHORITY + "/" + GroupEntry.TABLE_NAME + "/" + groupid);
		default:
			int tableid = mUriMatcher.match(uri);
//			String table = "Table" + tableid;
			String table = groupDB.queryGroupUUID(tableid);
			if (table!=null) {
				long id = msgDB.getWritableDatabase().insert("'"+table+"'", null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return Uri.parse("content://" + AUTHORITY + "/" + table + "/" + id);
			}
			Log.e("Jenny", "insert uri null");
			return null;
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		switch (mUriMatcher.match(uri)) {
		case 100:
			return 0;
		default:
			int tableid = mUriMatcher.match(uri);
//			String table = "Table" + tableid;
			String table = groupDB.queryGroupUUID(tableid);
			msgDB.getWritableDatabase().delete("'"+table+"'", selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return 0;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		
		switch (mUriMatcher.match(uri)) {
		case 100:
			return 0;
		default:
			int tableid = mUriMatcher.match(uri);
//			String table = "Table" + tableid;
			String table = groupDB.queryGroupUUID(tableid);
			int updateCount = msgDB.getWritableDatabase().update("'"+table+"'", values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return updateCount;
		}
	}

}
