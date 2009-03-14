package net.lecousin.framework.ui.eclipse.control;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

public class SimpleBorderPaint implements PaintListener {

	public static SimpleBorderPaint putBorder(Control control, Color color) {
		SimpleBorderPaint border = new SimpleBorderPaint(control, color);
		control.addPaintListener(border);
		return border;
	}
	public static SimpleBorderPaint putBorder(Control control) {
		return putBorder(control, new Color(control.getDisplay(), 0, 0, 0));
	}
	
	private SimpleBorderPaint(Control control, Color color) {
		this.control = control;
		this.color = color;
	}
	
	private Control control;
	private Color color;

	public void paintControl(PaintEvent e) {
        Point size = control.getSize();
        e.gc.setLineStyle(SWT.LINE_SOLID);
        e.gc.setForeground(color);
        e.gc.drawRectangle(0, 0, size.x - 1, size.y - 1);
	}
	
	public void setColor(Color color) {
		this.color = color;
		this.control.redraw();
	}
}
