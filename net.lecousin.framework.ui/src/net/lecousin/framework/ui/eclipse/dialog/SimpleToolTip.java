package net.lecousin.framework.ui.eclipse.dialog;

import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SimpleToolTip {

	public SimpleToolTip(Control control) {
		this.control = control;
	}
	
	private Control control;
	private Shell shell = null;
	private Label label;
	private String text = "";
	private int x=0, y=0;
	
	public void show() {
		if (shell == null || shell.isDisposed()) {
			shell = new Shell(control.getShell(), SWT.ON_TOP | SWT.NO_TRIM);
			FillLayout layout = new FillLayout();
			layout.marginHeight = 2;
			layout.marginWidth = 3;
	        shell.setLayout(layout);
			shell.setBackground(ColorUtil.get(255,255,175));
			label = new Label(shell, SWT.NONE);
			label.setBackground(ColorUtil.get(255,255,175));
			label.setText(text);
			shell.setLocation(x, y);
			Point pt = label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			shell.setSize(pt.x + layout.marginWidth*2, pt.y + layout.marginHeight*2);
		}
		shell.setVisible(true);
	}
	public void hide() {
		shell.setVisible(false);
	}
	
	public void setText(String str) {
		text = str;
		if (label != null && !label.isDisposed())
			label.setText(str);
	}
	
	public void setPosition(int x, int y) {
		Point ctrl = control.toDisplay(control.getLocation());
		this.x = x + ctrl.x + 20;
		this.y = y + ctrl.y + 20;
		if (shell != null && !shell.isDisposed())
			shell.setLocation(this.x, this.y);
	}
}
