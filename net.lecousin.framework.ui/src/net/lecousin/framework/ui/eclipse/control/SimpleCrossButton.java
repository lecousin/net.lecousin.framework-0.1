package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.control.button.ButtonStyle;
import net.lecousin.framework.ui.eclipse.control.button.ButtonStyleApply;
import net.lecousin.framework.ui.eclipse.control.button.HoverStyle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class SimpleCrossButton extends Canvas implements PaintListener, MouseTrackListener, MouseListener {

	public SimpleCrossButton(Composite parent, ButtonStyle buttonStyle, int margin) {
		super(parent, SWT.NONE);
		this.hover = buttonStyle.hover;
		this.margin = margin;
		addPaintListener(this);
		addMouseListener(this);
		addMouseTrackListener(this);
		new ButtonStyleApply(this, buttonStyle);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				SimpleCrossButton.this.removeDisposeListener(this);
				SimpleCrossButton.this.hover = null;
				SimpleCrossButton.this.click.free();
				SimpleCrossButton.this.click = null;
				SimpleCrossButton.this.removePaintListener(SimpleCrossButton.this);
				SimpleCrossButton.this.removeMouseListener(SimpleCrossButton.this);
				SimpleCrossButton.this.removeMouseTrackListener(SimpleCrossButton.this);
			}
		});
	}
	
	private HoverStyle hover;
	private boolean isHover = false;
	private int margin;
	private Event<SimpleCrossButton> click = new Event<SimpleCrossButton>();
	
	public void paintControl(PaintEvent e) {
		Point size = getSize();
		
		if (hover.equals(HoverStyle.BOLD))
			e.gc.setLineWidth(isHover ? 2 : 1);
		
		e.gc.drawLine(margin, margin, size.x-1-margin, size.y-1-margin);
		e.gc.drawLine(margin, size.y-1-margin, size.x-1-margin, margin);
	}
	
	public void mouseEnter(MouseEvent e) {
		isHover = true;
		redraw();
	}
	public void mouseExit(MouseEvent e) {
		isHover = false;
		redraw();
	}
	public void mouseHover(MouseEvent e) {
	}
	
	public void mouseDoubleClick(MouseEvent e) {
	}
	public void mouseDown(MouseEvent e) {
	}
	public void mouseUp(MouseEvent e) {
		if (e.button == 1)
			click.fire(this);
	}
	
	public void addClickListener(Listener<SimpleCrossButton> listener) {
		click.addListener(listener);
	}
	public void removeClickListener(Listener<SimpleCrossButton> listener) {
		click.removeListener(listener);
	}
}
