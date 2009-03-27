package net.lecousin.framework.collections;

import java.util.Collection;

public interface SelfMap<HashType, EntryType extends SelfMap.Entry<HashType>>
  extends Collection<EntryType>
{
  public static interface Entry<HashType> {
    public HashType getHashObject();
  }
  
  public static class SelfContainer<HashType, EntryType> implements Entry<HashType> {
	  public SelfContainer(HashType hash, EntryType entry) {
		  this.hash = hash;
		  this.entry = entry;
	  }
	  private HashType hash;
	  private EntryType entry;
	  public HashType getHashObject() { return hash; }
	  public EntryType contained() { return entry; } 
  }
  
  public void put(EntryType entry);
  public EntryType get(HashType key);
  public EntryType removeEntry(EntryType entry);
  public EntryType removeKey(HashType key);
  public boolean containsKey(HashType key);
  public boolean containsEntry(EntryType entry);
}
