package net.lecousin.framework.ui.eclipse.control.error;

import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class ErrorContainerControl extends Composite {

	public ErrorContainerControl(Composite parent) {
		super(parent, SWT.NONE);
		UIUtil.gridLayout(this, 1);
		err = new ErrorControl(this, UIUtil.gridDataHoriz(1, true));
	}
	
	private ErrorControl err;

	public void setError(String message) {
		err.setError(message);
	}
}
