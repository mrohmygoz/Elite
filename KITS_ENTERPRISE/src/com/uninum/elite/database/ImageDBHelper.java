package com.uninum.elite.database;

import java.util.List;

import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.image.BitmapProcess;
import com.uninum.elite.image.DrawCircularImage;
import com.uninum.elite.object.ContactGroup;
import com.uninum.elite.object.Image;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.provider.BaseColumns;
import android.util.Log;


public class ImageDBHelper extends SQLiteOpenHelper {
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String BYTEARRAY_TYPE = " BLOB";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + ImageEntry.TABLE_NAME + " (" + 
			ImageEntry._ID + " INTEGER PRIMARY KEY," + 
			ImageEntry.COLUMN_IMAGE_PIC_ID + TEXT_TYPE + COMMA_SEP +
			ImageEntry.COLUMN_IMAGE_GROUPUUID + TEXT_TYPE + COMMA_SEP +
			ImageEntry.COLUMN_IMAGE_CONTACTUUID + TEXT_TYPE + COMMA_SEP +
			ImageEntry.COLUMN_IMAGE_TIMESTAMP + INTEGER_TYPE + COMMA_SEP +
			ImageEntry.COLUMN_IMAGE_BYTEARRAY + BYTEARRAY_TYPE + " )";
	
	String[] image_projection = {
			ImageEntry._ID,
			ImageEntry.COLUMN_IMAGE_PIC_ID,
			ImageEntry.COLUMN_IMAGE_GROUPUUID,
			ImageEntry.COLUMN_IMAGE_CONTACTUUID,
			ImageEntry.COLUMN_IMAGE_TIMESTAMP,
			ImageEntry.COLUMN_IMAGE_BYTEARRAY
	};
	
	public static final int IMAGE_DATABASE_VERSION = 1;
	public static final String IMAGE_DATAABASE_NAME = "Image.db";		
	private static final String SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + ImageEntry.TABLE_NAME;
	private static ImageDBHelper instance;
	
	public static synchronized ImageDBHelper getInstance(Context context){
		if(instance == null){
			instance = new ImageDBHelper(context);
		}
		return instance;
	}
	private ImageDBHelper(Context context) {
		super(context, IMAGE_DATAABASE_NAME, null, IMAGE_DATABASE_VERSION);
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
//		int upgradeTo = oldVersion + 1;
//		while(newVersion >= upgradeTo){
//			boolean success = false;			
//			Log.d("ELITE","Upgrade DB"+ "oldVersion:" + oldVersion);
//			db.beginTransaction();
//			switch(oldVersion){
////			case 2:
////				db.execSQL(ADD_COLUMN_GROUP_TIMESTAMP);
////				success = true;
////				break;
//			default :
//				db.execSQL(SQL_DELETE_ENTRIES);
//				onCreate(db);
//				success = true;
//				break;
//			}				
//			if(success){
//				db.setTransactionSuccessful();
//			}
//			db.endTransaction();
//			upgradeTo++;
//			}
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
		}
	public synchronized void insertImage(Image image){
			ContentValues values = new ContentValues();
			values.put(ImageEntry.COLUMN_IMAGE_PIC_ID, image.getPicID());
			values.put(ImageEntry.COLUMN_IMAGE_GROUPUUID, image.getGroupUUID());
			values.put(ImageEntry.COLUMN_IMAGE_TIMESTAMP, image.getTimeStamp());
			values.put(ImageEntry.COLUMN_IMAGE_CONTACTUUID, image.getContactUUID());
			values.put(ImageEntry.COLUMN_IMAGE_BYTEARRAY, BitmapProcess.BitmapToByte(image.getImageBitmap()));
			long result = getWritableDatabase().insert(ImageEntry.TABLE_NAME, null, values);
			Log.d("kits new","insertImage:"+image.getPicID() + "unitUUID:"+image.getGroupUUID() + "  contactUUID:"+ image.getContactUUID() +
					 " timeStamp:"+ image.getTimeStamp() +" success:"+ result);	
	}
	public synchronized void insertImage(List<Image> imageList){
		for(int i=0;i<imageList.size();i++){
			ContentValues values = new ContentValues();
			values.put(ImageEntry.COLUMN_IMAGE_PIC_ID, imageList.get(i).getPicID());
			values.put(ImageEntry.COLUMN_IMAGE_GROUPUUID, imageList.get(i).getGroupUUID());
			values.put(ImageEntry.COLUMN_IMAGE_TIMESTAMP, imageList.get(i).getTimeStamp());
			values.put(ImageEntry.COLUMN_IMAGE_CONTACTUUID, imageList.get(i).getContactUUID());
			values.put(ImageEntry.COLUMN_IMAGE_BYTEARRAY, BitmapProcess.BitmapToByte(imageList.get(i).getImageBitmap()));
			getWritableDatabase().insert(ImageEntry.TABLE_NAME, null, values);
			Log.d("kits new","insert:"+imageList.get(i).getPicID());
		}
		
	}
	public synchronized void updateImage(Image image){
		String where = ImageEntry.COLUMN_IMAGE_PIC_ID + " = ? AND "+ ImageEntry.COLUMN_IMAGE_GROUPUUID + " = ? ";
		String[] whereArgs = {image.getPicID(), image.getGroupUUID()};
		ContentValues values = new ContentValues();
		values.put(ImageEntry.COLUMN_IMAGE_PIC_ID, image.getPicID());
		values.put(ImageEntry.COLUMN_IMAGE_GROUPUUID, image.getGroupUUID());
		values.put(ImageEntry.COLUMN_IMAGE_TIMESTAMP, image.getTimeStamp());
		values.put(ImageEntry.COLUMN_IMAGE_CONTACTUUID , image.getContactUUID());
		values.put(ImageEntry.COLUMN_IMAGE_BYTEARRAY , BitmapProcess.BitmapToByte(image.getImageBitmap()));
		getWritableDatabase().update(ImageEntry.TABLE_NAME, values, where, whereArgs);
	}
	
