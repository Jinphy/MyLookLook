package com.example.jinphy.mylooklook.util;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

/**
 * liujingyuan
 */
public class ScreenUtils {

	private static int[] deviceWidthHeight = new int[2];
	public static int[] getDeviceInfo(Context context) {
		if ((deviceWidthHeight[0] == 0) && (deviceWidthHeight[1] == 0)) {
			DisplayMetrics metrics = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay()
					.getMetrics(metrics);

			deviceWidthHeight[0] = metrics.widthPixels;
			deviceWidthHeight[1] = metrics.heightPixels;
		}
		return deviceWidthHeight;
	}
	/**
	 * @param context 上下文
	 * @param dpValue dp数值
	 * @return dp to  px
	 */
	public static int dp2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);

	}
	/**
	 * 获取状态来的高度，单位为像素
	 * @param context 上下文
	 * @return 返回状态栏像素单位的高度
	 * */
	public static int getStatusBarHeight(Context context){
		return dp2px(context,24);
	}
	/**
	 * 把状态栏设置为指定的颜色
	 * @param activity activity 对象
	 * @param color 颜色值
	 * */
	public static void setStatusBarColor(@NonNull Activity activity, int color){
		activity.getWindow().setStatusBarColor(color);
	}

	/**
	 *
	 * 获取对应activity对象下的状态栏颜色
	 * @param activity activity对象
	 *
	 * */
	public static int getStatusBarColor(Activity activity) {
		return activity.getWindow().getStatusBarColor();
	}

	/**
	 * 把对应activity对象下的屏幕设置成全屏，该方法必须在
	 * activity.setContentView()之前调用
	 * @param activity activity对象
	 * */
	public static void setFullScreen(Activity activity) {
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);

		activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	/**
	 * 获取屏幕宽度
	 * @param context 上下文
	 * @return 屏幕宽度，单位为像素
	 * */
	public static int getScreenWidth(Context context){
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * 获取屏幕高度
	 * @param context 上下文
	 * @return 屏幕高度，单位为像素
	 * */
	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	/**
	 * 获取屏幕密度比
	 * @param context 上下文
	 * @return 屏幕密度比，xml文件中的dp单位的大小乘上该值即为像素大小
	 * */
	public static float getDensity(Context context) {
		return context.getResources().getDisplayMetrics().density;
	}

	/**
	 * 获取字体的密度比
	 * @param context 上下文
	 * @return 字体的密度比，xml文件中的sp单位的字体大小乘上该值，即为字体的像素大小
	 * */
	public static float getScaleDensity(Context context) {
		return context.getResources().getDisplayMetrics().scaledDensity;
	}
	/**
	 * 把像素单位转换为像素无关单位dp
	 * @param context    上下文
	 * @param pxValue  px的数值
	 * @return  px to dp
	 */
	public static int px2dp(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);

	}
}
