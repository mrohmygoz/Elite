package com.uninum.elite.system;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.uninum.elite.R;
import com.uninum.elite.object.Image;
import com.uninum.elite.utility.ImageCache;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

@ReportsCrashes(
        formKey = "",
        formUri = "https://hsnl.cloudant.com/acra-elite/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin="pendiandsconswereedstoom",
        formUriBasicAuthPassword="kcILBOiXvKKeQCNEDq3CQboI",
        // Your usual ACRA configuration
        mode = ReportingInteractionMode.DIALOG,
        resDialogIcon = R.drawable.ic_launcher,
        resToastText = R.string.acra_crash_toast_text,
        resDialogText = R.string.acra_crash_dialog_text, 
        resDialogCommentPrompt = R.string.acra_crash_dialog_comment_prompt
        )


public class KitsApplication extends Application  {
	private static KitsApplication instance;
	private RequestQueue wsRequestQ;
	public static final String VOLLEY_TAG = "KITsWS";
	private static int DISK_IMAGECACHE_SIZE = 1024*10*1024;
	private static CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.PNG;
	private static int DISK_IMAGECACHE_QUALITY = 100;  //PNG is lossless so quality is ignored but must be provided
	public final static String PICID_DEFAULT = "default";
	public static ImageCache<String, Image> imageRequestRecord = new ImageCache<String, Image>();
	public static HashMap<String, Bitmap> messageImageRecord = new HashMap<String, Bitmap>();
	DisplayImageOptions options;
	ImageLoader imageLoader = ImageLoader.getInstance();
	
	
	
	// < ---------- for pubnub & tab message ---------- >
	
	private static boolean UpdateHistory;
	
	public boolean getUpdateHistory(){
		return UpdateHistory;
	}
	
	public void setUpdateHistory() {
		UpdateHistory = true;
	}
	
	public void unsetUpdateHistory(){
		UpdateHistory = false;
	}
	
	//---------------------------------------
	
	private static boolean isMsgActivityVisible;
	
	public boolean isMsgActivityVisible() {
		return isMsgActivityVisible;
	}
	
	public void activityResumed(){
		isMsgActivityVisible = true;
	}
	
	public void activityPaused(){
		isMsgActivityVisible = false;
	}
	
	//---------------------------------------
	
		private static boolean isMsgActivityStarted;
		
		public boolean isMsgActivityStarted() {
			return isMsgActivityStarted;
		}
		
		public void MsgActivityStarted(){
			isMsgActivityStarted = true;
		}
	
	//---------------------------------------
	
	private static boolean isTabMsgStarted;
	
	public boolean isTabMsgStarted() {
		return isTabMsgStarted;
	}
	
	public void TabMsgStarted(){
		isTabMsgStarted = true;
	}
	
	//---------------------------------------
	
	private static boolean isMainStarted;
	
	public boolean isMainStarted() {
		return isMainStarted;
	}
	
	public void MainStarted(){
		isMainStarted = true;
	}
	
	//---------------------------------------
	
	private static boolean isTabHistoryStarted;
	
	public boolean isTabHistoryStarted() {
		return isTabHistoryStarted;
	}
	
	public void TabHistoryStarted(){
		isTabHistoryStarted = true;
	}
	
	//---------------------------------------
	
	private static boolean isLogin;
	
	public boolean isLogin() {
		return isLogin;
	}
	
	public void Login(){
		isLogin = true;
	}
	
	//---------------------------------------
	
	private static List<String> subscribedTable;
	
	public void addGroup(String groupuuid){
		subscribedTable.add(groupuuid);
	}
	
	public boolean isGroupSubscribed(String groupuuid){
		return subscribedTable.contains(groupuuid);
	}
	
	// < ---------- for pubnub & tab message ---------- >
	
	

	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		super.onCreate();
		instance = this;
		getRequestQueue();
		ACRA.init(this);
		isMsgActivityVisible = false;
		isMsgActivityStarted = false;
		isTabMsgStarted = false;
		isMainStarted = false;
		isTabHistoryStarted = false;
		isLogin = false;
		UpdateHistory = false;
		subscribedTable = new ArrayList<String>();
		
