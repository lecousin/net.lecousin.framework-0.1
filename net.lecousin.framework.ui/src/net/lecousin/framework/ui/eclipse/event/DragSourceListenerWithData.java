package net.lecousin.framework.ui.eclipse.event;

import org.eclipse.swt.dnd.DragSourceListener;

public abstract class DragSourceListenerWithData<T> implements DragSourceListener {

	public DragSourceListenerWithData(T data) { this.data = data; }
	private T data;
	public T data() { return data; }
	
}
