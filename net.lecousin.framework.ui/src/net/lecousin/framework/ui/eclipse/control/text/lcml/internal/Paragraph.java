package net.lecousin.framework.ui.eclipse.control.text.lcml.internal;

import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class Paragraph extends SectionContainer {

	public Paragraph() {
		
	}

	private Composite panel = null;
	
	public void removeControls() {
		if (panel == null) return;
		panel.dispose();
		panel = null;
	}
	
	public Point refreshControls(Composite parent, int maxWidth) {
		refreshSize(parent, new Position(), maxWidth, true);
		return panel.getSize();
	}
	
	public Point refreshSize(Composite parent, int maxWidth, boolean updateControls) {
		Position pos = new Position();
		refreshSize(parent, pos, maxWidth, updateControls);
		Point size = new Point(pos.width, pos.y + pos.lineHeight);
		return size;
	}

	@Override
	protected void refreshSize(Composite parent, Position pos, int maxWidth, boolean updateControls) {
		if (panel == null)
			panel = UIUtil.newComposite(parent);
		pos.x = 0;
		pos.y += pos.lineHeight;
		pos.lineHeight = 0;
		//panel.setLocation(pos.x, pos.y);
		Position subPos = new Position();
		for (Section s : sections) {
			s.refreshSize(panel, subPos, maxWidth, updateControls);
		}
		subPos.y += subPos.lineHeight;
		//Point size = new Point(subPos.width+200, subPos.y+100);
		if (updateControls)
			panel.setBounds(0, pos.y, subPos.width, subPos.y);
		pos.y += subPos.y;
		if (subPos.width > pos.width)
			pos.width = subPos.width;
	}
}
