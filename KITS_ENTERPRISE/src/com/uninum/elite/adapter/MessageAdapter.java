package com.uninum.elite.adapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.uninum.elite.R;
import com.uninum.elite.database.MessageDBHelper;
import com.uninum.elite.database.MessageDBHelper.GroupMessageEntry;
import com.uninum.elite.image.ImageViewLoader;
import com.uninum.elite.object.Contact;
import com.uninum.elite.object.SingleMessage;
import com.uninum.elite.ui.Activity_Message;
import com.uninum.elite.ui.ContactInfo;
import com.uninum.elite.ui.DownloadActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageAdapter extends CursorAdapter {

	private final int TYPE_ME = 0;
	private final int TYPE_OTHER = 1;
	private static final String TAG = "Jenny";
	
	private Context context;
	private Cursor cursor;
	private int type;
	private SimpleDateFormat formatTime;
	private Date callDate;
	private String url;

	public MessageAdapter(Context context, Cursor c, String url) {
		super(context, c);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.cursor = c;
		this.url = url;
	}

	@Override
	public void bindView(View view, Context cont, Cursor cur) {
		// TODO Auto-generated method stub
		int position = cur.getPosition();
		type = getItemViewType(position);
		final ViewHolder holder = (ViewHolder) view.getTag();
		final SingleMessage singlemsg = new SingleMessage(cur,context);
		holder.message.setText(singlemsg.getMessage());
		
		if (singlemsg.getType()==0) {
			setVisibility(0, holder);
		} else if (singlemsg.getType()==1){
			setVisibility(1, holder);
			ImageViewLoader ivl = new ImageViewLoader(context);
			ivl.loadImageToView(singlemsg.getPreviewUrl(), holder.image);
//			Log.d("Jenny","msg adapter preview url: " + singlemsg.getPreviewUrl());
			holder.image.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, DownloadActivity.class);
					intent.putExtra("url", singlemsg.getDownloadUrl());
					intent.putExtra("type", 1);
					intent.putExtra("name", singlemsg.getMessage());
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					context.startActivity(intent);
				}
				
			});
		} else if (singlemsg.getType()==2) {
			setVisibility(2, holder);
//			Log.d("Jenny","video download url: " + singlemsg.getDownloadUrl());
			ImageViewLoader ivl = new ImageViewLoader(context);
			ivl.loadImageToView(singlemsg.getPreviewUrl(), holder.video);
			holder.play.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Log.e("Jenny", "msg adapter video clicked!");
					Intent intent = new Intent(context, DownloadActivity.class);
					intent.putExtra("url", singlemsg.getDownloadUrl());
					intent.putExtra("type", 2);
					intent.putExtra("name", singlemsg.getMessage());
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					context.startActivity(intent);
				}
			});
			
		} else if (singlemsg.getType()==4) {
			setVisibility(4, holder);
			holder.message.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					new AsyncTask<String, Void, String>(){
						
						InputStream input;
						OutputStream output;
						HttpURLConnection connection;

						@SuppressLint("NewApi")
						@Override
						protected String doInBackground(String... params) {
							try {
								
								Log.i(TAG, "msg adapter msg type: " + singlemsg.getTypeString());
								Log.i(TAG, "msg adapter file type: " + singlemsg.getMessage().substring(  singlemsg.getMessage().lastIndexOf(".")+1  ));
								
								DownloadManager dm = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
								Uri Download_Uri = Uri.parse(singlemsg.getDownloadUrl());
								DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
								request.setTitle(singlemsg.getMessage())
										.setMimeType(singlemsg.getTypeString() + "/" + singlemsg.getMessage().substring(  singlemsg.getMessage().lastIndexOf(".")+1  ))
										.setDescription("elite download")
										.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,singlemsg.getMessage());
								request.allowScanningByMediaScanner();
								long downloadReference = dm.enqueue(request);
								Log.i("Jenny", "downloadReference: " + downloadReference);
					            
					            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					            intent.setData(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory((Environment.DIRECTORY_DOWNLOADS)),singlemsg.getMessage())));
				            	context.sendBroadcast(intent);
							} finally {
								try {
									if (input!=null)
										input.close();
									if (output!=null)
										output.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
								if (connection != null)
					                connection.disconnect();
							}
							return null;
						}

						@Override
						protected void onPostExecute(String result) {
							super.onPostExecute(result);
							if (result!=null)
								Log.e("Jenny", "msg adapter on post execute: "+result.toString());
						}
						
					}.execute(singlemsg.getDownloadUrl());
				}
				
			});
		} 
		
		
		//set date label
		formatTime = new SimpleDateFormat(" M/d'（'E'）'");
		callDate = new Date(singlemsg.getCreatedTime());
		String date = formatTime.format(callDate);
		callDate = new Date(System.currentTimeMillis());
		String now = formatTime.format(callDate);
		if (cur.moveToPrevious()){
			callDate = new Date(cur.getLong(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME)));
			String prevDate = formatTime.format(callDate);
			if (!date.equals(prevDate)) {
				if (now.equals(date))
					holder.date_label.setText("  今 天  ");
				else if (System.currentTimeMillis()-singlemsg.getCreatedTime() <= 1000*60*60*48)
					holder.date_label.setText("  昨 天  ");
				else
					holder.date_label.setText(date);
				holder.date_label.setVisibility(View.VISIBLE);
			}else
				holder.date_label.setVisibility(View.GONE);
		}else{
			if (now.equals(date))
				holder.date_label.setText("  今 天  ");
			else if (System.currentTimeMillis()-singlemsg.getCreatedTime() <= 1000*60*60*48)
				holder.date_label.setText("  昨 天  ");
			else
				holder.date_label.setText(date);
			holder.date_label.setVisibility(View.VISIBLE);
		}
		
		//set time
		formatTime = new SimpleDateFormat("H:mm");
		callDate = new Date(singlemsg.getCreatedTime());
		String time =  formatTime.format(callDate);
		if (singlemsg.getStatus().equals(SingleMessage.STATUS_SENDING) || singlemsg.getStatus().equals(SingleMessage.STATUS_FAILED)) {
			holder.time.setVisibility(View.INVISIBLE);
		} else {
			holder.time.setVisibility(View.VISIBLE);
			holder.time.setText(time);
		}
		
		if (type==TYPE_ME){ //0
			if (singlemsg.getStatus().equals(SingleMessage.STATUS_FAILED)) 
				holder.warning.setVisibility(View.VISIBLE);
			else
				holder.warning.setVisibility(View.GONE);
			
			holder.warning.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {

					final String where = GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID + " = ?";
					final String[] selectionArgs = {singlemsg.getMessageUUID()};
					
					new AlertDialog.Builder(context)
					.setTitle(R.string.message_send_failed)
					.setPositiveButton(R.string.message_resend, new  DialogInterface.OnClickListener ()  {			
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//change message status to sending
						    ContentValues values = new ContentValues();
						    values.put(GroupMessageEntry.COLUMN_MESSAGE_STATUS, SingleMessage.STATUS_SENDING);
							context.getContentResolver().update(Uri.parse(url), values, where, selectionArgs);
							
							JSONObject object = singlemsg.getJSON();
							Message msg = new Message();
							msg.obj = object;
							Activity_Message.sendMsgTask.sendMessage(msg);
						}					
					})
					.setNeutralButton(R.string.message_delete, new  DialogInterface.OnClickListener (){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Activity_Message.decreaseCurrentPos.sendEmptyMessage(0);
							context.getContentResolver().delete(Uri.parse(url), where, selectionArgs);
						}
						
					})
					.setNegativeButton(R.string.history_delete_no, new  DialogInterface.OnClickListener ()  {
				
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							return ;
						}
						
					}).show();	
				}
			});
			
			if (singlemsg.getReadedNumber()==0)
				holder.readed_num.setVisibility(View.INVISIBLE);
			else{
				holder.readed_num.setVisibility(View.VISIBLE);
				holder.readed_num.setText("已讀 " + singlemsg.getReadedNumber());
			}
		}else{ //1
			holder.name.setText(singlemsg.getFromName());
			ImageViewLoader imageViewLoader = new ImageViewLoader(context);
			final Contact contact = singlemsg.getContact();
			if (contact!=null){
				imageViewLoader.loadImageToView(contact, holder.bighead);
				holder.bighead.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						FragmentTransaction ft = ((ActionBarActivity)context).getSupportFragmentManager().beginTransaction();
						DialogFragment contactInfoFragment = new ContactInfo();
						Bundle args = new Bundle();
						args.putSerializable(Contact.BUNDLE_CONTACT, contact);
						contactInfoFragment.setArguments(args);
						contactInfoFragment.show(ft, "dialog");
					}
				});
			}
			else
				Log.d("Jenny", "msg adapter: " + singlemsg.getFromName() + " set bighead failed");
		}
	}

	@Override
	public View newView(Context cont, Cursor cur, ViewGroup arg2) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view;
		ViewHolder holder = new ViewHolder();
		int position = cur.getPosition();
