package com.ambigu.util;

import android.content.SharedPreferences;
import android.util.Log;

public class ApplicationVar {
	public static String appid = "";

	private static boolean isSharing=false;
	/***
	 * 4.4以下(也就是kitkat以下)的版本
	 */
	public static final int KITKAT_LESS = 0;
	/***
	 * 4.4以上(也就是kitkat以上)的版本,当然也包括最新出的5.0棒棒糖
	 */
	public static final int KITKAT_ABOVE = 1;
	
	/***
	 * 裁剪图片成功后返回
	 */
	public static final int INTENT_CROP = 2;
	static SharedPreferences sharedPreferences = null;
	

	public static SharedPreferences getSharedPreferences() {
		return sharedPreferences;
	}

	public static void setSharedPreferences(SharedPreferences sharedPreferences) {
		ApplicationVar.sharedPreferences = sharedPreferences;
	}
	public static String getId(){
		if(sharedPreferences==null) return null;
		Log.e("username",sharedPreferences.getString("username", null)+"1");
		return sharedPreferences.getString("username", null);
	}
	
	public static boolean isSharing() {
		return isSharing;
	}

	public static void setSharing(boolean isSharing) {
		ApplicationVar.isSharing = isSharing;
	}

	

}
