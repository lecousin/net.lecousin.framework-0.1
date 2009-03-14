package net.lecousin.framework.comparator;

import java.util.Comparator;

public abstract class StringComparator<T> implements Comparator<T> {

	public int compare(T o1, T o2) {
		return getStringToCompare(o1).compareTo(getStringToCompare(o2));
	}
	
	public abstract String getStringToCompare(T o);
	
}
