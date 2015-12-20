package com.uninum.elite.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uninum.elite.R;
import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.ContactDBHelper.ContactEntry;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.database.MessageDBHelper;
import com.uninum.elite.object.Contact;
import com.uninum.elite.object.ContactGroup;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.ui.MainPage;
import com.uninum.elite.ui.TabContact;
import com.uninum.elite.webservice.SimpleWSJsonRequest;
import com.uninum.elite.webservice.VolleyWSJsonRequest;
import com.uninum.elite.webservice.WSContact;

public class UpdateContacts {
	private Context context;
	private String[] unitUUID;
	private String[] unitName;
	private String[] selfUUID;
	private int groupNum = 0;
	private int countGroupNum = 0;
	GroupDBHelper groupDb;
	MessageDBHelper msgDb;
	ContactDBHelper contactDb;
	private String callback;
	public static String GROUP_NOT_MODIFY = "304";
	private List<ContactGroup> contactGroups = new ArrayList<ContactGroup>();
	
	public UpdateContacts(Context context, String callback){
		this.context = context;
		groupDb = GroupDBHelper.getInstance(context);
		contactDb = ContactDBHelper.getInstance(context);
		msgDb = MessageDBHelper.getInstance(context);
		this.callback = callback;
	}
	public void getGroupsUUID(){
		WSContact.WSContactGroup getContactUUIDRequest = new WSContact.WSContactGroup(SystemManager.getToken(context), new Response.Listener<JSONObject>() {		
			@Override
			public void onResponse(JSONObject response) {
				// TODO Auto-generated method stub					
				
				String status ="";
				try {
					contactGroups = new ArrayList<ContactGroup>(); //get all groups
					status = response.getString(WSContact.STATUS);							
					Log.d("KITs getUnitUUID",response.toString());
					JSONArray unitList = new JSONArray();
					unitList = response.getJSONArray(ContactGroup.UNIT_ARRAY);
					Log.d("ELITE",response.toString());
					
					
					if (unitList != null) {
						try {
							SystemManager.putPreLong(context, SystemManager.CONTACT_UPDATE_TIME, System.currentTimeMillis());
							int length = unitList.length();
							unitUUID = new String[length]; //group uuid
							unitName = new String[length]; //group name
							selfUUID = new String[length]; //uuid of myself
							
							for (int i = 0; i != length; ++i) { // 
								unitUUID[i] = unitList.getJSONObject(i).getString(ContactGroup.UNIT_UUID);
								unitName[i] = unitList.getJSONObject(i).getString(ContactGroup.UNIT_NAME);
								ContactGroup contactGroup = new ContactGroup(unitName[i], unitUUID[i]);
								contactGroup.setGroupPoint("");
								contactGroup.setGroupType("");							
								if(!contactGroup.getGroupUUID().equals("930NTHU")){ // default 930NTHU can't save
									contactGroups.add(i, contactGroup);
								}
							}		
							
							List<ContactGroup> insertGroup = new ArrayList<ContactGroup>();
							
							//update if exist, add to insert list if not exist
							for(int i=0;i<contactGroups.size();i++){
								Cursor groupCursor = groupDb.queryGroupUUID(contactGroups.get(i).getGroupUUID());
								groupCursor.moveToFirst();
								if(groupCursor.getCount()>0) // if exist in local DB
									Log.d("Kits new","update:"+contactGroups.get(i).getGroupName());
								else{
									Log.d("Kits new","insert Add:"+contactGroups.get(i).getGroupName());
									contactGroups.get(i).setGroupNewUpdate(true);//new data
									insertGroup.add(contactGroups.get(i));
								}
								groupCursor.close();
							}
							Cursor cursor = groupDb.queryAllGroup();
							cursor.moveToFirst();
							
							
							//delete old group 
							for(int i=0;i<cursor.getCount();i++){
								String columnGroupUUID = cursor.getString(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_UUID));
								for(int j=0;j<contactGroups.size();j++){
									if(contactGroups.get(j).getGroupUUID().equals(columnGroupUUID)){ // if find match data then stop search
										break;
									}else if(j+1==contactGroups.size()){ // if is last item and it's not match then start to delete contacts in the group 
										Cursor deleteContactCursor = contactDb.queryContacts(columnGroupUUID);
										deleteContactCursor.moveToFirst();
										if(deleteContactCursor !=null && deleteContactCursor.getCount()>0){
											for(int k=0;k<deleteContactCursor.getCount();k++){  // delete contacts' image cache
												Contact deleteContact = new Contact(deleteContactCursor);
												KitsApplication.getInstance().deleteImageInHashMap(deleteContact.getPicture(), deleteContact.getUnitUUID());
												deleteContactCursor.moveToNext();
											}												
										}	
										
										groupDb.deleteGroup(columnGroupUUID); // delete group
										msgDb.deleteTable(columnGroupUUID); // delete table
										contactDb.deleteGroupContacts(columnGroupUUID);	 // delete contacts in group
									}
								}
								if(!cursor.isLast())
									cursor.moveToNext();
							}
								
								
							cursor.close();
							groupDb.insertGroup(insertGroup); //insert group
							for(int i=0; i<insertGroup.size(); i++){
								msgDb.createGroupMsgTable(insertGroup.get(i).getGroupUUID()); //create table
							}
							
							
							groupNum = contactGroups.size();
							if(groupNum==0){ //dismiss loading progress bar
								if(callback.equals(MainPage.callbackMainPage)){ 
									MainPage.updateContactFinishHandler.sendEmptyMessage(0);
								}else if(callback.equals(TabContact.callbackTabContact)){
									Message msg = new Message();
									msg.obj = insertGroup;
									TabContact.updateViewHandler.sendMessage(msg);
								}
							}else{ // start download contacts from each group
								for(int i=0;i<contactGroups.size();i++){
									getContactList(SystemManager.getToken(context), contactGroups.get(i).getGroupUUID(), contactGroups.get(i));
								}
							}	
							
						} catch (JSONException e) {
							e.printStackTrace();
						}

					} else {
						Toast.makeText(context, R.string.network_invalid,
								Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
						
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				try {
					
					NetworkResponse response = error.networkResponse;
					if(response != null && response.data != null){
						JSONObject result = new JSONObject(new String(response.data));; 
						String status = result.getString(WSContact.STATUS);
						if(WSContact.WSContactGroup.errorHandle(status, context)){
							Log.i("Jenny", "update contact: WSContact group error");
							return ;
						}else {
							Toast.makeText(context, R.string.internal_error + "("+ status +")", Toast.LENGTH_SHORT).show();
						}
					}
				}catch(Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					Message msg = new Message();
					msg.obj = false;
					
					if(callback.equals(MainPage.callbackMainPage)){ 
						MainPage.updateContactFinishHandler.sendMessage(msg);
					}else if(callback.equals(TabContact.callbackTabContact)){
						TabContact.updateViewHandler.sendEmptyMessage(0);
					}
				}
			}
		} );
		KitsApplication.getInstance().addToRequestQueue(getContactUUIDRequest, WSContact.GET_GROUP_UUID);	
	}

	public void getContactList(final String token, final String uuid, final ContactGroup group){
		final String getContactUUID_url = "https://ts.kits.tw/projectLYS/v0/Contact/";	
		new AsyncTask<Void, Void, String>(){ // 
			@Override
			protected String doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				long groupTimeStamp = groupDb.queryGroupTimeStamp(uuid);
				SimpleWSJsonRequest simpleWSJ = new SimpleWSJsonRequest();
				String result;
				try {
					result = simpleWSJ.WSJsonRequestGET(getContactUUID_url + token +"/"+uuid+"?timeStamp="+ groupTimeStamp);
					return result;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				} 
				
			}
			
			@Override
			protected void onPostExecute(String result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
				Log.d("KITs new", "Response: "+result);
				try{
				JSONObject response = new JSONObject(result);
				String status = response.getString(WSContact.STATUS);
				
				if(status.equals(WSContact.GET_CONTACT_SUCCESS)){
						group.setGroupType(response.getString(ContactGroup.UNIT_TYPE));
						group.setGroupPoint(response.getString(ContactGroup.CURRENT_POINT));
						group.setGroupLeadUUID(response.getString(ContactGroup.LEADER_UUID));
						group.setGroupLead(Boolean.valueOf(response.getString(ContactGroup.IS_LEADER)));
						group.setSelfUUID(response.getString(ContactGroup.SELF_UUID)); // user's contact uuid
						group.setGroupPrivacy(ContactGroup.PRIVACY_PUBLIC);	//default group privacy
						group.setGroupTimeStamp(System.currentTimeMillis()); // long with 13 bit
						
						JSONArray groupList = new JSONArray();
						groupList = response.getJSONArray(Contact.UNIT_CONTACT_ARRAY);
						List<Contact> insertContacts = new ArrayList<Contact>(); // new contacts for insert db
						Cursor cursorContacts = contactDb.queryContacts(group.getGroupUUID()); // old contacts in db
						List<String> contactsUUID = new ArrayList<String>(); // temp contacts uuid list
						List<String> contactsGroupUUID = new ArrayList<String>(); // temp contacts groupUuid list
						cursorContacts.moveToFirst();
						
						//add old contacts to temp list
						for(int i=0;i<cursorContacts.getCount();i++){
							contactsUUID.add(i, cursorContacts.getString(cursorContacts.getColumnIndex(ContactEntry.COLUMN_CONTACT_CONTACTUUID)));
							contactsGroupUUID.add(i, group.getGroupUUID());
							if(!cursorContacts.isLast())
								cursorContacts.moveToNext();
						}
						
						cursorContacts.close();
						String number = SystemManager.getPreString(context, SystemManager.USER_ACCOUNT);
						
						if (groupList != null) {
							for(int i=0;i<groupList.length();i++){
								JSONObject unit = groupList.getJSONObject(i);
								
								if(unit.getString(Contact.CONTACT_UUID).equals(group.getSelfUUID())){ // pick user to decide group's privacy 
									if(Boolean.valueOf(unit.getString(Contact.IS_PRIVATE)))
										group.setGroupPrivacy(ContactGroup.PRIVACY_PRIVATE);
									else
										group.setGroupPrivacy(ContactGroup.PRIVACY_PUBLIC);	
									
								}else if(unit.getString(Contact.LEVEL).equals(Contact.LEVEL1)){
									// not use
									
								}else if(!unit.getString(Contact.CONTACT_UUID).equals("")){
									Contact contact = new Contact();
									contact.setName(unit.getString(Contact.NAME));
									contact.setPhone(unit.getString(Contact.PHONE));
									contact.setMvpn(Boolean.valueOf(unit.getString(Contact.ISMVPN)));
									contact.setPicture(unit.getString(Contact.PIC_ID));
									contact.setUnitname(unit.getString(Contact.UNITNAME));
									contact.setContactUUID(unit.getString(Contact.CONTACT_UUID));
									contact.setUnitUUID(group.getGroupUUID());
									contact.setUnitType(group.getGroupType());
									contact.setPrivate(Boolean.valueOf(unit.getString(Contact.IS_PRIVATE))); 
									contact.setBelong(unit.getString(Contact.BELONG));
									
									if(group.getGroupLeadUUID().equals(unit.getString(Contact.CONTACT_UUID)))
									{
										contact.setLeader(true);
									}else{
										contact.setLeader(false);
									}
									if(unit.getBoolean(Contact.IS_STICKY)){
										contact.setSticky(true);
									}else{
										contact.setSticky(false);
									}
									
									String unitType = group.getGroupType();
									//group leader is free
									if(unitType.equals(ContactGroup.TYPE_NORMAL)){
										contact.setFree(false);
									}else{
										if(group.getGroupLeadUUID().equals(group.getSelfUUID())){
											contact.setFree(true);
										}else if(unitType.equals(ContactGroup.TYPE_GLORY)){			
											contact.setFree(true);
										}else if(unitType.equals(ContactGroup.TYPE_GLORY_MULTI)&&contact.isLeader()){
											contact.setFree(true);
										}else if(unitType.equals(ContactGroup.TYPE_GLORY_UNI)){
											contact.setFree(true);
										}else{
											contact.setFree(false);
										}								
									}
									
									Cursor cursor = contactDb.queryContact(contact.getContactUUID(), contact.getUnitUUID());
									cursor.moveToFirst();
									Log.d("kits new","getCount:"+cursor.getCount());
									
									if(cursor.getCount()>0){
										//if contact exist in current db, remove contact in list and update it
										contactDb.updateContact(contact);
										contactsUUID.remove(contact.getContactUUID()); //remove contact in temp list
										contactsGroupUUID.remove(contact.getUnitUUID()); // remove contact's group in temp list
										Log.d("kits new", "update:"+contact.getName()+ " phone:"+contact.getPhone() +" groupUUID:"+contact.getUnitUUID());
										cursor.close();
									}else{
										//if contact not exist, add contact to list and wait for insert 
										Log.d("kits new", "insert add:"+contact.getName()+ " phone:"+contact.getPhone()+" groupUUID:"+contact.getUnitUUID());
										group.setGroupNewUpdate(true);
										contact.setNewUpdate(true);
										insertContacts.add(contact);
									}
									
								} // prevent level 1 without phone 										
							}							
							
							groupDb.updateGroup(group);	
							for(int i=0;i<contactsUUID.size();i++){
								//delete contact that do not contain in new contact list
								Cursor deleteContactCursor = contactDb.queryContact(contactsUUID.get(i), contactsGroupUUID.get(i));
								deleteContactCursor.moveToFirst();
								if(deleteContactCursor !=null && deleteContactCursor.getCount()>0){
									Contact deleteContact = new Contact(deleteContactCursor);
									KitsApplication.getInstance().deleteImageInHashMap(deleteContact.getPicture(), deleteContact.getUnitUUID());
								}								
								contactDb.deleteGroupContact(contactsUUID.get(i), contactsGroupUUID.get(i));
							}
							
							//insert contact list that do not exist in current database
							contactDb.insertContacts(insertContacts);
						}
				}else if(status.equals(GROUP_NOT_MODIFY)){
					
				}else if(VolleyWSJsonRequest.errorHandle(status, context)){
					
				}else if(status.equals(WSContact.ERROR_GROUPUUID)){
					Toast.makeText(context, context.getString(R.string.response_params_error), Toast.LENGTH_SHORT).show();				
				}else if(status.equals(WSContact.ERROR_POINTCARD)){
					Toast.makeText(context, context.getString(R.string.response_params_error), Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(context, context.getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
				}
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					countGroupNum++;
					if(countGroupNum==groupNum){
							if(callback.equals(MainPage.callbackMainPage)){
								MainPage.updateContactFinishHandler.sendEmptyMessage(0);
							}else if(callback.equals(TabContact.callbackTabContact)){
								TabContact.updateViewHandler.sendEmptyMessage(0);
							}
							countGroupNum = 0;
					}
				}
			}			
		}.execute();
	}
}
