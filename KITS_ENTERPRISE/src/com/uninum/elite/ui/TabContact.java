package com.uninum.elite.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uninum.elite.adapter.ContactAdapter;
import com.uninum.elite.data.UpdateContacts;
import com.uninum.elite.database.ContactDBHelper;
import com.uninum.elite.database.GroupDBHelper;
import com.uninum.elite.database.ContactDBHelper.ContactEntry;
import com.uninum.elite.database.GroupDBHelper.GroupEntry;
import com.uninum.elite.object.Contact;
import com.uninum.elite.object.ContactGroup;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.MyContentProvider;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.utility.NetworkUtility;
import com.uninum.elite.webservice.PubnubHelper;
import com.uninum.elite.webservice.SimpleWSJsonRequest;
import com.uninum.elite.webservice.WSContact;
import com.uninum.elite.R;




































import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class TabContact extends Fragment implements OnRefreshListener {
	
	private ExpandableListView expandableListView;
	private PullToRefreshLayout mPullToRefreshLayout;
	private List<ContactGroup> contactGroups = new ArrayList<ContactGroup>();
	private String[] unitUUID;
	private String[] unitName;
	public static Handler updateHandler;
	public static Handler updateViewHandler;
	private ContactAdapter adapter;
	private GroupDBHelper groupDb;
	private ContactDBHelper contactDb;
	private PubnubHelper pubnubHelper;
	public static String callbackTabContact = "TAB_CONTACT_CALLBACK";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootview = inflater.inflate(R.layout.fragment_contact, container, false);
//		LinearLayout linearLayout = (LinearLayout)rootview.findViewById(R.id.contact_linearLayout);		
		expandableListView = (ExpandableListView)rootview.findViewById(R.id.elsv_contact);
		mPullToRefreshLayout = (PullToRefreshLayout)rootview.findViewById(R.id.ptr_layout);
		ActionBarPullToRefresh.from(getActivity())
              .allChildrenArePullable()
              .listener(this)
              .setup(mPullToRefreshLayout);
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
		pubnubHelper = new PubnubHelper(getActivity());
		
		updateHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				Log.d("KITs update","get message");
				super.handleMessage(msg);
				getGroups();
			}
			
		};
		
		updateViewHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				groupDb = GroupDBHelper.getInstance(getActivity());
				contactDb = ContactDBHelper.getInstance(getActivity());
				Cursor cursor = groupDb.queryAllGroup();
				adapter = new ContactAdapter(cursor , getActivity());
				expandableListView.setAdapter(adapter);
				if(mPullToRefreshLayout!=null)
					mPullToRefreshLayout.setRefreshComplete();
				
				List<ContactGroup> insertGroup = (List<ContactGroup>) msg.obj;
				if (insertGroup!=null && insertGroup.size()>0){
					for (int i=0; i<insertGroup.size(); i++){
						pubnubHelper.Subscribe(insertGroup.get(i).getGroupUUID());		//subscribe
						getActivity().getContentResolver().call(						//add uri
								MyContentProvider.CONTENT_URI, 
								"addURI", 
								insertGroup.get(i).getGroupUUID(), 
								null);
					}
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
	
	private void getGroups(){		
			if(NetworkUtility.isConnected(getActivity())){
				UpdateContacts updateContacts = new UpdateContacts(getActivity(), callbackTabContact);
				updateContacts.getGroupsUUID();
			}else{
				Toast.makeText(getActivity(), getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
			}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		groupDb = GroupDBHelper.getInstance(getActivity());
		contactDb = ContactDBHelper.getInstance(getActivity());
		Cursor cursor = groupDb.queryAllGroup();
		adapter = new ContactAdapter(cursor , getActivity());
		expandableListView.setAdapter(adapter);	
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onRefreshStarted(View view) {
		// TODO Auto-generated method stub
		if ( NetworkUtility.isConnected(getActivity()) ){
        	getGroups();
		}else{
			if(mPullToRefreshLayout!=null)
				mPullToRefreshLayout.setRefreshComplete();
			Toast.makeText(getActivity(), getString(R.string.network_invalid), Toast.LENGTH_SHORT).show();
		}
			
	}

}

