package net.lecousin.framework.ui.eclipse.event;

import org.eclipse.swt.events.ModifyListener;

public abstract class ModifyListenerWithData<T> implements ModifyListener {

	public ModifyListenerWithData(T data) {
		this.data = data;
	}

	private T data;
	
	public T data() { return data; }
}
