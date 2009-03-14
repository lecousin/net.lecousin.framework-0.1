package net.lecousin.framework.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class LinkedArrayList<T> implements List<T>, QueueFast<T> {

	public LinkedArrayList(int arraySize, Class<T> clazz) { this.arraySize = arraySize; this.clazz = clazz; }
	
	private int arraySize;
	private Class<T> clazz;
	private Array<T> head = null, tall = null;
	
	private static class Array<T> {
		@SuppressWarnings("unchecked")
		public Array(int size, Class<T> clazz, Array<T> previous, Array<T> next) {
			array = (T[])java.lang.reflect.Array.newInstance(clazz, size);
			this.previous = previous;
			this.next = next;
			if (previous != null)
				previous.next = this;
			if (next != null)
				next.previous = this;
		}
		T[] array;
		int size = 0;
		Array<T> previous, next;
		
		void add(T o) {
			array[size++] = o;
		}
		int indexOf(Object o) {
			for (int i = 0; i < size; ++i)
				if (array[i].equals(o))
					return i;
			return -1;
		}
		int lastIndexOf(Object o) {
			for (int i = size-1; i >= 0; --i)
				if (array[i].equals(o))
					return i;
			return -1;
		}
	}
	
	public boolean add(T o) {
		if (tall == null) {
			head = tall = new Array<T>(arraySize, clazz, null, null);
		} else if (tall.size == arraySize) {
			tall = new Array<T>(arraySize, clazz, tall, null);
		}
		tall.add(o);
		return true;
	}
	public void addFast(T o) { add(o); }
	
	public void add(int index, T element) {
		if (head == null) {
			add(element);
			return;
		}
		Array<T> a = head;
		int i = 0;
		while (i + arraySize <= index && a.next != null) {
			a = a.next;
			i += arraySize;
		}
		add(element, a, index - i);
	}
	
	private void add(T element, Array<T> array, int index) {
		if (index > array.size) index = array.size;
		shiftRight(array, index);
		array.array[index] = element; 
	}
	private void shiftRight(Array<T> a, int i) {
		if (i == a.size) return;
		if (a.size == arraySize) {
			if (a.next == null) {
				tall = new Array<T>(arraySize, clazz, a, null);
				tall.add(a.array[arraySize-1]);
			} else {
				add(a.array[arraySize-1], a.next, 0);
			}
			a.size--;
		}
		System.arraycopy(a.array, i, a.array, i+1, a.size-i);
	}
	
	public boolean addAll(Collection<? extends T> c) {
		for (Iterator<? extends T> it = c.iterator(); it.hasNext(); )
			add(it.next());
		return true;
	}
	public boolean addAll(int index, Collection<? extends T> c) {
		for (Iterator<? extends T> it = c.iterator(); it.hasNext(); ++index)
			add(index, it.next());
		return true;
	}
	public void clear() {
		head = tall = null;
	}
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}
	public boolean containsAll(Collection<?> c) {
		for (Iterator<?> it = c.iterator(); it.hasNext(); )
			if (!contains(it.next())) return false;
		return true;
	}
	public T element() {
		if (head == null) throw new NoSuchElementException();
		return head.array[0];
	}
	public T get(int index) {
		if (head == null) throw new IndexOutOfBoundsException(Integer.toString(index));
		if (index < 0) throw new IndexOutOfBoundsException(Integer.toString(index));
		Array<T> a = head;
		int i = 0;
		while (i + arraySize <= index && a.next != null) {
			a = a.next;
			i += arraySize;
		}
		i = index - i;
		if (i >= a.size) throw new IndexOutOfBoundsException(Integer.toString(index));
		return a.array[i];
	}
	public int indexOf(Object o) {
		int index = 0;
		for (Array<T> a = head; a != null; index += a.size, a = a.next) {
			int i = a.indexOf(o);
			if (i >= 0) return i + index;
		}
		return -1;
	}
	public boolean isEmpty() { return head == null; }
	public boolean offer(T o) { return add(o); }
	public T peek() { 
		if (head == null) return null;
		return head.array[0];
	}
	public T remove() {
		if (head == null) throw new NoSuchElementException();
		T result = head.array[0];
		shiftLeft(head, 0);
		return result;
	}
	public T poll() {
		if (head == null) return null;
		T result = head.array[0];
		shiftLeft(head, 0);
		return result;
	}
	public T pollFast() {
		if (tall == null) return null;
		T result = tall.array[tall.size-1];
		shiftLeft(tall, tall.size-1);
		return result;
	}
	private void shiftLeft(Array<T> a, int i) {
		if (i >= a.size) return;
		if (a.size == 1) {
			if (head == a) {
				if (tall == a)
					tall = null;
				else
					head.next.previous = null;
				head = head.next;
			} else if (tall == a) {
				tall.previous.next = null;
				tall = tall.previous;
			} else {
				a.previous.next = a.next;
				a.next.previous = a.previous;
			}
			return;
		}
		System.arraycopy(a.array, i+1, a.array, i, a.size - i - 1);
		a.size--;
	}
	public T remove(int index) {
		if (head == null) throw new IndexOutOfBoundsException(Integer.toString(index));
		if (index < 0) throw new IndexOutOfBoundsException(Integer.toString(index));
		Array<T> a = head;
		int i = 0;
		while (i + arraySize <= index && a.next != null) {
			a = a.next;
			i += arraySize;
		}
		i = index - i;
		if (i >= a.size) throw new IndexOutOfBoundsException(Integer.toString(index));
		T element = a.array[i];
		shiftLeft(a, i);
		return element;
	}
	public boolean remove(Object o) {
		int i = indexOf(o);
		if (i == -1) return false;
		remove(i);
		return true;
	}
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Iterator<?> it = c.iterator(); it.hasNext(); )
			changed |= remove(it.next());
		return changed;
	}
	public int size() {
		int i = 0;
		for (Array<T> a = head; a != null; a = a.next)
			i += a.size;
		return i;
	}
	public boolean retainAll(Collection<?> c) {
		boolean changed = false;
		for (ListIterator<T> it = listIterator(); it.hasNext(); ) {
			T e = it.next();
			if (!c.contains(e)) { it.remove(); changed = true; }
		}
		return changed;
	}
	public ListIterator<T> listIterator(int index) {
		return new LALIterator(index);
	}
	public ListIterator<T> listIterator() {
		return listIterator(0);
	}
	public Iterator<T> iterator() {
		return listIterator();
	}
	
	private class LALIterator implements ListIterator<T> {
		LALIterator(int index) {
			
		}
		
		private Array<T> ptrNext = head;
		private int posNext = 0;
		private int nextIndex = 0;
		
		public boolean hasNext() { return ptrNext != tall || (ptrNext != null && posNext < ptrNext.size); }
		public boolean hasPrevious() { return ptrNext != head || posNext > 0; }
		public T next() {
			T e = ptrNext.array[posNext++];
			if (posNext == arraySize) {
				if (ptrNext != tall) {
					ptrNext = ptrNext.next;
					posNext = 0;
				}
			}
			nextIndex++;
			return e;
		}
		public int nextIndex() { return nextIndex; }
		public T previous() {
			T e;
			if (posNext > 0) {
				e = ptrNext.array[--posNext];
			} else {
				ptrNext = ptrNext.previous;
				posNext = ptrNext.size-1;
				e = ptrNext.array[posNext];
			}
			nextIndex--;
			return e;
		}
		public int previousIndex() { return nextIndex - 1; }
		public void add(T o) { LinkedArrayList.this.add(nextIndex, o); }
		public void remove() { LinkedArrayList.this.remove(nextIndex-1); }
		public void set(T o) {
			if (posNext > 0) {
				ptrNext.array[posNext-1] = o;
			} else {
				ptrNext.previous.array[arraySize-1] = o;
			}
		}
	}
	@SuppressWarnings("unchecked")
	public <T2> T2[] toArray(T2[] a) {
        if (a.length < size())
            a = (T2[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size());
        int i = 0;
        for (Array<T> array = head; array != null; array = array.next) {
        	System.arraycopy(array.array, 0, a, i, array.size);
        	i += array.size;
        }
        return a;
	}
	public Object[] toArray() {
		Object[] a = new Object[size()];
        int i = 0;
        for (Array<T> array = head; array != null; array = array.next) {
        	System.arraycopy(array.array, 0, a, i, array.size);
        	i += array.size;
        }
		return a;
	}
	public int lastIndexOf(Object o) {
		int i = size()-1;
		for (Array<T> a = tall; a != null; a = a.previous) {
			int index = a.lastIndexOf(o);
			if (index >= 0) return i - index;
			i -= a.size;
		}
		return -1;
	}
	public T set(int index, T element) {
		if (head == null) throw new IndexOutOfBoundsException(Integer.toString(index));
		if (index < 0) throw new IndexOutOfBoundsException(Integer.toString(index));
		Array<T> a = head;
		int i = 0;
		while (i + arraySize <= index && a.next != null) {
			a = a.next;
			i += arraySize;
		}
		i = index - i;
		if (i >= a.size) throw new IndexOutOfBoundsException(Integer.toString(index));
		T old = a.array[i];
		a.array[i] = element;
		return old;
	}
	public List<T> subList(int fromIndex, int toIndex) {
		List<T> result = new ArrayList<T>(toIndex - fromIndex);
		for (int i = fromIndex; i < toIndex; ++i)
			result.add(get(i));
		return result;
	}
}
