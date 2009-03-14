package net.lecousin.framework.math;

public class MathUtil {

	private MathUtil() { /* no instantiation allowed */ }
	
	public static boolean isIntersection(long start1, long end1, long start2, long end2) {
		if (end1 >= start2 && end1 <= end2) return true;
		if (start1 <= end2 && start1 >= start2) return true;
		return false;
	}
	
}
