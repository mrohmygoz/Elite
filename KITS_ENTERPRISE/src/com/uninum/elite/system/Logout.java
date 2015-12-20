package com.uninum.elite.system;

import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.HistoryDBHelper;
import com.uninum.elite.ui.Activity_Main;

import android.content.Context;

public class Logout {
	private Context context;
	public Logout(Context context){
		this.context = context;
	}
	
	public void LogoutApp(){
		SystemManager.clearAll();
		GroupDBHelper.getInstance(context).deleteAllGroup();
		ContactDBHelper.getInstance(context).deleteAllContacts();
		HistoryDBHelper.getInstance(context).removeAll();
		SystemManager.clearAll();
	}
}
