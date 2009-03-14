package net.lecousin.framework.ui.eclipse.event;

import org.eclipse.swt.widgets.Listener;

public abstract class ListenerWithData<T> implements Listener {

	public ListenerWithData(T data) {
		this.data = data;
	}
	private T data;
	public T data() { return data; }
	
}
