package com.uninum.elite.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class DrawCircularImage {

	public static Bitmap DrawCircular(Bitmap bitmap){
		 int targetWidth = 320;
	      int targetHeight = 320;
	      Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,Bitmap.Config.ARGB_8888); 
	      BitmapShader shader;
	      shader = new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);

	      Paint paint = new Paint();
	      paint.setAntiAlias(true);
	      paint.setShader(shader);
	      Canvas canvas = new Canvas(targetBitmap);
	      Path path = new Path();
	      path.addCircle(((float) targetWidth - 1) / 2,
	      ((float) targetHeight - 1) / 2,
	      (Math.min(((float) targetWidth),((float) targetHeight)) / 2),Path.Direction.CCW);
	      paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	      paint.setStyle(Paint.Style.STROKE);
	      canvas.clipPath(path);
	      Bitmap sourceBitmap = bitmap;
	      canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(),sourceBitmap.getHeight()),
	      new Rect(0, 0, targetWidth,targetHeight), null);
	      return targetBitmap;
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,float roundPx)
    {         
	     Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), 
	                                        bitmap.getHeight(), 
	                                        Config.ARGB_8888);  
	     Canvas canvas = new Canvas(output);  
	     final int color = 0xff424242;  
	     final Paint paint = new Paint();  
	     final Rect rect = new Rect(0, 0, bitmap.getWidth(),
	                                     bitmap.getHeight());  
	     final RectF rectF = new RectF(rect);  
	     paint.setAntiAlias(true);  
	     canvas.drawARGB(0, 0, 0, 0);  
	     paint.setColor(color);  
	     canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
	     paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
	     canvas.drawBitmap(bitmap, rect, rect, paint);  
	     return output;  
    }  
}
