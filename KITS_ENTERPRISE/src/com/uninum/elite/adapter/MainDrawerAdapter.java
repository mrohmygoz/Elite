package com.uninum.elite.adapter;

import java.util.List;

import com.uninum.elite.object.MainDrawerItem;
import com.uninum.elite.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainDrawerAdapter extends ArrayAdapter<MainDrawerItem> {
	int layoutResID;
	Context context;
	List<MainDrawerItem> drawerItemList;
	public MainDrawerAdapter(Context context, int layoutResID, List<MainDrawerItem> listItems) {
		super(context, layoutResID, listItems);
		// TODO Auto-generated constructor stub
		this.layoutResID = layoutResID;
		this.drawerItemList = listItems;
		this.context = context;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		DrawerItemHolder drawerHolder;
		for(int i=0;i<drawerItemList.size();i++){
			Log.d("kits new", "item"+i + " Name:"+ drawerItemList.get(i).getItemName() + " ID:"+ drawerItemList.get(i).getImgResID());
		}
		View view = convertView;
		if(view == null){
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			drawerHolder = new DrawerItemHolder();			
			view = inflater.inflate(layoutResID, parent, false);
			drawerHolder.itemName = (TextView) view.findViewById(R.id.drawer_itemName);
			drawerHolder.icon = (ImageView)view.findViewById(R.id.drawer_icon);
			view.setTag(drawerHolder);
		}else{
			drawerHolder = (DrawerItemHolder)view.getTag();
		}
		MainDrawerItem aItem = (MainDrawerItem)this.drawerItemList.get(position);
		drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(aItem.getImgResID()));
		drawerHolder.itemName.setText(aItem.getItemName());
		return view;
	}

	private static class DrawerItemHolder {
		TextView itemName;
		ImageView icon;
	}
}
