package com.uninum.elite.ui;

import com.uninum.elite.R;
import com.uninum.elite.system.Logout;
import com.uninum.elite.system.SystemManager;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class SystemSetting extends Fragment implements OnClickListener {
	private static String APP_VERSION = "APP_VERSION";
	private TextView account_tv;
	private Button about_btn, version_tv;
	private TableRow versionRow_tr;
	private int secretLogout=0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootview = inflater.inflate(R.layout.fragment_setting, container, false);
		account_tv = (TextView)rootview.findViewById(R.id.setting_account_tv);
		version_tv = (Button)rootview.findViewById(R.id.setting_version_tv);
		version_tv.setOnClickListener(this);
		versionRow_tr = (TableRow)rootview.findViewById(R.id.setting_version_row);
		versionRow_tr.setOnClickListener(this);
		about_btn = (Button)rootview.findViewById(R.id.setting_about_btn);
		about_btn.setOnClickListener(this);
		return rootview;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		try {
		account_tv.setText(SystemManager.getPreString(getActivity(), SystemManager.USER_ACCOUNT));
		version_tv.setText(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch(view.getId()){
		case R.id.setting_about_btn:
			createAboutDialog();
			break;
		case R.id.setting_version_tv:
			secretLogout++;
			if(secretLogout==8){
				Toast.makeText(getActivity(), "登出", Toast.LENGTH_SHORT).show();
				Logout logout = new Logout(getActivity());
				logout.LogoutApp();
				getActivity().finish();
			}else if(secretLogout == 6){
				Toast.makeText(getActivity(), "再按兩下即可登出", Toast.LENGTH_SHORT).show();
			}else if(secretLogout == 7){
//				Toast.makeText(getActivity(), "再按一下即可登出", Toast.LENGTH_SHORT).show();
			}
			   new Handler().postDelayed(new Runnable() {

			        @Override
			        public void run() {
			        	secretLogout = 0;                       
			        }
			    }, 5000);
			break;
		}
	}


	private void createAboutDialog(){
		 AlertDialog.Builder builder = new Builder(getActivity());
		 builder.setMessage(getActivity().getResources().getString(R.string.setting_info_about_message));
		 builder.setTitle(getActivity().getResources().getString(R.string.setting_info_about));
		 builder.setPositiveButton(getActivity().getResources().getString(R.string.setting_info_dialog_finish), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();	
			}
	
		});
		builder.create().show();
	}
}
