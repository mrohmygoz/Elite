package com.uninum.elite.ui;

import java.util.ArrayList;
import java.util.List;

import com.uninum.elite.adapter.FavoriteAdapter;
import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.object.Favorite;
import com.uninum.elite.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

public class TabFavorite extends Fragment{

	ContactDBHelper contactDB;
	FavoriteAdapter adapter;
	GridView grid ;
	TextView  defaultListHint_tv;
	public static Handler updateFavHandler;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootview = inflater.inflate(R.layout.fragment_favorite, container, false);
		grid = (GridView)rootview.findViewById(R.id.favorite_grid);
		defaultListHint_tv = (TextView)rootview.findViewById(R.id.favorite_default_hint_tv);
		return rootview;
	}

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

	}


	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateFavorite();
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		updateFavHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				updateFavorite();
			}
			
		};
		
	}
	
	private void updateFavorite(){
		contactDB = ContactDBHelper.getInstance(getActivity());
		adapter = new FavoriteAdapter(getActivity(), contactDB.queryFavoriteContacts(), CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		grid.setAdapter(adapter);
		if(adapter.getCount()>0){
			defaultListHint_tv.setVisibility(TextView.INVISIBLE);
			
		}else{
			defaultListHint_tv.setVisibility(TextView.VISIBLE);
		}
	}

	
	
}
