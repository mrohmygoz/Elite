package com.uninum.elite.object;

import java.io.Serializable;

import com.uninum.elite.database.ContactDBHelper.ContactEntry;

import android.database.Cursor;

public class Contact implements Serializable {
	public static String NAME = "name";
	public static String PHONE = "phone";
	public static String UNITNAME = "unitName";
	public static String BELONG = "belong";
	public static String ISMVPN = "isMVPN";
	public static String PIC_ID = "picID";
	public static String LEVEL = "level";
	public static String LEVEL1 = "1";
	public static String UNIT_TYPE = "unitType";
	public static String UNITUUID = "unitUUID";
	public static String CONTACT_UUID = "uuid";
	public static String IS_LEAD = "isLead";
	public static String IS_FREE = "isFree";
	public static String IS_PRIVATE = "isPrivate";
	public static String IS_STICKY = "isSticky";
	public static String JSON_VALUE_PRIVATE = "Private";
	public static String UNIT_CONTACT_ARRAY = "unitContactArray";
	public static String DEFAULT_PICTURE = "default";
	public static String BUNDLE_CONTACT = "contact";
	private String name;
	private String phone;
	private String picture;	
	private String belong;
	private String unitname;
	private String unitUUID;
	private String contactUUID;
	private String unitType;
	private boolean favorite;
	private boolean mvpn;
	private boolean leader;
	private boolean free;
	private boolean isPrivate;
	private boolean isSticky;
	private boolean isNewUpdate;
	

	public Contact(){
	}
	public Contact(Cursor cursor){
		this.setName(cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_NAME)));
		this.setPhone(cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_PHONE)));
		this.setPicture(cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_PICTURE)));
		this.setUnitname(cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_UNITNAME)));
		this.setContactUUID(cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_CONTACTUUID)));
		this.setUnitUUID(cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_UNITUUID)));
		this.setUnitType(cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_UNITTYPE)));
		if(cursor.getInt(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_MVPN))==1)
			this.setMvpn(true);
		else
			this.setMvpn(false);
		if(cursor.getInt(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_FAVORITE))==1)
			this.setFavorite(true);
		else
			this.setFavorite(false);
		if(cursor.getInt(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_PRIVATE))==1)
			this.setPrivate(true);
		else
			this.setPrivate(false);
		if(cursor.getInt(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_LEADER))==1)
			this.setLeader(true);
		else
			this.setLeader(false);
		if(cursor.getInt(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_FREE))==1)
			this.setFree(true);
		else
			this.setFree(false);
		if(cursor.getInt(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_STICKY))==1)
			this.setSticky(true);
		else
			this.setSticky(false);
		if(cursor.getInt(cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_NEWUPDATE))==1)
			this.setNewUpdate(true);
		else
			this.setNewUpdate(false);
		
	}
	public String getUnitType() {
		return unitType;
	}
	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public String getBelong() {
		return belong;
	}
	public void setBelong(String belong) {
		this.belong = belong;
	}
	public String getUnitname() {
		return unitname;
	}
	public void setUnitname(String unitname) {
		this.unitname = unitname;
	}
	public boolean isFavorite() {
		return favorite;
	}
	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}
	public boolean isMvpn() {
		return mvpn;
	}
	public void setMvpn(boolean mvpn) {
		this.mvpn = mvpn;
	}
	public String getUnitUUID() {
		return unitUUID;
	}
	public void setUnitUUID(String unitUUID) {
		this.unitUUID = unitUUID;
	}	
	public boolean isLeader() {
		return leader;
	}
	public void setLeader(boolean leader) {
		this.leader = leader;
	}
	public boolean isFree() {
		return free;
	}
	public void setFree(boolean free) {
		this.free = free;
	}
	public boolean isPrivate() {
		return isPrivate;
	}
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}
	
	public String getContactUUID() {
		return contactUUID;
	}
	public void setContactUUID(String contactUUID) {
		this.contactUUID = contactUUID;
	}
	public boolean isSticky() {
		return isSticky;
	}
	public void setSticky(boolean isSticky) {
		this.isSticky = isSticky;
	}
	public boolean isNewUpdate() {
		return isNewUpdate;
	}
	public void setNewUpdate(boolean isNewUpdate) {
		this.isNewUpdate = isNewUpdate;
	}
}
