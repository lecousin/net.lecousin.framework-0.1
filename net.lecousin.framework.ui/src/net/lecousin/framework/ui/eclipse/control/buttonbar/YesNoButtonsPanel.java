package net.lecousin.framework.ui.eclipse.control.buttonbar;

import net.lecousin.framework.ui.eclipse.Local;
import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.swt.widgets.Composite;

public abstract class YesNoButtonsPanel extends ButtonsPanel {
	
	public YesNoButtonsPanel(Composite parent, boolean withImages) {
		super(parent, new ButtonInPanel[] {
			new ButtonInPanel("yes", withImages ? SharedImages.getImage(SharedImages.icons.x16.basic.OK) : null, Local.Yes.toString()),	
			new ButtonInPanel("no", withImages ? SharedImages.getImage(SharedImages.icons.x16.basic.CANCEL) : null, Local.No.toString()),	
		});
	}
	
	@Override
	protected void handleButton(String id) {
		if (id.equals("yes")) {
			if (handleYes()) close();
		} else if (id.equals("no")) {
			if (handleNo()) close();
		}
	}
	
	private void close() {
		getParent().getShell().close();
	}
	
	protected abstract boolean handleYes();
	protected abstract boolean handleNo();
	
	public void enableYes(boolean enabled) {
		enable("yes", enabled);
	}
	public void enableNo(boolean enabled) {
		enable("no", enabled);
	}

}
