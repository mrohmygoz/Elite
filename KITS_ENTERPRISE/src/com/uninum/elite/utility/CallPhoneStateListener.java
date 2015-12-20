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
              //�q�ܪ��A�O���m��
              case TelephonyManager.CALL_STATE_IDLE:
                  break;
              //�q�ܪ��A�O���_��
              case TelephonyManager.CALL_STATE_OFFHOOK:
                  break;
              //�q�ܪ��A�O�T�_��
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
