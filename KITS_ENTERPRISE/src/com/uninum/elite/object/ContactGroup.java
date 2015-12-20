package com.uninum.elite.object;

import java.util.ArrayList;
import java.util.List;

public class ContactGroup {
	public static String UNIT_UUID = "unitUUID";
	public static String UNIT_NAME = "unitName";
	public static String UNIT_TYPE = "unitType";
	public static String IS_LEADER = "isLeader";
	public static String CURRENT_POINT = "currentPoint";
	public static String UNIT_ARRAY = "unitArray";
	public static String LEADER_UUID = "leaderUUID";
	public static String TYPE_NORMAL = "Normal";
	public static String TYPE_GLORY = "Glory";
	public static String TYPE_GLORY_MULTI = "GloryMulti";
	public static String TYPE_GLORY_UNI = "GloryUni";
	public static String SELF_UUID = "selfUUID";
	public static String PRIVACY = "privacy";
	public static String PRIVACY_PRIVATE = "1";
	public static String PRIVACY_PUBLIC = "0";
	private String groupName;
	private String groupType; // group type ( groly or normal )
	private String groupUUID; // group uuid
	private String selfUUID;
	private String groupPoint;// point of group calling quota
	private boolean groupLead;
	private String groupLeadUUID;
	private String groupPrivacy;
	private boolean groupNewUpdate;
	private long groupTimeStamp;

	private List<Contact> contacts;	
	
	public ContactGroup(String groupName, String groupUUID){
		this.groupName = groupName;
		this.groupUUID = groupUUID;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public String getGroupUUID() {
		return groupUUID;
	}

	public void setGroupUUID(String groupUUID) {
		this.groupUUID = groupUUID;
	}

	public String getGroupPoint() {
		return groupPoint;
	}

	public void setGroupPoint(String groupPoint) {
		this.groupPoint = groupPoint;
	}

	public List<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}

	public boolean isGroupLead() {
		return groupLead;
	}

	public void setGroupLead(boolean groupLead) {
		this.groupLead = groupLead;
	}

	public String getGroupLeadUUID() {
		return groupLeadUUID;
	}

	public void setGroupLeadUUID(String groupLeadUUID) {
		this.groupLeadUUID = groupLeadUUID;
	}

	public String getSelfUUID() {
		return selfUUID;
	}

	public void setSelfUUID(String selfUUID) {
		this.selfUUID = selfUUID;
	}

	public String getGroupPrivacy() {
		return groupPrivacy;
	}

	public void setGroupPrivacy(String groupPrivacy) {
		this.groupPrivacy = groupPrivacy;
	}

	public boolean isGroupNewUpdate() {
		return groupNewUpdate;
	}

	public void setGroupNewUpdate(boolean groupNewUpdate) {
		this.groupNewUpdate = groupNewUpdate;
	}

	public long getGroupTimeStamp() {
		return groupTimeStamp;
	}

	public void setGroupTimeStamp(long groupTimeStamp) {
		this.groupTimeStamp = groupTimeStamp;
	}
}
