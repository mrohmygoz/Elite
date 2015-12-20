package com.uninum.elite.object;

import java.util.ArrayList;
import java.util.List;

import com.uninum.elite.database.HistoryDBHelper.HistoryEntry;

import android.database.Cursor;
import android.util.Log;

public class History {

	public static int INCOMMING = 1;
	public static int OUTGOING = 0;
	public static String out = "out";
	public static String in = "in";
	private String hisUUID;
	private String groupUUID;
	private String peerUUID;
	private String number;
	private String name;
	private long startTime;
	private long endTime;
	private long duration;
	private int status;
	private boolean upload;

	public History(String hisUUID, String groupUUID, String peerUUID, String number, String name, long startTime, long endTime, int status, boolean upload){
		this.hisUUID = hisUUID;
		this.groupUUID = groupUUID;
		this.peerUUID = peerUUID;
		this.number = number;
		this.name= name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.status = status;
		this.upload = upload;
		if(endTime>=startTime){
			this.duration = endTime-startTime;
		}else{
			endTime = startTime;
			this.duration = 0;
		}
		Log.d("ELITE","hisUUID:"+hisUUID);

	}
	public History(Cursor cursor){
		this.peerUUID = cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_PEERUUID));
		this.groupUUID = cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_UNITUUID));
		this.hisUUID = cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_UUID));
		this.number = cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_NUMBER));
		this.name = cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_NAME));
		this.startTime = cursor.getLong(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_START_TIME));
		this.endTime = cursor.getLong(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_END_TIME));
		this.duration = cursor.getLong(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_DURATION));
		this.status = cursor.getInt(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_DURATION));
		Log.d("ELITE", "cursor:"+ cursor.getInt(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_UPLOAD)));
		if(cursor.getInt(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_UPLOAD))>0)
			this.upload = true;
		else
			this.upload = false;
	}
	
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getHisUUID() {
		return hisUUID;
	}

	public void setHisUUID(String hisUUID) {
		this.hisUUID = hisUUID;
	}

	public String getGroupUUID() {
		return groupUUID;
	}

	public void setGroupUUID(String groupUUID) {
		this.groupUUID = groupUUID;
	}

	public String getPeerUUID() {
		return peerUUID;
	}

	public void setPeerUUID(String peerUUID) {
		this.peerUUID = peerUUID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	public boolean isUpload() {
		return upload;
	}

	public void setUpload(boolean upload) {
		this.upload = upload;
	}

}
