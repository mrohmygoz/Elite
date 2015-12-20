package com.uninum.elite.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.uninum.elite.R;
import com.uninum.elite.adapter.ContactAdapter.ViewHolder;
import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.ContactDBHelper.ContactEntry;
import com.uninum.elite.image.DrawCircularImage;
import com.uninum.elite.object.Contact;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.ui.ContactInfo;
import com.uninum.elite.ui.TabHistory;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorJoiner.Result;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SearchAdapter extends CursorAdapter implements Filterable{

	private Context context;
	private ContactDBHelper contactDB;
	private GroupDBHelper groupDB;
	private static final List displayedImages = Collections.synchronizedList(new LinkedList());
	public SearchAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
		this.context = context;
		contactDB = ContactDBHelper.getInstance(context);
		groupDB = GroupDBHelper.getInstance(context);
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		if(view==null || cursor==null)
			return ;
		final Contact contact = new Contact(cursor);
		final String name = contact.getName();
		final String phone = contact.getPhone();
		final String picID = contact.getPicture();
		final String unitUUID = contact.getUnitUUID();
		final String unitName = contact.getUnitname();
		final String unitType = contact.getUnitType();
		final String contactUUID = contact.getContactUUID();
		final boolean isLead = contact.isLeader();
		final boolean isFree = contact.isFree();
		final boolean isPrivate = contact.isPrivate();
		final boolean isNewUpdate = contact.isNewUpdate();
		final String leadNum = groupDB.queryGroupLead(unitUUID);
		final ViewHolder viewHolder = (ViewHolder)view.getTag();
		viewHolder.groupNameTV.setText(unitName);
		if(!picID.equals(Contact.DEFAULT_PICTURE)){
			String url = "";
			url =  "https://ts.kits.tw/projectLYS/v2/File/"+SystemManager.getToken(context)+"/image/"+picID+"?timeStamp=0&unitUuid="+unitUUID;			
			ImageLoader.getInstance().displayImage(url, viewHolder.contactIcon, new ImageLoadingListener(){
				
				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
					// TODO Auto-generated method stub
					
				}
	
				@Override
				public void onLoadingComplete(String imageUrl, View view, Bitmap loadedImage) {
					// TODO Auto-generated method stub
	//				if(loadedImage!=null)
	//				((ImageView)view ).setImageBitmap(DrawCircularImage.DrawCircular(loadedImage));
					if (loadedImage != null) {
						ImageView imageView = (ImageView) view;
						boolean firstDisplay = !displayedImages.contains(imageUrl);
						if (firstDisplay) {
							AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
							fadeImage.setDuration(500);
							fadeImage.setInterpolator(new DecelerateInterpolator());
							imageView.setImageBitmap(DrawCircularImage.DrawCircular(loadedImage));
							imageView.startAnimation(fadeImage);					
						} else {
							imageView.setImageBitmap(DrawCircularImage.DrawCircular(loadedImage));
						}
						displayedImages.add(imageUrl);
					}
				}
	
				@Override
				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
					// TODO Auto-generated method stub
					
				}
	
				@Override
				public void onLoadingStarted(String arg0, View arg1) {
					// TODO Auto-generated method stub
					
				}
			});
		}else{
			ImageLoader.getInstance().displayImage("drawable://"+R.drawable.contact_head_big, viewHolder.contactIcon);			
		}
		
		viewHolder.contactName.setText(name);
		// leader mode
		if( isFree && isLead){
			viewHolder.mvpnText.setVisibility(TextView.VISIBLE);
		}else if(isFree){
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
				Log.d("kits new","isFavorite:"+isFavorite);
				if(isFavorite){					
					viewHolder.ibtnFavorite.setImageResource(R.drawable.btn_important);	
				}else{
					viewHolder.ibtnFavorite.setImageResource(R.drawable.btn_important_click);
				}
//				TabHistory.updateHistoryHandler.sendEmptyMessage(0);
				contactDB.updateContactFavorite(contactUUID, unitUUID, !isFavorite);
			}
			
		});
	}

	@Override
	public View newView(Context context, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = (View)inflater.inflate(R.layout.contact_search_listrow, null);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.contactIcon = (ImageView)view.findViewById(R.id.contact_search_icon_niv);
		viewHolder.contactIcon.setImageResource(R.drawable.contact_head_big);
		viewHolder.contactName = (TextView) view.findViewById(R.id.contact_search_name_tv);
		viewHolder.mvpnText = (TextView)view.findViewById(R.id.contact_search_mvpn_tv);
		viewHolder.ibtnFavorite = (ImageButton)view.findViewById(R.id.contact_search_favorite_ibtn);
		viewHolder.contactLayout = (LinearLayout)view.findViewById(R.id.contact_search_contact_layout);
		viewHolder.groupNameTV = (TextView)view.findViewById(R.id.contact_search_group_tv);
		
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
		TextView groupNameTV;
	}

}
