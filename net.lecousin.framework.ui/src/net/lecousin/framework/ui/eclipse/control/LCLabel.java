package net.lecousin.framework.ui.eclipse.control;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class LCLabel extends Canvas {

	public LCLabel(Composite parent, String text) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		this.text = text;
		addPaintListener(new Painter());
	}
	
	private String text;
	private int hAlign = SWT.LEFT;
	private int vAlign = SWT.TOP;
	
	public void setHorizontalAlignment(int align) { if (align == hAlign) return; hAlign = align; redraw(); }
	public void setVerticalAlignment(int align) { if (align == vAlign) return; vAlign = align; redraw(); }
	public void setText(String txt) { if (txt == null || txt.equals(text)) return; text = txt; redraw(); }
	
	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
		Point size = new Point(hint, hint2);
		if (hint != SWT.DEFAULT || hint2 != SWT.DEFAULT) {
			GC gc = new GC(getParent());
			Point p = gc.textExtent(text);
			if (hint == SWT.DEFAULT)
				size.x = p.x;
			if (hint2 == SWT.DEFAULT)
				size.y = p.y;
		}
		return size;
	}
	
	private class Painter implements PaintListener {
		public void paintControl(PaintEvent e) {
			Point size = getSize();
			Point textSize = null;
			if (hAlign != SWT.LEFT || vAlign != SWT.TOP)
				textSize = e.gc.textExtent(text);
			int x = 0, y = 0;
			if (hAlign == SWT.CENTER)
				x = size.x/2-textSize.x/2;
			else if (hAlign == SWT.RIGHT)
				x = size.x - textSize.x;
			if (vAlign == SWT.CENTER)
				y = size.y/2-textSize.y/2;
			else if (vAlign == SWT.BOTTOM)
				y = size.y-textSize.y;
			e.gc.drawText(text, x, y);
		}
	}
}
