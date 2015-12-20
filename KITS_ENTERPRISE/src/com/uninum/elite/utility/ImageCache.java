package com.uninum.elite.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

import com.uninum.elite.object.Image;

public class ImageCache<K, V extends Image> extends HashMap<K, V> {
	List<V> list = new ArrayList<V>();
	int CACHE_SIZE = 30;
	@Override
	public V get(Object key) {
		// TODO Auto-generated method stub
		return super.get(key);
	}

	@Override
	public V put(K key, V value) {
		// TODO Auto-generated method stub
//		if(list.size()>=30){
//			for(int i=list.size()-1;i>= CACHE_SIZE ; i--){
//				super.remove(list.get(i).getPicID()+list.get(i).getGroupUUID());
//				list.remove(i);
//				Log.d("IMAGE_CACHE","remove old image");
//			}
//		}
//		list.add(value);
		return super.put(key, value);
	}  

}
