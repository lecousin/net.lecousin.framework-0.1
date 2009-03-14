package net.lecousin.framework.memory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.log.Log;
import net.lecousin.framework.monitoring.Monitor;

public class AutoFreeMemory<T> {

	public AutoFreeMemory(Monitor monitor, long keepTime, Loader<T> loader) {
		this(monitor, keepTime, null, loader);
	}
	public AutoFreeMemory(Monitor monitor, long keepTime, T data, Loader<T> loader) {
		this.loader = loader;
		this.keepTime = keepTime;
		this.data = data;
		if (data != null)
			loadTime = System.currentTimeMillis();
		else
			loadTime = 0;
		synchronized (objectsInMemory) {
			if (monitorWork == null) {
				monitorWork = new AutoFree();
				monitor.newWork(monitorWork, 60000);
			}
		}
		if (data != null)
			synchronized (objectsInMemory) { objectsInMemory.add(this); }
		else
			synchronized (objectsFreed) { objectsFreed.add(this); }
	}
	
	private Loader<T> loader;
	private T data;
	private long loadTime;
	private long keepTime;
	
	private static AutoFree monitorWork = null;
	private static List<AutoFreeMemory<?>> objectsInMemory = new LinkedList<AutoFreeMemory<?>>();
	private static List<AutoFreeMemory<?>> objectsFreed = new LinkedList<AutoFreeMemory<?>>();
	
	public static interface Loader<T> {
		T load();
	}
	
	public T get() {
		boolean loaded = false;
		synchronized (objectsFreed) {
			if (data == null) {
				objectsFreed.remove(this);
				data = loader.load();
				loaded = true;
				if (Log.debug(this))
					Log.debug(this, "Object " + data.getClass().getName() + " loaded");
			}
		}
		loadTime = System.currentTimeMillis();
		if (loaded)
			synchronized (objectsInMemory) { objectsInMemory.add(this); }
		return data;
	}
	public void free() {
		synchronized (objectsFreed) { objectsFreed.remove(this); }
		synchronized (objectsInMemory) { objectsInMemory.remove(this); }
	}
	
	private static class AutoFree implements Runnable {
		public void run() {
			long time = System.currentTimeMillis();
			synchronized(objectsInMemory) {
				for (Iterator<AutoFreeMemory<?>> it = objectsInMemory.iterator(); it.hasNext(); ) {
					AutoFreeMemory<?> o = it.next();
					if (o.loadTime + o.keepTime <= time) {
						o.loadTime = 0;
						if (o.data != null) {
							if (Log.debug(this))
								Log.debug(this, "Object " + o.data.getClass().getName() + " freed");
							synchronized (objectsFreed) { o.data = null; objectsFreed.add(o); }
						}
						it.remove();
					}
				}
			}
		}
	}
}
