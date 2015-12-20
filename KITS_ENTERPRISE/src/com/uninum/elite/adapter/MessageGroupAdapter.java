package com.uninum.elite.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.uninum.elite.R;
import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.database.MessageDBHelper;
import com.uninum.elite.database.MessageDBHelper.GroupMessageEntry;
import com.uninum.elite.image.ImageViewLoader;
import com.uninum.elite.object.Contact;
import com.uninum.elite.ui.Activity_Message;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageGroupAdapter extends CursorAdapter {
	
	private Context context;
	private MessageDBHelper msgDB;
	private SimpleDateFormat formatTime;
	private Date callDate;
	private static final long ONE_DAY = 1000*60*60*24;

	public MessageGroupAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
		
		this.context = context;
		msgDB = MessageDBHelper.getInstance(context);
	}

	@Override
	public void bindView(View view, final Context context, final Cursor cursor) {
		// TODO Auto-generated method stub
		if(view==null || cursor==null)
			return;
		ViewHolder holder = (ViewHolder) view.getTag();
		final String groupName = cursor.getString( cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_NAME));
		final String groupUUID = cursor.getString(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_UUID));
		final String selfUUID = cursor.getString(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_SELFUUID));
		final String leadUUID = cursor.getString(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_LEADUUID));
		
		ImageViewLoader imageViewLoader = new ImageViewLoader(context);
		Cursor contactCur = ContactDBHelper.getInstance(context).queryContact(leadUUID, groupUUID);
		if (contactCur.moveToFirst()){
			Contact contact = new Contact(contactCur);
			imageViewLoader.loadImageToView(contact, holder.bighead);
		}
		
		
		holder.groupNameText.setText(groupName);

		Cursor cur = msgDB.queryAllMessage(groupUUID);
		if (cur.moveToLast()) {
			holder.LastSaidText.setText(cur.getString(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_MSG)));

			formatTime = new SimpleDateFormat("M/d");
			
			long timeNow = System.currentTimeMillis();
			callDate = new Date(timeNow);
			String now = formatTime.format(callDate);
			
			long timeYesterday = timeNow - ONE_DAY;
			callDate = new Date(timeYesterday);
			String yesterday = formatTime.format(callDate);
			
			long timeStamp = cur.getLong(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME));
			callDate = new Date(timeStamp);
			String time = formatTime.format(callDate);
			
			if (time.equals(now)){
				formatTime = new SimpleDateFormat("H:mm");
				time = formatTime.format(callDate);
			} else if (time.equals(yesterday)) 
				time = "¬Q¤Ñ";
			else if (timeNow - timeStamp < ONE_DAY*6) {
				formatTime = new SimpleDateFormat("EEE");
				time = formatTime.format(callDate);
			}
			
			holder.LastSaidDate.setText(time);
		}
		else {
			holder.LastSaidText.setText("");
			holder.LastSaidDate.setText("");
		}
		
		
		//number
		cur = msgDB.queryLastUnread(groupUUID);
		int count = cur.getCount();
		if (cur.moveToFirst()){
			holder.NewMessageNumberText.setText(""+count);
			holder.NewMessageNumberText.setVisibility(View.VISIBLE);
		}
		else
			holder.NewMessageNumberText.setVisibility(View.GONE);
		
		
		holder.groupLayout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MessageDBHelper msgDB = MessageDBHelper.getInstance(context);
				if (msgDB.IsTableExist(groupUUID)){
					Log.d("Jenny", "msg group adapter: group " + groupUUID + " exist");
					Intent intent = new Intent();
					intent.setClass(context, Activity_Message.class);
					Bundle msgBundle = new Bundle();
					msgBundle.putString(Activity_Message.GROUP_NAME, groupName);
					msgBundle.putString(Activity_Message.GROUP_UUID, groupUUID);
					msgBundle.putString(Activity_Message.SELF_UUID, selfUUID);
					intent.putExtras(msgBundle);
					context.startActivity(intent);
				}else{
					Log.d("Jenny", "msg group adapter: group " + groupUUID + " not exist");
				}
			}
			
		});
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup arg2) {
		// TODO Auto-generated method stub
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = (View)inflater.inflate(R.layout.message_listrow, null);
		
		ViewHolder holder = new ViewHolder();
		holder.groupLayout = (LinearLayout) view.findViewById(R.id.message_listrow_layout);
		holder.groupNameText = (TextView) view.findViewById(R.id.message_group_name);
		holder.LastSaidText = (TextView) view.findViewById(R.id.message_last_said);
		holder.LastSaidDate = (TextView) view.findViewById(R.id.last_said_date_tv);
		holder.NewMessageNumberText = (TextView) view.findViewById(R.id.message_new_number);
		holder.bighead = (ImageView) view.findViewById(R.id.message_bighead);
		view.setTag(holder);
		
		return view;
	}
	
	public class ViewHolder{
		LinearLayout groupLayout;
		TextView groupNameText;
		TextView LastSaidText;
		TextView LastSaidDate;
		TextView NewMessageNumberText ;
		ImageView bighead;
	}

}
