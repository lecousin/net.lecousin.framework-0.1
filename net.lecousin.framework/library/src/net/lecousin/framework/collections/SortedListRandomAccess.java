package net.lecousin.framework.collections;

import java.util.Comparator;

public class SortedListRandomAccess<T> implements Iterable<T> {

	public SortedListRandomAccess(Comparator<T> cmp, int shortcutsNumber) {
		this.cmp = cmp;
		this.shortcutsNumber = shortcutsNumber;
	}
	public SortedListRandomAccess(Comparator<T> cmp) {
		this(cmp, 25);
	}

	private static class Element<T> {
		Element(T element, Element<T> next, Element<T> previous)
		{ this.element = element; this.next = next; this.previous = previous; }
		T element;
		Element<T> next;
		Element<T> previous;
	}
	
	private Comparator<T> cmp;
	private Element<T> head = null;
	private Element<T>[] shortcuts = null;
	private int[] shortcutsIndexes = null;
	private int shortcutsNumber;
	private int size;
	
	public int size() { return size; }
	
	public void add(T element) {
		if (head == null) {
			head = new Element<T>(element, null, null);
			size++;
			return;
		}
		if (size < shortcutsNumber) {
			add_from(element, head, 0, 0);
			return;
		}
		add_lookup_shortcut(element, shortcutsIndexes.length / 2, 0, shortcutsIndexes.length - 1);
	}
	
	public T first() {
		return head != null ? head.element : null;
	}
	
	public T poll() {
		if (head == null) return null;
		T e = head.element;
		head = head.next;
		refreshShortcutsRemove(0);
		size--;
		if (size == 0) {
			shortcuts = null;
			shortcutsIndexes = null;
		} else if (size % shortcutsNumber == 0) {
			rebuildShortcuts();
		}
		return e;
	}
	
	private void add_from(T element, Element<T> ptr, int ptrIndex, int sens) {
		int c = cmp.compare(element, ptr.element);
		int i;
		if (c == 0) {
			Element<T> e = new Element<T>(element, ptr.next, ptr);
			if (ptr.next != null)
				ptr.next.previous = e;
			ptr.next = e;
			i = ptrIndex + 1;
		} else if (c < 0) {
			if (ptr.previous == null) {
				Element<T> e = new Element<T>(element, ptr, null);
				ptr.previous = e;
				i = ptrIndex;
				head = e;
			} else {
				if (sens == 1) {
					Element<T> e = new Element<T>(element, ptr, ptr.previous);
					ptr.previous.next = e;
					ptr.previous = e;
					i = ptrIndex;
				} else {
					add_from(element, ptr.previous, ptrIndex - 1, -1);
					return;
				}
			}
		} else if (c > 0) {
			if (ptr.next == null) {
				Element<T> e = new Element<T>(element, null, ptr);
				ptr.next = e;
				i = ptrIndex + 1;
			} else {
				if (sens == -1) {
					Element<T> e = new Element<T>(element, ptr.next, ptr);
					ptr.next.previous = e;
					ptr.next = e;
					i = ptrIndex + 1;
				} else {
					add_from(element, ptr.next, ptrIndex + 1, 1);
					return;
				}
			}
		} else
			i = 0; // for compile only
		endAdd(i);
	}
	
	private void endAdd(int index) {
		refreshShortcutsInsert(index);
		if (++size % shortcutsNumber == 0)
			rebuildShortcuts();
	}
	
	private void refreshShortcutsInsert(int insertIndex) {
		if (shortcutsIndexes == null) return;
		for (int i = 0; i < shortcutsIndexes.length; ++i)
			if (shortcutsIndexes[i] >= insertIndex)
				shortcutsIndexes[i]++;
	}
	private void refreshShortcutsRemove(int removeIndex) {
		if (shortcutsIndexes == null) return;
		for (int i = 0; i < shortcutsIndexes.length; ++i)
			if (shortcutsIndexes[i] >= removeIndex)
				shortcutsIndexes[i]--;
	}
	
	@SuppressWarnings("unchecked")
	private void rebuildShortcuts() {
		if (shortcuts == null) {
			shortcuts = new Element[1];
			shortcutsIndexes = new int[1];
			shortcutsIndexes[0] = shortcutsNumber / 2;
			Element<T> ptr = head;
			for (int i = 0; i < shortcutsIndexes[0]; ++i)
				ptr = ptr.next;
			shortcuts[0] = ptr;
			return;
		}
		
		Element<T>[] newsc = new Element[shortcuts.length + 1];
		int[] newsci = new int[shortcuts.length + 1];
		
		for (int i = shortcutsNumber / 2, si = 0; i < size; i+= shortcutsNumber, ++si) {
			newsci[si] = i;
			int sci = shortcutsIndexes[si >= shortcutsIndexes.length ? shortcutsIndexes.length - 1 : si];
			Element<T> s = shortcuts[si >= shortcuts.length ? shortcuts.length - 1 : si];
			while (sci < i) {
				s = s.next;
				sci++;
			}
			while (sci > i) {
				s = s.previous;
				sci--;
			}
			newsc[si] = s;
		}
		shortcuts = newsc;
		shortcutsIndexes = newsci;
	}
	
	private void add_lookup_shortcut(T element, int pivot, int start, int end) {
		int c = cmp.compare(element, shortcuts[pivot].element);
		if (c == 0) {
			Element<T> e = new Element<T>(element, shortcuts[pivot].next, shortcuts[pivot]);
			if (shortcuts[pivot].next != null)
				shortcuts[pivot].next.previous = e;
			shortcuts[pivot].next = e;
			endAdd(shortcutsIndexes[pivot]+1);
			return;
		}
		if (c < 0) {
			if (pivot == start) {
				add_from(element, shortcuts[pivot].previous, shortcutsIndexes[pivot]-1, -1);
				return;
			}
			add_lookup_shortcut(element, (pivot - start) / 2 + start, start, pivot-1);
			return;
		}
		if (pivot == end || end == pivot+1) {
			add_from(element, shortcuts[pivot].next, shortcutsIndexes[pivot]+1, 1);
			return;
		}
		add_lookup_shortcut(element, (end - pivot) / 2 + pivot, pivot+1, end);
	}
	
	private class Iterator implements java.util.Iterator<T> {
		public Iterator() {
			ptr = head;
		}
		private Element<T> ptr;
		
		public boolean hasNext() {
			return ptr != null;
		}
		public T next() {
			T e = ptr.element;
			ptr = ptr.next;
			return e;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public java.util.Iterator<T> iterator() { return new Iterator(); }
}
