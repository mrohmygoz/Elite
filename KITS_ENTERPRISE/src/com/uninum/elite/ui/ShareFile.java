package com.uninum.elite.ui;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.uninum.elite.R;
import com.uninum.elite.data.UpdateMessage;
import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.database.MessageDBHelper.GroupMessageEntry;
import com.uninum.elite.image.ImageViewLoader;
import com.uninum.elite.object.Contact;
import com.uninum.elite.object.SingleMessage;
import com.uninum.elite.system.MyContentProvider;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.webservice.MultipartHelper;
import com.uninum.elite.webservice.MultipartHelper.ProgressListener;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ShareFile extends ActionBarActivity {
	
	private static final String TAG = "Jenny";
	private static final String UPLOAD_FILE_URL = "http://130.211.246.100:8080/rest/v1/File/mediaObject/";
	private ListView listview;
	private TextView text_to;
	private List<Group> targetGroups;
	private Button send;
	private ProgressBar progressBar;
	private String fileName;
	private Timer timer, failTimer;
	private UpdateMessage updateMsg;
	private UploadFileToServer mUploadFileToServer;
	private Context context;
	private Uri fileUri;
	private long totalSize;
	
	private class Group {
		
		String msgId;
		String groupUUID;
		String selfUUID;
		
		public Group (String msgid, String groupid, String selfid){
			msgId = msgid;
			groupUUID = groupid;
			selfUUID = selfid;
		}
		
		public String getMsgId () {
			return msgId;
		}
		
		public String getGroupId () {
			return groupUUID;
		}
		
		public String getSelfId () {
			return selfUUID;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sharefile);
		listview = (ListView) findViewById(R.id.share_file_list);
		text_to = (TextView) findViewById(R.id.share_file_to);
		send = (Button) findViewById(R.id.share_file_send);
		progressBar = (ProgressBar) findViewById(R.id.share_file_pb);
		ShareFileAdapter adapter = new ShareFileAdapter(this, GroupDBHelper.getInstance(this).queryAllGroup());
		listview.setAdapter(adapter);
		targetGroups = new ArrayList<Group>();
		updateMsg = new UpdateMessage(this);
		mUploadFileToServer = new UploadFileToServer();
		context = this;
		
		Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();
	    
	    if (Intent.ACTION_SEND.equals(action) && type != null) {
	        fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
	        if (fileUri != null) {
	        	fileName = fileUri.getLastPathSegment();
	        	Log.d(TAG, "file name: " + fileName);
	        }
	    }
	    
	    Cursor cur = getContentResolver().query(fileUri, null, null, null, null);
	    if (cur!=null) {
	    	cur.moveToFirst();
	    	int index = cur.getColumnIndex(MediaStore.Images.Media.DATA);
	    	Log.i(TAG, "share file col index = " + index);
	    	String filepath;
		    if (index>=0) {
		    	filepath = cur.getString(index);
		    	Log.i(TAG, "share file path = " + index);
		    }
	    }
	    
	    send.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				for (int i=0; i<targetGroups.size(); i++){
					insertMsg(targetGroups.get(i));
					mUploadFileToServer.execute(
							UPLOAD_FILE_URL + SystemManager.getToken(context) + "/file/" + targetGroups.get(i).getMsgId(),
							fileUri.getPath().replace(" ", "+"),
							targetGroups.get(i).getGroupId(),
							targetGroups.get(i).getSelfId(),
							targetGroups.get(i).getMsgId());
				}
				
				onBackPressed();
			}
	    	
	    });
		
	}
	
	private class ShareFileAdapter extends CursorAdapter {

		public ShareFileAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
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
			
			view.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					String oldText = (String) text_to.getText();
					String newText = oldText + groupName + "¡B";
					text_to.setText(newText);
					targetGroups.add( new Group(UUID.randomUUID().toString(),groupUUID,selfUUID) );
				}
				
			});
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup arg2) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = (View)inflater.inflate(R.layout.sharefile_listrow, null);
			
			ViewHolder holder = new ViewHolder();
			holder.groupNameText = (TextView) view.findViewById(R.id.sharefile_group_name);
			holder.bighead = (ImageView) view.findViewById(R.id.sharefile_bighead);
			view.setTag(holder);
			
			return view;
		}
		
		public class ViewHolder {
			TextView groupNameText;
			ImageView bighead;
		}
		
	}
	
	private void insertMsg (Group insertgroup) {
		ContentValues values = new ContentValues();
		values.put(GroupMessageEntry.COLUMN_MESSAGE_MSG, fileName);
		values.put(GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID, insertgroup.getMsgId());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_FROM_UUID, insertgroup.getSelfId());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_FROM_NAME, insertgroup.getSelfId());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_GROUP_UUID, insertgroup.getGroupId());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME, System.currentTimeMillis());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_TIME, "");
		values.put(GroupMessageEntry.COLUMN_MESSAGE_TYPE, 4);
		values.put(GroupMessageEntry.COLUMN_MESSAGE_STATUS, SingleMessage.STATUS_SENDING);
		values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_NUMBER, 0);
		getContentResolver().insert(Uri.parse("content://" + MyContentProvider.AUTHORITY + "/" + insertgroup.getGroupId()), values);
	}
	
	private class UploadFileToServer extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			return uploadFile(params[0], params[1], params[2], params[3], params[4]);
		}
		
		private String uploadFile(String url, String filePath, String groupUUID, String selfUUID, String msgid) {
			String responseString = null;
			Log.i(TAG, "share file url: " + url);
			
			HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            MultipartHelper entity = new MultipartHelper( 
            		new ProgressListener(){

						@Override
						public void transferred(long num) {
							publishProgress((int) ((num / (float) totalSize) * 100));
						}
            	
            });
			
            try {
            	
				File sourceFile = new File(  new URI(filePath)  );
				
				// Adding file data to http body
				entity.addPart("file", new FileBody(sourceFile));

				// Extra parameters if you want to pass to server
				entity.addPart("log", new StringBody("from asus test"));

				totalSize = entity.getContentLength();
				httppost.setEntity(entity);
				httppost.setHeader("apiKey", "e5141ddacca87c02f80f120ea21f2fd6e2e7912e71b215d926f185380c5ccb96");
				HttpResponse response = httpclient.execute(httppost);
				
				HttpEntity r_entity = response.getEntity();
				
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);

            		try {
            			
                        JSONObject responseObj = new JSONObject(responseString);
                        ContentValues values = new ContentValues();
                        String where = GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID + " = ? ";
                		String[] whereArgs = {msgid};
                		String downloadUrl = responseObj.getString("downloadUrl").replace("\\","");
                		String previewUrl = responseObj.getString("previewUrl").replace("\\","");
                		values.put(GroupMessageEntry.COLUMN_MESSAGE_DOWNLOAD_URL, downloadUrl);
                		values.put(GroupMessageEntry.COLUMN_MESSAGE_PREVIEW_URL, previewUrl);
                		getContentResolver().update(Uri.parse("content://" + MyContentProvider.AUTHORITY + "/" + groupUUID), values, where, whereArgs);

            			JSONObject object;
            			object = new JSONObject();
            			object.put(SingleMessage.JSON_TAG_MSGID, msgid);
            			object.put(SingleMessage.JSON_TAG_MESSAGE, fileName);
            			object.put(SingleMessage.JSON_TAG_FROM, selfUUID);
            			object.put(SingleMessage.JSON_TAG_TO, groupUUID);
            			object.put(SingleMessage.JSON_TAG_TYPE, 4);
            			object.put(SingleMessage.JSON_TAG_DOWNLOAD_URL, downloadUrl);
            			object.put(SingleMessage.JSON_TAG_PREVIEW_URL, previewUrl);
            			
            			sendMsg(object);
            			
            		} catch (JSONException e) {
            			e.printStackTrace();
            		}
            		
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Log.e(TAG, "share file " + e.toString());
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				Log.e(TAG, "share file " + e.toString());
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "share file " + e.toString());
			} catch (URISyntaxException e1) {
				Log.e(TAG, "share file " + e1.toString());
				e1.printStackTrace();
			}
            
			return responseString;
		}
		
		private void sendMsg (JSONObject object) {
			timer = new Timer();
			failTimer = new Timer();
			timer.scheduleAtFixedRate(new resendTask(object), 0, 1000*3);
			failTimer.schedule(new failedTask(object), 1000*10);
		}
		
		private void sendMsgFailed (JSONObject object) {
			try {
				String selection = GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID + " = ?";
				String[] selectionArgs = {object.getString(SingleMessage.JSON_TAG_MSGID)};
				String table =  object.getString(SingleMessage.JSON_TAG_TO);
				ContentValues values = new ContentValues();
				values.put(GroupMessageEntry.COLUMN_MESSAGE_STATUS, SingleMessage.STATUS_FAILED);
				
				getContentResolver().update(
						Uri.parse("content://" + MyContentProvider.AUTHORITY + "/" + table), values,
						selection, selectionArgs);
				
				Log.e("Jenny", "activity msg: send msg failed GGGGGGGG");
			} catch (JSONException e) {
				e.printStackTrace();
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
				sendMsgFailed(object);
			}
			
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.e(TAG, "Response from server: " + result);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			// Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);
 
            // updating progress bar value
            progressBar.setProgress(progress[0]);
 
            // updating percentage value
            //progressText.setText(String.valueOf(progress[0]) + "%");
		}
		
		
	}
	
}
