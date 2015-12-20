package com.uninum.elite.ui;

import com.uninum.elite.image.DrawCircularImage;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.R;
import com.kits.enterprise.connect.AESprocess;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Welcome extends ActionBarActivity {

	private Button btnRegister;
	private View viewRegister;
	private LinearLayout welcome_screen;
	private static final long FADE_IN_DURATION = 2000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome_main);	
		btnRegister = (Button)findViewById(R.id.btn_welcome_register);
		viewRegister = (View)findViewById(R.id.view_welcome_register);
		welcome_screen = (LinearLayout)findViewById(R.id.welcome_screen);
		Animation animation = new AlphaAnimation(0,1);
		animation.setDuration(FADE_IN_DURATION);
		welcome_screen.setAnimation(animation);
		animation.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
				if(SystemManager.getPreString(Welcome.this, SystemManager.USER_ACCOUNT) != ""){					
					Intent intent = new Intent();
					intent.setClass(Welcome.this, Activity_Main.class);
					startActivity(intent);
					finish();
					overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);  
				}else{
					Intent intent = new Intent();
					intent.setClass(Welcome.this, Register.class);
					startActivity(intent);
					finish();
					overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);  
				}
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub				
			}			
		});
		animation.startNow();
		btnRegister.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(Welcome.this, Register.class);
				startActivity(intent);			
			}
			
		});
		Log.d("KITs","Welcome: user account:"+ SystemManager.getPreString(Welcome.this, SystemManager.USER_ACCOUNT) );		
	}
}