	public synchronized Bitmap queryImage(String groupUUID, String picID) {
		String selection = ImageEntry.COLUMN_IMAGE_PIC_ID + " = ? AND "+ ImageEntry.COLUMN_IMAGE_GROUPUUID + " = ? ";
		String[] selectionArgs = {picID, groupUUID};
		Cursor cursor =  getReadableDatabase().query(ImageEntry.TABLE_NAME, image_projection,
				selection, selectionArgs, null, null, null);
		if(cursor!=null && cursor.getCount()>0){
			cursor.moveToFirst();
			Log.d("ELITE", "queryimage success");
			return BitmapProcess.BytesToBitmap(cursor.getBlob(cursor.getColumnIndex(ImageEntry.COLUMN_IMAGE_BYTEARRAY)));
		}else{
			Log.d("ELITE", "queryimage NULL");
			return null;
		}
	}
	
	public synchronized long queryImageTimeStamp(String groupUUID, String picID) {
		String selection = ImageEntry.COLUMN_IMAGE_PIC_ID + " = ? AND "+ ImageEntry.COLUMN_IMAGE_GROUPUUID + " = ? ";
		String[] selectionArgs = {picID, groupUUID};
		Cursor cursor =  getReadableDatabase().query(ImageEntry.TABLE_NAME, image_projection,
				selection, selectionArgs, null, null, null);
		if(cursor!=null && cursor.getCount()>0){
			cursor.moveToFirst();
			return cursor.getLong(cursor.getColumnIndex(ImageEntry.COLUMN_IMAGE_TIMESTAMP));
		}else{
			return 0;
		}
	}
	
	public synchronized void deleteAllImage(){
		getWritableDatabase().delete(ImageEntry.TABLE_NAME, null, null);
	}
	public synchronized void deleteImage(String groupUUID, String picID) {
		
		String where= ImageEntry.COLUMN_IMAGE_PIC_ID + " = ? AND "+ ImageEntry.COLUMN_IMAGE_GROUPUUID + " = ? ";
		String[] whereArgs =  {picID, groupUUID};
		getWritableDatabase().delete(GroupEntry.TABLE_NAME, where, whereArgs);
	}
	
	public final class ImageEntry implements BaseColumns{
		public static final String TABLE_NAME = "ImageTable";
		public static final String COLUMN_IMAGE_PIC_ID = "PICID";
		public static final String COLUMN_IMAGE_GROUPUUID = "GROUPUUID";
		public static final String COLUMN_IMAGE_TIMESTAMP = "TIMESTAMP";
		public static final String COLUMN_IMAGE_CONTACTUUID = "CONTACTUUID";
		public static final String COLUMN_IMAGE_BYTEARRAY = "IMAGE_BYTEARRAY";
	}
}
