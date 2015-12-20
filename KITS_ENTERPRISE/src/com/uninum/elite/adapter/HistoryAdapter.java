package com.uninum.elite.adapter;


import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.HistoryDBHelper;
import com.uninum.elite.database.ContactDBHelper.ContactEntry;
import com.uninum.elite.database.HistoryDBHelper.HistoryEntry;
import com.uninum.elite.image.DrawCircularImage;
import com.uninum.elite.image.ImageViewLoader;
import com.uninum.elite.object.Contact;
import com.uninum.elite.object.ContactGroup;
import com.uninum.elite.object.History;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.ui.ContactInfo;
import com.uninum.elite.ui.TabFavorite;
import com.uninum.elite.R;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class HistoryAdapter extends CursorTreeAdapter {
	public Context context;
	private static final List displayedImages = Collections.synchronizedList(new LinkedList());
	private ContactDBHelper contactDB;
	public HistoryAdapter(Cursor cursor, Context context) {
		super(cursor, context);
		// TODO Auto-generated constructor stub
		this.context = context;
		contactDB = ContactDBHelper.getInstance(context);
	}
	@Override
	protected void bindChildView(View view, Context context, Cursor cursor,
			boolean isLastChild) {
		// TODO Auto-generated method stub
		if(view==null || cursor == null)
			return;
		ViewHolder viewholder = (ViewHolder)view.getTag();
		String number = cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_NUMBER));
		if(number.equals(Contact.JSON_VALUE_PRIVATE))
			viewholder.historyNumber.setText(context.getString(R.string.contact_privacy_private));
		else
			viewholder.historyNumber.setText(number);
		Long time = cursor.getLong(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_DURATION ));
		int hour = (int)(time/1000)/3600;
		int mins = (int)(time/1000)%3600/60;
		int sec = (int)(time/1000)%3600%60;
		String timeCombine;
		if(hour>0){
			timeCombine= hour+ context.getString(R.string.history_hour )+ mins + context.getString(R.string.history_min)+ sec+ context.getString(R.string.history_sec);
		}else if(mins>0){
			timeCombine= mins + context.getString(R.string.history_min)+ sec+ context.getString(R.string.history_sec);
		}else{
			timeCombine= sec+ context.getString(R.string.history_sec);
		}
		viewholder.historyDuration.setText(timeCombine);
	}
	@Override
	protected void bindGroupView(View view, final Context context, Cursor cursor,
			boolean isExpanded) {
		// TODO Auto-generated method stub
		
		if(view==null || cursor == null)
			return;
		final ViewHolder viewholder = (ViewHolder)view.getTag();
		viewholder.historyName.setText(cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_NAME)));
		SimpleDateFormat formatDate = new SimpleDateFormat("yyyy¦~MM¤ëdd¤é");
		SimpleDateFormat formatTime = new SimpleDateFormat(" EEE HH:mm:ss");
		Date callDate = new Date(cursor.getLong(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_START_TIME)));
		String date =  formatDate.format(callDate);
		String time =  formatTime.format(callDate);
		viewholder.historyDate.setText(date+"\n"+time);
		int status = cursor.getInt(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_STATUS));
		if(status == History.INCOMMING){
			viewholder.historyStatus.setText(context.getString(R.string.history_incoming));
		}else if(status == History.OUTGOING){
			viewholder.historyStatus.setText(context.getString(R.string.history_outgoing));
		}
		
		final String peerUUID = cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_PEERUUID));
		final String unitUUID = cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_UNITUUID));
		final String historyUUID = cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_UUID));
		Cursor contactCursor = ContactDBHelper.getInstance(context).queryContact(peerUUID, unitUUID);
		if(contactCursor.getCount()>0){
			String timeStamp = "0";
			contactCursor.moveToFirst();
			final Contact history_contact = new Contact(contactCursor);
			final String picID = history_contact.getPicture();
			ImageViewLoader imageViewLoader = new ImageViewLoader(context);
			imageViewLoader.loadImageToView(history_contact, viewholder.historyIcon);

				viewholder.historyIcon.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						FragmentTransaction ft = ((ActionBarActivity)context).getSupportFragmentManager().beginTransaction();
						DialogFragment contactInfoFragment = new ContactInfo();
						Bundle args = new Bundle();
						Contact contact = new Contact();
						args.putSerializable(Contact.BUNDLE_CONTACT, history_contact);
						contactInfoFragment.setArguments(args);
						contactInfoFragment.show(ft, "dialog");
					}
					
				});
				if(history_contact.getUnitType().equals(ContactGroup.TYPE_NORMAL)){
					viewholder.historyIcon.setBackgroundResource(R.drawable.history_icon_ring);
				}else{
					viewholder.historyIcon.setBackgroundResource(R.drawable.history_icon_ring_glory);
				}
		}
		contactCursor.close();
		if(isExpanded){
			viewholder.historyGroupLayout.setBackgroundResource(R.drawable.history_detail_rectangle_shape);
			viewholder.historyName.setTextColor(context.getResources().getColor(R.color.elite_theme_color_history_item_click_header_color));
			viewholder.historyDate.setTextColor(context.getResources().getColor(R.color.elite_theme_color_history_item_click_header_color));
			viewholder.historyGroupLine.setVisibility(View.GONE);
		}else{
			viewholder.historyGroupLayout.setBackgroundResource(R.color.elite_theme_color_history_item_background);
			viewholder.historyName.setTextColor(context.getResources().getColor(R.color.elite_theme_color_history_item_name_font));
			viewholder.historyDate.setTextColor(context.getResources().getColor(R.color.elite_theme_color_history_item_name_font));
			viewholder.historyGroupLine.setVisibility(View.VISIBLE);
		}
		
		boolean isFavortie = contactDB.queryContactIsFavorite(peerUUID, unitUUID);
		if(isFavortie){
			viewholder.historyFavorite.setImageResource(R.drawable.btn_important_click);			
		}else{
			viewholder.historyFavorite.setImageResource(R.drawable.btn_important);	
		}		
		viewholder.historyFavorite.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				boolean isFavorite = contactDB.queryContactIsFavorite(peerUUID, unitUUID);
				Log.d("kits new","isFavorite:"+isFavorite);
				if(isFavorite){					
					viewholder.historyFavorite.setImageResource(R.drawable.btn_important);	
				}else{
					viewholder.historyFavorite.setImageResource(R.drawable.btn_important_click);
				}
				notifyDataSetChanged();
				TabFavorite.updateFavHandler.sendEmptyMessage(0);
				contactDB.updateContactFavorite(peerUUID, unitUUID, !isFavorite);
			}
			
		});
	}
	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		// TODO Auto-generated method stub
		return HistoryDBHelper.getInstance(context).queryHistory(groupCursor.getString(groupCursor.getColumnIndex(HistoryEntry.COLUMN_HISTORY_UUID)));
	}
	@Override
	protected View newChildView(Context context, Cursor cursor,
			boolean isLastChild, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = (View)inflater.inflate(R.layout.history_listrow_detail, null);
		ViewHolder viewholder = new ViewHolder();
		viewholder.historyNumber = (TextView)view.findViewById(R.id.history_number_tv);
		viewholder.historyDuration = (TextView)view.findViewById(R.id.history_duration_tv);
		view.setTag(viewholder);
		return view;
	}
	@Override
	protected View newGroupView(Context context, Cursor cursor,
			boolean isExpanded, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = (View)inflater.inflate(R.layout.history_listrow_group, null);
		ViewHolder viewholder = new ViewHolder();
		viewholder.historyGroupLayout = (RelativeLayout)view.findViewById(R.id.rl_history);
		viewholder.historyIcon = (ImageView)view.findViewById(R.id.history_bighead_iv);
		viewholder.historyName = (TextView)view.findViewById(R.id.history_name_tv);
		viewholder.historyStatus = (TextView)view.findViewById(R.id.history_status_tv);
		viewholder.historyDate = (TextView)view.findViewById(R.id.history_date_tv);
		viewholder.historyGroupLine = (View)view.findViewById(R.id.history_group_line);
		viewholder.historyFavorite = (ImageView)view.findViewById(R.id.history_favorite_ibtn);
		view.setTag(viewholder);
		return view;
	}

	
	private class ViewHolder{
		RelativeLayout historyGroupLayout;
		ImageView historyIcon;
		ImageView historyFavorite;
		TextView historyName;
		TextView historyDate;
		TextView historyStatus;
		TextView historyDuration;
		TextView historyNumber;	
		View historyGroupLine;
	}
	
}
