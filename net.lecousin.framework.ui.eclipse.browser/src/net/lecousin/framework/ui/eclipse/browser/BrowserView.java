package net.lecousin.framework.ui.eclipse.browser;

import net.lecousin.framework.ui.eclipse.browser.BrowserControl.WindowProvider;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

public class BrowserView extends ViewPart {

	private BrowserControl control;
	
	@Override
	public void createPartControl(Composite parent) {
		control = new BrowserControl(parent, SWT.NONE, null, new WindowProvider() {
			public IToolBarManager getToolBar() {
		        IActionBars actionBars = getViewSite().getActionBars();
		        return actionBars.getToolBarManager();
			}
			public IStatusLineManager getStatusLine() {
				return null;
			}
			public void setTitle(String title) {
				setPartName(title);
			}
		});
	}
	
	public void setLocation(String url) {
		control.setLocation(url);
	}
	

	@Override
	public void setFocus() {
		control.setFocus();
	}
}
