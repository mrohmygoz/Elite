package com.uninum.elite.object;

import com.uninum.elite.database.ImageDBHelper.ImageEntry;
import com.uninum.elite.image.BitmapProcess;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;

public class Image {
	private String picID;
	private String groupUUID;
	private String contactUUID;
	private Bitmap imageBitmap;
	private long timeStamp;
	
	public Image(String picID, String groupUUID, String contactUUID, Bitmap imageBitmap, long timeStamp){
		this.picID = picID;
		this.groupUUID = groupUUID;
		this.contactUUID = contactUUID;
		this.imageBitmap = imageBitmap;
		this.timeStamp = timeStamp;
	}
	public Image(Cursor cursor){
		this.picID = cursor.getString(cursor.getColumnIndex(ImageEntry.COLUMN_IMAGE_PIC_ID));
		this.groupUUID = cursor.getString(cursor.getColumnIndex(ImageEntry.COLUMN_IMAGE_GROUPUUID));
		this.contactUUID = cursor.getString(cursor.getColumnIndex(ImageEntry.COLUMN_IMAGE_CONTACTUUID));
		this.imageBitmap = BitmapProcess.StringToBitmap(cursor.getString(cursor.getColumnIndex(ImageEntry.COLUMN_IMAGE_BYTEARRAY)));
		this.timeStamp = cursor.getLong(cursor.getColumnIndex(ImageEntry.COLUMN_IMAGE_TIMESTAMP));
	}
	public String getPicID() {
		return picID;
	}
	public void setPicID(String picID) {
		this.picID = picID;
	}
	public String getGroupUUID() {
		return groupUUID;
	}
	public void setGroupUUID(String groupUUID) {
		this.groupUUID = groupUUID;
	}
	public String getContactUUID() {
		return contactUUID;
	}
	public void setContactUUID(String contactUUID) {
		this.contactUUID = contactUUID;
	}
	public Bitmap getImageBitmap() {
		return imageBitmap;
	}
	public void setImageBitmap(Bitmap imageBitmap) {
		this.imageBitmap = imageBitmap;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	

}
