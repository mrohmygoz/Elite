package com.uninum.elite.database;

import java.util.List;











import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.object.Contact;
import com.uninum.elite.object.ContactGroup;
import com.uninum.elite.object.History;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

public class ContactDBHelper extends SQLiteOpenHelper{
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + ContactEntry.TABLE_NAME + " (" + 
			ContactEntry._ID + " INTEGER PRIMARY KEY," + 
			ContactEntry.COLUMN_CONTACT_NAME + TEXT_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_PHONE + TEXT_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_PICTURE + TEXT_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_MVPN + INTEGER_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_BELONG + TEXT_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_UNITNAME + TEXT_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_UNITUUID + TEXT_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_CONTACTUUID + TEXT_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_UNITTYPE + TEXT_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_FAVORITE + INTEGER_TYPE + COMMA_SEP + 
			ContactEntry.COLUMN_CONTACT_LEADER + INTEGER_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_FREE + INTEGER_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_PRIVATE + INTEGER_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_STICKY + INTEGER_TYPE + COMMA_SEP +
			ContactEntry.COLUMN_CONTACT_NEWUPDATE + INTEGER_TYPE +" )";
	
	public static final int CONTACT_DATABASE_VERSION = 3;
	public static final String CONTACT_DATABASE_NAME = "Contact.db";		
	private static final String SQL_DELETE_TABLE =
			"DROP TABLE IF EXISTS " + ContactEntry.TABLE_NAME;
	private static final String SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + ContactEntry.TABLE_NAME;
	private static ContactDBHelper instance;
	
	String[] contact_projection = {
			ContactEntry._ID,
			ContactEntry.COLUMN_CONTACT_NAME,
			ContactEntry.COLUMN_CONTACT_PHONE,
			ContactEntry.COLUMN_CONTACT_PICTURE,
			ContactEntry.COLUMN_CONTACT_MVPN,
			ContactEntry.COLUMN_CONTACT_BELONG,
			ContactEntry.COLUMN_CONTACT_UNITNAME,
			ContactEntry.COLUMN_CONTACT_UNITUUID,
			ContactEntry.COLUMN_CONTACT_CONTACTUUID,
			ContactEntry.COLUMN_CONTACT_UNITTYPE,
			ContactEntry.COLUMN_CONTACT_FAVORITE,
			ContactEntry.COLUMN_CONTACT_LEADER,
			ContactEntry.COLUMN_CONTACT_FREE,
			ContactEntry.COLUMN_CONTACT_PRIVATE,
			ContactEntry.COLUMN_CONTACT_STICKY,
			ContactEntry.COLUMN_CONTACT_NEWUPDATE
	};
	private String ADD_COLUMN_CONTACT_STICKY = " ALTER TABLE "+ ContactEntry.TABLE_NAME + " ADD COLUMN "+ ContactEntry.COLUMN_CONTACT_STICKY + INTEGER_TYPE ;
	private String ADD_COLUMN_CONTACT_NEWUPDATE = " ALTER TABLE "+ ContactEntry.TABLE_NAME + " ADD COLUMN "+ ContactEntry.COLUMN_CONTACT_NEWUPDATE + INTEGER_TYPE ;
	public static synchronized ContactDBHelper getInstance(Context context){
		if(instance == null){
			instance = new ContactDBHelper(context);
		}
		return instance;
	}
	private ContactDBHelper(Context context) {
		super(context, CONTACT_DATABASE_NAME, null, CONTACT_DATABASE_VERSION);
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
		Log.i("ELITE","Contact DB Upgrade Version:"+ oldVersion + " New Version:"+ newVersion);
		switch(oldVersion){
		case 1:
			db.execSQL(ADD_COLUMN_CONTACT_STICKY);
			db.execSQL(ADD_COLUMN_CONTACT_NEWUPDATE);
			break;
		case 2:
			db.execSQL(ADD_COLUMN_CONTACT_NEWUPDATE);
			break;
		default :
			db.execSQL(SQL_DELETE_ENTRIES);
	        onCreate(db);
	        break;
		}
	}
	
