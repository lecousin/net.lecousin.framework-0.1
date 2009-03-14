package net.lecousin.framework.ui.eclipse.control;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class Separator extends Canvas implements PaintListener {

	public enum Style {
		SIMPLE_LINE,
	}
	
	public Separator(Composite parent, boolean horizontal, Style style, int margin) {
		super(parent, SWT.NONE);
		this.horiz = horizontal;
		this.style = style;
		this.margin = margin;
		addPaintListener(this);
	}
	
	private boolean horiz;
	private Style style;
	private int margin;

	public void paintControl(PaintEvent e) {
		Point size = getSize();
		switch (style) {
		case SIMPLE_LINE:
			e.gc.setForeground(getForeground());
			e.gc.setLineStyle(SWT.LINE_SOLID);
			if (horiz) {
				if (size.x > margin*2)
					e.gc.drawLine(margin, size.y/2, size.x-1-margin, size.y/2);
			} else {
				if (size.y > margin*2)
					e.gc.drawLine(size.x/2, margin, size.x/2, size.y-1-margin);
			}
			break;
		}
	}
	
	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
		Point size = super.computeSize(hint, hint2, changed);
		if (horiz) {
			if (hint == SWT.DEFAULT)
				size.x = 1;
			size.y = 1;
		} else {
			size.x = 1;
			if (hint2 == SWT.DEFAULT)
				size.y = 1;
		}
		return size;
	}
}
