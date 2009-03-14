package net.lecousin.framework.ui.eclipse.control.text.lcml.internal;

import org.eclipse.swt.widgets.Composite;

public class BreakLine extends Section {

	@Override
	protected void refreshSize(Composite parent, Position pos, int maxWidth, boolean updateControls) {
		pos.x = 0;
		pos.y += pos.lineHeight;
		pos.lineHeight = 0;
	}
	
}
