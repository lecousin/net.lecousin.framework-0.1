package net.lecousin.framework.ui.eclipse.control.list;

import org.eclipse.swt.graphics.Image;

public interface LCImageProvider<T> {

	public Image getImage(T element);
	
}
