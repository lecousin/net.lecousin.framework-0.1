package net.lecousin.framework.time;

import java.util.Calendar;

import net.lecousin.framework.strings.StringUtil;

public class DateTimeUtil {

	private DateTimeUtil() { /* no instantiation allowed */ }
	
	public static String getDateTimeString(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		StringBuilder str = new StringBuilder();
		str
			.append(StringUtil.toString(c.get(Calendar.DAY_OF_MONTH),2)).append("/")
			.append(StringUtil.toString(c.get(Calendar.MONTH)+1,2)).append("/")
			.append(StringUtil.toString(c.get(Calendar.YEAR),4)).append(" ")
			.append(StringUtil.toString(c.get(Calendar.HOUR_OF_DAY),2)).append(":")
			.append(StringUtil.toString(c.get(Calendar.MINUTE),2)).append(":")
			.append(StringUtil.toString(c.get(Calendar.SECOND),2));
		return str.toString();
	}
	public static String getDateTimeString(long time, int startYear) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0);
		c.set(Calendar.YEAR, startYear);
		c.add(Calendar.DAY_OF_YEAR, (int)(time/((long)24*60*60*1000)));
		time = time % ((long)24*60*60*1000);
		c.add(Calendar.MILLISECOND, (int)time);
		StringBuilder str = new StringBuilder();
		str
			.append(StringUtil.toString(c.get(Calendar.DAY_OF_MONTH),2)).append("/")
			.append(StringUtil.toString(c.get(Calendar.MONTH)+1,2)).append("/")
			.append(StringUtil.toString(c.get(Calendar.YEAR),4)).append(" ")
			.append(StringUtil.toString(c.get(Calendar.HOUR_OF_DAY),2)).append(":")
			.append(StringUtil.toString(c.get(Calendar.MINUTE),2)).append(":")
			.append(StringUtil.toString(c.get(Calendar.SECOND),2));
		return str.toString();
	}
	
	public static String getDayTimeString(long time) {
		StringBuilder str = new StringBuilder();
		str.append(StringUtil.toString((int)(time/((long)60*60*1000)),2)).append(':');
		time = time % ((long)60*60*1000);
		str.append(StringUtil.toString((int)(time/((long)60*1000)),2)).append(':');
		time = time % ((long)60*1000);
		str.append(StringUtil.toString((int)(time/((long)1000)),2));
		return str.toString();
	}
	
	public static String getTimeMinimalString(long time) {
		StringBuilder str = new StringBuilder();
		if (time >= 60*60*1000)
			str.append(time / (60*60*1000)).append("h");
		if (time >= 60*1000)
			str.append((time % (60*60*1000)) / (60*1000)).append("m");
		str.append((time % (60*1000)) / 1000).append("s");
		return str.toString();
	}
	
	public static long getTimeFromMinimalString(String s) {
		if (!s.endsWith("s")) throw new NumberFormatException();
		int i,j;
		long hour = 0, minute = 0, second;
		i = s.indexOf('h');
		if (i > 0)
			hour = Long.parseLong(s.substring(0, i));
		else
			i = 0;
		j = s.indexOf('m');
		if (j > 0) {
			minute = Long.parseLong(s.substring(i+1, j));
			i = j;
		}
		second = Long.parseLong(s.substring(i+1, s.length()-1));
		return ((hour * 60 + minute) * 60 + second) * 1000;
	}
	
	public static void resetHours(Calendar c) {
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
	}
	
	public static String getTimeString(long time, boolean hour, boolean minute, boolean seconds, boolean millis) {
		StringBuilder str = new StringBuilder();
		if (hour) {
			long h = time/(1000*60*60);
			time -= h*(1000*60*60);
			str.append(StringUtil.toString((int)h, 2));
		}
		if (minute) {
			if (str.length() > 0) str.append(':');
			long m = time/(1000*60);
			time -= m*(1000*60);
			str.append(StringUtil.toString((int)m, 2));
		}
		if (seconds) {
			if (str.length() > 0) str.append(':');
			long s = time / 1000;
			time -= s * 1000;
			str.append(StringUtil.toString((int)s, 2));
		}
		if (millis) {
			if (str.length() > 0) str.append('.');
			str.append(StringUtil.toString((int)time, 3));
		}
		return str.toString();
	}
	
	public static long getTimeFromString(String s, boolean hours, boolean minutes, boolean seconds, boolean millis) {
		int i = s.lastIndexOf(':');
		long time = 0;
		if (millis) {
			if (i < 0) return Long.parseLong(s.trim());
			time += Long.parseLong(s.substring(i+1).trim());
			s = s.substring(0, i);
			i = s.lastIndexOf(':');
		}
		if (seconds) {
			if (i < 0) return time + Long.parseLong(s.trim())*1000;
			time += Long.parseLong(s.substring(i+1).trim())*1000;
			s = s.substring(0, i);
			i = s.lastIndexOf(':');
		}
		if (minutes) {
			if (i < 0) return time + Long.parseLong(s.trim())*60*1000;
			time += Long.parseLong(s.substring(i+1).trim())*60*1000;
			s = s.substring(0, i);
		}
		if (hours)
			return time + Long.parseLong(s.trim())*60*60*1000;
		return time;
	}
	
	public static String getDateString(long date) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date);
		return new StringBuilder()
			.append(StringUtil.toString(c.get(Calendar.DAY_OF_MONTH), 2)).append('/')
			.append(StringUtil.toString(c.get(Calendar.MONTH)+1, 2)).append('/')
			.append(StringUtil.toString(c.get(Calendar.YEAR), 4))
			.toString();
	}
	
	public static long getDateFromString(String s) {
		int day, month, year;
		int i = s.indexOf('/');
		if (i < 0) return 0;
		try { day = Integer.parseInt(s.substring(0, i)); }
		catch (NumberFormatException e) { return 0; }
		int j = s.indexOf('/', i+1);
		try { month = Integer.parseInt(s.substring(i+1, j)); }
		catch (NumberFormatException e) { return 0; }
		try { year = Integer.parseInt(s.substring(j+1)); }
		catch (NumberFormatException e) { return 0; }
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month-1);
		c.set(Calendar.DAY_OF_MONTH, day);
		resetHours(c);
		return c.getTimeInMillis();
	}
}
