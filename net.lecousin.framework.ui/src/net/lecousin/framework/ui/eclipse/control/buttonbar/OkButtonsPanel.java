package net.lecousin.framework.ui.eclipse.control.buttonbar;

import net.lecousin.framework.ui.eclipse.Local;
import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.swt.widgets.Composite;

public abstract class OkButtonsPanel extends ButtonsPanel {
	
	public OkButtonsPanel(Composite parent, boolean withImages) {
		super(parent, new ButtonInPanel[] {
			new ButtonInPanel("ok", withImages ? SharedImages.getImage(SharedImages.icons.x16.basic.OK) : null, Local.Ok.toString()),	
		});
	}
	
	@Override
	protected void handleButton(String id) {
		if (id.equals("ok")) {
			if (handleOk()) close();
		}
	}
	
	private void close() {
		getParent().getShell().close();
	}
	
	protected abstract boolean handleOk();
	
	public void enableOk(boolean enabled) {
		enable("ok", enabled);
	}

}
