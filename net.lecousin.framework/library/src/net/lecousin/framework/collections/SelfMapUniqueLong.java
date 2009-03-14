package net.lecousin.framework.collections;

import java.util.Collection;
import java.util.Iterator;

public class SelfMapUniqueLong<EntryType extends SelfMap.Entry<Long>> implements SelfMap<Long,EntryType> {

	private static class Element<T> {
		Element(T e, Element<T> n) { element = e; next = n; }
		T element;
		Element<T> next;
	}

	@SuppressWarnings("unchecked")
	public SelfMapUniqueLong(int nbBuckets) {
		buckets = new Element[nbBuckets];
	}
	public SelfMapUniqueLong() { this(20); }

	private Element<EntryType>[] buckets;
	private int size = 0;
	
	private static final int bankSize = 200;
	@SuppressWarnings("unchecked")
	private static Element[] bankOfElements = new Element[bankSize];
	private static int indexBank = -1;
	
	@SuppressWarnings("unchecked")
	private static synchronized <T> Element<T> createElement(T e, Element<T> n) {
		if (indexBank < 0) return new Element<T>(e, n);
		Element<T> elt = bankOfElements[indexBank--];
		elt.element = e;
		elt.next = n;
		return elt;
	}
	private static synchronized <T> void free(Element<T> e) {
		if (indexBank == bankSize-1) return;
		bankOfElements[++indexBank] = e;
		e.element = null;
		e.next = null;
	}

	private Element<EntryType> getBucket(long id) {
		int hc = (int)(id % buckets.length);
		return buckets[hc];
	}

	public void put(EntryType entry) {
		size++;
		long id = entry.getHashObject();
		int hc = (int)(id % buckets.length);
		Element<EntryType> ptr = buckets[hc];
		if (ptr == null) {
			buckets[hc] = createElement(entry, null);
			return;
		}
		long pid = ptr.element.getHashObject();
		if (pid > id) {
			buckets[hc] = createElement(entry, ptr);
			return;
		}
		if (ptr.next == null) {
			ptr.next = createElement(entry, null);
			return;
		}
		do {
			pid = ptr.next.element.getHashObject();
			if (pid > id) {
				ptr.next = createElement(entry, ptr.next);
				return;
			}
			ptr = ptr.next;
		} while (ptr.next != null);
		ptr.next = createElement(entry, null);
	}
	public boolean add(EntryType entry) { put(entry); return true; }
	public boolean addAll(Collection<? extends EntryType> entries) {
		for (EntryType e : entries)
			put(e);
		return true;
	}
	public void clear() {
		for (int i = 0; i < buckets.length; ++i)
			buckets[i] = null;
		size = 0;
	}

	public EntryType remove(long id) {
		int hc = (int)(id % buckets.length);
		Element<EntryType> ptr = buckets[hc];
		if (ptr == null) return null;
		long pid = ptr.element.getHashObject();
		if (pid == id) {
			buckets[hc] = ptr.next;
			EntryType result = ptr.element;
			free(ptr);
			size--;
			return result;
		}
		if (pid > id) return null;
		if (ptr.next == null) return null;
		do {
			EntryType e = ptr.next.element;
			pid = e.getHashObject();
			if (pid == id) {
				Element<EntryType> next = ptr.next.next;
				free(ptr.next);
				ptr.next = next;
				size--;
				return e;
			}
			if (pid > id) return null;
			ptr = ptr.next;
		} while (ptr.next != null);
		return null;
	}

	public EntryType remove(EntryType entry) {
		return remove((long)entry.getHashObject());
	}
	
	public EntryType removeFirst() {
		if (size == 0) return null;
		for (int i = 0; i < buckets.length; ++i) {
			Element<EntryType> ptr = buckets[i];
			if (ptr == null) continue;
			buckets[i] = ptr.next;
			EntryType entry = ptr.element;
			free(ptr);
			size--;
			return entry;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public boolean remove(Object o)	{ 
		return remove((EntryType)o) != null;
	}

	@SuppressWarnings("unchecked")
	public boolean removeAll(Collection<?> c) {
		boolean result = false;
		for (EntryType e : (Iterable<? extends EntryType>)c) {
			if (remove(e) != null) result = true;
		}
		return result;
	}

	public EntryType get(long id) {
		Element<EntryType> ptr = getBucket(id);
		if (ptr == null) return null;
		do {
			long pid = ptr.element.getHashObject();
			if (pid == id) return ptr.element;
			if (pid > id) return null;
			ptr = ptr.next;
		} while (ptr != null);
		return null;
	}

	public boolean containsKey(long id) {
		Element<EntryType> ptr = getBucket(id);
		if (ptr == null) return false;
		do {
			long pid = ptr.element.getHashObject();
			if (pid == id) return true;
			if (pid > id) return false;
			ptr = ptr.next;
		} while (ptr != null);
		return false;
	}

	public boolean containsEntry(EntryType entry) { return containsKey(entry.getHashObject()); }
	@SuppressWarnings("unchecked")
	public boolean contains(Object o) { return containsEntry((EntryType)o); }

	@SuppressWarnings("unchecked")
	public boolean containsAll(Collection<?> entries) {
		for (EntryType e : (Iterable<? extends EntryType>)entries)
			if (!containsEntry(e)) return false;
		return true;
	}

	public boolean retainAll(Collection<?> c) {
		boolean result = false;
		for (int iBucket = 0; iBucket < buckets.length; ++iBucket) {
			Element<EntryType> ptr = buckets[iBucket];
			if (ptr == null) continue;
			do {
				if (!c.contains(ptr.element))
					ptr = buckets[iBucket] = ptr.next;
				else break;
			} while (ptr != null);
			if (ptr == null) continue;
			result = true;
			if (ptr.next == null) continue;
			do {
				if (!c.contains(ptr.next.element))
					ptr.next = ptr.next.next;
				else
					ptr = ptr.next;
			} while (ptr.next  != null);
		}
		return result;
	}

	public int size() { return size; }
	public boolean isEmpty(){ return size == 0; }

	public Object[] toArray() {
		Object[] result = new Object[size()];
		fillArray(result);
		return result;
	}
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		if (a.length < size())
			a = (T[])java.lang.reflect.Array.newInstance(
					a.getClass().getComponentType(), size());
		fillArray(a);
		return a;
	}
	private void fillArray(Object[] result) {
		int pos = 0;
		for (int i = 0; i < buckets.length; ++i) {
			Element<EntryType> ptr = buckets[i];
			while (ptr != null) {
				result[pos++] = ptr.element;
				ptr = ptr.next;
			}
		}
	}
	
	public Iterator<EntryType> iterator() { return new SelfMapIterator(); }

	private class SelfMapIterator implements Iterator<EntryType> {
		SelfMapIterator() {
			iBucket = 0;
			while ((ptr = buckets[iBucket]) == null) if (++iBucket >= buckets.length) break;
		}
		int iBucket;
		Element<EntryType> ptr;

		public boolean hasNext() { return ptr != null; }
		public EntryType next() {
			EntryType e = ptr.element;
			if ((ptr = ptr.next) != null) return e;
			if (++iBucket >= buckets.length) return e;
			while ((ptr = buckets[iBucket]) == null) if (++iBucket >= buckets.length) break;
			return e;
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
