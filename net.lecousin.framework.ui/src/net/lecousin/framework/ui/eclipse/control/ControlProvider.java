package net.lecousin.framework.ui.eclipse.control;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface ControlProvider {

	public Control create(Composite parent);
	
}
