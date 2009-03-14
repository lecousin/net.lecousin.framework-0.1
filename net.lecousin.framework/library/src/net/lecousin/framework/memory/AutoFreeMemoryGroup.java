package net.lecousin.framework.memory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.lecousin.framework.log.Log;
import net.lecousin.framework.monitoring.Monitor;

public class AutoFreeMemoryGroup {

	public AutoFreeMemoryGroup(Monitor monitor) {
		monitor.newWork(new AutoFree(), 60000);
	}
	
	private Map<String,AutoFreeGroup<?>> groups = new HashMap<String,AutoFreeGroup<?>>();
	
	public class AutoFreeObject<T> {
		private AutoFreeObject() {}
		private Loader<T> loader;
		private T data;
		private AutoFreeGroup<T> group;
		
		public T get() {
			synchronized (this) {
				if (data != null) return data;
				data = loader.load();
				if (data != null) {
					group.loaded(this);
					if (Log.debug(this))
						Log.debug(this, "Object " + data.getClass().getName() + " loaded from group");
				}
				return data;
			}
		}
		public void free() {
			group.free(this);
		}
	}
	
	public class AutoFreeGroup<T> {
		private AutoFreeGroup(int maxObjects) { this.maxObjects = maxObjects; }
		private int maxObjects;
		private LinkedList<AutoFreeObject<T>> objectsInMemory = new LinkedList<AutoFreeObject<T>>();
		
		public AutoFreeObject<T> create(T data, Loader<T> loader) {
			AutoFreeObject<T> o = new AutoFreeObject<T>();
			o.loader = loader;
			o.data = data;
			o.group = this;
			if (data != null) {
				synchronized(objectsInMemory) {
					objectsInMemory.add(o);
				}
			}
			return o;
		}
		private void loaded(AutoFreeObject<T> o) {
			synchronized (objectsInMemory) {
				objectsInMemory.add(o);
			}
		}
		public void free(AutoFreeObject<T> o) {
			synchronized (o) {
				if (o.data == null) return;
				synchronized (objectsInMemory) {
					objectsInMemory.remove(o);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> AutoFreeGroup<T> getGroup(String id) {
		synchronized (groups) {
			return (AutoFreeGroup<T>)groups.get(id);
		}
	}
	
	public <T> AutoFreeGroup<T> createGroup(String id, int maxObjectsInMemory) {
		AutoFreeGroup<T> group = new AutoFreeGroup<T>(maxObjectsInMemory);
		synchronized (groups) {
			groups.put(id, group);
		}
		return group;
	}
	
	public static interface Loader<T> {
		T load();
	}
	
	
	private class AutoFree implements Runnable {
		public void run() {
			synchronized(groups) {
				for (AutoFreeGroup<?> group : groups.values()) {
					synchronized (group.objectsInMemory) {
						while (group.objectsInMemory.size() > group.maxObjects) {
							AutoFreeObject<?> o = group.objectsInMemory.removeFirst();
							if (Log.debug(this))
								Log.debug(this, "Object " + o.data.getClass().getName() + " freed from group");
							o.data = null;
						}
					}					
				}
			}
		}
	}
}
