package net.lecousin.framework.ui.eclipse.event;

import org.eclipse.ui.forms.events.IHyperlinkListener;

public abstract class HyperlinkListenerWithData<T> implements IHyperlinkListener {

	public HyperlinkListenerWithData(T data) { this.data = data; }
	private T data;
	public T data() { return data; }
	
}
