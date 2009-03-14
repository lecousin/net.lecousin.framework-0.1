package net.lecousin.framework.collections;

import java.util.Iterator;

public interface SortedList<T> extends Iterable<T> {

	public void add(T element);
	
	public int size();
	public boolean isEmpty();
	
	public T first();
	public T last();
	public T get(int index);
	
	public Iterator<T> iterator();
	
}
