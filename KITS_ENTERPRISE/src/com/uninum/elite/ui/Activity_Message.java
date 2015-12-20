package com.uninum.elite.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.uninum.elite.R;
import com.uninum.elite.adapter.MessageAdapter;
import com.uninum.elite.data.UpdateMessage;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.MessageDBHelper;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.database.MessageDBHelper.GroupMessageEntry;
import com.uninum.elite.object.SingleMessage;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.MyContentProvider;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.utility.LoginService;
import com.uninum.elite.webservice.PubnubHelper;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import me.leolin.shortcutbadger.ShortcutBadger;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

public class Activity_Message extends ActionBarActivity implements LoaderCallbacks<Cursor>, OnRefreshListener{
	
	public static final String GROUP_NAME = "GROUP_NAME";
	public static final String GROUP_UUID = "GROUP_UUID";
	public static final String SELF_UUID = "SELF_UUID";
	public static final String UPLOAD_FILE_URL = "https://ts.kits.tw/rest/v1/File/mediaObject/";
	
	private static final int REQUEST_PICK_IMAGE = 300;
	private static final int REQUEST_PICK_VIDEO = 400;
	private static final int REQUEST_TAKE_IMAGE = 500;
	private static final int REQUEST_TAKE_VIDEO = 600;
	
	public static Handler fetchMoreHandler, fetchNoMore, fetchError, stopService, updateMessage, 
							timerStop, sendMsgFailed, decreaseCurrentPos, sendMsgTask, catchPhoto;

	private ListView 		MessageDialog;
	private Button 			sendBtn, addFile;
	private LinearLayout 	up;
	private FrameLayout		delete;
	private EditText 		editText;
	private String 			groupUUID, groupName, selfUUID, url, fileName;
	private MessageAdapter 	adapter;
	private Context 		context;
	private MessageDBHelper msgDB;
	private int 			currentPos;
	private UpdateMessage 	updateMsg;
	private Timer			timer, failTimer;
	private PullToRefreshLayout mPullToRefreshLayout;
//	private BottomSheet.Builder bottomSheetBuilder;
	
	//fetch more
	private ProgressBar 	fetchmore_pb;
	private Button 			fetchmore_btn;
	private View 			loadmoreView;
	
	//pubnub
    private PubnubHelper 	pubnubHelper;
	private LoaderManager.LoaderCallbacks<Cursor> loaderCallback;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d("Jenny", "activity msg on create");
		
		Bundle bundle = new Bundle();
		bundle = getIntent().getExtras();
		groupName = bundle.getString(GROUP_NAME);
		groupUUID = bundle.getString(GROUP_UUID);
		selfUUID = bundle.getString(SELF_UUID);
		url = "content://" + MyContentProvider.AUTHORITY + "/" + groupUUID;
		
