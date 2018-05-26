package com.ambigu.util;

import java.util.Calendar;
import java.util.TimeZone;

import android.util.Log;

public class DateUtil {
	// 获得当前年月日时分秒星期
	public static String getTime() {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		String mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
		String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
		String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
		String mHour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));// 时
		String mMinute = String.valueOf(c.get(Calendar.MINUTE));// 分
		String mSecond = String.valueOf(c.get(Calendar.SECOND));// 秒
		if(mHour.length()==1) mHour="0"+mHour;
		if(mMinute.length()==1) mMinute="0"+mMinute;
		if(mSecond.length()==1) mSecond="0"+mSecond;
		String date = mYear + "年" + mMonth + "月" + mDay + "日" + " " + mHour + ":" + mMinute + ":" + mSecond;
		Log.e("date", date);
		return date;
	}

}
