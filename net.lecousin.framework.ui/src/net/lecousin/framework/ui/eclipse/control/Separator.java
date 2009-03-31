package net.lecousin.framework.ui.eclipse.control;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class Separator extends Canvas implements PaintListener {

	public static abstract class Style {
		protected abstract void paint(PaintEvent e, Point size, int margin, boolean horiz);
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
		style.paint(e, size, margin, horiz);
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
	
	public static class SimpleLine extends Style {
		public SimpleLine(Color color) {
			this.color = color;
		}
		private Color color;
		@Override
		protected void paint(PaintEvent e, Point size, int margin, boolean horiz) {
			e.gc.setForeground(color);
			e.gc.setLineStyle(SWT.LINE_SOLID);
			if (horiz) {
				if (size.x > margin*2)
					e.gc.drawLine(margin, size.y/2, size.x-1-margin, size.y/2);
			} else {
				if (size.y > margin*2)
					e.gc.drawLine(size.x/2, margin, size.x/2, size.y-1-margin);
			}
		}
	}
	public static class GradientLine extends Style {
		public GradientLine(Color color1, Color color2) {
			this.color1 = color1;
			this.color2 = color2;
		}
		private Color color1;
		private Color color2;
		@Override
		protected void paint(PaintEvent e, Point size, int margin, boolean horiz) {
			e.gc.setLineStyle(SWT.LINE_SOLID);
			if (horiz) {
				if (size.x > margin*2) {
					e.gc.setForegroundPattern(new Pattern(e.display, margin, 1, size.x-1-margin, 1, color1, color2));
					e.gc.drawLine(margin, size.y/2, size.x-1-margin, size.y/2);
				}
			} else {
				if (size.y > margin*2) {
					e.gc.setForegroundPattern(new Pattern(e.display, 1, margin, 1, size.y-1-margin, color1, color2));
					e.gc.drawLine(size.x/2, margin, size.x/2, size.y-1-margin);
				}
			}
		}
	}
}
