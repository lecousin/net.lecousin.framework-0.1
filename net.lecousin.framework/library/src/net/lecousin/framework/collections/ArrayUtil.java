package net.lecousin.framework.collections;

import java.util.LinkedList;
import java.util.List;

public class ArrayUtil {

	public static <T> boolean contains(T[] array, T value) {
		if (array == null) return false;
		for (int i = 0; i < array.length; ++i)
			if (array[i].equals(value))
				return true;
		return false;
	}
	
	public static <T> boolean equals(T[] a1, T[] a2) {
		if (a1 == null) return a2 == null;
		if (a2 == null) return false;
		if (a1.length != a2.length) return false;
		for (int i = 0; i < a1.length; ++i)
			if (!a1[i].equals(a2[i])) return false;
		return true;
	}
	public static <T> boolean equals(byte[] a1, byte[] a2) {
		if (a1 == null) return a2 == null;
		if (a2 == null) return false;
		if (a1.length != a2.length) return false;
		for (int i = 0; i < a1.length; ++i)
			if (a1[i] != a2[i]) return false;
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] createGenericArray(Class<T> clazz, int size) {
		return (T[])java.lang.reflect.Array.newInstance(clazz.getComponentType(), size);
	}
	@SuppressWarnings("unchecked")
	public static <T> T[] createGenericArray(Class<T> clazz, Object[] content) {
		T[] array = createGenericArray(clazz, content.length);
		for (int i = 0; i < content.length; ++i)
			array[i] = (T)content[i];
		return array;
	}
	
	public static <T> List<T> unionIdentity(T[] a1, T[] a2) {
		List<T> result = new LinkedList<T>();
		for (T e1 : a1)
			for (T e2 : a2)
				if (e1 == e2) {
					result.add(e1);
					break;
				}
		return result;
	}
}
