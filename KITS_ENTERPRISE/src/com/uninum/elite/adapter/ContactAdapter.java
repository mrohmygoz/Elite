package com.uninum.elite.adapter;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.image.DrawCircularImage;
import com.uninum.elite.image.ImageViewLoader;
import com.uninum.elite.object.Contact;
import com.uninum.elite.object.ContactGroup;
import com.uninum.elite.ui.ContactInfo;
import com.uninum.elite.ui.GroupSetting;
import com.uninum.elite.ui.TabHistory;
import com.uninum.elite.R;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.CursorTreeAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContactAdapter extends CursorTreeAdapter{
	private Context context;
	private ContactDBHelper contactDB;
	private GroupDBHelper groupDB;
	private static final List displayedImages = Collections.synchronizedList(new LinkedList());
	
	public ContactAdapter(Cursor cursor, Context context) {
		super(cursor, context);
		// TODO Auto-generated constructor stub
		this.context = context;
		contactDB = ContactDBHelper.getInstance(context);
		groupDB = GroupDBHelper.getInstance(context);
		
	}

	@Override
	protected void bindChildView(final View view, final Context context, final Cursor cursor,
			boolean isLastChild) {
		// TODO Auto-generated method stub
		if(view==null || cursor==null)
			return ;
		final Contact contact = new Contact(cursor);
		final String name = contact.getName();
		final String unitUUID = contact.getUnitUUID();
		final String contactUUID = contact.getContactUUID();
		final boolean isLead = contact.isLeader();
		final boolean isFree = contact.isFree();
		final boolean isNewUpdate = contact.isNewUpdate();
		final ViewHolder viewHolder = (ViewHolder)view.getTag();
		TextView contactNew_tv = (TextView)view.findViewById(R.id.contact_new_tv);
		if( isNewUpdate){			
			contactNew_tv.setVisibility(TextView.VISIBLE);
		}else{
			contactNew_tv.setVisibility(TextView.INVISIBLE);
		}
		ImageViewLoader imageViewLoader = new ImageViewLoader(context);
		imageViewLoader.loadImageToView(new Contact(cursor), viewHolder.contactIcon);		

		viewHolder.contactName.setText(name);
		// leader mode
		if(isFree && isLead){
			viewHolder.mvpnText.setVisibility(TextView.VISIBLE);
		}else if( isFree){
			viewHolder.mvpnText.setVisibility(TextView.VISIBLE);
		}else{
			viewHolder.mvpnText.setVisibility(TextView.INVISIBLE);
		}
		
		view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				FragmentTransaction ft = ((ActionBarActivity)context).getSupportFragmentManager().beginTransaction();
				DialogFragment contactInfoFragment = new ContactInfo();
				Bundle args = new Bundle();
				args.putSerializable(Contact.BUNDLE_CONTACT, contact);
				contactInfoFragment.setArguments(args);
				contactInfoFragment.show(ft, "dialog");
				
			}			
		});
		boolean isFavortie = contactDB.queryContactIsFavorite(contactUUID, unitUUID);
		if(isFavortie){
			viewHolder.ibtnFavorite.setImageResource(R.drawable.btn_important_click);			
		}else{
			viewHolder.ibtnFavorite.setImageResource(R.drawable.btn_important);	
		}		
		viewHolder.ibtnFavorite.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				boolean isFavorite = contactDB.queryContactIsFavorite(contactUUID, unitUUID);
				if(isFavorite){					
					viewHolder.ibtnFavorite.setImageResource(R.drawable.btn_important);	
				}else{
					viewHolder.ibtnFavorite.setImageResource(R.drawable.btn_important_click);
				}
				TabHistory.updateHistoryHandler.sendEmptyMessage(0);
				contactDB.updateContactFavorite(contactUUID, unitUUID, !isFavorite);
			}
			
		});
	}


	@Override
	protected void bindGroupView(View view, final Context context, Cursor cursor,
			boolean isExpanded) {
		// TODO Auto-generated method stub
		if(view==null || cursor==null)
			return;
		ViewHolder viewHolder = (ViewHolder)view.getTag();
		final String groupName = cursor.getString( cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_NAME));
		final String groupType = cursor.getString( cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_TYPE));
		final String groupPoint = cursor.getString( cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_POINT));
		final String groupPrivacy = cursor.getString( cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_PRIVACY));
		final String groupUUID = cursor.getString(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_UUID));
		final int groupLead = cursor.getInt(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_LEAD));
		int groupNewUpdate = cursor.getInt(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_UPDATE));

		if(groupType.equals(ContactGroup.TYPE_GLORY)
				|| groupType.equals(ContactGroup.TYPE_GLORY_MULTI)|| groupType.equals(ContactGroup.TYPE_GLORY_UNI)){					
			viewHolder.groupLayout.setBackgroundResource(R.drawable.contact_group_shape_glory);
			if(cursor.getInt(cursor.getColumnIndex(GroupEntry.COLUMN_GROUP_LEAD))>0){
//				glory current point 
				NumberFormat nf = NumberFormat.getInstance();
				float point = Float.valueOf(groupPoint);
				viewHolder.groupPointText.setText("$ "+ nf.format(point)+ "ÂI");	
				viewHolder.groupPointText.setBackgroundColor(context.getResources().getColor(R.color.elite_theme_color_contact_item_point_background_glory));
				if(point<10)
					viewHolder.groupPointText.setTextColor(context.getResources().getColor(R.color.elite_theme_color_contact_group_item_members_new));
			}else{
				viewHolder.groupPointText.setText("");
				viewHolder.groupPointText.setBackgroundColor(Color.TRANSPARENT);
			}
		}else{
			viewHolder.groupLayout.setBackgroundResource(R.drawable.contact_group_shape);	
			viewHolder.groupPointText.setBackgroundColor(Color.TRANSPARENT);
			viewHolder.groupPointText.setText("");		
		}
		viewHolder.groupSetting.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				FragmentTransaction ft = ((ActionBarActivity)context).getSupportFragmentManager().beginTransaction();
				DialogFragment contactInfoFragment = new GroupSetting();
				Bundle args = new Bundle();
				args.putString(ContactGroup.UNIT_NAME, groupName);
				args.putString(ContactGroup.UNIT_TYPE, groupType);
				args.putString(ContactGroup.CURRENT_POINT, groupPoint);
				args.putString(ContactGroup.PRIVACY, groupPrivacy);
				args.putString(ContactGroup.UNIT_UUID, groupUUID);
				args.putInt(ContactGroup.IS_LEADER, groupLead);
				contactInfoFragment.setArguments(args);
				contactInfoFragment.show(ft, "dialog");
			}
			
		});
		
		//set group members hint
		viewHolder.groupNameText.setText(groupName);
		Cursor groupContactsMembersCursor = contactDB.queryContacts(groupUUID);
		int members = groupContactsMembersCursor.getCount();
		viewHolder.groupMembersText.setText(""+members);
		Cursor tempCursor = groupDB.queryGroupUUID(groupUUID);
		if(isExpanded){
			if(groupNewUpdate>0){
				groupDB.updateGroupUpdate(groupUUID, false);
				viewHolder.groupMembersText.setBackgroundResource(R.drawable.contact_group_members);
				this.changeCursor(groupDB.queryAllGroup()); //update view		
			}else{
				contactDB.updateContactNewUpdate(groupUUID, false);
			}
		}else{ //not expanded
			if(groupNewUpdate>0){
				viewHolder.groupMembersText.setBackgroundResource(R.drawable.contact_group_members_new);
			}else{
				viewHolder.groupMembersText.setBackgroundResource(R.drawable.contact_group_members);
			}
		}

	}

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		// TODO Auto-generated method stub		
		return ContactDBHelper.getInstance(context).queryContacts(groupCursor.getString(groupCursor.getColumnIndex(GroupEntry.COLUMN_GROUP_UUID)));
	}

	@Override
	protected View newChildView(Context context, Cursor cursor,
			boolean isLastChild, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = (View)inflater.inflate(R.layout.contact_listrow_contact, null);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.contactIcon = (ImageView)view.findViewById(R.id.contact_icon_niv);
		viewHolder.contactIcon.setImageResource(R.drawable.contact_head_big);
		viewHolder.contactName = (TextView) view.findViewById(R.id.contact_name_tv);
		viewHolder.mvpnText = (TextView)view.findViewById(R.id.contact_mvpn_tv);
		viewHolder.ibtnFavorite = (ImageButton)view.findViewById(R.id.contact_favorite_ibtn);
		viewHolder.contactLayout = (LinearLayout)view.findViewById(R.id.contact_contact_layout);
		view.setTag(viewHolder);

		return view;
	}

	@Override
	protected View newGroupView(final Context context, Cursor cursor,
			boolean isExpanded, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = (View)inflater.inflate(R.layout.contact_listrow_group, null);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.groupLayout = (LinearLayout)view.findViewById(R.id.group_layout);
		viewHolder.groupNameText = (TextView)view.findViewById(R.id.contact_group_name_tv);	
		viewHolder.groupPointText = (TextView)view.findViewById(R.id.group_point_tv);
		viewHolder.groupSetting = (ImageView)view.findViewById(R.id.group_setting_ib);
		viewHolder.groupMembersText = (TextView)view.findViewById(R.id.contact_group_members_tv);
		view.setTag(viewHolder);
		return view;
	}
	
	public class ViewHolder{
		TextView contactName;
		ImageView contactIcon;
		TextView mvpnText;
		ImageButton ibtnFavorite;
		TextView groupNameText;
		LinearLayout contactLayout;
//		RelativeLayout groupLayout;
		LinearLayout groupLayout;
		TextView groupPointText ;
		ImageView groupSetting;
		TextView groupMembersText ;
	}
	
	private class RoundedFadeInBitmapDisplayer extends FadeInBitmapDisplayer{


		public RoundedFadeInBitmapDisplayer(int durationMillis,
				boolean animateFromNetwork, boolean animateFromDisk,
				boolean animateFromMemory) {
			super(durationMillis, animateFromNetwork, animateFromDisk, animateFromMemory);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void display(Bitmap bitmap, ImageAware imageAware,
				LoadedFrom loadedFrom) {
			// TODO Auto-generated method stub
			super.display(DrawCircularImage.DrawCircular(bitmap), imageAware, loadedFrom);
		}
		
		public  void animateLoad(View imageView, int durationMillis) {
			if (imageView != null) {
				AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
				fadeImage.setDuration(durationMillis);
				fadeImage.setInterpolator(new DecelerateInterpolator());
				imageView.startAnimation(fadeImage);
			}
		}
	}
}
