package net.lecousin.framework.ui.eclipse.control.error;

import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ErrorControl extends Composite {

	public ErrorControl(Composite parent, GridData layoutData) {
		this(parent, (Object)layoutData);
	}
	public ErrorControl(Composite parent, RowData layoutData) {
		this(parent, (Object)layoutData);
	}
	private ErrorControl(Composite parent, Object data) {
		super(parent, SWT.NONE);
		setLayoutData(data);
		UIUtil.gridLayout(this, 2);
		icon = new Label(this, SWT.NONE);
		icon.setImage(SharedImages.getImage(SharedImages.icons.x16.basic.ERROR));
		message = new Label(this, SWT.NONE);
		setError(null);
	}
	
	private String text = "";
	private Label icon;
	private Label message;

	public void setError(String error) {
		if (text == null) {
			if (error == null) return;
			show(true);
		} else {
			if (error != null && error.equals(text)) return;
			if (error == null)
				show(false);
		}
		this.text = error;
		if (error != null)
			message.setText(error);
		layout(true, true);
		UIControlUtil.autoresize(icon);
	}
	
	private void show(boolean show) {
		Object data = getLayoutData();
		if (data instanceof GridData)
			((GridData)data).exclude = true;
		else if (data instanceof RowData)
			((RowData)data).exclude = true;
	}
}
