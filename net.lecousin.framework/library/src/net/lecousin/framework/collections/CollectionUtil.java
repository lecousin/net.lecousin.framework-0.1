/**
 * Project:  GFramework Basics
 * Package:  net.lecousin.framework.basics.collections
 * Filename: CollectionUtils.java
 * Created on 31 déc. 2005 23:27:45
 * Author: Guillaume LE COUSIN
 */
package net.lecousin.framework.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.lecousin.framework.reflection.ClassUtil;

/**
 * @author Guillaume LE COUSIN
 *
 */
public class CollectionUtil
{
  private CollectionUtil() { /* instantiation not allowed */ }
  
  public static <T> void add_news(Collection<T> target, Collection<? extends T> to_add)
  {
    for (Iterator<? extends T> it = to_add.iterator(); it.hasNext(); )
    {
      T element = it.next();
      if (!target.contains(element))
        target.add(element);
    }
  }
  
  public static <T> void add_new(Collection<T> target, T element) {
	  if (!(target.contains(element)))
		  target.add(element);
  }
  
  public static <T> void add_notnull(Collection<T> target, T element) {
	  if (element != null)
		  target.add(element);
  }
  
  @SuppressWarnings("unchecked")
  public static <T> Collection<? extends T> add_all(Collection<? extends T> target, Collection<? extends T> source)
  {
    ((Collection<T>)target).addAll(source);
    return target;
  }
  
  public static <T> void addAll(Collection<T> target, Iterable<? extends T> source) {
	  for (T e : source)
		  target.add(e);
  }
  
  @SuppressWarnings("unchecked")
  public static <T> Collection<T> basis(Collection<? extends T> col)
  {
    return (Collection<T>)col;
  }

  @SuppressWarnings("unchecked")
public static <T, U extends T> List<Class<? extends T>> get_class_list(Class<U> cl)
  {
    return Arrays.asList((Class<? extends T>[])Arrays.asList(cl).toArray());
  }
  
  @SuppressWarnings("unchecked")
  public static <T> Collection<T> reverse(Collection<T> col)
  {
    Collection<T> ret = ClassUtil.new_instance_no_exception(col, new Object[] {});
    if (ret == null)
      return null;
    T[] elements = (T[])col.toArray();
    for (int i = elements.length - 1; i >= 0; --i)
      ret.add(elements[i]);
    return ret;
  }
  
  public static <T> Set<T> intersection(Collection<? extends T> c1, Collection<? extends T> c2) {
    Set<T> result = new HashSet<T>();
    for (Iterator<? extends T> it = c1.iterator(); it.hasNext(); ) {
        T element = it.next();
        if (c2.contains(element))
            result.add(element);
    }
    return result;
  }
  
  public static <T> Set<T> union(Collection<? extends T> c1, Collection<? extends T> c2) {
	  Set<T> result = new HashSet<T>();
	  result.addAll(c1);
	  result.addAll(c2);
	  return result;
  }
  
  public static <T> List<T> single_element_list(T element) {
      List<T> result = new ArrayList<T>(1);
      result.add(element);
      return result;
  }
  
  public static <T> Collection<T> single_element_collection(T element) {
      return single_element_list(element);
  }
  
  public static <T> void arraycopy(T[] src, int srcPos, T[] dst, int len) {
      for (int i = 0; i < len; ++i)
          dst[i] = src[srcPos + i];
  }
  
  @SuppressWarnings("unchecked")
  public static <T> List<T> emptyList(Class<T> clazz) {
    return (List<T>)Collections.EMPTY_LIST;
  }
  
  public static <T> List<T> singleList(T element) {
      List<T> result = new ArrayList<T>(1);
      result.add(element);
      return result;
  }
  
  public static <T> Collection<T> singleCollection(T element) {
      return singleList(element);
  }
  
  public static <T> void addAll(Collection<T> collection, T[] array) {
  	for (int i = 0; i < array.length; ++i)
  		collection.add(array[i]);
  }
  
  public static <T> void addSorted(List<T> list, T element, Comparator<T> cmp) {
	  if (list.size() == 0)
		  list.add(element);
	  else
		  addSorted(list, element, cmp, 0, list.size() - 1);
  }
  private static <T> void addSorted(List<T> list, T element, Comparator<T> cmp, int start, int end) {
	  int iPivot = start + (end - start) / 2;
	  T pivot = list.get(iPivot);
	  int i = cmp.compare(element, pivot);
	  if (i < 0) {
		  if (iPivot == start)
			  list.add(start, element);
		  else
			  addSorted(list, element, cmp, start, iPivot-1);
	  } else if (i > 0) {
		  if (iPivot == end)
			  list.add(element);
		  else
			  addSorted(list, element, cmp, iPivot + 1, end);
	  } else
		  list.add(iPivot, element);
  }
  
  public static <T> SortedListRandomAccess<T> sort(Iterable<T> col, Comparator<T> cmp) {
	  SortedListRandomAccess<T> sorted = new SortedListRandomAccess<T>(cmp);
	  for (Iterator<T> it = col.iterator(); it.hasNext(); )
		  sorted.add(it.next());
	  return sorted;
  }
  
  public static <T> T getAt(Collection<T> col, int index) {
	  if (col instanceof List)
		  return ((List<T>)col).get(index);
	  int i; Iterator<T> it;
	  for (i = 0, it = col.iterator(); i < index && it.hasNext(); it.next());
	  return it.hasNext() ? it.next() : null;
  }
  
  public static <T> ArrayList<T> list(T[] array) {
	  ArrayList<T> list = new ArrayList<T>();
	  for (int i = 0; i < array.length; ++i)
		  list.add(array[i]);
	  return list;
  }
  
  public static <T> boolean containsIdentity(Iterable<? extends T> col, T element) {
	  for (T e : col)
		  if (e == element)
			  return true;
	  return false;
  }
}
