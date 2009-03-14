package net.lecousin.framework.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class SelfMapLinkedList<HashType, EntryType extends SelfMap.Entry<HashType>>
  implements SelfMap<HashType, EntryType>
{
  @SuppressWarnings("unchecked")
  public SelfMapLinkedList(int nbBuckets) {
    buckets = new LinkedList[nbBuckets];
    for (int i = 0; i < nbBuckets; ++i)
      buckets[i] = new LinkedList();
  }
  public SelfMapLinkedList() { this(20); }
  
  private LinkedList<EntryType>[] buckets;

  private LinkedList<EntryType> getBucket(HashType hash) {
    int hc = hash.hashCode() % buckets.length;
    if (hc < 0) hc = -hc;
    return buckets[hc];
  }
  private LinkedList<EntryType> getBucket(EntryType entry) {
    return getBucket(entry.getHashObject());
  }
  
  public void put(EntryType entry) {
    getBucket(entry).add(entry);
  }
  public boolean add(EntryType entry)
  {
    put(entry);
    return true;
  }
  public boolean addAll(Collection<? extends EntryType> entries)
  {
    for (Iterator<? extends EntryType> it = entries.iterator(); it.hasNext(); )
      put(it.next());
    return true;
  }
  public void clear()
  {
    for (int i = 0; i < buckets.length; ++i)
      buckets[i].clear();
  }
  
  public EntryType removeKey(HashType hash) {
    LinkedList<EntryType> bucket = getBucket(hash);
    for (Iterator<EntryType> it = bucket.iterator(); it.hasNext(); ) {
      EntryType entry = it.next();
      if (entry.getHashObject().equals(hash)) {
        it.remove();
        return entry;
      }
    }
    return null;
  }
  
  public EntryType removeEntry(EntryType entry) {
    LinkedList<EntryType> bucket = getBucket(entry);
    for (Iterator<EntryType> it = bucket.iterator(); it.hasNext(); ) {
      EntryType e = it.next();
      if (e.equals(entry)) {
        it.remove();
        return entry;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public boolean remove(Object o)
  {
    return removeEntry((EntryType)o) != null;
  }

  public EntryType removeIdentity(EntryType entry) {
    LinkedList<EntryType> bucket = getBucket(entry);
    for (Iterator<EntryType> it = bucket.iterator(); it.hasNext(); ) {
      EntryType e = it.next();
      if (e == entry) {
        it.remove();
        return entry;
      }
    }
    return null;
  }
  
  public boolean removeAll(Collection<?> c)
  {
    boolean result = false;
    for (int iBucket = 0; iBucket < buckets.length; ++iBucket) {
      for (Iterator<EntryType> it = buckets[iBucket].iterator(); it.hasNext(); ) {
        EntryType entry = it.next();
        if (c.contains(entry)) {
          it.remove();
          result = true;
        }
      }
    }
    return result;
  }

  public EntryType get(HashType hash) {
	    LinkedList<EntryType> bucket = getBucket(hash);
	    for (Iterator<EntryType> it = bucket.iterator(); it.hasNext(); ) {
	      EntryType entry = it.next();
	      if (entry.getHashObject().equals(hash)) 
	        return entry;
	    }
	    return null;
	  }
  
  public boolean containsKey(HashType hash) {
    LinkedList<EntryType> bucket = getBucket(hash);
    for (Iterator<EntryType> it = bucket.iterator(); it.hasNext(); ) {
      EntryType entry = it.next();
      if (entry.getHashObject().equals(hash)) 
        return true;
    }
    return false;
  }
  
  public boolean containsEntry(EntryType entry) {
    LinkedList<EntryType> bucket = getBucket(entry);
    for (Iterator<EntryType> it = bucket.iterator(); it.hasNext(); ) {
      EntryType e = it.next();
      if (e.equals(entry)) 
        return true;
    }
    return false;
  }
  @SuppressWarnings("unchecked")
  public boolean contains(Object o)
  {
    return containsEntry((EntryType)o);
  }
  
  public boolean containsEntryIdentity(EntryType entry) {
    LinkedList<EntryType> bucket = getBucket(entry);
    for (Iterator<EntryType> it = bucket.iterator(); it.hasNext(); ) {
      EntryType e = it.next();
      if (e == entry) 
        return true;
    }
    return false;
  }

  public boolean containsAll(Collection<?> entries) {
    Object[] toFind = entries.toArray();
    int nbFind = toFind.length;
    for (int iBucket = 0; iBucket < buckets.length; ++iBucket) {
      for (Iterator<EntryType> it = buckets[iBucket].iterator(); it.hasNext(); ) {
        EntryType entry = it.next();
        for (int i = 0; i < toFind.length; ++i) {
          if (toFind[i] == null) continue;
          if (toFind[i].equals(entry)) {
            toFind[i] = null;
            if (--nbFind == 0) return true;
            break;
          }
        }
      }
    }
    return false;
  }
  
  public boolean retainAll(Collection<?> c)
  {
    boolean result = false;
    for (int iBucket = 0; iBucket < buckets.length; ++iBucket) {
      for (Iterator<EntryType> it = buckets[iBucket].iterator(); it.hasNext(); ) {
        EntryType entry = it.next();
        if (!c.contains(entry)) {
          it.remove();
          result = true;
        }
      }
    }
    return result;
  }
  
  public int size() {
    int result = 0;
    for (int i = 0; i < buckets.length; ++i)
      result += buckets[i].size();
    return result;
  }
  public boolean isEmpty()
  {
    for (int i = 0; i < buckets.length; ++i)
      if (!buckets[i].isEmpty())
        return false;
    return true;
  }
  
  public Object[] toArray()
  {
    Object[] result = new Object[size()];
    fillArray(result);
    return result;
  }
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a)
  {
    if (a.length < size())
      a = (T[])java.lang.reflect.Array.newInstance(
                          a.getClass().getComponentType(), size());
    fillArray(a);
    return a;
  }
  private void fillArray(Object[] result) {
    int pos = 0;
    for (int i = 0; i < buckets.length; ++i)
      for (int j = 0; j < buckets[i].size();++j)
        result[pos++] = buckets[i].get(j);
  }
  
  public Iterator<EntryType> iterator() { return new SelfMapIterator(); }
  
  private class SelfMapIterator implements Iterator<EntryType> {
    SelfMapIterator() {
      goNext();
    }
    int index = -1;
    Iterator<EntryType> it = null;
    
    private void goNext() {
      if (index < 0) { index = 0; it = buckets[index].iterator(); }
      if (it == null) return;
      if (it.hasNext()) return;
      if (++index >= buckets.length) { it = null; return; }
      it = buckets[index].iterator();
      goNext();
    }
    
    public boolean hasNext() { return it != null; }
    public EntryType next() {
      if (it == null || !it.hasNext()) return null;
      EntryType entry = it.next();
      goNext();
      return entry;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