		//test	
		options = new DisplayImageOptions.Builder()
        .showImageOnLoading(R.drawable.contact_head_big) // resource or drawable
        .showImageForEmptyUri(R.drawable.contact_head_big) // resource or drawable
        .showImageOnFail(R.drawable.contact_head_big) // resource or drawable
        .resetViewBeforeLoading(false)  // default
        .delayBeforeLoading(100)
        .cacheInMemory(true) // default
        .cacheOnDisk(false) // default
        .considerExifParams(false) // default
        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
        .bitmapConfig(Bitmap.Config.ARGB_8888) // default
        .displayer(new SimpleBitmapDisplayer()) // default
        .handler(new Handler()) // default
        .build();
		
		File cacheDir = StorageUtils.getCacheDirectory(getApplicationContext());
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
		        .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
		        .diskCacheExtraOptions(480, 800, null)
		        .threadPoolSize(3) // default
		        .threadPriority(Thread.NORM_PRIORITY - 2) // default
		        .tasksProcessingOrder(QueueProcessingType.FIFO) // default
		        .denyCacheImageMultipleSizesInMemory()
		        .memoryCache(new LruMemoryCache(5 * 1024 * 1024))
		        .memoryCacheSize(5 * 1024 * 1024)
		        .memoryCacheSizePercentage(25) // default
		        .diskCache(new UnlimitedDiscCache(cacheDir)) // default
		        .diskCacheSize(50 * 1024 * 1024)
		        .diskCacheFileCount(100)
		        .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
		        .imageDownloader(new BaseImageDownloader(getApplicationContext())) // default
		        .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
		        .writeDebugLogs()
		        .defaultDisplayImageOptions(options)
		        .build();
		
		imageLoader.init(config);
	}
	
	//test
	public synchronized static  KitsApplication getInstance(){		
		return instance;
	}
	
	public RequestQueue getRequestQueue(){
		if(wsRequestQ == null){
			wsRequestQ = Volley.newRequestQueue(getApplicationContext());
			
		}
		return wsRequestQ;
	}
	
	public <T> void addToRequestQueue(Request<T> req, String tag){
		req.setTag(TextUtils.isEmpty(tag) ? VOLLEY_TAG : tag);
		VolleyLog.d("Adding request to queue: %s",  req.getUrl());
		getRequestQueue().add(req);
	}
	
	public <T> void addToRequestQueue(Request<T> req){
		//set the default tag if tag is empty;
		req.setTag(VOLLEY_TAG);
		getRequestQueue().add(req);
	}
	
	public void cancelPendingRequests(Object tag){
		if(wsRequestQ != null){
			wsRequestQ.cancelAll(tag);
		}
	}
	
	public void deleteImageInHashMap(String picID, String groupUUID){
		if(KitsApplication.imageRequestRecord.get(picID+groupUUID)!= null){
			KitsApplication.imageRequestRecord.remove(picID+groupUUID);
			Log.i("ELITE","Delete Image"+ "now Size:"+ imageRequestRecord.size());
		}
		
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
		imageRequestRecord.clear();
	}
	
	@Override
	public void onTrimMemory(int level) {
        Log.v("ELITE", "onTrimMemory() with level=" + level);

        // Memory we can release here will help overall system performance, and
        // make us a smaller target as the system looks for memory

        if (level >= TRIM_MEMORY_MODERATE) { // 60
            // Nearing middle of list of cached background apps; evict our
            // entire thumbnail cache
            Log.i("ELITE", "Memory rate:"+ TRIM_MEMORY_MODERATE);

        } else if (level >= TRIM_MEMORY_BACKGROUND) { // 40
            // Entering list of cached background apps; evict oldest half of our
            // thumbnail cache
        	Log.i("ELITE", "Memory rate:"+ TRIM_MEMORY_BACKGROUND);
        } else if (level > 15){
        	imageRequestRecord.clear();
        	messageImageRecord.clear();
        	Log.i("ELITE", "Memory rate 15~40:");	
        } else if (level < TRIM_MEMORY_RUNNING_CRITICAL){
        	imageRequestRecord.clear();
        	messageImageRecord.clear();
        	Log.i("ELITE", "Memory rate:"+ TRIM_MEMORY_RUNNING_CRITICAL);
        }
    }

	
	
}