//		Log.d("Jenny", "msg adapter: new view at " + position);
		type = getItemViewType(position);
		if (type==TYPE_ME){ //0
			view = inflater.inflate(R.layout.message_dialog_listrow_me, null);
			holder.date_label = (TextView) view.findViewById(R.id.date_label_me);
			holder.message = (TextView) view.findViewById(R.id.single_message);
			holder.image = (ImageView) view.findViewById(R.id.single_message_iv);
			holder.videoframe = (FrameLayout) view.findViewById(R.id.single_message_vv_frame);
			holder.video = (ImageView) view.findViewById(R.id.single_message_vv);
			holder.time = (TextView) view.findViewById(R.id.single_msg_time);
			holder.readed_num = (TextView) view.findViewById(R.id.readed_number);
			holder.warning = (Button) view.findViewById(R.id.button_warning);
			holder.play = (Button) view.findViewById(R.id.video_play);
		}else{ //1
			view = inflater.inflate(R.layout.message_dialog_listrow_other, null);
			holder.date_label = (TextView) view.findViewById(R.id.date_label_other);
			holder.message = (TextView) view.findViewById(R.id.single_message_other);
			holder.image = (ImageView) view.findViewById(R.id.single_message_iv_other);
			holder.videoframe = (FrameLayout) view.findViewById(R.id.single_message_vv_other_frame);
			holder.video = (ImageView) view.findViewById(R.id.single_message_vv_other);
			holder.time = (TextView) view.findViewById(R.id.single_msg_time_other);
			holder.name = (TextView) view.findViewById(R.id.single_message_name);
			holder.bighead = (ImageView) view.findViewById(R.id.msg_dialog_bighead);
			holder.play = (Button) view.findViewById(R.id.video_play_other);
		}
		view.setTag(holder);
		return view;
	}

	
	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		Cursor cursor = (Cursor) getItem(position);
		type = getItemType(cursor);
		return type;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}
	
	private int getItemType(Cursor cursor){
		if (cursor.getString(cursor.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_FROM_UUID))
				.equals(cursor.getString(cursor.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_FROM_NAME))))
			return TYPE_ME;
		else
			return TYPE_OTHER;
	}
	
	private class ViewHolder {
		TextView date_label;
		TextView message;
		ImageView image;
		FrameLayout videoframe;
		ImageView video;
		TextView time;
		TextView name;
		ImageView bighead;
		TextView readed_num;
		Button warning;
		Button play;
	}
	
	//0: message, 1:image, 3:video, 4:file
	private void setVisibility (int type, ViewHolder holder) {
		switch (type) {
		case 0:
			holder.message.setVisibility(View.VISIBLE);
			holder.image.setVisibility(View.GONE);
			holder.videoframe.setVisibility(View.GONE);
			holder.message.setClickable(false);
			holder.message.setPaintFlags(0);
			holder.message.setTextColor(Color.BLACK);
			holder.message.setTypeface(null, Typeface.NORMAL);
			return;
		case 1:
			holder.message.setVisibility(View.GONE);
			holder.image.setVisibility(View.VISIBLE);
			holder.videoframe.setVisibility(View.GONE);
			return;
		case 2:
			holder.message.setVisibility(View.GONE);
			holder.image.setVisibility(View.GONE);
			holder.videoframe.setVisibility(View.VISIBLE);
			return;
		case 4:
			holder.message.setVisibility(View.VISIBLE);
			holder.image.setVisibility(View.GONE);
			holder.videoframe.setVisibility(View.GONE);
			holder.message.setClickable(true);
			holder.message.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
			holder.message.setTextColor(Color.BLUE);
			holder.message.setTypeface(null, Typeface.BOLD);
			return;
		}
	}
}
