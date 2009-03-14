package net.lecousin.framework.ui.eclipse.event;

import org.eclipse.swt.events.DisposeListener;

public abstract class DisposeListenerWithData<T> implements DisposeListener {

	public DisposeListenerWithData(T data) { this.data = data; }
	private T data;
	public T data() { return data; }
	
}
