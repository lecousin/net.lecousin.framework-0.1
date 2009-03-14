package net.lecousin.framework.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Random {

	private static java.util.Random rand = new java.util.Random(System.currentTimeMillis());
	
	public static int randInt() { return rand.nextInt(); }
	public static long randLong() { return rand.nextLong(); }
	
	public static int randInt(int max) { int i = randInt() % max; if (i < 0) i = -i; return i; }
	public static int randInt(int min, int max) { int i = randInt() % (max-min); if (i < 0) i = -i; return min+i; }
	public static long randLong(long max) { long i = randLong() % max; if (i < 0) i = -i; return i; }
	public static long randLong(long min, long max) { long i = randLong() % (max-min); if (i < 0) i = -i; return min+i; }
	
	public static float randFloat() { return rand.nextFloat(); }
	public static float randFloat(double min, double max) { float f = rand.nextFloat(); return (float)((max - min) * f + min); }
	
	public static double randDouble() { return rand.nextDouble(); }
	public static double randDouble(double min, double max) { double f = rand.nextDouble(); return (max - min) * f + min; }
	
	public static boolean randBoolean() { return rand.nextBoolean(); }
	
	private static final char[] alphanum = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9'};
	public static char randAlphaNum() { return alphanum[randInt(alphanum.length)]; }
	public static String randAlphaNum(int length) {
		char chars[] = new char[length];
		for (int i = 0; i < length; ++i) chars[i] = randAlphaNum();
		return new String(chars);
	}
	
	public static <T> List<T> randList(Collection<T> list) {
		List<T> result = new ArrayList<T>(list.size());
		List<T> dupList = new ArrayList<T>(list);
		while (!dupList.isEmpty()) {
			int i = randInt(dupList.size());
			result.add(dupList.get(i));
			dupList.remove(i);
		}
		return result;
	}
}
