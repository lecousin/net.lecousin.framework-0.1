package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;

public abstract class BorderStyle {

    public static final Color DEFAULT_SHADOW_DOWN = ColorUtil.get(128, 128, 128);
    public static final Color DEFAULT_SHADOW_UP = ColorUtil.get(240, 240, 240);
    
    public static final Color DEFAULT_LINE_COLOR = ColorUtil.getBlack();
    public static final int DEFAULT_LINE_WIDTH = 1;

    public static final NoBorder NONE = new NoBorder();
	public static final ShadowDown SHADOW_DOWN = new ShadowDown(DEFAULT_SHADOW_DOWN, DEFAULT_SHADOW_UP);
	public static final ShadowUp SHADOW_UP = new ShadowUp(DEFAULT_SHADOW_DOWN, DEFAULT_SHADOW_UP);
	public static final SimpleLine SIMPLE_LINE = new SimpleLine(DEFAULT_LINE_COLOR, DEFAULT_LINE_WIDTH);
	
	public final void paint(PaintEvent e, int margin) {
		Rectangle r = ((Control)e.widget).getBounds();
		r.x = margin;
		r.y = margin;
		r.width -= margin*2;
		r.height -= margin*2;
		paintLocation(e, r);
	}
	public abstract void paintLocation(PaintEvent e, Rectangle r);

	public static class NoBorder extends BorderStyle {
		public void paintLocation(PaintEvent e, Rectangle r) {}
	}
	
	public static class ShadowDown extends BorderStyle {
		public ShadowDown(Color down, Color up) { this.down = down; this.up = up; }
		private Color down, up;
		public void paintLocation(PaintEvent e, Rectangle r) {
            e.gc.setLineWidth(1);
            e.gc.setForeground(down);
            e.gc.drawLine(r.x, r.y, r.x+r.width-1, r.y);
            e.gc.drawLine(r.x, r.y, r.x, r.y+r.height-2);
            e.gc.setForeground(up);
            e.gc.drawLine(r.x+r.width-1, r.y+1, r.x+r.width-1, r.y+r.height-1);
            e.gc.drawLine(r.x, r.y+r.height-1, r.x+r.width-1, r.y+r.height-1);
		}
	}
	
	public static class ShadowUp extends BorderStyle {
		public ShadowUp(Color down, Color up) { this.down = down; this.up = up; }
		private Color down, up;
		public void paintLocation(PaintEvent e, Rectangle r) {
            e.gc.setLineWidth(1);
            e.gc.setForeground(up);
            e.gc.drawLine(r.x, r.y, r.x+r.width-1, r.y);
            e.gc.drawLine(r.x, r.y, r.x, r.y+r.height-2);
            e.gc.setForeground(down);
            e.gc.drawLine(r.x+r.width-1, r.y+1, r.x+r.width-1, r.y+r.height-1);
            e.gc.drawLine(r.x, r.y+r.height-1, r.x+r.width-1, r.y+r.height-1);
		}
	}
	
	public static class SimpleLine extends BorderStyle {
		public SimpleLine(Color color, int width) { this.color = color; this.width = width; }
		private Color color;
		private int width;
		public void paintLocation(PaintEvent e, Rectangle r) {
            e.gc.setLineWidth(width);
            e.gc.setForeground(color);
            e.gc.drawLine(r.x, r.y+1, r.x, r.y+r.height-2);
            e.gc.drawLine(r.x+1, r.y, r.x+r.width-2, r.y);
            e.gc.drawLine(r.x+1, r.y+r.height-1, r.x+r.width-2, r.y+r.height-1);
            e.gc.drawLine(r.x+r.width-1, r.y+1, r.x+r.width-1, r.y+r.height-2);
			//e.gc.drawRectangle(r.x, r.y, r.x + r.width - 1, r.y + r.height - 1);
		}
	}
	
	public static void attach(Control control, BorderStyle style, int margin) {
		control.addPaintListener(new PaintOnControl(style, margin));
		control.redraw();
	}
	public static void detach(Control control) {
		Listener[] listeners = control.getListeners(SWT.Paint);
		for (Listener l : listeners)
			if (l instanceof PaintOnControl)
				control.removePaintListener((PaintOnControl)l);
		control.redraw();
	}
	
	private static class PaintOnControl implements PaintListener {
		PaintOnControl(BorderStyle style, int margin) { this.style = style; this.margin = margin; }
		BorderStyle style;
		int margin;
		public void paintControl(PaintEvent e) {
			style.paint(e, margin);
		}
	}
		
	public static void attachToParent(Control control, BorderStyle style, int margin) {
		control.getParent().addPaintListener(new PaintOnParent(control, style, margin));
		control.getParent().redraw();
	}
	public static void detachFromParent(Control control) {
		Listener[] listeners = control.getParent().getListeners(SWT.Paint);
		for (Listener l : listeners)
			if (l instanceof PaintOnParent && ((PaintOnParent)l).control == control)
				control.getParent().removePaintListener((PaintOnParent)l);
		control.getParent().redraw();
	}
	
	private static class PaintOnParent implements PaintListener {
		PaintOnParent(Control control, BorderStyle style, int margin)
		{ this.control = control; this.style = style; this.margin = margin; }
		Control control;
		BorderStyle style;
		int margin;
		public void paintControl(PaintEvent e) {
			if (control.isDisposed()) return;
			Rectangle r = control.getBounds();
			r.x += margin;
			r.y += margin;
			r.width -= margin*2;
			r.height -= margin*2;
			style.paintLocation(e, r);
		}
	}
}