		//set action bar
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.message_actionbar, null);
		TextView title = (TextView) view.findViewById(R.id.actionbar_title);
		title.setText(groupName);
		ActionBar ab = getActionBar();
		ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
		ab.setDisplayShowHomeEnabled(false);
		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayShowCustomEnabled(true);
		ab.setCustomView(view,params);
		
		setContentView(R.layout.activity_message_dialog);
		pubnubHelper = new PubnubHelper(this);
		context = this;
		updateMsg = new UpdateMessage(context);
		
		//set widget
		editText = (EditText) findViewById(R.id.text_field_text);
		sendBtn = (Button) findViewById(R.id.text_field_send);
		MessageDialog = (ListView) findViewById(R.id.dialog_list);
		loadmoreView = inflater.inflate(R.layout.footerview_loadmore, null, false);
		MessageDialog.addHeaderView(loadmoreView);
		fetchmore_btn = (Button)loadmoreView.findViewById(R.id.footer_fetchmore_btn);
		fetchmore_pb = (ProgressBar)loadmoreView.findViewById(R.id.footerview_pb);
		delete = (FrameLayout) view.findViewById(R.id.message_delete);
		up = (LinearLayout) view.findViewById(R.id.message_up);
		mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
		addFile = (Button) findViewById(R.id.add_file);
		ActionBarPullToRefresh.from(this)
						        .allChildrenArePullable()
						        .listener(this)
						        .setup(mPullToRefreshLayout);
		
		loaderCallback = this;
		createHandler();
		CheckLogin();
		msgDB = MessageDBHelper.getInstance(this);
		KitsApplication.getInstance().MsgActivityStarted();
		
		
		delete.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(context)
				.setTitle(R.string.message_delete_all_title)
				.setPositiveButton(R.string.message_delete_yes, new  DialogInterface.OnClickListener ()  {			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						getContentResolver().delete(
								Uri.parse("content://" + MyContentProvider.AUTHORITY + "/" + groupUUID), null, null);
					}					
				})
				.setNegativeButton(R.string.message_delete_no, new  DialogInterface.OnClickListener ()  {
			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						return ;
					}
					
				}).show();
			}
			
		});
		
		up.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
			
		});
		
		sendBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String text = editText.getText().toString();
				if (!text.equals("")) {
					editText.setText("");

					try {
						
						String msgid = UUID.randomUUID().toString();
						ContentValues values = new ContentValues();
						values.put(GroupMessageEntry.COLUMN_MESSAGE_MSG, text);
						values.put(GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID, msgid);
						values.put(GroupMessageEntry.COLUMN_MESSAGE_FROM_UUID, selfUUID);
						values.put(GroupMessageEntry.COLUMN_MESSAGE_FROM_NAME, selfUUID);
						values.put(GroupMessageEntry.COLUMN_MESSAGE_GROUP_UUID, groupUUID);
						values.put(GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME, System.currentTimeMillis());
						values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_TIME, "");
						values.put(GroupMessageEntry.COLUMN_MESSAGE_TYPE, SingleMessage.JSON_TYPE[0]);
						values.put(GroupMessageEntry.COLUMN_MESSAGE_STATUS, SingleMessage.STATUS_SENDING);
						values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_NUMBER, 0);
						
						JSONObject object = new JSONObject();
						object.put(SingleMessage.JSON_TAG_MSGID, msgid);
						object.put(SingleMessage.JSON_TAG_MESSAGE, text);
						object.put(SingleMessage.JSON_TAG_FROM, selfUUID);
						object.put(SingleMessage.JSON_TAG_TO, groupUUID);
						object.put(SingleMessage.JSON_TAG_TYPE, SingleMessage.JSON_TYPE[0]);
						
						Message msg = new Message();
						msg.obj = object;
						context.getContentResolver().insert(Uri.parse("content://" + MyContentProvider.AUTHORITY + "/" + groupUUID), values);
						sendMsgTask.sendMessage(msg);
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
			}
			
		});
		

		fetchmore_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				fetchmore_pb.setVisibility(View.VISIBLE);
				UpdateMessage updateMsg = new UpdateMessage(context);
				Cursor cur = msgDB.queryAllMessage(groupUUID);
				if (cur.moveToFirst()){
					long timeStamp = cur.getLong(cur.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME)) - 1;
					updateMsg.updateOldMsgRequest(timeStamp, groupUUID);
					Log.i("Jenny", "fetch more message pressed, timestamp=" + timeStamp);
				}else
					updateMsg.updateOldMsgRequest(System.currentTimeMillis(), groupUUID);
				
			}
			
		});
		
		addFile.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				FragmentTransaction ft = ((ActionBarActivity)context).getSupportFragmentManager().beginTransaction();
				DialogFragment addfileFragment = new AddFile();
				addfileFragment.show(ft, "dialog");
			}
			
		});
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		switch (requestCode){
		case REQUEST_PICK_IMAGE:
			
			if (resultCode == Activity_Message.this.RESULT_OK){
				String[] filePathColumn = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME };
				Cursor cursor = getContentResolver().query(intent.getData(), filePathColumn, null, null, null);
				cursor.moveToFirst();
				String picturePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
				String displayName = cursor.getString(cursor.getColumnIndex(filePathColumn[1]));
	            cursor.close();

	            String msgid = UUID.randomUUID().toString();
				Intent uploadIntent = new Intent(Activity_Message.this,UploadActivity.class);
	            uploadIntent.putExtra(GROUP_NAME, groupName);
	            uploadIntent.putExtra(GROUP_UUID, groupUUID);
	            uploadIntent.putExtra(SELF_UUID, selfUUID);
				uploadIntent.putExtra("filePath", picturePath);
	            uploadIntent.putExtra("name", displayName);
				uploadIntent.putExtra("requestType", REQUEST_PICK_IMAGE);
				uploadIntent.putExtra("msgId", msgid);
				uploadIntent.putExtra("url", UPLOAD_FILE_URL + SystemManager.getToken(context) + "/image/" + msgid);
				uploadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(uploadIntent);
			}
			
			break;
			
		case REQUEST_PICK_VIDEO:
			
			if (resultCode == Activity_Message.this.RESULT_OK) {
				
				String[] filePathColumn = { MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME };
				Cursor cursor = getContentResolver().query(intent.getData(), filePathColumn, null, null, null);
				cursor.moveToFirst();
				String videoPath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
				String displayName = cursor.getString(cursor.getColumnIndex(filePathColumn[1]));
	            cursor.close();
	            
	            String msgid = UUID.randomUUID().toString();
	            Intent uploadIntent = new Intent(Activity_Message.this,UploadActivity.class);
	            uploadIntent.putExtra(GROUP_NAME, groupName);
	            uploadIntent.putExtra(GROUP_UUID, groupUUID);
	            uploadIntent.putExtra(SELF_UUID, selfUUID);
	            uploadIntent.putExtra("filePath", videoPath);
	            uploadIntent.putExtra("name", displayName);
				uploadIntent.putExtra("requestType", REQUEST_PICK_VIDEO);
				uploadIntent.putExtra("msgId", msgid);
				uploadIntent.putExtra("url", UPLOAD_FILE_URL + SystemManager.getToken(context) + "/video/" + msgid);
				uploadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(uploadIntent);
			}
			
			break;
			
		case REQUEST_TAKE_IMAGE:
			
			if (resultCode == Activity_Message.this.RESULT_OK) {
				
				Intent i = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);
	            i.setData(Uri.fromFile( file ));
            	sendBroadcast(i);
				
            	String msgid = UUID.randomUUID().toString();
	            Intent uploadIntent = new Intent(Activity_Message.this,UploadActivity.class);
	            uploadIntent.putExtra(GROUP_NAME, groupName);
	            uploadIntent.putExtra(GROUP_UUID, groupUUID);
	            uploadIntent.putExtra(SELF_UUID, selfUUID);
	            uploadIntent.putExtra("filePath", file.getAbsolutePath());
	            uploadIntent.putExtra("name", fileName);
				uploadIntent.putExtra("requestType", REQUEST_TAKE_IMAGE);
				uploadIntent.putExtra("msgId", msgid);
				uploadIntent.putExtra("url", UPLOAD_FILE_URL + SystemManager.getToken(context) + "/image/" + msgid);
				uploadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(uploadIntent);
			}
			
			break;
			
		case REQUEST_TAKE_VIDEO:
			
			if (resultCode == Activity_Message.this.RESULT_OK) {
				
				Intent i = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), fileName);
	            i.setData(Uri.fromFile( file ));
            	sendBroadcast(i);
				
            	String msgid = UUID.randomUUID().toString();
	            Intent uploadIntent = new Intent(Activity_Message.this,UploadActivity.class);
	            uploadIntent.putExtra(GROUP_NAME, groupName);
	            uploadIntent.putExtra(GROUP_UUID, groupUUID);
	            uploadIntent.putExtra(SELF_UUID, selfUUID);
	            uploadIntent.putExtra("filePath", file.getAbsolutePath());
	            uploadIntent.putExtra("name", fileName);
				uploadIntent.putExtra("requestType", REQUEST_TAKE_VIDEO);
				uploadIntent.putExtra("msgId", msgid);
				uploadIntent.putExtra("url", UPLOAD_FILE_URL + SystemManager.getToken(context) + "/video/" + msgid);
				uploadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(uploadIntent);
			}
			
			break;
		}
	}

	private class resendTask extends TimerTask{
		
		JSONObject object;
		
		public resendTask(JSONObject object){
			this.object = object;
		}

		@Override
		public void run() {
			Log.i("Jenny", "activity msg: sending message.......");
			updateMsg.sendMsgRequest(object);
		}
		
	}
	
	private class failedTask extends TimerTask{

		JSONObject object;
		
		public failedTask(JSONObject object){
			this.object = object;
		}
		
		@Override
		public void run() {
			timer.cancel();
			timer.purge();
			Message msg = new Message();
			msg.obj = object;
			sendMsgFailed.sendMessage(msg);
		}
		
	}
	
	private void CheckLogin(){
		if (KitsApplication.getInstance().isLogin()){
			Message msg = new Message();
			msg.obj = groupUUID;
			updateMessage.sendMessage(msg);
			return;
		}
		Intent serviceIntent = new Intent(this,LoginService.class);
		Bundle serviebundle = new Bundle();
		serviebundle.putString(LoginService.CALLBACK, "Activity_Message");
		serviceIntent.putExtras(serviebundle);
		startService(serviceIntent);
	}
	
	private void createHandler(){
		
		catchPhoto = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				fileName = (String) msg.obj;
			}
			
		};
		
		sendMsgTask = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				JSONObject object = (JSONObject) msg.obj;
				timer = new Timer();
				failTimer = new Timer();
				timer.scheduleAtFixedRate(new resendTask(object), 0, 1000*5);
				failTimer.schedule(new failedTask(object), 1000*16);
			}
			
		};
		
		timerStop = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (timer!=null) {
					timer.cancel();
					timer.purge();
				}
				if (failTimer!=null) {
					failTimer.cancel();
					failTimer.purge();
				}
			}
			
		};
		
		sendMsgFailed = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				try {
					JSONObject object = (JSONObject) msg.obj;
					String selection = GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID + " = ?";
					String[] selectionArgs = {object.getString(SingleMessage.JSON_TAG_MSGID)};
					String table =  object.getString(SingleMessage.JSON_TAG_TO);
					ContentValues values = new ContentValues();
					values.put(GroupMessageEntry.COLUMN_MESSAGE_STATUS, SingleMessage.STATUS_FAILED);
					context.getContentResolver().update(
							Uri.parse("content://" + MyContentProvider.AUTHORITY + "/" + table), values,
							selection, selectionArgs);
					Log.e("Jenny", "activity msg: send msg failed GGGGGGGG");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		};
		
		decreaseCurrentPos = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (currentPos>0)
					currentPos--;
			}
			
		};
		
		updateMessage = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				if (groupUUID.equals( (String) msg.obj) ) {
					
					//set unread messages read
					Cursor cursor;
					cursor = msgDB.queryLastUnread(groupUUID);
					if (cursor.moveToFirst()) {
						
						do {
							String msgid = cursor
									.getString(cursor.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID));
							String from = cursor
									.getString(cursor.getColumnIndex(GroupMessageEntry.COLUMN_MESSAGE_FROM_UUID));
							JSONObject object = new JSONObject();
							try {
								object.put(SingleMessage.JSON_TAG_MSGID, msgid);
								object.put(SingleMessage.JSON_TAG_FROM, from);
								object.put(SingleMessage.JSON_TAG_TO, groupUUID);
								object.put(SingleMessage.JSON_TAG_TYPE, 5);
								object.put(SingleMessage.JSON_TAG_READTIME, System.currentTimeMillis());
								object.put(SingleMessage.JSON_TAG_READFROM, selfUUID);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							updateMsg.sendReadMsg(object);
						} while (cursor.moveToNext());
							
					}
					//new & set adapter
					try {
						cursor = msgDB.queryAllMessage(groupUUID);
						adapter = new MessageAdapter(context, cursor, url);
						MessageDialog.setAdapter(adapter);
						cursor.moveToLast();
						currentPos = cursor.getPosition();
						MessageDialog.setSelection(currentPos);

						LoaderManager lm = getLoaderManager();
						lm.initLoader(0, null, loaderCallback);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e("Jenny", "activity msg: query all msg failed");
					} 
					
				}
			}
			
		};
		
		stopService = new Handler(){  //add uri, subscribe, update message

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Intent serviceIntent = new Intent(context,LoginService.class);
				stopService(serviceIntent);
				
				String groupuuid = "";
				
				Cursor cur = GroupDBHelper.getInstance(context).queryAllGroup();
				if (cur.moveToFirst()){
					do{
						groupuuid = cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_UUID));
						msgDB.deleteAllMessage(groupuuid);
						updateMsg.updateMsgRequest("Activity_Message", groupuuid,System.currentTimeMillis(),"old");
						pubnubHelper.Subscribe(groupuuid);
						context.getContentResolver().call(
								MyContentProvider.CONTENT_URI, 
								"addURI", 
								groupuuid,
								null);
					}while(cur.moveToNext());
				}
				
				Message message = new Message();
				message.obj = groupUUID;
				updateMessage.sendMessage(message);	//update view
				SystemManager.putPreLong(context, SystemManager.MESSAGE_UPDATE_TIME, System.currentTimeMillis());
				
			}
			
		};
		
		
		
		fetchMoreHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				adapter.changeCursor(msgDB.queryAllMessage(groupUUID));
				adapter.notifyDataSetChanged();
				loadmoreView.setVisibility(LinearLayout.VISIBLE);
				fetchmore_pb.setVisibility(View.INVISIBLE);
			}
			
		};
		
		fetchError = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				loadmoreView.setVisibility(LinearLayout.VISIBLE);
				fetchmore_pb.setVisibility(View.INVISIBLE);
				Toast.makeText(context, R.string.message_update_failed, Toast.LENGTH_SHORT).show();
			}
			
		};
		
		fetchNoMore = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				loadmoreView.setVisibility(LinearLayout.INVISIBLE);
				fetchmore_pb.setVisibility(View.INVISIBLE);
				Toast.makeText(context, "已取得全部對話紀錄", Toast.LENGTH_SHORT).show();
			}
			
		};
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (mPullToRefreshLayout!=null && mPullToRefreshLayout.isRefreshing())
			mPullToRefreshLayout.setRefreshComplete();
		if (!KitsApplication.getInstance().isTabMsgStarted()) {
			Intent intent = new Intent();
			intent.setClass(context, Activity_Communication.class);
			Bundle bundle = new Bundle();
			bundle.putString(Activity_Communication.TAB_SELECT, Activity_Communication.TAB_MESSAGE);
			intent.putExtras(bundle);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		
		CursorLoader loader = new CursorLoader(this,Uri.parse(url),MessageDBHelper.group_msg_projection,null,null,null);
		
		
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO Auto-generated method stub
		adapter.swapCursor(data);
		Log.d("pubnub", "last: " + MessageDialog.getLastVisiblePosition() + ", current: " + currentPos);
		if (MessageDialog.getLastVisiblePosition() > currentPos)
			MessageDialog.setSelection(++currentPos);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		adapter.swapCursor(null);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		KitsApplication.getInstance().activityResumed();
		ShortcutBadger.with(context).remove();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		KitsApplication.getInstance().activityPaused();
		timerStop.sendEmptyMessage(0);
		
		ContentValues values = new ContentValues();
		values.put(GroupMessageEntry.COLUMN_MESSAGE_STATUS, SingleMessage.STATUS_FAILED);
		String where = GroupMessageEntry.COLUMN_MESSAGE_STATUS + " = ? ";
		String[] selectionArgs = {SingleMessage.STATUS_SENDING};
		getContentResolver().update(Uri.parse("content://" + MyContentProvider.AUTHORITY + "/" + groupUUID), values, where, selectionArgs);
		
	}

	@Override
	public void onRefreshStarted(View view) {
		// TODO Auto-generated method stub
		Log.d("Jenny", "on refresh started");
//		if (mPullToRefreshLayout!=null && !mPullToRefreshLayout.isRefreshing())
//			mPullToRefreshLayout.setRefreshing(true);
	}
}
