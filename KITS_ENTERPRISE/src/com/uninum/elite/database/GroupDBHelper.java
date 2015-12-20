package com.uninum.elite.database;

import java.util.List;





import com.uninum.elite.object.ContactGroup;
import com.uninum.elite.object.SingleMessage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.Contacts.GroupsColumns;
import android.util.Log;

public class GroupDBHelper extends SQLiteOpenHelper {
		private static final String TEXT_TYPE = " TEXT";
		private static final String INTEGER_TYPE = " INTEGER";
		private static final String COMMA_SEP = ",";
		private static final String SQL_CREATE_ENTRIES =
				"CREATE TABLE " + GroupEntry.TABLE_NAME + " (" + 
				GroupEntry._ID + " INTEGER PRIMARY KEY," + 
				GroupEntry.COLUMN_GROUP_NAME + TEXT_TYPE + COMMA_SEP +
				GroupEntry.COLUMN_GROUP_UUID + TEXT_TYPE + COMMA_SEP +
				GroupEntry.COLUMN_GROUP_TYPE + TEXT_TYPE + COMMA_SEP +
				GroupEntry.COLUMN_GROUP_POINT + TEXT_TYPE + COMMA_SEP +
				GroupEntry.COLUMN_GROUP_LEADUUID + TEXT_TYPE + COMMA_SEP +
				GroupEntry.COLUMN_GROUP_SELFUUID + TEXT_TYPE + COMMA_SEP +
				GroupEntry.COLUMN_GROUP_LEAD + INTEGER_TYPE + COMMA_SEP +
				GroupEntry.COLUMN_GROUP_PRIVACY + TEXT_TYPE + COMMA_SEP +
				GroupEntry.COLUMN_GROUP_UPDATE + INTEGER_TYPE + COMMA_SEP +
				GroupEntry.COLUMN_GROUP_TIMESTAMP + INTEGER_TYPE + " )";
		
		public static String[] group_projection = {
				GroupEntry._ID,
				GroupEntry.COLUMN_GROUP_NAME,
				GroupEntry.COLUMN_GROUP_UUID,
				GroupEntry.COLUMN_GROUP_TYPE,
				GroupEntry.COLUMN_GROUP_POINT,
				GroupEntry.COLUMN_GROUP_LEAD,
				GroupEntry.COLUMN_GROUP_LEADUUID,
				GroupEntry.COLUMN_GROUP_SELFUUID,
				GroupEntry.COLUMN_GROUP_PRIVACY,
				GroupEntry.COLUMN_GROUP_UPDATE,
				GroupEntry.COLUMN_GROUP_TIMESTAMP
		};

		public final class GroupEntry implements BaseColumns{
			public static final String TABLE_NAME = "GroupTable";
			public static final String COLUMN_GROUP_NAME = "NAME";
			public static final String COLUMN_GROUP_UUID = "UUID";
			public static final String COLUMN_GROUP_TYPE = "TYPE";
			public static final String COLUMN_GROUP_POINT = "POINT";
			public static final String COLUMN_GROUP_LEAD = "LEAD";
			public static final String COLUMN_GROUP_LEADUUID = "LEADUUID";//leader phone number
			public static final String COLUMN_GROUP_SELFUUID = "SELFUUID"; //self's contactUUID
			public static final String COLUMN_GROUP_PRIVACY = "PRIVACY"; //self's privacy
			public static final String COLUMN_GROUP_UPDATE = "NEW_UPDATE";
			public static final String COLUMN_GROUP_TIMESTAMP = "TIME_STAMP";
		}
		
		private String ADD_COLUMN_GROUP_UPDATE = " ALTER TABLE "+ GroupEntry.TABLE_NAME + " ADD COLUMN "+ GroupEntry.COLUMN_GROUP_UPDATE + INTEGER_TYPE ;
		private String ADD_COLUMN_GROUP_TIMESTAMP = " ALTER TABLE "+ GroupEntry.TABLE_NAME + " ADD COLUMN "+ GroupEntry.COLUMN_GROUP_TIMESTAMP + INTEGER_TYPE ;
		
		public static final int GROUP_DATABASE_VERSION = 3;
		public static final String GROUP_DATAABASE_NAME = "ContactGroup.db";		
		private static final String SQL_DELETE_ENTRIES =
				"DROP TABLE IF EXISTS " + GroupEntry.TABLE_NAME;
		private static GroupDBHelper instance;
		
		
		public static synchronized GroupDBHelper getInstance(Context context){
			if(instance == null){
				instance = new GroupDBHelper(context);
			}
			return instance;
		}
		
		private GroupDBHelper(Context context) {
			super(context, GROUP_DATAABASE_NAME, null, GROUP_DATABASE_VERSION);
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
			Log.d("Jenny","Group DB Upgrade Version:"+ oldVersion + " New Version:"+ newVersion);
			switch(oldVersion){
			case 1:
				db.execSQL(ADD_COLUMN_GROUP_UPDATE);
				db.execSQL(ADD_COLUMN_GROUP_TIMESTAMP);
				break;
			case 2:
				db.execSQL(ADD_COLUMN_GROUP_TIMESTAMP);
				break;
			default :
				db.execSQL(SQL_DELETE_ENTRIES);
		        onCreate(db);
		        break;
			}
		}
		
