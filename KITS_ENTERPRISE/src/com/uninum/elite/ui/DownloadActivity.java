package com.uninum.elite.ui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.uninum.elite.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.CenterLayout;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class DownloadActivity extends Activity {
	
	private Button cancel, download;
	private ImageView image;
	private String url, name, mimeType;
	private int type;
	private static String TAG = "Jenny";
	private File file;
	private Context context;
	private VideoView video;
	private CenterLayout cl;
	private ProgressBar pb;
	private PhotoViewAttacher mAttacher;
	private boolean bottomLayoutView = true;
	private LinearLayout bottomLayout, topBanner;
	
	public static Handler photoViewHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_download);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		cancel = (Button) this.findViewById(R.id.download_cancel);
		download = (Button) this.findViewById(R.id.download_button);
		image = (ImageView) this.findViewById(R.id.download_imgPrev);
		mAttacher = new PhotoViewAttacher(image, "DownloadActivity");
		video = (VideoView) this.findViewById(R.id.download_videoPrev);
		cl = (CenterLayout) this.findViewById(R.id.download_centerlayout);
		pb = (ProgressBar) this.findViewById(R.id.download_pb);
		bottomLayout = (LinearLayout) this.findViewById(R.id.download_bottomlayout);
		topBanner = (LinearLayout) this.findViewById(R.id.download_topBanner);
		context = this;
		
		
		Intent intent = this.getIntent();
		url	= intent.getStringExtra("url");
		name = intent.getStringExtra("name");
		type = intent.getIntExtra("type", 0);
		
		if (url!=null && type!=0)
			preview();
		
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
		
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
			
		});
		
		download.setOnClickListener(new OnClickListener() {

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
							
							DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
							Uri Download_Uri = Uri.parse(url);
							DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
							request.setTitle(name)
									.setMimeType(mimeType + "/" + name.substring(  name.lastIndexOf(".")+1  ))
//									.setMimeType(mimeType + "/quicktime")
									.setDescription("elite download")
									.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
							request.allowScanningByMediaScanner();
							long downloadReference = dm.enqueue(request);
							Log.i(TAG, "downloadReference: " + downloadReference);
				            
				            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				            intent.setData(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory((Environment.DIRECTORY_DOWNLOADS)), name)));
			            	sendBroadcast(intent);
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
							Log.e(TAG, "msg adapter on post execute: " + result.toString());
					}
					
				}.execute(url);
				onBackPressed();
				
			}
			
		});
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (file!=null)
			if (file.exists())
				file.delete();
	}

	private void preview (){
		switch (type) {
		case 0:
			break;
		case 1:
			image.setVisibility(View.VISIBLE);
			cl.setVisibility(View.GONE);
			mimeType = "image";
			
			DisplayImageOptions options;
			options = new DisplayImageOptions.Builder()
					.showImageForEmptyUri(R.drawable.contact_head_big)
					.showImageOnFail(R.drawable.contact_head_big)
					.resetViewBeforeLoading(true)
					.cacheOnDisk(true)
					.imageScaleType(ImageScaleType.EXACTLY)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.considerExifParams(true)
					.displayer(new FadeInBitmapDisplayer(300))
					.build();
			
			ImageLoader.getInstance().displayImage(url, image, options, new ImageLoadingListener(){

				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
					
				}

				@Override
				public void onLoadingComplete(String arg0, View view, Bitmap bitmap) {
					ImageView imageView = (ImageView) view;
					imageView.setImageBitmap(bitmap);
					Log.d(TAG, "image view loader loading complete");
					pb.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingFailed(String arg0, View view, FailReason failReason) {
					Log.e(TAG, "url: " + url);
					Log.e(TAG, "image view loader loading failed: " + failReason.getType().toString());
					pb.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingStarted(String arg0, View arg1) {
					pb.setVisibility(View.GONE);
				}
				
			});
			break;
		case 2:
			image.setVisibility(View.GONE);
			cl.setVisibility(View.VISIBLE);
			mimeType = "video";
			
			video.setVideoPath(url);
			video.setBufferSize(2048);
			video.setVideoQuality(16);
			video.setMediaController(new MediaController(this));
			video.requestFocus();
			video.start();
			
//			video.setVideoURI(Uri.parse(url));
//			video.setMediaController(new MediaController(context));
//			video.requestFocus();
//			video.start();
			
//			DownloadVideo mDownloadVideo = new DownloadVideo();
//			mDownloadVideo.execute();
			break;
		}
	}
	
	private class DownloadVideo extends AsyncTask<Void,Integer,String>{
		
		private final int TIMEOUT_CONNECTION = 5000;	//5sec
		private final int TIMEOUT_SOCKET = 30000;		//30sec

		@Override
		protected String doInBackground(Void... params) {
			
			try {
				file = new File(Environment.getExternalStorageDirectory() + "/videoTemp");
				if (file.exists()) {
		            Log.i(TAG, "download file " + file.getAbsolutePath() + " exists");
		        } else
		            Log.e(TAG, "download file " + file.getAbsolutePath() + " not exists");
				
				URL Url = new URL(url);
				URLConnection connection = Url.openConnection();
				connection.setReadTimeout(TIMEOUT_CONNECTION);
				connection.setConnectTimeout(TIMEOUT_SOCKET);
				
				//Define InputStreams to read from the URLConnection.
	            // uses 3KB download buffer
	            InputStream is = connection.getInputStream();
	            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
	            FileOutputStream outStream = new FileOutputStream(file);
	            byte[] buff = new byte[5 * 1024];

	            //Read bytes (and store them) until there is nothing more to read(-1)
	            int count;
	            long currentLength = 0, totalsize = connection.getContentLength();
	            while ((count = inStream.read(buff)) != -1) {
	            	currentLength += count;
	            	publishProgress( (int) (currentLength*100/totalsize) );
	                outStream.write(buff,0,count);
	            }

	            //clean up
	            outStream.flush();
	            outStream.close();
	            inStream.close();
	            
	            if (file.exists()) {
	            	Log.i(TAG, "download file " + file.getAbsolutePath() + " exists");
	            } else
	            	Log.e(TAG, "download file " + file.getAbsolutePath() + " not exists");
	            
	            return file.getAbsolutePath();
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			return null;
		}


		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			pb.setProgress(values[0]);
		}


		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			if (result==null)
				return;
			pb.setVisibility(View.GONE);
			video.setVideoPath(result);
			video.setMediaController(new MediaController(context));
			video.requestFocus();
			video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
	            @Override
	            public void onPrepared(MediaPlayer mediaPlayer) {
	            	Log.i(TAG, "video on prepared");
	            }
	        });
		}
		
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

}
