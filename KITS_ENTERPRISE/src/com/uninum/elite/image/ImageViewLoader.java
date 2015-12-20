package com.uninum.elite.image;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.uninum.elite.R;
import com.uninum.elite.database.ImageDBHelper;
import com.uninum.elite.object.Contact;
import com.uninum.elite.object.Image;
import com.uninum.elite.system.KitsApplication;
import com.uninum.elite.system.SystemManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class ImageViewLoader {
	private Context context;
	private static final int REQUEST_TIME_MS = 60*1000;
	private static final List displayedImages = Collections.synchronizedList(new LinkedList());
	public ImageViewLoader(Context context){
		this.context = context;
	}
	
	public void loadImageToView (final String url, final ImageView imageView) {
		
		if (KitsApplication.messageImageRecord.get(url)==null) {
			ImageLoader.getInstance().displayImage(url, imageView, new ImageLoadingListener(){
				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
					
				}
	
				@Override
				public void onLoadingComplete(String imageUrl, View view, Bitmap loadedImage) {
					// TODO Auto-generated method stub
					if (loadedImage != null) {
						// Source image size
						int width = loadedImage.getWidth();
						int height = loadedImage.getHeight();
						  
						// create result bitmap output
						Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
						  
						// set canvas for painting
						Canvas canvas = new Canvas(result);
						canvas.drawARGB(0, 0, 0, 0);
						 
						// configure paint
						final Paint paint = new Paint();
						paint.setAntiAlias(true);
						paint.setColor(Color.BLACK);
						 
						// configure rectangle for embedding
						final Rect rect = new Rect(0, 0, width, height);
						final RectF rectF = new RectF(rect);
						
						// draw Round rectangle to canvas
						canvas.drawRoundRect(rectF, 30, 30, paint);
						 
						// create Xfer mode
						paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
						  
						// draw source image to canvas
						canvas.drawBitmap(loadedImage, rect, rect, paint);
						
						ImageView imageView = (ImageView) view;
						boolean firstDisplay = !displayedImages.contains(imageUrl);
						if (firstDisplay) {
							AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
							fadeImage.setDuration(500);
							fadeImage.setInterpolator(new DecelerateInterpolator());
							imageView.setImageBitmap(result);
							imageView.startAnimation(fadeImage);					
						} else {
							imageView.setImageBitmap(result);
						}
						//KitsApplication.imageRequestRecord.put(picID+unitUUID, new Image(picID, unitUUID, contactUUID, loadedImage, System.currentTimeMillis()));
						KitsApplication.messageImageRecord.put(url, result);
						displayedImages.add(imageUrl);
					}
				}
	
				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					Log.e("Jenny", "url: " + url);
					Log.e("Jenny", "image view loader loading failed: " + failReason.getType().toString());
				}
	
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					
				}
			});
		} else {
//			Log.i("Jenny","image view loader reuse image");
			Bitmap bitmap = KitsApplication.messageImageRecord.get(url);
			if(bitmap!=null){
				imageView.setImageBitmap(bitmap);
			}else{
//				imageView.setImageResource(R.drawable.totoro);
			}
		}
	}
	
	public void loadImageToView(Contact contact, ImageView imageView){
		final String picID = contact.getPicture();
		final String unitUUID = contact.getUnitUUID();
		final String contactUUID = contact.getContactUUID();
			if(!picID.equals(Contact.DEFAULT_PICTURE) ){
				Log.d("ELITE","hashmap size:"+KitsApplication.imageRequestRecord.size());
				
				//if the requested image isn't in image cache
				if(KitsApplication.imageRequestRecord.get(picID + unitUUID) == null || (System.currentTimeMillis() - KitsApplication.imageRequestRecord.get(picID + unitUUID).getTimeStamp()) > REQUEST_TIME_MS){
							if(KitsApplication.imageRequestRecord.get(picID + unitUUID) == null){
								Log.d("ELITE", "hashmap null");
							}else{
								Log.d("ELITE", "TIME:"+ (System.currentTimeMillis() - KitsApplication.imageRequestRecord.get(picID + unitUUID).getTimeStamp()));
							}
							long timeStamp = ImageDBHelper.getInstance(context).queryImageTimeStamp(unitUUID, picID);
							String url = "";
							url =  "https://ts.kits.tw/projectLYS/v2/File/"+SystemManager.getToken(context)+"/image/"+picID+"?timeStamp=" + timeStamp +"&unitUuid="+unitUUID;		
							ImageLoader.getInstance().displayImage(url, imageView, new ImageLoadingListener(){
							
							@Override
							public void onLoadingCancelled(String arg0, View arg1) {
								// TODO Auto-generated method stub
								
							}
				
							@Override
							public void onLoadingComplete(String imageUrl, View view, Bitmap loadedImage) {
								// TODO Auto-generated method stub
								if (loadedImage != null) {
									Image image = new Image(picID, unitUUID , contactUUID, loadedImage, System.currentTimeMillis());
									ImageDBHelper.getInstance(context).insertImage(image);
									ImageView imageView = (ImageView) view;
									boolean firstDisplay = !displayedImages.contains(imageUrl);
									if (firstDisplay) {
										AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
										fadeImage.setDuration(500);
										fadeImage.setInterpolator(new DecelerateInterpolator());
										imageView.setImageBitmap(DrawCircularImage.DrawCircular(loadedImage));
										imageView.startAnimation(fadeImage);					
									} else {
										imageView.setImageBitmap(DrawCircularImage.DrawCircular(loadedImage));
									}
									KitsApplication.imageRequestRecord.put(picID+unitUUID, new Image(picID, unitUUID, contactUUID, loadedImage, System.currentTimeMillis()));
									Log.d("ELITE","Get hashmap after put:"+picID + " hash:"+KitsApplication.imageRequestRecord.get(picID + unitUUID).getPicID());
									displayedImages.add(imageUrl);
								}
							}
				
							@Override
							public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
								// TODO Auto-generated method stub
								Log.d("ELITE", failReason.getType().toString());
								Image tempImage = KitsApplication.imageRequestRecord.get(picID+unitUUID);
								if(tempImage!=null)
									KitsApplication.imageRequestRecord.put(picID+unitUUID, new Image(picID, unitUUID, contactUUID, tempImage.getImageBitmap(), System.currentTimeMillis()));
								Bitmap bitmap = ImageDBHelper.getInstance(context).queryImage(unitUUID, picID);
								KitsApplication.imageRequestRecord.put(picID+unitUUID, new Image(picID, unitUUID, contactUUID, bitmap, System.currentTimeMillis()));
								ImageView imageView = (ImageView)view;
								if(bitmap!=null){
									imageView.setImageBitmap(DrawCircularImage.DrawCircular(bitmap));
								}else{
									imageView.setImageResource(R.drawable.contact_head_big);
								}
							}
				
							@Override
							public void onLoadingStarted(String imageUri, View view) {
								// TODO Auto-generated method stub
							}
						});
				}else{
					Log.d("ELITE","Reuse Image");
					Log.d("ELITE", "TIME2:"+ ( System.currentTimeMillis() - KitsApplication.imageRequestRecord.get(picID + unitUUID).getTimeStamp() ));
					Bitmap bitmap = KitsApplication.imageRequestRecord.get(picID + unitUUID).getImageBitmap();
					if(bitmap!=null){
						imageView.setImageBitmap(DrawCircularImage.DrawCircular(bitmap));
					}else{
						imageView.setImageResource(R.drawable.contact_head_big);
					}
				}
		}else{
			ImageLoader.getInstance().displayImage("drawable://"+R.drawable.contact_head_big, imageView);			
		}
	}

}