		public synchronized void insertGroup(List<ContactGroup> groups){
			for(int i=0;i<groups.size();i++){
				ContentValues values = new ContentValues();
				values.put(GroupEntry.COLUMN_GROUP_NAME, groups.get(i).getGroupName());
				values.put(GroupEntry.COLUMN_GROUP_UUID, groups.get(i).getGroupUUID());
				values.put(GroupEntry.COLUMN_GROUP_TYPE, groups.get(i).getGroupType());
				values.put(GroupEntry.COLUMN_GROUP_POINT, groups.get(i).getGroupPoint());
				values.put(GroupEntry.COLUMN_GROUP_LEADUUID, groups.get(i).getGroupLeadUUID());
				values.put(GroupEntry.COLUMN_GROUP_LEAD, groups.get(i).isGroupLead()?1:0);
				values.put(GroupEntry.COLUMN_GROUP_SELFUUID, groups.get(i).getSelfUUID());
				values.put(GroupEntry.COLUMN_GROUP_PRIVACY, groups.get(i).getGroupPrivacy());
				values.put(GroupEntry.COLUMN_GROUP_UPDATE, groups.get(i).isGroupNewUpdate()?1:0);
				values.put(GroupEntry.COLUMN_GROUP_TIMESTAMP, groups.get(i).getGroupTimeStamp());
				getWritableDatabase().insert(GroupEntry.TABLE_NAME, null, values);
				Log.d("kits new","insert:"+groups.get(i).getGroupName());
			}
			
		}
		
		public synchronized void updateGroup(ContactGroup group){
			String where = GroupEntry.COLUMN_GROUP_UUID + " = ? ";
			String[] whereArgs = {group.getGroupUUID()};
			ContentValues values = new ContentValues();
			values.put(GroupEntry.COLUMN_GROUP_NAME, group.getGroupName());
			values.put(GroupEntry.COLUMN_GROUP_TYPE, group.getGroupType());
			values.put(GroupEntry.COLUMN_GROUP_POINT, group.getGroupPoint());
			values.put(GroupEntry.COLUMN_GROUP_TYPE, group.getGroupType());
			values.put(GroupEntry.COLUMN_GROUP_LEAD, group.isGroupLead()?1:0);
			values.put(GroupEntry.COLUMN_GROUP_LEADUUID, group.getGroupLeadUUID());
			values.put(GroupEntry.COLUMN_GROUP_SELFUUID, group.getSelfUUID());
			values.put(GroupEntry.COLUMN_GROUP_PRIVACY, group.getGroupPrivacy());
			values.put(GroupEntry.COLUMN_GROUP_UPDATE, group.isGroupNewUpdate()?1:0);
			values.put(GroupEntry.COLUMN_GROUP_TIMESTAMP, group.getGroupTimeStamp());
			Log.d("ELITE","UPDATE GROUP TIMESTAMP:"+ group.getGroupTimeStamp());
			getWritableDatabase().update(GroupEntry.TABLE_NAME, values, where, whereArgs);
		}
		
		public synchronized void updateGroupPrivacy(String groupUUID, boolean setPrivate){
			String where = GroupEntry.COLUMN_GROUP_UUID + " = ? ";
			String[] whereArgs = {groupUUID};
			ContentValues values = new ContentValues();
			if(setPrivate){
				values.put(GroupEntry.COLUMN_GROUP_PRIVACY, 1);
			}else{
				values.put(GroupEntry.COLUMN_GROUP_PRIVACY, 0);
			}		
			getWritableDatabase().update(GroupEntry.TABLE_NAME, values, where, whereArgs);
		}
		
		public synchronized void updateGroupUpdate(String groupUUID, boolean newUpdate){
			String where = GroupEntry.COLUMN_GROUP_UUID + " = ? ";
			String[] whereArgs = {groupUUID};
			ContentValues values = new ContentValues();
			if(newUpdate){
				values.put(GroupEntry.COLUMN_GROUP_UPDATE, 1);
			}else{
				values.put(GroupEntry.COLUMN_GROUP_UPDATE, 0);
			}		
			getWritableDatabase().update(GroupEntry.TABLE_NAME, values, where, whereArgs);
		}
		
