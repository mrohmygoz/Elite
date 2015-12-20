package com.uninum.elite.ui;



import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.uninum.elite.database.HistoryDBHelper;
import com.uninum.elite.database.HistoryDBHelper.HistoryEntry;
import com.uninum.elite.database.ImageDBHelper;
import com.uninum.elite.image.DrawCircularImage;
import com.uninum.elite.image.ImageViewLoader;
import com.uninum.elite.object.Contact;
import com.uninum.elite.object.ContactGroup;
import com.uninum.elite.object.History;
import com.uninum.elite.object.Image;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.webservice.WSCall;
import com.uninum.elite.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ContactInfo extends DialogFragment {
	private HistoryDBHelper historyDB;
	private RelativeLayout header_rl, info_rl, call_rl;
	private static final List displayedImages = Collections.synchronizedList(new LinkedList());
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	historyDB = HistoryDBHelper.getInstance(getActivity());
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
	
	final Contact contact = (Contact)getArguments().getSerializable(Contact.BUNDLE_CONTACT);
	//
	final String name = contact.getName();
	final String phone = contact.getPhone();
	final String picID = contact.getPicture();
	final String contactUUID = contact.getContactUUID();
	final String unitName = contact.getUnitname();
	final String unitType = contact.getUnitType();
	final String unitUUID = contact.getUnitUUID();
	final boolean isLead = contact.isLeader();
	final boolean isFree = contact.isFree();
	final boolean isPrivate = contact.isPrivate();
	LayoutInflater mInflater = LayoutInflater.from(getActivity());
	View v = mInflater.inflate(R.layout.fragment_contactinfo2 ,null);
	ImageButton dismissDialog = (ImageButton)v.findViewById(R.id.contactinfo_dismiss_ibtn);
	ImageButton callBtn = (ImageButton)v.findViewById(R.id.contactinfo_call_ibtn);
	TextView contactName = (TextView)v.findViewById(R.id.contact_name_tv);
	TextView contactPhone = (TextView)v.findViewById(R.id.contact_phone_tv);
	TextView contactUnitName = (TextView)v.findViewById(R.id.contact_unitname_tv);
//	TextView contactUnitType = (TextView)v.findViewById(R.id.contact_unittype_tv);
	ImageView userIcon = (ImageView)v.findViewById(R.id.iv_user_head);
	RelativeLayout header_rl = (RelativeLayout)v.findViewById(R.id.rl_header);
	RelativeLayout info_rl = (RelativeLayout)v.findViewById(R.id.rl_info);
	RelativeLayout call_rl = (RelativeLayout)v.findViewById(R.id.rl_call);
	ImageView crown_iv = (ImageView)v.findViewById(R.id.iv_crown);
	contactName.setText(name);
	if(isPrivate)
		contactPhone.setText(getActivity().getString(R.string.contact_privacy_private));
	else
		contactPhone.setText(phone);
	contactUnitName.setText(unitName);
	
	if(unitType.equals(ContactGroup.TYPE_GLORY) || unitType.equals(ContactGroup.TYPE_GLORY_MULTI) || unitType.equals(ContactGroup.TYPE_GLORY_UNI)){
		header_rl.setBackgroundResource(R.color.elite_theme_color_contactinfo_header_glory);
		info_rl.setBackgroundResource(R.color.elite_theme_color_contactinfo_transparentblue_glory);
		call_rl.setBackgroundResource(R.color.elite_theme_color_contactinfo_bottom_transparentblue_glory);
		crown_iv.setVisibility(ImageView.VISIBLE);
	}else{
		
	}

	ImageViewLoader imageViewLoader = new ImageViewLoader(getActivity());
	imageViewLoader.loadImageToView(contact, userIcon);
	
	callBtn.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			getDialog().dismiss();
			if(unitType.equals(ContactGroup.TYPE_GLORY)&& isFree){
				WSCall.CallingWithTimer wsCalling = new WSCall.CallingWithTimer();	
				wsCalling.Calling(getActivity(),SystemManager.getToken(getActivity()), contactUUID, unitUUID, name );
			}else if(unitType.equals(ContactGroup.TYPE_GLORY_MULTI)&& isFree){
					WSCall.CallingWithTimer wsCalling = new WSCall.CallingWithTimer();	
					wsCalling.Calling(getActivity(),SystemManager.getToken(getActivity()), contactUUID, unitUUID, name);
			}else if(unitType.equals(ContactGroup.TYPE_GLORY_UNI)&& isFree){
					WSCall.CallingWithTimer wsCalling = new WSCall.CallingWithTimer();	
					wsCalling.Calling(getActivity(),SystemManager.getToken(getActivity()), contactUUID, unitUUID, name );			
			}else{
				if(!isPrivate){
					Intent callIntent = new Intent(Intent.ACTION_CALL);
				    callIntent.setData(Uri.parse("tel:"+phone));
				    getActivity().startActivity(callIntent);
				    Date nowTime = new Date();
				    History history = new History(UUID.randomUUID().toString(),
				    		unitUUID, contactUUID, phone, name, 
				    		nowTime.getTime(), 
				    		nowTime.getTime(), 
				    		History.OUTGOING, false);
				    historyDB.insertHistory(history);
				    Log.d("ELITE","Call:"+phone);
				}
			}
		}
		
	});
	dismissDialog.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			getDialog().dismiss();
		}
		
	});
	return new AlertDialog.Builder(getActivity())
	.setView(v)
	.create();
	}
}