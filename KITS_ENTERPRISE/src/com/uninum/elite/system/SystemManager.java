package com.uninum.elite.system;

import java.util.UUID;

import com.uninum.elite.image.BitmapProcess;
import com.uninum.elite.image.DrawCircularImage;
import com.uninum.elite.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

public class SystemManager {
	private static final String ELITE_SHAREPREFERENCES = "ELITE_SHAREPREFERENCES";
	public static final String USER_BIGHEAD = "USER_BIGHEAD";
	public static final String USER_BIGHEAD_TIME = "USER_BIGHEAD_TIME";
	public static final String USER_BIGHEAD_ID = "USER_BIGHEAD_ID";
	public static final String USER_ACCOUNT = "USER_ACCOUNT";
	public static final String USER_NAME = "USER_NAME";
	public static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
	public static final String USER_FIRST_OPEN = "USER_FIRST";
	public static final String USER_TOKEN = "TOKEN";
	public static final String CONTACT_UPDATE_TIME = "CONTACT_UPDATE_TIME";
	public static final String HISTORT_UPDATE_TIME = "HISTORY_UPDATE_TIME";
	public static final String MESSAGE_UPDATE_TIME = "MESSAGE_UPDATE_TIME";
	public static final String NOTIFICATION = "NOTIFICATION";
	
	private static String uniqueID = null;
	private static final float ROUND_PX = 150.0f;
	private static SharedPreferences prefs;
	
	public synchronized static String getUniqueID(Context context) {
		String androidID = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
		if (androidID != null && !androidID.equals("") && !androidID.equals("9774d56d682e549c")) {
			return androidID;
		} else if (uniqueID == null  || androidID.equals("")) {
			uniqueID = getPreferences(context).getString(PREF_UNIQUE_ID, null);
			if (uniqueID == null) {
				uniqueID = UUID.randomUUID().toString();
				Editor editor = getPreferences(context).edit();
				editor.putString(PREF_UNIQUE_ID, uniqueID);
				editor.commit();
			}
		}
		return uniqueID;
	}
	
	public static Bitmap getUserBigHead(Context context){
		String bitmap_str = getPreString(context, USER_BIGHEAD);
		if(bitmap_str!=""){
			return BitmapProcess.StringToBitmap(bitmap_str);
		}
		else{
//			return DrawCircularImage.getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.contact_head_big),  ROUND_PX);
			return null;
		}
	}
	private static SharedPreferences getPreferences(Context context){
		if(prefs==null){
			prefs = context.getSharedPreferences(ELITE_SHAREPREFERENCES, Context.MODE_PRIVATE);
		}
			return prefs;	
	}
	public static String getPreString(Context context, String key){
		return getPreferences(context).getString(key, "");
	}
	public static boolean putUserBigHead(Context context, Bitmap bitmap){
//		bitmap = DrawCircularImage.getRoundedCornerBitmap(bitmap,  ROUND_PX);		
		if(bitmap!=null){
			bitmap = DrawCircularImage.DrawCircular(bitmap);
			return putPreString(context, USER_BIGHEAD, BitmapProcess.bitmapToString(bitmap));
		}
			//		}else{
////			bitmap = DrawCircularImage.getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.contact_head_small),  ROUND_PX);
////			return putPreString(context, USER_BIGHEAD, BitmapProcess.bitmapToString(bitmap));
//		}
		return false;
	}
	
	public static void putNotificationCount (Context context, String key, int value) {
		getPreferences(context).edit().putInt(key, value);
	}
	
	public static int getNotificationCount (Context context, String key) {
		return getPreferences(context).getInt(key, 0);
	}
	
	public static boolean putPreString(Context context, String key, String value){
		return getPreferences(context).edit().putString(key, value).commit();
	}
	
	public static void setToken(Context context, String token){
		putPreString(context, USER_TOKEN, token);
	}
	public static String getToken(Context context){
		return getPreString(context, USER_TOKEN);
	}
	public static boolean putPreBoolean(Context context, String key, boolean value){
		return getPreferences(context).edit().putBoolean(key, value).commit();
	}
	public static boolean getPreBoolean(Context context, String key){
		return getPreferences(context).getBoolean(key, true);
	}
	public static boolean putPreLong(Context context, String key, long value){
		return getPreferences(context).edit().putLong(key, value).commit();
	}
	public static long getPreLong(Context context, String key){
		return getPreferences(context).getLong(key, 0);
	}
	public static void deletePreString(Context context, String key){
		getPreferences(context).edit().remove(key).apply();
	}
	public static void clearAll(){
		prefs.edit().clear().commit();
	}
}
