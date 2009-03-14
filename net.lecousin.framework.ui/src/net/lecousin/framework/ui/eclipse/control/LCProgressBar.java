package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class LCProgressBar extends Canvas {
	
	public LCProgressBar(Composite parent, Style style, Color barColor) {
		super(parent, SWT.NO_BACKGROUND);
		this.style = style;
		this.barColor = barColor;
		addPaintListener(new Painter());
	}
	
	private int min = 0, max = 1, pos = 0;
	private Color barColor;
	private Style style;
	
	public enum Style {
		PLAIN,
		PLAIN_BLOCK,
		ROUND,
		ROUND_BLOCK
	}
	
	public void setMinimum(int m) { if (m == min) return; min = m; redraw(); }
	public void setMaximum(int m) { if (m == max) return; max = m; redraw(); }
	public void setPosition(int p) { if (p == pos) return; pos = p; redraw(); }
	public int getMinimum() { return min; }
	public int getMaximum() { return max; }
	public int getPosition() { return pos; }
	
	private class Painter implements PaintListener {
		public void paintControl(PaintEvent e) {
			Point size = getSize();
			e.gc.setForeground(ColorUtil.get(150, 150, 150));
			e.gc.drawRectangle(0, 0, size.x-1, size.y-1);
			e.gc.setForeground(ColorUtil.goTo(getParent().getBackground(), ColorUtil.get(150, 150, 150), 2));
			e.gc.drawPoint(0, 0);
			e.gc.drawPoint(0, size.y-1);
			e.gc.drawPoint(size.x-1, 0);
			e.gc.drawPoint(size.x-1, size.y-1);
			int x = max != min ? (pos-min)*(size.x-2)/(max-min) : 0;
			switch (style) {
			case PLAIN:
				e.gc.setBackground(barColor);
				e.gc.fillRectangle(1, 1, x, size.y-2);
				break;
			case PLAIN_BLOCK:
				// TODO
				break;
			case ROUND:
				for (int i = 1; i < size.y-1; ++i) {
					int clear, clear2;
					if (i < size.y/2) {
						clear = 100 + (size.y/2-i)*10;
						clear2 = 15+(size.y/2-i)*5;
					} else {
						clear = (i-size.y/2)*5;
						clear2 = clear;
					}
					Color col1 = ColorUtil.clearer(barColor, clear);
					Color col2 = ColorUtil.clearer(ColorUtil.get(200, 200, 200), clear2);
					e.gc.setForeground(col1);
					e.gc.drawLine(1, i, 1+x, i);
					e.gc.setForeground(col2);
					e.gc.drawLine(1+x+1, i, size.x-2, i);
				}
				break;
			case ROUND_BLOCK:
				// TODO
				break;
			}
		}
	}

	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
		return new Point(
				hint == SWT.DEFAULT ? 100 : hint,
				hint2 == SWT.DEFAULT ? 18 : hint2
				);
	}
}
