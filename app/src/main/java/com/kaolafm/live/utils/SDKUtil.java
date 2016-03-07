package com.kaolafm.live.utils;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/******************************************
 * 类描述： 系统版本工具类 类名称：SDKUtil
 * 
 * @version: 1.0
 * @author: shaoningYang
 * @time: 2015-3-20 15:17
 ******************************************/

@SuppressLint("NewApi")
public class SDKUtil {

	private SDKUtil() {
	}

	public static int getSdkInt() {
		if (VERSION.RELEASE.startsWith("1.5")) {
			return 3;
		}

		return HelperInternal.getSdkIntInternal();
	}

	private static class HelperInternal {
		private static int getSdkIntInternal() {
			return VERSION.SDK_INT;
		}
	}

	/**
	 * (Android 2.2.x)
	 * 
	 * @return >=API LEVEL 8 true，否则 false
	 */
	public static boolean hasFroyo() {
		// Can use static final constants like FROYO, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed
		// behavior.
		return VERSION.SDK_INT >= VERSION_CODES.FROYO;
	}

	/**
	 * (Android 2.3.2 Android 2.3.1 Android 2.3)
	 * 
	 * @return >=API LEVEL 9 true，否则 false
	 */
	public static boolean hasGingerbread() {
		return VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD;
	}

	/**
	 * (Android 2.3.4 Android 2.3.3)
	 * 
	 * @return >=API LEVEL 10 true，否则 false
	 */
	public static boolean hasGingerbread_mr1() {
		return VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD_MR1;
	}

	/**
	 * (Android 3.0.x)
	 * 
	 * @return >=API LEVEL 11 true，否则 false
	 */
	public static boolean hasHoneycomb() {
		return VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;
	}

	/**
	 * (Android 3.1.x)
	 * 
	 * @return >=API LEVEL 12 true，否则 false
	 */
	public static boolean hasHoneycombMR1() {
		return VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;
	}

	/**
	 * (Android 4.1, 4.1.1)
	 * 
	 * @return >=API LEVEL 16 true，否则 false
	 */
	public static boolean hasJellyBean() {
		return VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;
	}

	/**
	 * (Android 2.0)
	 * 
	 * @return >=API LEVEL 5 true，否则 false
	 */
	public static boolean hasECLAIR() {
		return VERSION.SDK_INT >= VERSION_CODES.ECLAIR;
	}

	/**
	 * (Android 4.0, 4.0.1, 4.0.2)
	 * 
	 * @return >=API LEVEL 14 true，否则 false
	 */
	public static boolean hasICE_CREAM_SANDWICH() {
		return VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	/**
	 * (Android 4.2, 4.2.2)
	 * 
	 * @return >=API LEVEL 17 true，否则 false
	 */
	public static boolean hasJELLY_BEAN_MR1() {
		return VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1;
	}

	/**
	 * (Android 4.3)
	 * 
	 * @return >=API LEVEL 18 true，否则 false
	 */
	public static boolean hasJELLY_BEAN_MR2() {
		return VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2;
	}

	/**
	 * (Android 4.4, KitKat)
	 * 
	 * @return >=API LEVEL 19 true，否则 false
	 */
	public static boolean hasKITKAT() {
		return VERSION.SDK_INT >= VERSION_CODES.KITKAT;
	}

	/**
	 * (Android 5.0)
	 * 
	 * @return >=API LEVEL 21 true，否则 false
	 */
	public static boolean hasLOLLIPOP() {
		return VERSION.SDK_INT > VERSION_CODES.KITKAT_WATCH;
	}

	/**
	 * 为视图设置背景
	 * 
	 * @param view
	 */
	@SuppressWarnings("deprecation")
	public static void setBackgroundDrawable(View view, Drawable drawable) {
		if (hasJellyBean()) {
			view.setBackground(drawable);
		} else {
			view.setBackgroundDrawable(drawable);
		}
	}

	/**
	 * 删除视图观察对象
	 * 
	 * @param view
	 * @param onGlobalLayoutListener
	 */
	@SuppressWarnings("deprecation")
	public static void remove(View view,
		OnGlobalLayoutListener onGlobalLayoutListener) {
		if (hasJellyBean()) {
			view.getViewTreeObserver().removeOnGlobalLayoutListener(
				onGlobalLayoutListener);
		} else {
			view.getViewTreeObserver().removeGlobalOnLayoutListener(
				onGlobalLayoutListener);
		}
	}
}
