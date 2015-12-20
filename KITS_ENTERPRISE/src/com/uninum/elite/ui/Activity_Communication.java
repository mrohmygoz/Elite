package com.uninum.elite.ui;


import java.util.ArrayList;
import java.util.List;

import com.uninum.elite.adapter.MainDrawerAdapter;
import com.uninum.elite.object.MainDrawerItem;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.Logout;
import com.uninum.elite.system.SystemManager;
import com.uninum.elite.R;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar.Tab;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Activity_Communication extends  ActionBarActivity{

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private LinearLayout mLlvDrawerContent;
	private ListView mLsvDrawerMenu;
	private MainDrawerAdapter drawerAdapter;
	
	public static final String TAB_SELECT = "TAB_SELECT";
	public static final String TAB_FAVORITE = "TAB_FAVORITE";
	public static final String TAB_HISTORY = "TAB_HISTORY";
	public static final String TAB_CONTACT = "TAB_CONTACT";
	public static final String TAB_MESSAGE = "TAB_MESSAGE";
	
	public static final String TAB_HISTORY_UPDATE = "TAB_HISTORY_UPDATE";
	
	public static final int TAB_FAVORITE_POSITION = 0;
	public static final int TAB_HISTORY_POSITION = 1;
	public static final int TAB_CONTACT_POSITION = 2;
	public static final int TAB_MESSAGE_POSITION = 3;
	
	private int mCurrentMenuItemPosition = -1;
	private int currentPagePos;
	private final String[] currentPageString = {TAB_FAVORITE,TAB_HISTORY,TAB_CONTACT,TAB_MESSAGE};

	public String[] tabs;
	public static String[] naviga_titles ;
	private ViewPager viewPager;
	private PagerAdapter pagerAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		tabs = getResources().getStringArray(R.array.mainpage_tab_name_array);
		setContentView(R.layout.activity_communication);
		setUpNavigationDrawer();
		setDrawerMenu();
		
		viewPager = (ViewPager)findViewById(R.id.comm_contact_pager);
		pagerAdapter = new PagerAdapter(getSupportFragmentManager());
		viewPager.setPageTransformer(true, new ZoomOutPageTransformer() );
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOffscreenPageLimit(1);
		final ActionBar actionBar = getSupportActionBar();
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			
			@Override
			public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction arg1) {
				// TODO Auto-generated method stub
				viewPager.setCurrentItem(tab.getPosition());			
			}
			
			@Override
			public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
				// TODO Auto-generated method stub
				
			}
		};
		
		//select communication tab
		Bundle bundle = new Bundle();
		bundle = getIntent().getExtras();
		if(bundle!=null){
			String tab_select = bundle.getString(TAB_SELECT);
			if(tab_select.equals(TAB_FAVORITE))
				viewPager.setCurrentItem(TAB_FAVORITE_POSITION);
			else if(tab_select.equals(TAB_HISTORY)){
				if (bundle.getBoolean(TAB_HISTORY_UPDATE))
					KitsApplication.getInstance().setUpdateHistory();
				viewPager.setCurrentItem(TAB_HISTORY_POSITION);
			}
			else if(tab_select.equals(TAB_CONTACT))
				viewPager.setCurrentItem(TAB_CONTACT_POSITION);
			else if(tab_select.equals(TAB_MESSAGE))
				viewPager.setCurrentItem(TAB_MESSAGE_POSITION);
		}else
			viewPager.setCurrentItem(TAB_HISTORY_POSITION);
		
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (!KitsApplication.getInstance().isMainStarted()) {
			Intent intent = new Intent();
			intent.setClass(Activity_Communication.this, Activity_Main.class);
			Bundle bundle = new Bundle();
			bundle.putString(Activity_Main.MAIN_SELECT, Activity_Main.MAIN_SELECT_MENU);
			intent.putExtras(bundle);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}


	public class PagerAdapter extends FragmentStatePagerAdapter implements OnPageChangeListener{
		
		public PagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
			viewPager.setOnPageChangeListener(this);
		}

		@Override
		public Fragment getItem(int position) {
			// TODO Auto-generated method stub
			
			switch(position){
			case 0:
				Fragment FavoriteFragment = new TabFavorite();
				Bundle argsMain = new Bundle();
				FavoriteFragment.setArguments(argsMain);
				return FavoriteFragment;
			case 1:
				Fragment HistoryFragment = new TabHistory();
				Bundle args2 = new Bundle();
				HistoryFragment.setArguments(args2);
				return HistoryFragment;
			case 2:
				Fragment ContactFragment = new TabContact();
				Bundle args3 = new Bundle();
				ContactFragment.setArguments(args3);
				return ContactFragment;
			case 3:
				Fragment MessageFragment = new TabMessage();
				Bundle args4 = new Bundle();
				MessageFragment.setArguments(args4);
				return MessageFragment;
			}
			return null;
			
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return tabs.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// TODO Auto-generated method stub		
			return tabs[position];
		}

		@Override
		public void onPageSelected(int page) {
			// TODO Auto-generated method stub
			currentPagePos = page;
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}
		
	}

	public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
	    private static final float MIN_SCALE = 0.85f;
	    private static final float MIN_ALPHA = 0.5f;

	    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public void transformPage(View view, float position) {
	        int pageWidth = view.getWidth();
	        int pageHeight = view.getHeight();

	        if (position < -1) { // [-Infinity,-1)
	            // This page is way off-screen to the left.
	            view.setAlpha(0);

	        } else if (position <= 1) { // [-1,1]
	            // Modify the default slide transition to shrink the page as well
	            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
	            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
	            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
	            if (position < 0) {
	                view.setTranslationX(horzMargin - vertMargin / 2);
	            } else {
	                view.setTranslationX(-horzMargin + vertMargin / 2);
	            }

	            // Scale the page down (between MIN_SCALE and 1)
	            view.setScaleX(scaleFactor);
	            view.setScaleY(scaleFactor);

	            // Fade the page relative to its size.
	            view.setAlpha(MIN_ALPHA +
	                    (scaleFactor - MIN_SCALE) /
	                    (1 - MIN_SCALE) * (1 - MIN_ALPHA));

	        } else { // (1,+Infinity]
	            // This page is way off-screen to the right.
	            view.setAlpha(0);
	        }
	    }
	}
	
	private void setUpNavigationDrawer(){
		
		mDrawerLayout = (DrawerLayout)findViewById(R.id.comm_drw_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		mDrawerToggle = new ActionBarDrawerToggle(
	            this, 
	            mDrawerLayout,    // 讓 Drawer Toggle 知道母體介面是誰
	            R.drawable.btn_more, // Drawer 的 Icon
	            R.string.main_open_left_drawer, // Drawer 被打開時的描述
	            R.string.main_close_left_drawer // Drawer 被關閉時的描述
	            ) {
	                //被打開後要做的事情
	                @Override
	                public void onDrawerOpened(View drawerView) {
	                    // 將 Title 設定為自定義的文字
	                    getSupportActionBar().setTitle(R.string.main_open_left_drawer);
	                }

	                //被關上後要做的事情
	                @Override
	                public void onDrawerClosed(View drawerView) {
	                	String[] title_array = getResources().getStringArray(R.array.menu_items);
	                	if(mCurrentMenuItemPosition > -1){
	                		getSupportActionBar().setTitle(title_array[mCurrentMenuItemPosition]);
	                	}else {
	                		// 將 Title 設定回 APP 的名稱
	                        getSupportActionBar().setTitle(R.string.app_name);
	                	}
	                }
	    };
	    mDrawerLayout.setDrawerListener(mDrawerToggle);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    getSupportActionBar().setDisplayShowHomeEnabled(false);
	    //Enable Up Button 
	    getSupportActionBar().setHomeButtonEnabled(true);
	    getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

	private void setDrawerMenu(){
		mLsvDrawerMenu = (ListView)findViewById(R.id.comm_lsv_drawer_menu);
		mLlvDrawerContent = (LinearLayout) findViewById(R.id.comm_llv_left_drawer);
		mLsvDrawerMenu.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Log.d("KITs", "ITEM:"+position);
				selectMenuItem(position);
			}
			
		});
		String[] menu_items = getResources().getStringArray(R.array.menu_items);
		List<MainDrawerItem> itemList;
		itemList = new ArrayList<MainDrawerItem>();
		itemList.add(new MainDrawerItem(menu_items[0], R.drawable.ic_main_home));
		itemList.add(new MainDrawerItem(menu_items[1], R.drawable.ic_main_edit));
		itemList.add(new MainDrawerItem(menu_items[2], R.drawable.ic_main_info));
		itemList.add(new MainDrawerItem(menu_items[3], R.drawable.ic_main_info));

		drawerAdapter = new MainDrawerAdapter(this, R.layout.drawer_menu_item, itemList);
		mLsvDrawerMenu.setAdapter(drawerAdapter);
	}

	private void selectMenuItem(int position){
		mCurrentMenuItemPosition = position;		
		mLsvDrawerMenu.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mLlvDrawerContent);
		
		Intent intent = new Intent();
		intent.setClass(Activity_Communication.this, Activity_Main.class);
		
		Bundle bundle = new Bundle();
		switch(position){
		case Activity_Main.MAIN_MENU:
			bundle.putString(Activity_Main.MAIN_SELECT, Activity_Main.MAIN_SELECT_MENU);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case Activity_Main.USER_PROFILE:
			bundle.putString(Activity_Main.MAIN_SELECT, Activity_Main.MAIN_SELECT_USERPROFILE);
			bundle.putString(Activity_Main.MAIN_CALLBACK, Activity_Main.CALLBACK_COMM);
			bundle.putString(TAB_SELECT, currentPageString[currentPagePos]);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case Activity_Main.INFORMATION_MENU:
			bundle.putString(Activity_Main.MAIN_SELECT, Activity_Main.MAIN_SELECT_INFORMATION);
			bundle.putString(Activity_Main.MAIN_CALLBACK, Activity_Main.CALLBACK_COMM);
			bundle.putString(TAB_SELECT, currentPageString[currentPagePos]);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case Activity_Main.SYSTEM_INFO:
			Intent systemInfoIntent = new Intent();
			systemInfoIntent.setClass(Activity_Communication.this, SystemInfo.class);
			systemInfoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(systemInfoIntent);
			break;
		}
		
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if(mDrawerToggle.onOptionsItemSelected(item)){
			return true;
		}
		switch (item.getItemId()) {
		case R.id.menu_main_search:
			Intent intent = new Intent();
			intent.setClass(this, SearchContact.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.menu_communication, menu);
//		MenuItem logout = menu.add(0, LOGOUT_MENU, LOGOUT_MENU,
//				R.string.main_logout);
		return true;
	}


}