		public synchronized Cursor queryAllGroup(){
			// sort by GroupType (distinguish to Two type, Glory & Normal)then Lead then Name
			String sortOrder = 	
					"(CASE "+ GroupEntry.COLUMN_GROUP_TYPE + 
			" WHEN '" + ContactGroup.TYPE_GLORY+  "' THEN '1' " +
			" WHEN '" + ContactGroup.TYPE_GLORY_MULTI +  "' THEN '1' " +
			" WHEN '" + ContactGroup.TYPE_GLORY_UNI +  "' THEN '1' " +
            " ELSE 100" + " END) DESC " +"," +
            "(CASE "+ GroupEntry.COLUMN_GROUP_LEAD + 
			" WHEN '1'" +  " THEN '1' " +
            " ELSE 100" + " END) DESC " + "," +
			GroupEntry.COLUMN_GROUP_NAME + " COLLATE LOCALIZED ASC";
				/*	GroupEntry.COLUMN_GROUP_TYPE + " ASC,"+
									GroupEntry.COLUMN_GROUP_LEAD + " DESC,"+	
											GroupEntry.COLUMN_GROUP_NAME + " ASC"; */
			return getReadableDatabase().query(GroupEntry.TABLE_NAME, group_projection,
					null, null, null, null, sortOrder);	
		}

		public synchronized long queryGroupTimeStamp(String groupUUID){
			String selection = GroupEntry.COLUMN_GROUP_UUID + " = ?";
			String[] selectionArgs = {groupUUID};
			String[] progection = {
					GroupEntry.COLUMN_GROUP_TIMESTAMP
			};
			Cursor cursor =  getReadableDatabase().query(GroupEntry.TABLE_NAME, progection, selection, selectionArgs, null, null, null);
			
			if(cursor!=null&&cursor.getCount()>0){
				cursor.moveToFirst();
				return cursor.getLong(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_TIMESTAMP));
			}else{
				return 0;
			}
		}
		
		public synchronized String queryGroupPrivacy(String groupUUID){
			String selection = GroupEntry.COLUMN_GROUP_UUID + " = ?";
			String[] selectionArgs = {groupUUID};
			String[] progection = {
					GroupEntry.COLUMN_GROUP_PRIVACY
			};
			Cursor cursor =  getReadableDatabase().query(GroupEntry.TABLE_NAME, progection,
					selection, selectionArgs, null, null, null);
			if(cursor!=null&&cursor.getCount()>0){
				cursor.moveToFirst();
				return cursor.getString(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_PRIVACY));
			}else
				return null;			
		}
		
		public synchronized Cursor queryGroupUUID(String groupUUID){
			String selection = GroupEntry.COLUMN_GROUP_UUID + " = ?";
			String[] selectionArgs = {groupUUID};
			Cursor cursor =  getReadableDatabase().query(GroupEntry.TABLE_NAME, group_projection,
					selection, selectionArgs, null, null, null);
			if(cursor!=null)
				return cursor;
			else
				return null;			
		}
		
		public synchronized String queryGroupUUID (int id){
			String selection = GroupEntry._ID + " = ?";
			String[] selectionArgs = {Integer.toString(id)};
			Cursor cursor =  getReadableDatabase().query(GroupEntry.TABLE_NAME, group_projection,
					selection, selectionArgs, null, null, null);
			if (cursor.moveToFirst())
				return cursor.getString(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_UUID));
			else
				return null;
		}
		
		public synchronized String queryGroupLead(String groupUUID){
			String selection = GroupEntry.COLUMN_GROUP_UUID + " = ?";
			String[] selectionArgs = {groupUUID};
			Cursor cursor =  getReadableDatabase().query(GroupEntry.TABLE_NAME, group_projection,
					selection, selectionArgs, null, null, null);
			if(cursor!=null && cursor.getCount()>0){
				cursor.moveToFirst();
				return cursor.getString(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_LEADUUID));
			}else
				return null;			
		}
		
		public synchronized int queryGroupID (String groupUUID) {
			int id;
			String selection = GroupEntry.COLUMN_GROUP_UUID + " = ?";
			String[] selectionArgs = {groupUUID};
			Cursor cursor =  getReadableDatabase().query(GroupEntry.TABLE_NAME, group_projection,
					selection, selectionArgs, null, null, null);
			
			if (cursor.moveToFirst()) {
				id = cursor.getInt(cursor.getColumnIndex(GroupEntry._ID));
				return id;
			}
			
			return -1;
		}
		
		public synchronized boolean isSelf (String groupUUID, String selfUUID){
			String selection = GroupEntry.COLUMN_GROUP_UUID + " = ?";
			String[] selectionArgs = {groupUUID};
			boolean isSelf = false;
			Cursor cursor =  getReadableDatabase().query(GroupEntry.TABLE_NAME,group_projection,
					selection, selectionArgs, null, null, null);
			if (cursor.moveToFirst())
				if (  selfUUID.equals(cursor.getString(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_SELFUUID)))  )
					return true;
			return isSelf;
		}
		
		public synchronized void deleteAllGroup(){
			getWritableDatabase().delete(GroupEntry.TABLE_NAME, null, null);
		}
		
		public synchronized void deleteGroup(String groupUUID){
			String where= GroupEntry.COLUMN_GROUP_UUID + " = ? ";
			String[] whereArgs = {groupUUID};
			getWritableDatabase().delete(GroupEntry.TABLE_NAME, where, whereArgs);
		}

}
