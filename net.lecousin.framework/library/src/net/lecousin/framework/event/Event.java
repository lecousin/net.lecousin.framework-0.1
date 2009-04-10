package net.lecousin.framework.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import net.lecousin.framework.log.Log;

public class Event<T> {

    public Event() {
        // nothing
    }

    public static interface Listener<T> {
        public void fire(T event);
    }
    public static abstract class ListenerData<T,DataType> implements Listener<T> {
        public ListenerData(DataType data) {
            this.data = data;
        }
        private DataType data;
        public DataType data() { return data; }
    }
    
    private LinkedList<Listener<T>> listeners = new LinkedList<Listener<T>>();
    private LinkedList<Runnable> fireListeners = new LinkedList<Runnable>();
    
    public void free() {
    	listeners.clear();
    	listeners = null;
    	fireListeners.clear();
    	fireListeners = null;
    }
    
    public void addListener(Listener<T> listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }
    public void removeListener(Listener<T> listener) {
    	if (listeners != null)
    		listeners.remove(listener);
    }
    public void addFireListener(Runnable listener) {
    	if (!fireListeners.contains(listener))
    		fireListeners.add(listener);
    }
    public void removeFireListener(Runnable listener) {
    	if (fireListeners != null)
    		fireListeners.remove(listener);
    }
    
    public void fire(T event) {
    	if (listeners == null) return; // already free
    	ArrayList<Listener<T>> list = new ArrayList<Listener<T>>(listeners);
        for (Iterator<Listener<T>> it = list.iterator(); it.hasNext(); ) {
        	Listener<T> listener = it.next();
        	if (!listeners.contains(listener)) continue;
            try { listener.fire(event); }
            catch (Throwable t) {
            	handleListenerError(t);
            }
        }
    	ArrayList<Runnable> list2 = new ArrayList<Runnable>(fireListeners);
        for (Iterator<Runnable> it = list2.iterator(); it.hasNext(); ) {
        	Runnable listener = it.next();
        	if (!fireListeners.contains(listener)) continue;
            try { listener.run(); }
            catch (Throwable t) {
            	handleListenerError(t);
            }
        }
    }
    
    protected void handleListenerError(Throwable t) {
    	Log.error(this, "A listener thrown an exception", t);
    }
}
