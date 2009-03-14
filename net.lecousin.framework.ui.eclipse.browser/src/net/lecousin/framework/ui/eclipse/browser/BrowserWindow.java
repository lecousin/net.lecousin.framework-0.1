package net.lecousin.framework.ui.eclipse.browser;

import net.lecousin.framework.ui.eclipse.browser.BrowserControl.WindowProvider;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class BrowserWindow extends ApplicationWindow {

	public BrowserWindow(String title, String homeURL, boolean toolBar, boolean statusLine) {
		super(null);
		this.title = title;
		this.homeURL = homeURL;
		if (toolBar)
			addToolBar(SWT.PUSH);
		if (statusLine)
			addStatusLine();
	}

	BrowserControl control;
	String title;
	String homeURL;

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		return control = new BrowserControl(parent, SWT.BORDER, homeURL, new WindowProvider() {
			public IToolBarManager getToolBar() {
				return getToolBarManager();
			}
			public IStatusLineManager getStatusLine() {
				return getStatusLineManager();
			}
			public void setTitle(String title) {
				if (BrowserWindow.this.title == null) return;
				getShell().setText(title + " - " + BrowserWindow.this.title);
			}
		});
	}
	
	public void setLocation(String url) {
		control.setLocation(url);
	}
}
