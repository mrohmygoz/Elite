package com.uninum.elite.ui;

import java.util.List;

import com.uninum.elite.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

public class IncomingMessageDialog extends Activity {
    private View floatDialogView;
	private WindowManager windowManager;
	private WindowManager.LayoutParams params;
	public static final String BUNDLE_MESSAGE = "BUNDLE_MESSAGE";
	public static Handler handler;
	private TelephonyManager telephony;
	private Context context;
	private IncomingCallPhoneStateListener incomingCallPhoneStateListener ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = IncomingMessageDialog.this;
        String message = getIntent().getExtras().getString(BUNDLE_MESSAGE);
        windowManager = (WindowManager)getApplicationContext().
        		getSystemService(Context.WINDOW_SERVICE);
		params = new WindowManager.LayoutParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		LayoutInflater inFlater = LayoutInflater.from(this);
		floatDialogView = inFlater.inflate(R.layout.dialog_incomingcall, null);
		TextView message_tv = (TextView)floatDialogView.
				findViewById(R.id.incoming_call_message);
		message_tv.setText(message);
		ImageButton btn_close = (ImageButton)floatDialogView.
				findViewById(R.id.incoming_call_delete_ib); 
		telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		incomingCallPhoneStateListener = new IncomingCallPhoneStateListener(context);
		telephony.listen(incomingCallPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		new CountDownTimer(60000,60000){

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
				if(incomingCallPhoneStateListener == null)
					incomingCallPhoneStateListener = new IncomingCallPhoneStateListener(context);
				telephony.listen(incomingCallPhoneStateListener, PhoneStateListener.LISTEN_NONE);	
				Log.d("ELITE","Release phoneStateListener");
			}
			
		}.start();
		btn_close.setOnClickListener(new OnClickListener() { 
			@Override 
			public void onClick(View v) {
				if (floatDialogView != null) {
					windowManager.removeView(floatDialogView);
					floatDialogView = null;
					Log.d("incoming Dialog","cancel");
				}
			}
		});
		
		/*
		 * Pop up the dialog view here, and send parameters
		 */
		windowManager.addView(floatDialogView, params);
		finish();
		
		 handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if(floatDialogView!=null)
				{
							windowManager.removeView(floatDialogView);
							floatDialogView = null;
							TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
							if(incomingCallPhoneStateListener == null)
								incomingCallPhoneStateListener = new IncomingCallPhoneStateListener(context);
							telephony.listen(incomingCallPhoneStateListener, PhoneStateListener.LISTEN_NONE);					
				}
				
			}
			
		};
    }
    

public class IncomingCallPhoneStateListener extends PhoneStateListener {
	private Context context;
	private boolean listenState = false;
	private IncomingCallPhoneStateListener (Context context){
		this.context= context;
	}
      @Override
      public void onCallStateChanged(int state, String phoneNumber) {
          switch (state) {
              //電話狀態是閒置的
              case TelephonyManager.CALL_STATE_IDLE:
            	  Log.d("ELITE","Phone is idle");
            	  if(listenState == true){
            		    TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
						telephony.listen(this, PhoneStateListener.LISTEN_NONE);			
            	  }
                  break;
              //電話狀態是接起的
              case TelephonyManager.CALL_STATE_OFFHOOK:
            	  Log.d("ELITE","Phone is offhook");
            	  handler.sendEmptyMessage(0);			//cancel the dialog when the phone is picked
                  break;
              //電話狀態是響起的
              case TelephonyManager.CALL_STATE_RINGING:
            	  Log.d("ELITE","Phone is ringing");
            	  listenState = true;
                  break;
              default:
                  break;
          }
      }
  }

}