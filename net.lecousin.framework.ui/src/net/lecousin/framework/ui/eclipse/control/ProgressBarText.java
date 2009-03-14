package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class ProgressBarText extends Canvas implements PaintListener {

	public ProgressBarText(Composite parent, int style, Color textColor) {
		super(parent, style & (SWT.BORDER));
		this.textColor = textColor;
		addPaintListener(this);
	}
	public ProgressBarText(Composite parent, int style) {
		this(parent, style, ColorUtil.getBlack());
	}
	
	private int min = 0, max = 1, pos = 0;
	private String text;
	private Color textColor;
	private Color barColorStart = ColorUtil.getBlue(), barColorMiddle = ColorUtil.getBlue(), barColorEnd = ColorUtil.getBlue();
	
	public int getMinimum() { return min; }
	public void setMinimum(int minimum) { min = minimum; redraw(); }
	public int getMaximum() { return max; }
	public void setMaximum(int maximum) { max = maximum; redraw(); }
	public int getPosition() { return pos; }
	public void setPosition(int position) { pos = position; redraw(); }
	public String getText() { return text; }
	public void setText(String text) { this.text = text; redraw(); }
	
	public void setPosition(int position, String text) { pos = position; this.text = text; redraw(); } 
	
	public void setBarColor(Color start, Color middle, Color end) {
		barColorStart = start;
		barColorMiddle = middle;
		barColorEnd = end;
	}
	public void setBarColor(Color start, Color end) {
		setBarColor(start, ColorUtil.goTo(start, end, 2));
	}
	public void setBarColor(Color color) {
		setBarColor(color, color, color);
	}
	
	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
		return new Point(hint == SWT.DEFAULT ? 100 : hint, hint2 == SWT.DEFAULT ? 18 : hint2);
	}
	
	public void paintControl(PaintEvent e) {
		Point size = getSize();
		int x = size.x * (pos-min) / (max-min);
		Color color;
		if (pos == 0)
			color = barColorStart;
		else if (pos < (max-min)/2)
			color = ColorUtil.goTo(barColorStart, barColorMiddle, (max-min)/2, pos-min);
		else if (pos == (max-min)/2)
			color = barColorMiddle;
		else if (pos == max)
			color = barColorEnd;
		else
			color = ColorUtil.goTo(barColorMiddle, barColorEnd, (max-min)-(max-min)/2, pos-min-(max-min)/2);
		e.gc.setBackground(color);
		e.gc.fillRectangle(0, 0, x, size.y);
		e.gc.setBackground(getBackground());
		if (text != null) {
			e.gc.setForeground(textColor);
			Point ts = e.gc.textExtent(text);
			x = size.x/2 - ts.x/2;
			int y = size.y/2 - ts.y/2 - (ts.y%2);
			e.gc.drawText(text, x, y, SWT.DRAW_TRANSPARENT);
		}
	}
}
