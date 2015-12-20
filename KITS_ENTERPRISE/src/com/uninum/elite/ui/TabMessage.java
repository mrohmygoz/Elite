package com.uninum.elite.ui;

import com.uninum.elite.R;
import com.uninum.elite.adapter.MessageGroupAdapter;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.database.MessageDBHelper;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import me.leolin.shortcutbadger.ShortcutBadger;

public class TabMessage extends Fragment {
	
	private MessageGroupAdapter adapter;
	private ListView listview;
	private GroupDBHelper groupDB;
	public static Handler updateViewHandler;
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Cursor cur = groupDB.queryAllGroup();
		adapter = new MessageGroupAdapter(getActivity(),cur);
		listview.setAdapter(adapter);
		ShortcutBadger.with(getActivity()).remove();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootview = inflater.inflate(R.layout.fragment_message, container, false);
		
		listview = (ListView) rootview.findViewById(R.id.elsv_message);
		
		return rootview;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
	    inflater.inflate(R.menu.menu_contact, menu);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		groupDB = GroupDBHelper.getInstance(getActivity());
		KitsApplication.getInstance().TabMsgStarted();
		
		updateViewHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(adapter!=null){
					Cursor cur = groupDB.queryAllGroup();
					adapter.changeCursor(cur);
					adapter.notifyDataSetChanged();
				}
			}
			
		};
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		
		}
		return super.onOptionsItemSelected(item);
	}
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
	}

}
