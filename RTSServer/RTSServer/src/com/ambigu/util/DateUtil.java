package com.ambigu.util;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

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
		if (mHour.length() == 1)
			mHour = "0" + mHour;
		if (mMinute.length() == 1)
			mMinute = "0" + mMinute;
		if (mSecond.length() == 1)
			mSecond = "0" + mSecond;
		String date = mYear + "年" + mMonth + "月" + mDay + "日" + " " + mHour + ":" + mMinute + ":" + mSecond;
		return date;
	}

	public static boolean isOneInfo(String date1, String date2, long max) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		try {
			long t = sdf.parse(date2).getTime() - sdf.parse(date1).getTime();
			System.out.println(t);
			if (t/1000 < max)
				return true;
			else
				return false;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}
	
	public static String oneInfoMaxDate(String date1, long max) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		try {
			long t = sdf.parse(date1).getTime()+max*1000;
			Date date=new Date(t);
			String date2=sdf.format(date);
			System.out.println(date2);
			return date2;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			return "";
		}
	}

}
