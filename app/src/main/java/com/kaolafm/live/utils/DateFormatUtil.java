/*
 * comlan
 */

package com.kaolafm.live.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateFormatUtil {

	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT_PATTERN_1 = "MM/dd HH:mm";
	public static final String DATE_FORMAT_PATTERN_2 = "mm时ss分";
	public static final String DATE_FORMAT_PATTERN_3 = "mm:ss";
	public static final String DATE_FORMAT_PATTERN_4 = "mm:ss";
	public static final String DATE_FORMAT_PATTERN_5 = "yyyy/MM/dd";
	public static final String DATE_FORMAT_HOUR = "HH:mm:ss";

	public static String getCurrDate() {
		Calendar c = Calendar.getInstance();
		Date date = c.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN);
		String dStr = sdf.format(date);
		return dStr;
	}

	public static String getCurrDateByPattern(String pattern) {
		Calendar c = Calendar.getInstance();
		Date date = c.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		String dStr = sdf.format(date);
		return dStr;
	}

	public static String getCurrDate(long time) {

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN);
		String dStr = sdf.format(new Date(time));
		return dStr;
	}

	public static String getFormateDateString(long time, String pattern) {
		/**
		 * 转换时区，否则时间会出现问题 东八区
		 */
		String timeZone = "GMT+8";
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		String dStr = sdf.format(new Date(time));
		return dStr;
	}

	public static String getFormateDateString(String getmPlayPosition,
		String pattern) {
		long time = 0;
		try {
			time = Long.parseLong(getmPlayPosition);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getFormateDateString(time, pattern);
	}

	/**
	 * translate time from origPatten to newPatten
	 * 
	 * @param origTime
	 * @param origPatten
	 * @param newPatten
	 * @return
	 */
	public static String translateTime(String origTime, String origPatten,
		String newPatten) {
		try {
			SimpleDateFormat orgSdf = new SimpleDateFormat(origPatten);
			SimpleDateFormat newSdf = new SimpleDateFormat(newPatten);
			Date date = orgSdf.parse(origTime);
			return newSdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return origTime;
	}

	/**
	 * format time.[HH:]mm:ss
	 * 
	 * @param t
	 * @return
	 * @author mfy
	 */
	public static String formatTime(long t) {

		int hour = 0;
		int min = 0;
		int sec = 0;
		String formatedTime = "";

		if (t > 60 * 60 * 1000) {
			hour = (int) (t / (60 * 60 * 1000));
			t -= hour * 60 * 60 * 1000;
		}
		if (t > 60 * 1000) {
			min = (int) (t / (60 * 1000));
			t -= min * 60 * 1000;
		}
		if (t > 1000) {
			sec = (int) (t / 1000);
			t -= sec * 1000;
		}

		if (hour >= 10) {
			formatedTime += hour + ":";
		} else if (hour > 0 && hour < 10) {
			formatedTime += "0" + hour + ":";
		}

		if (min >= 10 && min < 60) {
			formatedTime += min + ":";
		} else if (min > 0 && min < 10) {
			formatedTime += "0" + min + ":";
		} else {
			formatedTime += "00" + ":";
		}

		if (sec >= 10 && sec < 60) {
			formatedTime += sec + "";
		} else if (sec > 0 && sec < 10) {
			formatedTime += "0" + sec;
		} else {
			formatedTime += "00";
		}
		return formatedTime;
	}

	public static int getDayByYear(long time) {
		Calendar currentCalendar = Calendar.getInstance();
		currentCalendar.setTimeInMillis(time);
		int day = currentCalendar.get(Calendar.YEAR) * 1000
			+ currentCalendar.get(Calendar.DAY_OF_YEAR);
		return day;
	}

	public static String getDescriptiveTime(int ms) {
		if (ms <= 0) {
			return "00:00";
		}
		int s = ms / 1000;
		int i = s / 60;
		int j = s % 60;
		String min = i < 10 ? "0" + i : "" + i;
		String sec = j < 10 ? "0" + j : "" + j;
		return min + ":" + sec;
	}

	/**
	 * 得到时间间隔
	 * 
	 * @param start
	 *            开始时间
	 * @param end
	 *            结束时间
	 * @param format
	 *            开始和结束时间的format
	 * @return
	 */
	public static String getDuration(String start, String end, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		long between = 0;
		try {
			Date beginDate = sdf.parse(start);
			Date endDate = sdf.parse(end);
			between = (endDate.getTime() - beginDate.getTime());// 得到两者的毫秒数
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return formatTime(between);
	}

	/**
	 * 把时间转换为秒数
	 * 
	 * @param time
	 *            , format is hh:mm:ss
	 * @return
	 */

	public static long transletToSecond(String time) {
		long seconds = 0;
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_HOUR);
		try {
			Date timeDate = sdf.parse(time);
			long millisec = timeDate.getTime();
			seconds = timeDate.getTime() / 1000;

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return seconds;
	}

	/**
	 * 得到时间间隔
	 * 
	 * @param start
	 *            开始时间
	 * @param end
	 *            结束时间
	 * @param
	 * @return
	 */
	public static String getDuration(String start, String end) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN);
		long between = 0;
		try {
			Date beginDate = sdf.parse(start);
			Date endDate = sdf.parse(end);
			between = (endDate.getTime() - beginDate.getTime());// 得到两者的毫秒数
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return formatTime(between);
	}

	/**
	 * 从指定时间字符串中解析年月日十分秒
	 * 
	 * @param time
	 *            时间字符串
	 * @return
	 */
	public static Calendar getDateFormat(String time) {
		Calendar calendar = null;
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN);
		try {
			Date date = sdf.parse(time);
			calendar = Calendar.getInstance();
			calendar.setTime(date);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return calendar;
	}
}
