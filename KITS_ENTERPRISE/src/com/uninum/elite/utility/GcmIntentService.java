package com.uninum.elite.utility;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.uninum.elite.R;
import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.ContactDBHelper.ContactEntry;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.database.MessageDBHelper;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.ui.Activity_Communication;
import com.uninum.elite.ui.Activity_Main;
import com.uninum.elite.ui.Activity_Message;
import com.uninum.elite.ui.IncomingMessageDialog;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import me.leolin.shortcutbadger.ShortcutBadger;

public class GcmIntentService  extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    String TAG = "ELITE";

    public GcmIntentService() {
        super("GcmIntentService");
    }

	@Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            	
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            	
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	
				Log.i("Jenny", "gcm: "+extras.toString());
            	
				
				if (extras.getString("message")!=null) {
					
					
					/*  ------------------------------------------------------------------------------*/
	            	/*  Filter messages by the type. M for text messages, C for calling notifications.*/
	            	/* ------------------------------------------------------------------------------ */
					
					if (extras.getString("type").equals("c")) {
						
						// This loop represents the service doing some work.  
						// Post notification of received message.
						Intent incomingcallDialogIntent = new Intent();
						incomingcallDialogIntent.setClass(GcmIntentService.this, IncomingMessageDialog.class);
						Bundle bundle = new Bundle();
						bundle.putString(IncomingMessageDialog.BUNDLE_MESSAGE, extras.getString("message"));
						incomingcallDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						incomingcallDialogIntent.putExtras(bundle);
						startActivity(incomingcallDialogIntent);
						sendNotification(extras.getString("message"));
						
						
					} else if (extras.getString("type").equals("m")) {
						
						ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
						List<ActivityManager.RunningTaskInfo> task = activityManager.getRunningTasks(1);
						ComponentName componentInfo = task.get(0).topActivity;
						if (componentInfo.getPackageName().equals(this.getPackageName()))
							Log.d("Jenny", "gcm intent service: foreground");
						else{
							sendMsgNotification(extras.getString("From"),extras.getString("To"),extras.getString("message"));
						}
					}
					
					
				}
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
	
	
    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this,Activity_Communication.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(Activity_Communication.TAB_SELECT, Activity_Communication.TAB_HISTORY);
        bundle.putBoolean(Activity_Communication.TAB_HISTORY_UPDATE, true);
        intent.putExtras(bundle);
        
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_notification)
        .setVibrate(new long[]{0, 100, 300, 200, 100, 300, 200, 100, 300, 200, 100})
        .setContentTitle("Elite ³qª¾")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
    
    private void sendMsgNotification(String fromUUID, String groupUUID, String msg) {
        
        String tableName, groupName, selfUUID, fromName;
        Cursor cur = GroupDBHelper.getInstance(getApplicationContext()).queryGroupUUID(groupUUID);
        if (cur.moveToFirst()) {
			selfUUID = cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_SELFUUID));
			groupName = cur.getString(cur.getColumnIndex(GroupEntry.COLUMN_GROUP_NAME));
		}else 
			return;
		cur = ContactDBHelper.getInstance(getApplicationContext()).queryContact(fromUUID, groupUUID);
		if (cur.moveToFirst())
			fromName = cur.getString(cur.getColumnIndex(ContactEntry.COLUMN_CONTACT_NAME));
		else
			return;
		
		int count = SystemManager.getNotificationCount(getApplicationContext(), SystemManager.NOTIFICATION);
		SystemManager.putNotificationCount(getApplicationContext(), SystemManager.NOTIFICATION, ++count);
		ShortcutBadger.with(getApplicationContext()).count(count);
		
		Intent intent = new Intent(this.getApplicationContext(),Activity_Message.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(Activity_Message.GROUP_NAME, groupName);
        bundle.putString(Activity_Message.GROUP_UUID, groupUUID);
        bundle.putString(Activity_Message.SELF_UUID, selfUUID);
        intent.putExtras(bundle);
        
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

	    mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_notification)
//        .setVibrate(new long[]{0, 100, 300, 200, 100, 300, 200, 100, 300, 200, 100})
        .setVibrate(new long[]{0, 100, 300, 200, 100})
        .setContentTitle(groupName)
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(fromName + ": " + msg))
        .setContentText(fromName + ": " + msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
