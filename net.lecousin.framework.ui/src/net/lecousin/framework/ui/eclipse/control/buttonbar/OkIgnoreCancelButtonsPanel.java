package net.lecousin.framework.ui.eclipse.control.buttonbar;

import net.lecousin.framework.ui.eclipse.Local;
import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.swt.widgets.Composite;

public abstract class OkIgnoreCancelButtonsPanel extends ButtonsPanel {
	
	public OkIgnoreCancelButtonsPanel(Composite parent, boolean withImages) {
		super(parent, new ButtonInPanel[] {
			new ButtonInPanel("ok", withImages ? SharedImages.getImage(SharedImages.icons.x16.basic.OK) : null, Local.Ok.toString()),	
			new ButtonInPanel("ignore", withImages ? SharedImages.getImage(SharedImages.icons.x16.basic.IGNORE) : null, Local.Ignore.toString()),	
			new ButtonInPanel("cancel", withImages ? SharedImages.getImage(SharedImages.icons.x16.basic.CANCEL) : null, Local.Cancel.toString()),	
		});
	}
	
	@Override
	protected void handleButton(String id) {
		if (id.equals("ok")) {
			if (handleOk()) close();
		} else if (id.equals("cancel")) {
			if (handleCancel()) close();
		} else if (id.equals("ignore")) {
			if (handleIgnore()) close();
		}
	}
	
	private void close() {
		getParent().getShell().close();
	}
	
	protected abstract boolean handleOk();
	protected abstract boolean handleIgnore();
	protected abstract boolean handleCancel();
	
	public void enableOk(boolean enabled) {
		enable("ok", enabled);
	}
	public void enableIgnore(boolean enabled) {
		enable("ignore", enabled);
	}
	public void enableCancel(boolean enabled) {
		enable("cancel", enabled);
	}

}
