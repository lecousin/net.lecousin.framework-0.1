package net.lecousin.framework.event;

import java.util.LinkedList;
import java.util.List;

public class SplitProcessListener<T> {

	public SplitProcessListener(ProcessListener<T> original) {
		this.original = original;
	}
	
	private ProcessListener<T> original;
	private boolean started = false;
	private List<ProcessListener<T>> listeners = new LinkedList<ProcessListener<T>>();
	
	public ProcessListener<T> newListener() {
		SubListener sub = new SubListener();
		synchronized (listeners) {
			listeners.add(sub);
		}
		return sub;
	}
	
	private class SubListener implements ProcessListener<T> {
		public void started() {
			synchronized (listeners) {
				if (started) return;
				started = true;
			}
			original.started();
		}
		public void fire(T data) {
			original.fire(data);
		}
		public void done() {
			synchronized (listeners) {
				listeners.remove(this);
				if (listeners.isEmpty()) original.done();
			}
		}
	}
}
