package net.lecousin.framework.collections;


public class SortedListFileAccessLong<T extends SortedListFileAccessLong.LongSortable> implements Iterable<T>, SortedList<T> {

	public SortedListFileAccessLong() {
	}
	
	public static interface LongSortable {
		public long getLongSortValue();
	}
	
	private static class Element<T> {
		Element(T e, Element<T> n, long v) { element = e; next = n; value = v; }
		T element;
		Element<T> next;
		long value;
	}
	private Element<T> head = null;
	private Element<T> queue = null;
	private int size = 0;
	private static final int bankSize = 200;
	@SuppressWarnings("unchecked")
	private static Element[] bankOfElements = new Element[bankSize];
	private static int indexBank = -1;
	
	@SuppressWarnings("unchecked")
	private static synchronized <T> Element<T> createElement(T e, Element<T> n, long v) {
		if (indexBank == -1) return new Element<T>(e,n,v);
		Element<T> el = bankOfElements[indexBank--];
		el.element = e;
		el.next = n;
		el.value = v;
		return el;
	}
	private static synchronized <T> void freeElement(Element<T> e) {
		if (indexBank == bankSize-1) return;
		bankOfElements[++indexBank] = e;
		e.element = null;
		e.next = null;
	}
	
	public void clear() {
		head = queue = null;
		size = 0;
	}
	
	public void add(T o) {
		size++;
		long ol = o.getLongSortValue();
		if (head == null) {
			head = queue = createElement(o, null, ol);
			return;
		}
		if (ol <= head.value) {
			head = createElement(o, head, ol);
			return;
		}
		if (head.next == null) {
			head.next = createElement(o, null, ol);
			queue = head.next;
			return;
		}
		if (ol >= queue.value) {
			queue.next = createElement(o, null, ol);
			queue = queue.next;
			return;
		}
		Element<T> ptr = head;
		while (ol > ptr.next.value) ptr = ptr.next;
		Element<T> ne = createElement(o, ptr.next, ol);
		ptr.next = ne;
	}
	
	public java.util.Iterator<T> iterator() { return new Iterator(); }
	public int size() { return size; }
	public boolean isEmpty() { return head == null; }
	public T first() { return head.element; }
	public T removeFirst() {
		size--;
		T element = head.element;
		Element<T> e = head;
		if ((head = head.next) == null) queue = null;
		freeElement(e);
		return element;
	}
	public T last() { return queue.element; }
	public T get(int index) {
		Element<T> ptr = head;
		int i = 0;
		while (i++ < index) ptr = ptr.next;
		return ptr.element;
	}
	public boolean removeIdentity(T e) {
		if (head == null) return false;
		if (e == head.element) {
			Element<T> he = head;
			if ((head = head.next)==null) queue = null;
			freeElement(he);
			size--;
			return true;
		}
		long el = e.getLongSortValue();
		if (el < head.value) return false;
		if (head.next == null) return false;
		Element<T> ptr = head;
		while (e != ptr.next.element) {
			ptr = ptr.next;
			if (ptr.next == null) return false;
		}
		Element<T> nn = ptr.next.next;
		freeElement(ptr.next);
		if ((ptr.next = nn)==null) queue = ptr;
		size--;
		return true;
	}
	
	private class Iterator implements java.util.Iterator<T> {
		public Iterator() {
			this.next = head;
		}
		private Element<T> prev = null, prevprev = null;
		private Element<T> next;
		public boolean hasNext() {
			return next != null;
		}
		public T next() {
			T e = next.element;
			prevprev = prev;
			prev = next;
			next = next.next;
			return e;
		}
		public void remove() {
			if (prevprev != null) {
				prevprev.next = next;
				if (next == null) queue = prevprev;
			} else {
				head = next;
				if (next == null) queue = null;
			}
			size--;
			freeElement(prev);
		}
	}
}
