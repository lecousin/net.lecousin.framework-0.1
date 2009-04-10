package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class LCGroup extends Composite {

	public LCGroup(Composite parent, String text) {
		this(parent, text, ColorUtil.getBlack());
	}
	public LCGroup(Composite parent, String text, Color color) {
		super(parent, SWT.NONE);
		super.setBackground(parent.getBackground());
		this.text = text;
		this.color = color;
		GridLayout layout = UIUtil.gridLayout(this, 1);
		layout.marginTop = 10;
		layout.marginBottom = 5;
		layout.marginWidth = 5;
		innerControl = UIUtil.newComposite(this);
		innerControl.setLayoutData(UIUtil.gridData(1, true, 1, true));
		addPaintListener(new Painter());
	}
	
	private String text;
	private Color color;
	private Composite innerControl;
	
	public Composite getInnerControl() { return innerControl; }

	private class Painter implements PaintListener {
		public void paintControl(PaintEvent e) {
			e.gc.setForeground(color);
			e.gc.drawText(text, 15, 0);
			Point textSize = e.gc.textExtent(text);
			Point size = getSize();
			e.gc.drawLine(2, 7, 2, size.y-3);
			e.gc.drawLine(2, size.y-3, size.x-3, size.y-3);
			e.gc.drawLine(size.x-3, 7, size.x-3, size.y-3);
			e.gc.drawLine(2, 7, 12, 7);
			e.gc.drawLine(15 + textSize.x + 2, 7, size.x-3, 7);
		}
	}
	
	public void setBackground(Color color) {
		super.setBackground(color);
		innerControl.setBackground(color);
	}
}