	public synchronized void insertContacts(List<Contact> contacts){
//		deleteAllContacts(); // for testing
//		deleteGroupContacts(contacts.get(0).getUnitname());
		for(int i=0;i<contacts.size();i++){
			ContentValues values = new ContentValues();
			values.put(ContactEntry.COLUMN_CONTACT_NAME, contacts.get(i).getName());
			values.put(ContactEntry.COLUMN_CONTACT_PHONE, contacts.get(i).getPhone());
			values.put(ContactEntry.COLUMN_CONTACT_PICTURE, contacts.get(i).getPicture());
			values.put(ContactEntry.COLUMN_CONTACT_BELONG, contacts.get(i).getBelong());
			values.put(ContactEntry.COLUMN_CONTACT_UNITNAME, contacts.get(i).getUnitname());
			values.put(ContactEntry.COLUMN_CONTACT_UNITUUID, contacts.get(i).getUnitUUID());
			values.put(ContactEntry.COLUMN_CONTACT_UNITTYPE, contacts.get(i).getUnitType());
			values.put(ContactEntry.COLUMN_CONTACT_CONTACTUUID, contacts.get(i).getContactUUID());
			values.put(ContactEntry.COLUMN_CONTACT_MVPN, contacts.get(i).isMvpn()? 1:0);
			values.put(ContactEntry.COLUMN_CONTACT_LEADER, contacts.get(i).isLeader()? 1:0);
			values.put(ContactEntry.COLUMN_CONTACT_FAVORITE, contacts.get(i).isFavorite()? 1:0);
			values.put(ContactEntry.COLUMN_CONTACT_FREE, contacts.get(i).isFree()? 1:0);
			values.put(ContactEntry.COLUMN_CONTACT_PRIVATE, contacts.get(i).isPrivate()? 1:0);
			values.put(ContactEntry.COLUMN_CONTACT_STICKY, contacts.get(i).isSticky()? 1:0);
			values.put(ContactEntry.COLUMN_CONTACT_NEWUPDATE, contacts.get(i).isNewUpdate()? 1:0);
			getWritableDatabase().insert(ContactEntry.TABLE_NAME, null, values);
			Log.d("kits new", "insert:"+contacts.get(i).getName());
		}
		
	}
	public synchronized Cursor queryContacts(String unitUUID){

		String contactSelection = ContactEntry.COLUMN_CONTACT_UNITUUID
				+ " LIKE ? ";
		String[] contactSelectionArgs = { "%" + unitUUID + "%" };
		String contactSortOrder =  ContactEntry.COLUMN_CONTACT_STICKY + " DESC, " 
//				+ ContactEntry.COLUMN_CONTACT_LEADER + " DESC, "
				+ ContactEntry.COLUMN_CONTACT_NAME + " COLLATE LOCALIZED ASC";
		
		Cursor cursor = getReadableDatabase().query(ContactEntry.TABLE_NAME, contact_projection,
				contactSelection, contactSelectionArgs, null, null, contactSortOrder);
		cursor.moveToFirst();
		return cursor;
	}
	public synchronized Cursor queryAllContacts(){

		//String sortOrder = ContactEntry.COLUMN_GROUP_NAME + " DESC";
		Cursor cursor = getReadableDatabase().query(ContactEntry.TABLE_NAME, contact_projection,
				null, null, null, null, null);
		return cursor;
	}
	
	public synchronized Cursor queryContactBySearch(String searchString){
		return getReadableDatabase().rawQuery("SELECT * FROM "+
				ContactEntry.TABLE_NAME+ " WHERE "+
				ContactEntry.COLUMN_CONTACT_NAME + " LIKE ? "+
				" UNION SELECT * FROM "+ ContactEntry.TABLE_NAME + " WHERE "+
				ContactEntry.COLUMN_CONTACT_PHONE + " LIKE ? " +
				"ORDER BY "+ ContactEntry.COLUMN_CONTACT_NAME ,
				new String[]{"%"+searchString+"%","%"+searchString+"%"});
	}
	
	public synchronized Cursor queryContactByName (String name, String groupuuid){
		String selection = ContactEntry.COLUMN_CONTACT_NAME + " LIKE ? AND " + ContactEntry.COLUMN_CONTACT_UNITUUID + " = ? ";
		String selectionArgs[] = {"%"+name+"%",groupuuid};
		return getReadableDatabase().query(ContactEntry.TABLE_NAME, contact_projection, selection, selectionArgs, null, null, null);
	}
	
