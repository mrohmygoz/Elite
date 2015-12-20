package com.uninum.elite.object;

public class MainDrawerItem {
	String itemName;
	int imgResID;
	
	public MainDrawerItem(String itemName, int imgResID){
		this.itemName = itemName;
		this.imgResID = imgResID;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public int getImgResID() {
		return imgResID;
	}

	public void setImgResID(int imgResID) {
		this.imgResID = imgResID;
	}
	
	
}
