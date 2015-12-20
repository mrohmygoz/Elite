package com.uninum.elite.utility;

import android.content.res.Resources;
import android.util.TypedValue;


public class LayoutUtility {

	public static int dpToPixel(Resources r, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				r.getDisplayMetrics());
	}
}