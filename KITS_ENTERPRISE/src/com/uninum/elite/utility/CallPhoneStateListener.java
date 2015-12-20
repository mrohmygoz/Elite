package com.uninum.elite.utility;

import com.uninum.elite.webservice.WSCall;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class CallPhoneStateListener extends PhoneStateListener {
	  private Context context;
	  public CallPhoneStateListener(Context context){
		  this.context = context;
	  }
      @Override
      public void onCallStateChanged(int state, String phoneNumber) {
          switch (state) {
              //電話狀態是閒置的
              case TelephonyManager.CALL_STATE_IDLE:
                  break;
              //電話狀態是接起的
              case TelephonyManager.CALL_STATE_OFFHOOK:
                  break;
              //電話狀態是響起的
              case TelephonyManager.CALL_STATE_RINGING:
            	  if(WSCall.callCountDownTimer != null){
            		  WSCall.callCountDownTimer.cancel();
            		  TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
					  telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
            	  }
                  break;
              default:
                  break;
          }
      }
  }
