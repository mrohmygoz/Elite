package com.uninum.elite.ui;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.image.DrawCircularImage;
import com.uninum.elite.object.Contact;
import com.uninum.elite.object.ContactGroup;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.utility.NetworkUtility;
import com.uninum.elite.webservice.WSCall;
import com.uninum.elite.webservice.WSContact;
import com.uninum.elite.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupSetting extends DialogFragment {
	GroupDBHelper groupDB;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	@Override
	public void onActivityCreated(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onActivityCreated(arg0);
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		groupDB = GroupDBHelper.getInstance(getActivity());		
	final String groupPoint = getArguments().getString(ContactGroup.CURRENT_POINT);
	final String groupUUID = getArguments().getString(ContactGroup.UNIT_UUID);
	final String groupName = getArguments().getString(ContactGroup.UNIT_NAME);
	final String groupType = getArguments().getString(ContactGroup.UNIT_TYPE);
	final int groupLead = getArguments().getInt(ContactGroup.IS_LEADER);
	LayoutInflater mInflater = LayoutInflater.from(getActivity());
	View v = mInflater.inflate(R.layout.fragment_group_setting ,null);
	TextView groupName_tv = (TextView)v.findViewById(R.id.group_setting_groupname_tv);
	TextView groupPoint_tv  = (TextView)v.findViewById(R.id.group_setting_point_tv);
	final TextView groupPrivacy_tv = (TextView)v.findViewById(R.id.group_setting_privacy_tv);
	CheckBox groupPrivacy_cb = (CheckBox)v.findViewById(R.id.group_setting_privacy_cb);
	Button quitGroup_btn = (Button)v.findViewById(R.id.group_setting_quit_btn);
	ImageView userIcon = (ImageView)v.findViewById(R.id.iv_user_head);
	TableRow groupPoint_row = (TableRow)v.findViewById(R.id.group_point_row);
	TableRow privacySetting_row = (TableRow)v.findViewById(R.id.privacy_setting_row);
	TableRow groupPrivacy_row = (TableRow)v.findViewById(R.id.privacy_setting_row);
	TextView groupPrivacyHint_tv = (TextView)v.findViewById(R.id.group_provacy_descript_tv);
	groupName_tv.setText(groupName);
	if(groupType.equals(ContactGroup.TYPE_GLORY)||groupType.equals(ContactGroup.TYPE_GLORY_UNI)){
		groupPrivacy_cb.setVisibility(CheckBox.VISIBLE);
		groupPrivacy_row.setVisibility(TableRow.VISIBLE);
		groupPrivacyHint_tv.setText(R.string.groupsetting_privacy_hint_private);
		String groupPrivacy = groupDB.queryGroupPrivacy(groupUUID);
		Log.d("KITs privacy", "Privacy:"+groupPrivacy);
		if(groupPrivacy !=null && groupPrivacy.equals(ContactGroup.PRIVACY_PRIVATE)){
			groupPrivacy_tv.setText(getActivity().getString(R.string.contact_privacy_private2));
			groupPrivacy_cb.setChecked(true);
		}else if(groupPrivacy !=null && groupPrivacy.equals(ContactGroup.PRIVACY_PUBLIC)){
			groupPrivacy_tv.setText(getActivity().getString(R.string.contact_privacy_public));
			groupPrivacy_cb.setChecked(false);
		}else{
			
		}
	}else if(groupType.equals(ContactGroup.TYPE_GLORY_MULTI)){
		groupPrivacy_cb.setVisibility(CheckBox.INVISIBLE);
		groupPrivacyHint_tv.setText("");
		groupPrivacyHint_tv.setVisibility(TextView.GONE);
	}else{ //normal type
		groupPrivacy_cb.setVisibility(CheckBox.INVISIBLE);
		groupPrivacyHint_tv.setText("");
		groupPrivacyHint_tv.setVisibility(TextView.GONE);
		privacySetting_row.setVisibility(TableRow.GONE);
//		groupPrivacy_row.setVisibility(TableRow.INVISIBLE);
//		groupPrivacy_row.removeAllViewsInLayout();
	}
	if(groupLead>0){
		groupPoint_tv.setText(groupPoint);
		groupPoint_row.setVisibility(TableRow.VISIBLE);
		quitGroup_btn.setEnabled(false);
		quitGroup_btn.setBackgroundResource(R.drawable.button_rectangle_shape_dark);
	}else{
		groupPoint_tv.setText("");
		groupPoint_row.setVisibility(TableRow.INVISIBLE);
		groupPoint_row.removeAllViews();
	}
	
//	if(groupType.equals(ContactGroup.TYPE_GLORY)){
//		groupName_tv.setText(getActivity().getString(R.string.unitType_glory));
//	}else if(groupType.equals(ContactGroup.TYPE_GLORY_MULTI)){
//		groupName_tv.setText(getActivity().getString(R.string.unitType_glory_multi));
//	}else if(groupType.equals(ContactGroup.TYPE_GLORY_UNI)){
//		groupName_tv.setText(getActivity().getString(R.string.unitType_glory_uni));
//	}else{
//		groupName_tv.setText(getActivity().getString(R.string.unitType_normal));
//	}
	groupPrivacy_cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton btn, boolean check) {
			// TODO Auto-generated method stub
			if(NetworkUtility.isConnected(getActivity())){				
				Log.d("KITs privacy", "Privacy:"+groupDB.queryGroupPrivacy(groupUUID));
				if(check){
					setPrivacy(groupUUID, check);
					groupDB.updateGroupPrivacy(groupUUID, check);
					groupPrivacy_tv.setText(getActivity().getString(R.string.contact_privacy_private2));
				
				}else{
					setPrivacy(groupUUID, check);
					groupDB.updateGroupPrivacy(groupUUID, check);
					groupPrivacy_tv.setText(getActivity().getString(R.string.contact_privacy_public));
				}
			}else{
				Toast.makeText(getActivity(), R.string.network_invalid, Toast.LENGTH_SHORT).show();
			}
		}
		
	});
	
	quitGroup_btn.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			AlertDialog.Builder builder = new Builder(getActivity());
			builder.setMessage(R.string.contact_group_quit_message);
			builder.setTitle(R.string.contact_group_quit_title);
			builder.setPositiveButton(R.string.contact_group_quit_yes , new Dialog.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					if(NetworkUtility.isConnected(getActivity())){	
						quitGroup(groupUUID);
						getDialog().dismiss();
					}else{
						Toast.makeText(getActivity(), R.string.network_invalid, Toast.LENGTH_SHORT).show();
					}
				}
				
			});
			builder.setNegativeButton(R.string.contact_group_quit_no, new Dialog.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					
				}
				
			});
			builder.show();
		}
		
	});
	return new AlertDialog.Builder(getActivity())
	.setTitle(getActivity().getString(R.string.contact_group_setting_title))
	.setView(v)
	.setPositiveButton(getActivity().getString(R.string.contact_setting_complete),
	new DialogInterface.OnClickListener() {
	public void onClick(DialogInterface dialog, int whichButton) {
	//do something
		
	}
	}
	)
	.create();
	}
	
	private void setPrivacy(String groupUUID, boolean setPrivate){
		JSONObject json = new JSONObject();
		try {
			json.put(WSContact.JSON_PRIVACY, setPrivate);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WSContact.WSContactGroupPrivacy wsGroupPrivacy = new WSContact.WSContactGroupPrivacy(
				SystemManager.getToken(getActivity()), groupUUID, json , new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						String status;
						try {
							status = response.getString(WSContact.STATUS);
							Log.d("KITs Pirvacy","status:"+ status);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
						
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						NetworkResponse response = error.networkResponse;
						String status = String.valueOf(response.statusCode);
						if(WSContact.WSContactGroupPrivacy.errorHandle(status, getActivity()) )
							return ;
					}
				});
		KitsApplication.getInstance().addToRequestQueue(wsGroupPrivacy, WSContact.SET_PRIVACY);	
	}
	
	private void quitGroup(String groupUUID){
		WSContact.WSContactGroupDelete wsGroupPrivacy = new WSContact.WSContactGroupDelete(
				SystemManager.getToken(getActivity()), groupUUID , new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						String status;
						try {
							status = response.getString(WSContact.STATUS);
							if(TabContact.updateHandler!=null){
								TabContact.updateHandler.sendEmptyMessage(0);
								Log.d("KITs update","send message");
							}
							Log.d("KITs Remove","status:"+ status);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
						
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						NetworkResponse response = error.networkResponse;
						String status = String.valueOf(response.statusCode);
						if(WSContact.WSContactGroupDelete.errorHandle(status, getActivity()) )
							return ;
					}
				});
		KitsApplication.getInstance().addToRequestQueue(wsGroupPrivacy, WSContact.SET_PRIVACY);	
	}
}
