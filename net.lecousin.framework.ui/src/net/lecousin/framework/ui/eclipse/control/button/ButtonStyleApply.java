package net.lecousin.framework.ui.eclipse.control.button;

import net.lecousin.framework.ui.eclipse.control.UIControlUtil;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Control;

public class ButtonStyleApply implements PaintListener, MouseListener, MouseTrackListener {

	public ButtonStyleApply(Control control, ButtonStyle style) {
		this.control = control;
		this.style = style;
		control.addPaintListener(this);
		UIControlUtil.recursiveMouseListener(control, this, true);
		UIControlUtil.recursiveMouseTrackListener(control, this, true);
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				ButtonStyleApply.this.control.removeDisposeListener(this);
				ButtonStyleApply.this.control = null;
				ButtonStyleApply.this.style = null;
			}
		});
	}
	
	private Control control;
	private ButtonStyle style;
	
	private boolean pushed = false;;
	private boolean hover = false;
	private int entered = 0;
	
	public void paintControl(PaintEvent e) {
		if (pushed)
			style.border_push.paint(e, 0);
		else if (hover)
			style.border_hover.paint(e, 0);
		else
			style.border_normal.paint(e, 0);
	}
	
	public void mouseEnter(MouseEvent e) {
		entered++;
		if (!hover) {
			hover = true;
			if (control != null && !control.isDisposed())
				control.redraw();
		}
	}
	public void mouseExit(MouseEvent e) {
		if (--entered == 0 && hover) {
			hover = false;
			if (control != null && !control.isDisposed())
				control.redraw();
		}
	}
	public void mouseHover(MouseEvent e) {
	}
	public void mouseDown(MouseEvent e) {
		pushed = true;
		if (control != null && !control.isDisposed())
			control.redraw();
	}
	public void mouseUp(MouseEvent e) {
		pushed = false;
		if (control != null && !control.isDisposed())
			control.redraw();
	}
	public void mouseDoubleClick(MouseEvent e) {
	}
	
}
