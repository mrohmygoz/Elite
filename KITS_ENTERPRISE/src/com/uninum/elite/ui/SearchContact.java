package com.uninum.elite.ui;

import com.uninum.elite.R;
import com.uninum.elite.adapter.ContactAdapter;
import com.uninum.elite.adapter.SearchAdapter;
import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.GroupDBHelper;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

public class SearchContact extends ActionBarActivity{
	SearchAdapter adapter;
	GroupDBHelper groupDb;
	ContactDBHelper contactDb;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.search_title);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
//		getSupportActionBar().setIcon(R.drawable.action_search);
		setContentView(R.layout.activity_search);
		groupDb = GroupDBHelper.getInstance(this);
		contactDb = ContactDBHelper.getInstance(this);
		Cursor cursor = contactDb.queryAllContacts();
		adapter = new SearchAdapter(SearchContact.this, cursor, false);
		ListView list = (ListView)findViewById(R.id.listView1);
		list.setTextFilterEnabled(true);
		list.setAdapter(adapter);
		
		EditText searchEditText = (EditText)findViewById(R.id.editText1);
		searchEditText.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				Log.d("ELITE","search text:"+s);
				adapter.getFilter().filter(s.toString());
				adapter.notifyDataSetChanged();
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		adapter.setFilterQueryProvider(new FilterQueryProvider(){

			@Override
			public Cursor runQuery(CharSequence constraint) {
				// TODO Auto-generated method stub
				return ContactDBHelper.getInstance(SearchContact.this).queryContactBySearch(constraint.toString());
			}
			
		});
	}

}
