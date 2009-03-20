package net.lecousin.framework.ui.eclipse.event;

import org.eclipse.swt.events.ControlListener;

public abstract class ControlListenerWithData<T> implements ControlListener {

	public ControlListenerWithData(T data) {
		this.data = data;
	}

	private T data;
	
	public T data() { return data; }
}
