package net.lecousin.framework.ui.eclipse.control.text.lcml.internal;

import org.eclipse.swt.widgets.Composite;


public abstract class Section {

	protected static class Position {
		// position on current line
		int x = 0;
		int y = 0;
		// maximum width encountered
		int width = 0;
		// current line height
		int lineHeight = 0;
	}
	
	protected abstract void refreshSize(Composite parent, Position pos, int maxWidth, boolean updateControls);
	
}
