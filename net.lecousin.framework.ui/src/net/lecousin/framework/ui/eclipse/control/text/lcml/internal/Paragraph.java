package net.lecousin.framework.ui.eclipse.control.text.lcml.internal;

import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class Paragraph extends SectionContainer {

	public Paragraph(int marginTop, int marginBottom, int marginLeft) {
		this.marginTop = marginTop;
		this.marginBottom = marginBottom;
		this.marginLeft = marginLeft;
	}

	private Composite panel = null;
	private int marginTop, marginBottom, marginLeft;
	
	public void removeControls() {
		if (panel == null) return;
		panel.dispose();
		panel = null;
	}
	@Override
	void free() {
		removeControls();
	}
	
	public Point refreshControls(Composite parent, int maxWidth) {
		Position pos = new Position();
		refreshSize(parent, pos, maxWidth, true);
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
		if (panel == null) {
			panel = UIUtil.newComposite(parent);
			panel.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					panel = null;
					freeSections();
				}
			});
		}
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
			panel.setBounds(marginLeft, pos.y + marginTop, subPos.width, subPos.y + marginBottom);
		pos.y += marginTop + subPos.y + marginBottom;
		if (subPos.width + marginLeft > pos.width)
			pos.width = subPos.width + marginLeft;
	}
}
