package net.lecousin.framework.ui.eclipse.dialog;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public abstract class PopupMenu extends MyDialog {

	public PopupMenu(Control openWith, boolean stayOnTop) {
		super(getShell(openWith));
		Shell shell = getParent();
		if (shell != null && shell.getData() instanceof PopupMenu)
			parentMenu = (PopupMenu)shell.getData();
		else
			parentMenu = null;
		this.stayOnTop = stayOnTop;
		if (parentMenu != null) {
			parentMenu.addChild(this);
			parentMenu.getShell().addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (!PopupMenu.this.getShell().isDisposed())
						PopupMenu.this.close();
				}
			});
		}
	}
	
	private PopupMenu parentMenu;
	private boolean stayOnTop;
	private boolean allowClose = true;
	private List<PopupMenu> children = new LinkedList<PopupMenu>();

	public void setAllowClose(boolean value) { allowClose = value; }
	
	void addChild(PopupMenu menu) {
		children.add(menu);
	}
	void removeChild(PopupMenu menu) {
		children.remove(menu);
	}
	
	private static Shell getShell(Control openWith) {
		if (openWith == null)
			return null;
		if (openWith instanceof Shell)
			return (Shell)openWith;
		Composite parent = openWith.getParent();
		if (parent == null) return null;
		return getShell(parent);
	}
	
	@Override
	protected void create(String title, int flags) {
		super.create(title, flags);
		getShell().setData(this);
		getShell().addShellListener(new ShellListener() {
			public void shellActivated(ShellEvent e) {
			}
			public void shellClosed(ShellEvent e) {
				if (parentMenu != null)
					parentMenu.removeChild(PopupMenu.this);
			}
			public void shellDeactivated(ShellEvent e) {
				if (!stayOnTop && children.isEmpty() && allowClose)
					close();
			}
			public void shellDeiconified(ShellEvent e) {
			}
			public void shellIconified(ShellEvent e) {
			}
		});
	}
}
