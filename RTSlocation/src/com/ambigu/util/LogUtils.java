package com.ambigu.util;

import android.content.Context;
import android.util.Log;

public class LogUtils {

	public LogUtils() {
		// TODO Auto-generated constructor stub
	}
	public static void showLoG(Context context,String mes){
		Log.e(context.getClass().getName(),mes);
	}

}