	public synchronized Cursor queryFavoriteContacts(){
		String selections = ContactEntry.COLUMN_CONTACT_FAVORITE + " = 1 ";
		//sort by GroupType (distinguish to Two type, Glory & Normal)then Name
		String contactSortOrder =  "(CASE "+ ContactEntry.COLUMN_CONTACT_UNITTYPE + 
				" WHEN '" + ContactGroup.TYPE_GLORY+  "' THEN '1' " +
				" WHEN '" + ContactGroup.TYPE_GLORY_MULTI +  "' THEN '1' " +
				" WHEN '" + ContactGroup.TYPE_GLORY_UNI +  "' THEN '1' " +
	            " ELSE 100" + " END) DESC " +"," +
				ContactEntry.COLUMN_CONTACT_NAME + " COLLATE LOCALIZED ASC";		
		return getReadableDatabase().query(ContactEntry.TABLE_NAME, 
				contact_projection, selections, null, null, null, contactSortOrder);
		
	}
// need modify *********************	
	public synchronized Cursor queryContact(String contactUUID, String groupUUID){
//		Log.d("kits new", "queryContact:"+contactUUID +" groupUUID:"+groupUUID);
		String selections = ContactEntry.COLUMN_CONTACT_CONTACTUUID + " LIKE ? AND " + ContactEntry.COLUMN_CONTACT_UNITUUID + " LIKE ? ";
		String[] selectionsArg = {"%"+contactUUID+"%", "%"+groupUUID+"%"};
		Cursor cursor = getReadableDatabase().query(ContactEntry.TABLE_NAME, 
				contact_projection, selections, selectionsArg, null, null, null);
//		cursor.moveToFirst();
		return cursor;
	}
	
// need modify *****************	
	public synchronized boolean queryContactIsFavorite(String contactUUID, String groupUUID){
		String[] projection = {
				ContactEntry._ID,
				ContactEntry.COLUMN_CONTACT_FAVORITE,
				ContactEntry.COLUMN_CONTACT_NAME
		};
		String selections = ContactEntry.COLUMN_CONTACT_CONTACTUUID + " LIKE ? AND " + ContactEntry.COLUMN_CONTACT_UNITUUID + " LIKE ? ";
		String[] selectionsArg = {"%"+contactUUID+"%", "%"+groupUUID+"%"};
		Cursor cursor = getReadableDatabase().query(ContactEntry.TABLE_NAME, 
				projection, selections, selectionsArg, null, null, null);
		cursor.moveToFirst();
//		Log.d("kits new", "Cursor:"+cursor.getInt(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_FAVORITE)));
		if(cursor.getCount()>0 && cursor.getInt(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_FAVORITE))==1){
			cursor.close();
			return true;
		}
		cursor.close();
		return false;
	}
	
	public synchronized Cursor queryContactPicID(String contactUUID, String unitUUID){
		String selections = ContactEntry.COLUMN_CONTACT_CONTACTUUID + " LIKE ? AND " + ContactEntry.COLUMN_CONTACT_UNITUUID + " LIKE ? ";
		String[] selectionsArg = {"%"+contactUUID+"%", "%"+unitUUID+"%"};
		Cursor cursor = getReadableDatabase().query(ContactEntry.TABLE_NAME, 
				contact_projection, selections, selectionsArg, null, null, null);
		return cursor;
	}
	
	public synchronized void deleteAllContacts(){
		getWritableDatabase().delete(ContactEntry.TABLE_NAME, null, null);
	}
	
	public void deleteGroupContacts(String groupUUID){
		String where = ContactEntry.COLUMN_CONTACT_UNITUUID + " = ? ";
		String[] whereArgs = {groupUUID};
		getWritableDatabase().delete(ContactEntry.TABLE_NAME, 
				where, whereArgs);
		
	}
// need modify*****************	
	public void deleteGroupContact(String contactUUID, String groupUUID){
		String where = ContactEntry.COLUMN_CONTACT_UNITUUID + " = ? AND " + ContactEntry.COLUMN_CONTACT_CONTACTUUID + " = ? ";
		String[] whereArgs = {groupUUID, contactUUID};
		getWritableDatabase().delete(ContactEntry.TABLE_NAME, 
				where, whereArgs);		
	}
	

