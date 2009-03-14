package net.lecousin.framework;

public class CommonUtil {

	private CommonUtil() { /* instantiation not allowed */ }
	
	public static boolean equalsOrNull(Object o1, Object o2) {
		if (o1 == null) return o2 == null;
		return o1.equals(o2);
	}
	
}
