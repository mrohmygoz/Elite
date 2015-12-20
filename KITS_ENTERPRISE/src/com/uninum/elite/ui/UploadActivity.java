package com.uninum.elite.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.uninum.elite.R;
import com.uninum.elite.database.MessageDBHelper.GroupMessageEntry;
import com.uninum.elite.object.SingleMessage;
import com.uninum.elite.system.MyContentProvider;
import com.uninum.elite.webservice.MultipartHelper;
import com.uninum.elite.webservice.MultipartHelper.ProgressListener;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import uk.co.senab.photoview.PhotoViewAttacher;

@SuppressWarnings("deprecation")
public class UploadActivity extends Activity {
	
	private ImageView imagePrev;
	private VideoView videoPrev;
	private LinearLayout bottomLayout;
	private LinearLayout topBanner;
	private ProgressBar progressBar;
	private TextView progressText;
	private Button uploadBtn, cancelBtn;
	private String filePath, url, groupName, groupUUID, selfUUID, msgid, fileName;
	private long totalSize = 0;
	private int type, requestType;
	private PhotoViewAttacher mAttacher;
	private UploadFileToServer mUploadFileToServer = new UploadFileToServer();
	
	private boolean bottomLayoutView;
	private static final String TAG = "Jenny";
	
	private static final int REQUEST_PICK_IMAGE = 300;
	private static final int REQUEST_PICK_VIDEO = 400;
	private static final int REQUEST_TAKE_IMAGE = 500;
	private static final int REQUEST_TAKE_VIDEO = 600;
	
	public static Handler photoViewHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		imagePrev = (ImageView) this.findViewById(R.id.upload_imgPrev);
		mAttacher = new PhotoViewAttacher(imagePrev, "UploadActivity");
		videoPrev = (VideoView) this.findViewById(R.id.upload_videoPrev);
		progressBar = (ProgressBar) this.findViewById(R.id.upload_progressBar);
		progressText = (TextView) this.findViewById(R.id.upload_progressText);
		uploadBtn = (Button) this.findViewById(R.id.upload_button);
		cancelBtn = (Button) this.findViewById(R.id.upload_cancel);
		bottomLayout = (LinearLayout) this.findViewById(R.id.upload_bottomlayout);
		topBanner = (LinearLayout) this.findViewById(R.id.upload_topBanner);
		bottomLayoutView = true;
		
		Intent intent = this.getIntent();
		groupName	= intent.getStringExtra(Activity_Message.GROUP_NAME);
		groupUUID	= intent.getStringExtra(Activity_Message.GROUP_UUID);
		selfUUID	= intent.getStringExtra(Activity_Message.SELF_UUID);
		filePath 	= intent.getStringExtra("filePath");
		fileName	= intent.getStringExtra("name");
		url 		= intent.getStringExtra("url");
		msgid		= intent.getStringExtra("msgId");
		requestType = intent.getIntExtra("requestType", 200);
		
		if (requestType==REQUEST_PICK_IMAGE || requestType==REQUEST_TAKE_IMAGE)
			type = SingleMessage.JSON_TYPE[1];
		else if (requestType==REQUEST_PICK_VIDEO || requestType==REQUEST_TAKE_VIDEO)
			type = SingleMessage.JSON_TYPE[2];
		else 
			onBackPressed();
		
		Log.i(TAG, "upload activity file path: " + filePath);
		Log.i(TAG, "upload activity file name: " + fileName);
		Log.i(TAG, "upload activity url: " + url);
		
		if (filePath != null)
			previewImage();
		
		photoViewHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (bottomLayoutView) {
					bottomLayout.setVisibility(View.INVISIBLE);
					topBanner.setVisibility(View.INVISIBLE);
					bottomLayoutView = false;
				}
				else {
					bottomLayout.setVisibility(View.VISIBLE);
					topBanner.setVisibility(View.VISIBLE);
					bottomLayoutView = true;
				}
			}
			
		};
		
		uploadBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				uploadBtn.setVisibility(View.INVISIBLE);
				mUploadFileToServer.execute();
				insertMsg();
			}
			
		});
		
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mUploadFileToServer.cancel(true);
				onBackPressed();
			}
			
		});
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		switch(newConfig.orientation){
		case Configuration.ORIENTATION_LANDSCAPE:
			Log.d(TAG, "orientation = landscape");
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			Log.d(TAG, "orientation = portrait");
			break;
		case Configuration.ORIENTATION_SQUARE:
			Log.d(TAG, "orientation = square");
			break;
		}
	}

	private void previewImage(){
		
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
		try {
			ExifInterface exif = new ExifInterface(filePath);
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (requestType==REQUEST_PICK_IMAGE) {
			
			imagePrev.setVisibility(View.VISIBLE);
			videoPrev.setVisibility(View.GONE);
			// bitmap factory
            BitmapFactory.Options options = new BitmapFactory.Options();
 
            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, null);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
        	Matrix matrix = new Matrix();
        	Bitmap rotateBitmap;
			RotateBitmapToFile rbtf = new RotateBitmapToFile();
            
            Log.i(TAG, "upload image width= " + width);
            Log.i(TAG, "upload image height= " + height);
            Log.i(TAG, "upload image orientation= " + orientation);
            
            switch (orientation){
            case ExifInterface.ORIENTATION_UNDEFINED:
            	imagePrev.setImageBitmap(bitmap);
            	break;
            case ExifInterface.ORIENTATION_NORMAL:
            	imagePrev.setImageBitmap(bitmap);
            	break;
            case ExifInterface.ORIENTATION_ROTATE_180:
            	matrix.postRotate(180);
				rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
				imagePrev.setImageBitmap(rotateBitmap);
				rbtf.execute(rotateBitmap);
            	break;
            case ExifInterface.ORIENTATION_ROTATE_90:
				matrix.postRotate(90);
				rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
				imagePrev.setImageBitmap(rotateBitmap);
				rbtf.execute(rotateBitmap);
            	break;
            case ExifInterface.ORIENTATION_ROTATE_270:
            	matrix.postRotate(270);
				rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
				imagePrev.setImageBitmap(rotateBitmap);
				rbtf.execute(rotateBitmap);
            	break;
            default:
            	imagePrev.setImageBitmap(bitmap);
            }
            
            
		} else if (requestType==REQUEST_PICK_VIDEO) {

			String videoOrientation = "";
			try {

				MediaMetadataRetriever m = new MediaMetadataRetriever();
				m.setDataSource(filePath);
				if (Build.VERSION.SDK_INT >= 17)
					videoOrientation = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

				Log.i(TAG, "upload activity video orientation= " + videoOrientation);
				
//				VideoHandler vh = new VideoHandler(new File(filePath));
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			
//			if (!videoOrientation.equals("")) {
//				VideoHandler mVideoHandler = VideoHandler.getInstance();
//				mVideoHandler.rotateVideo(new File(filePath), Integer.parseInt(videoOrientation), fileName);
//			}
			
//			FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
//			fmmr.setDataSource(filePath);
//			String rotation = fmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
//			Log.e("Jenny", "upload activity" + rotation);
//			fmmr.release();
			
			imagePrev.setVisibility(View.GONE);
			videoPrev.setVisibility(View.VISIBLE);
			videoPrev.setVideoURI(Uri.fromFile(new File(filePath)));
			videoPrev.start();
			
		} else if (requestType==REQUEST_TAKE_IMAGE) {
			imagePrev.setVisibility(View.VISIBLE);
			videoPrev.setVisibility(View.GONE);
			
			// bitmap factory
            BitmapFactory.Options options = new BitmapFactory.Options();
 
            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, null);
            imagePrev.setImageBitmap(bitmap);
			
			mUploadFileToServer.execute();
			insertMsg();
		} else if (requestType==REQUEST_TAKE_VIDEO) {
			imagePrev.setVisibility(View.GONE);
			videoPrev.setVisibility(View.VISIBLE);
			videoPrev.setVideoURI(Uri.fromFile(new File(filePath)));
			videoPrev.start();
			
			mUploadFileToServer.execute();
			insertMsg();
		}
		
	}
	
	private void insertMsg () {
		ContentValues values = new ContentValues();
		values.put(GroupMessageEntry.COLUMN_MESSAGE_MSG, fileName);
		values.put(GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID, msgid);
		values.put(GroupMessageEntry.COLUMN_MESSAGE_FROM_UUID, selfUUID);
		values.put(GroupMessageEntry.COLUMN_MESSAGE_FROM_NAME, selfUUID);
		values.put(GroupMessageEntry.COLUMN_MESSAGE_GROUP_UUID, groupUUID);
		values.put(GroupMessageEntry.COLUMN_MESSAGE_CREATED_TIME, System.currentTimeMillis());
		values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_TIME, "");
		values.put(GroupMessageEntry.COLUMN_MESSAGE_TYPE, type);
		values.put(GroupMessageEntry.COLUMN_MESSAGE_STATUS, SingleMessage.STATUS_SENDING);
		values.put(GroupMessageEntry.COLUMN_MESSAGE_READED_NUMBER, 0);
		getContentResolver().insert(Uri.parse("content://" + MyContentProvider.AUTHORITY + "/" + groupUUID), values);
	}
	
	private class RotateBitmapToFile extends AsyncTask<Bitmap, Void, Void> {

		@Override
		protected Void doInBackground(Bitmap... params) {
			try {
        		
				//create a file to write bitmap data
				File f = new File(getCacheDir(), "uploadImg.jpg");
//				f.createNewFile();

				//Convert bitmap to byte array
//				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				FileOutputStream fos = new FileOutputStream(f);
				params[0].compress(CompressFormat.JPEG, 100 /*ignored for PNG*/, fos);
//				byte[] bitmapdata = bos.toByteArray();

				//write the bytes in file
//				FileOutputStream fos = new FileOutputStream(f);
//				fos.write(bitmapdata);
				fos.flush();
				fos.close();
				
				filePath = f.getAbsolutePath();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
	}
	
	private class UploadFileToServer extends AsyncTask<Void, Integer, String> {

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return uploadFile();
		}
		
		private String uploadFile() {
			String responseString = null;
			
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
            	
				File sourceFile = new File(filePath);
				
				// Adding file data to http body
				entity.addPart("file", new FileBody(sourceFile));

				// Extra parameters if you want to pass to server
				entity.addPart("log", new StringBody("from asus test"));

				totalSize = entity.getContentLength();
				httppost.setEntity(entity);
				httppost.setHeader("apiKey", "e5141ddacca87c02f80f120ea21f2fd6e2e7912e71b215d926f185380c5ccb96");
				HttpResponse response = httpclient.execute(httppost);
				
				Log.i(TAG, "upload activity http client execute");
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
            			object.put(SingleMessage.JSON_TAG_TYPE, type);
            			object.put(SingleMessage.JSON_TAG_DOWNLOAD_URL, downloadUrl);
            			object.put(SingleMessage.JSON_TAG_PREVIEW_URL, previewUrl);
            			
            			Message msg = new Message();
            			msg.obj = object;
            			Activity_Message.sendMsgTask.sendMessage(msg);
            			
            		} catch (JSONException e) {
            			e.printStackTrace();
            		}
            		
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                    String where = GroupMessageEntry.COLUMN_MESSAGE_MSG_UUID + " = ? ";
            		String[] whereArgs = {msgid};
            		getContentResolver().delete(Uri.parse("content://"+MyContentProvider.AUTHORITY+"/"+groupUUID), where, whereArgs);
            		Toast.makeText(UploadActivity.this, "�W���ɮץ��ѡA�Э��s�W��", Toast.LENGTH_SHORT).show();
                }
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            
			return responseString;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.e(TAG, "Response from server: " + result);
			onBackPressed();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			// Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);
 
            // updating progress bar value
            progressBar.setProgress(progress[0]);
 
            // updating percentage value
            progressText.setText(String.valueOf(progress[0]) + "%");
		}
		
		
	}
	
}
