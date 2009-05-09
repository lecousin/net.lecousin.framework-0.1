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
		this.color = color;
		GridLayout layout = UIUtil.gridLayout(this, 1);
		layout.marginHeight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 5;
		layout.marginWidth = 5;
		layout.verticalSpacing = 0;
		topControl = UIUtil.newGridComposite(this, 5, 0, 1, 2, 0);
		UIUtil.newLabel(topControl, text).setForeground(color);
		innerControl = UIUtil.newComposite(this);
		innerControl.setLayoutData(UIUtil.gridData(1, true, 1, true));
		addPaintListener(new Painter());
	}
	
	private Color color;
	private Composite topControl;
	private Composite innerControl;
	
	public Composite getInnerControl() { return innerControl; }

	private class Painter implements PaintListener {
		public void paintControl(PaintEvent e) {
			e.gc.setForeground(color);
			//e.gc.drawText(text, 15, 0);
			//Point textSize = e.gc.textExtent(text);
			Point size = getSize();
			Point topSize = topControl.getSize();
			
			int y = topSize.y/2; //7
			
			e.gc.drawLine(2, y, 2, size.y-3);
			e.gc.drawLine(2, size.y-3, size.x-3, size.y-3);
			e.gc.drawLine(size.x-3, y, size.x-3, size.y-3);
			e.gc.drawLine(2, y, 12, y);
			e.gc.drawLine(5 + topSize.x, 7, size.x-3, 7);
		}
	}
	
	public void setBackground(Color color) {
		super.setBackground(color);
		innerControl.setBackground(color);
		topControl.setBackground(color);
	}
	
	public Composite getTopControl(int nbAdded) {
		GridLayout layout = (GridLayout)topControl.getLayout();
		layout.numColumns += nbAdded;
		return topControl; 
	}
}