// need modify ********************
	public synchronized void updateContactFavorite(String contactUUID, String groupUUID, boolean yes){
		String where = ContactEntry.COLUMN_CONTACT_UNITUUID + " = ? AND " + ContactEntry.COLUMN_CONTACT_CONTACTUUID + " = ?";
		String[] whereArgs = {groupUUID, contactUUID};
		ContentValues values = new ContentValues();
			values.put(ContactEntry.COLUMN_CONTACT_FAVORITE, yes);
		int row = getWritableDatabase().update(ContactEntry.TABLE_NAME, values, where, whereArgs);
	}
	
	public synchronized void updateContactNewUpdate(String groupUUID, boolean isNewUpdate){
		String where = ContactEntry.COLUMN_CONTACT_UNITUUID + " = ? ";
		String[] whereArgs = {groupUUID};
		ContentValues values = new ContentValues();
		values.put(ContactEntry.COLUMN_CONTACT_NEWUPDATE, isNewUpdate);
		int row = getWritableDatabase().update(ContactEntry.TABLE_NAME, values, where, whereArgs);
		Log.d("ELITE","Update group contact DB row:"+row+ " update:"+String.valueOf(isNewUpdate));
	}
	
	public synchronized void updateContactNewUpdate(String contactUUID, String groupUUID, boolean isNewUpdate){
		String where = ContactEntry.COLUMN_CONTACT_UNITUUID + " = ? AND " + ContactEntry.COLUMN_CONTACT_CONTACTUUID + " = ? ";
		String[] whereArgs = {groupUUID, contactUUID};
		ContentValues values = new ContentValues();
			values.put(ContactEntry.COLUMN_CONTACT_NEWUPDATE, isNewUpdate);
		int row = getWritableDatabase().update(ContactEntry.TABLE_NAME, values, where, whereArgs);
		Log.d("ELITE","Update DB row:"+row+ " update:"+String.valueOf(isNewUpdate));
	}
	
	public synchronized void updateContact(Contact contact){
		String where = ContactEntry.COLUMN_CONTACT_CONTACTUUID + " = ? AND " + ContactEntry.COLUMN_CONTACT_UNITUUID + " = ?";
		String[] whereArgs = {contact.getContactUUID(),contact.getUnitUUID()};
		ContentValues values = new ContentValues();
		values.put(ContactEntry.COLUMN_CONTACT_NAME, contact.getName());
		values.put(ContactEntry.COLUMN_CONTACT_PHONE, contact.getPhone());
		values.put(ContactEntry.COLUMN_CONTACT_PICTURE, contact.getPicture());
		values.put(ContactEntry.COLUMN_CONTACT_BELONG, contact.getBelong());
		values.put(ContactEntry.COLUMN_CONTACT_UNITNAME, contact.getUnitname());
		values.put(ContactEntry.COLUMN_CONTACT_UNITUUID, contact.getUnitUUID());
		values.put(ContactEntry.COLUMN_CONTACT_UNITTYPE, contact.getUnitType());
		values.put(ContactEntry.COLUMN_CONTACT_CONTACTUUID, contact.getContactUUID());
		values.put(ContactEntry.COLUMN_CONTACT_MVPN, contact.isMvpn()? 1:0);
		values.put(ContactEntry.COLUMN_CONTACT_LEADER, contact.isLeader()? 1:0);
		values.put(ContactEntry.COLUMN_CONTACT_FREE, contact.isFree()? 1:0);
		values.put(ContactEntry.COLUMN_CONTACT_PRIVATE, contact.isPrivate()? 1:0);
		values.put(ContactEntry.COLUMN_CONTACT_STICKY, contact.isSticky()? 1:0);
		values.put(ContactEntry.COLUMN_CONTACT_NEWUPDATE, contact.isNewUpdate()? 1:0);
		getWritableDatabase().update(ContactEntry.TABLE_NAME, values, where, whereArgs);
		
	}

	public final class ContactEntry implements BaseColumns{

		
		public static final String TABLE_NAME = "ContactTable";
		public static final String COLUMN_CONTACT_NAME = "NAME";
		public static final String COLUMN_CONTACT_PHONE = "PHONE";
		public static final String COLUMN_CONTACT_PICTURE = "PICTURE";
		public static final String COLUMN_CONTACT_MVPN = "MVPN";
		public static final String COLUMN_CONTACT_BELONG = "BELONG";
		public static final String COLUMN_CONTACT_LEADER = "LEADER";
		public static final String COLUMN_CONTACT_FREE = "FREE";
		public static final String COLUMN_CONTACT_PRIVATE = "ISPRIVATE";
		public static final String COLUMN_CONTACT_UNITNAME = "UNITNAME";
		public static final String COLUMN_CONTACT_UNITTYPE = "UNITTYPE";
		public static final String COLUMN_CONTACT_FAVORITE = "FAVORITE";
		public static final String COLUMN_CONTACT_UNITUUID = "UNIT_UUID";
		public static final String COLUMN_CONTACT_CONTACTUUID = "CONTACTUUID";
		public static final String COLUMN_CONTACT_STICKY = "ISSTICKY";
		public static final String COLUMN_CONTACT_NEWUPDATE = "NEW_UPDATE";
	}
}
