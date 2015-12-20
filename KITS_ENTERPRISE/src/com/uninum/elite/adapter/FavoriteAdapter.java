package com.uninum.elite.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.uninum.elite.R;
import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.ContactDBHelper.ContactEntry;
import com.uninum.elite.image.DrawCircularImage;
import com.uninum.elite.image.ImageViewLoader;
import com.uninum.elite.object.Contact;
import com.uninum.elite.object.ContactGroup;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.ui.ContactInfo;
import com.uninum.elite.ui.TabFavorite;
import com.uninum.elite.ui.TabHistory;
import com.uninum.elite.webservice.WSCall;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FavoriteAdapter extends CursorAdapter {
	private Context context;
	private ContactDBHelper contactDB;
	private static final List displayedImages = Collections.synchronizedList(new LinkedList());
	public FavoriteAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		// TODO Auto-generated constructor stub
		this.context = context;
		contactDB = ContactDBHelper.getInstance(context);
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		if(cursor==null)
			return ;
		final Contact contact = new Contact(cursor);
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
		TextView favoriteName = (TextView)view.findViewById(R.id.favorite_name_tv);
		favoriteName.setText(name);
		ImageButton userHead = (ImageButton)view.findViewById(R.id.favorite_bighead_ibtn);
		userHead.setOnClickListener(new OnClickListener(){

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
//		userHead.setImageResource(R.drawable.contact_head_big);
//		userHead.setTag(contactUUID);
//		String timeStamp = "0";
//		DownloadImageTask downloadImg = new DownloadImageTask(context, userHead, view, picID, timeStamp, contactUUID);
//		downloadImg.execute();
		ImageViewLoader imageViewLoader = new ImageViewLoader(context);
		imageViewLoader.loadImageToView(contact, userHead);
	
		ImageButton favoriteCall = (ImageButton)view.findViewById(R.id.favorite_call_ibtn);
		favoriteCall.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d("KITs Favorite","UnitType:"+unitType);
				if(unitType.equals(ContactGroup.TYPE_GLORY)&& isFree){
					WSCall.CallingWithTimer wsCalling = new WSCall.CallingWithTimer();	
					wsCalling.Calling(context,SystemManager.getToken(context), contactUUID, unitUUID, name );
				}else if(unitType.equals(ContactGroup.TYPE_GLORY_MULTI)&& isFree){
						WSCall.CallingWithTimer wsCalling = new WSCall.CallingWithTimer();	
						wsCalling.Calling(context,SystemManager.getToken(context), contactUUID, unitUUID, name);
				}else if(unitType.equals(ContactGroup.TYPE_GLORY_UNI)&& isFree){
						WSCall.CallingWithTimer wsCalling = new WSCall.CallingWithTimer();	
						wsCalling.Calling(context,SystemManager.getToken(context), contactUUID, unitUUID, name );			
				}else{
					if( !isPrivate){
					Intent callIntent = new Intent(Intent.ACTION_CALL);
				    callIntent.setData(Uri.parse("tel:"+phone));
				    context.startActivity(callIntent);
					}
				}
			}
			
		});
		ImageButton favoriteDelete = (ImageButton)view.findViewById(R.id.favorite_delete_ibtn);
		favoriteDelete.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				contactDB.updateContactFavorite(contactUUID, unitUUID, false);
				swapCursor(ContactDBHelper.getInstance(context).queryFavoriteContacts());
				TabFavorite.updateFavHandler.sendEmptyMessage(0);
				TabHistory.updateHistoryHandler.sendEmptyMessage(0);
			}
			
		});
		RelativeLayout favoriteShape = (RelativeLayout)view.findViewById(R.id.favorite_shape_ll);
		RelativeLayout favortieBottom = (RelativeLayout)view.findViewById(R.id.favorite_bottom_layout);
		ImageView favoriteIcon = (ImageView)view.findViewById(R.id.favorite_glory_icon);
		if(unitType.equals(ContactGroup.TYPE_GLORY)){
			favoriteShape.setBackgroundResource(R.drawable.favorite_rectangle_shape_glory);
			favortieBottom.setBackgroundResource(R.drawable.favorite_rectangle_shape_bottom_glory);
			favoriteIcon.setVisibility(ImageView.VISIBLE);
		}else if(unitType.equals(ContactGroup.TYPE_GLORY_MULTI)){
			favoriteShape.setBackgroundResource(R.drawable.favorite_rectangle_shape_glory);
			favortieBottom.setBackgroundResource(R.drawable.favorite_rectangle_shape_bottom_glory);
			favoriteIcon.setVisibility(ImageView.VISIBLE);
		}else if(unitType.equals(ContactGroup.TYPE_GLORY_UNI)){
			favoriteShape.setBackgroundResource(R.drawable.favorite_rectangle_shape_glory);
			favortieBottom.setBackgroundResource(R.drawable.favorite_rectangle_shape_bottom_glory);
			favoriteIcon.setVisibility(ImageView.VISIBLE);
		}else{
			favoriteShape.setBackgroundResource(R.drawable.favorite_rectangle_shape);
			favortieBottom.setBackgroundResource(R.drawable.favorite_rectangle_shape_bottom);
			favoriteIcon.setVisibility(ImageView.INVISIBLE);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = (View)inflater.inflate(R.layout.favorite_item, null);
		return view;
	}
	
	

}
