package net.lecousin.framework.ui.eclipse.event;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public abstract class SelectionListenerWithData<T> implements SelectionListener {

	public SelectionListenerWithData(T data) { this.data = data; }
	
	private T data;
	
	public T data() { return data; }
	
	public void widgetDefaultSelected(SelectionEvent e) {
	}

}
