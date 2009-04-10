package net.lecousin.framework.collections;

import java.util.Comparator;

public class SortedListFileAccess<T> implements Iterable<T>, SortedList<T> {

	public SortedListFileAccess(Comparator<T> comparator) {
		this.comparator = comparator;
	}
	
	private static class Element<T> {
		Element(T e, Element<T> n) { element = e; next = n; }
		T element;
		Element<T> next;
	}
	private Element<T> head = null;
	private Element<T> queue = null;
	private Comparator<T> comparator;
	private int size = 0;
	private static final int bankSize = 200;
	@SuppressWarnings("unchecked")
	private static Element[] bankOfElements = new Element[bankSize];
	private static int indexBank = -1;
	
	@SuppressWarnings("unchecked")
	private static synchronized <T> Element<T> createElement(T e, Element<T> n) {
		if (indexBank == -1) return new Element<T>(e,n);
		Element<T> el = bankOfElements[indexBank--];
		el.element = e;
		el.next = n;
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
		if (head == null) {
			head = queue = createElement(o, null);
			return;
		}
		int cmp = comparator.compare(o, head.element);
		if (cmp <= 0) {
			head = createElement(o, head);
			return;
		}
		if (head.next == null) {
			head.next = createElement(o, null);
			queue = head.next;
			return;
		}
		cmp = comparator.compare(o, queue.element);
		if (cmp >= 0) {
			queue.next = createElement(o, null);
			queue = queue.next;
			return;
		}
		Element<T> ptr = head;
		while (comparator.compare(o, ptr.next.element) > 0) ptr = ptr.next;
		Element<T> ne = createElement(o, ptr.next);
		ptr.next = ne;
	}
	
	public java.util.Iterator<T> iterator() { return new Iterator(); }
	public int size() { return size; }
	public boolean isEmpty() { return head == null; }
	public T first() { return head.element; }
	public T removeFirst() {
		size--;
		T element = head.element;
		Element<T> he = head;
		if ((head = head.next) == null) queue = null;
		freeElement(he);
		return element;
	}
	public T last() { return queue.element; }
	public T get(int index) {
		Element<T> ptr = head;
		int i = 0;
		while (i++ < index) ptr = ptr.next;
		return ptr.element;
	}
	public boolean remove(T e) {
		if (head == null) return false;
		int cmp = comparator.compare(e, head.element);
		if (cmp < 0) return false;
		if (cmp == 0) {
			Element<T> he = head;
			if ((head = head.next)==null) queue = null;
			freeElement(he);
			size--;
			return true;
		}
		if (head.next == null) return false;
		Element<T> ptr = head;
		while (comparator.compare(e, ptr.next.element) != 0) {
			ptr = ptr.next;
			if (ptr.next == null) return false;
		}
		Element<T> nn = ptr.next.next;
		freeElement(ptr.next);
		if ((ptr.next = nn)==null) queue = ptr;
		size--;
		return true;
	}
	public boolean removeIdentity(T e) {
		if (head == null) return false;
		int cmp = comparator.compare(e, head.element);
		if (cmp < 0) return false;
		if (e == head.element) {
			Element<T> he = head;
			if ((head = head.next)==null) queue = null;
			freeElement(he);
			size--;
			return true;
		}
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
