package com.uninum.elite.image;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class BitmapProcess {

	public static String bitmapToString(Bitmap bitmap) 
	{
		
       
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 30 , baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
	
	public static Bitmap StringToBitmap(String base64_imgStr)
	{
		byte[] decodedString = Base64.decode(base64_imgStr, Base64.DEFAULT);
		Bitmap recover_bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
		return recover_bitmap;
	}
	
	public static byte[] BitmapToByte(Bitmap bitmap){
		if(bitmap!=null){
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
			byte[] bArray = bos.toByteArray();
			return bArray;
		}else{
			return null;
		}
	}
	
	public static Bitmap BytesToBitmap(byte[] byteArray){
	    if(byteArray.length!=0){
	    	return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
	    }
	    else {
	    	return null;
	    }
  }
}
