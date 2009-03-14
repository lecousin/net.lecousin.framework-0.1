package net.lecousin.framework.ui.eclipse.control.buttonbar;

import net.lecousin.framework.ui.eclipse.Local;
import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.swt.widgets.Composite;

public class CloseButtonPanel extends ButtonsPanel {
	
	public CloseButtonPanel(Composite parent, boolean withImages) {
		super(parent, new ButtonInPanel[] {
			new ButtonInPanel("close", withImages ? SharedImages.getImage(SharedImages.icons.x16.basic.CLOSE) : null, Local.Close.toString()),	
		});
	}
	
	public void enableClose(boolean enabled) {
		super.enable("close", enabled);
	}
	
	@Override
	protected void handleButton(String id) {
		handleClose();
	}

	protected void handleClose() {
		getParent().getShell().close();
	}
}
