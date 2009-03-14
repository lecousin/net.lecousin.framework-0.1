package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

public class Tip extends Shell {

	public Tip(String text, Control refCtrl, int x, int y) {
		this(Display.getCurrent(), text, refCtrl, x, y, true);
	}
	
	public Tip(String text, Control refCtrl, int x, int y, boolean arrow_on_top) {
		this(Display.getCurrent(), text, refCtrl, x, y, arrow_on_top);
	}
	
	public Tip(Image icon, String text, Control refCtrl, int x, int y) {
		this(Display.getCurrent(), icon, text, refCtrl, x, y, true);
	}
	
	public Tip(Image icon, String text, Control refCtrl, int x, int y, boolean arrow_on_top) {
		this(Display.getCurrent(), icon, text, refCtrl, x, y, arrow_on_top);
	}
	
	public Tip(Display display, String text, Control refCtrl, int x, int y) {
		this(display, SharedImages.getImage(SharedImages.icons.x16.basic.INFO), text, refCtrl, x, y, true);
	}
	
	public Tip(Display display, String text, Control refCtrl, int x, int y, boolean arrow_on_top) {
		this(display, SharedImages.getImage(SharedImages.icons.x16.basic.INFO), text, refCtrl, x, y, arrow_on_top);
	}
	
	public Tip(Display display, Image icon, String text, Control refCtrl, int x, int y, boolean arrow_on_top) {
		super(display, SWT.NO_TRIM | /*SWT.ON_TOP |*/ SWT.TOOL | SWT.NO_FOCUS | SWT.NO_BACKGROUND);
		this.icon = icon;
		this.text = text;
		this.arrow_on_top = arrow_on_top;
		refX = x;
		refY = y;
		this.refCtrl = refCtrl;
		
		refCtrl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (Tip.this.isDisposed()) 
					e.widget.removeDisposeListener(this);
				else
					Tip.this.closeTip();
			}
		});
		refCtrl.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (Tip.this.isDisposed())
					((Control)e.widget).removePaintListener(this);
				else
					e.display.asyncExec(new Runnable() {
						public void run() {
							if (Tip.this.isDisposed()) return;
							moveTip();
							Tip.this.setRedraw(true);
							Tip.this.setVisible(true);
							Tip.this.setActive();
						}
					});
			}
		});
		refCtrl.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
				if (Tip.this.isDisposed()) {
					Tip.this.refCtrl.removeControlListener(this);
					return;
				}
				moveToLocation();
			}
			public void controlResized(ControlEvent e) {
				if (Tip.this.isDisposed()) {
					Tip.this.refCtrl.removeControlListener(this);
					return;
				}
			}
		});

		painter = new Painter();
		mouser = new Mouser();
		addPaintListener(painter);
		addMouseListener(mouser);
		addMouseMoveListener(mouser);
		
		setBackground(new Color(display, 255, 255, 220));
		moveTip();
		textSize = new GC(display).textExtent(text);
		iconSize = icon.getBounds();
		Point size = new Point(MARGIN_WIDTH * 2 + iconSize.width + SPACE + textSize.x + SPACE + CLOSE_SIZE, 
				MARGIN_HEIGHT * 2 + Math.max(iconSize.height, textSize.y) + ARROW_HEIGHT);
		setSize(size);
		
		open();

		refCtrl.addMouseTrackListener(new MouseTrackListener() {
			public void mouseEnter(MouseEvent e) {
				if (Tip.this.isDisposed()) {
					Tip.this.refCtrl.removeMouseTrackListener(this);
					return;
				}
				if (Tip.this.refCtrl.isVisible()) {
					setVisible(true);
					setActive();
				}
			}
			public void mouseExit(MouseEvent e) {
				if (Tip.this.isDisposed()) {
					Tip.this.refCtrl.removeMouseTrackListener(this);
					return;
				}
			}
			public void mouseHover(MouseEvent e) {
				if (Tip.this.isDisposed()) {
					Tip.this.refCtrl.removeMouseTrackListener(this);
					return;
				}
				if (Tip.this.refCtrl.isVisible()) {
					setVisible(true);
					setActive();
				}
			}
		});
	}
	
	private Control refCtrl;
	private int refX, refY;
	private boolean arrow_on_top;
	private Image icon;
	private String text;
	private Rectangle iconSize;
	private Point textSize;
	private Painter painter;
	private Mouser mouser;
	private boolean mouseOnClose = false;
	
	public int ARROW_HEIGHT = 12;
	public int ARROW_WIDTH = 8;
	public int ARROW_MARGIN = 5;
	public int MARGIN_WIDTH = 3;
	public int MARGIN_HEIGHT = 3;
	public int SPACE = 5;
	public int CLOSE_SIZE = 8;
	
	@Override
	protected void checkSubclass() {
		// allow
	}
	
	private Rectangle getCloseRectangle() {
		int x = MARGIN_WIDTH + iconSize.width + SPACE + textSize.x + SPACE;
		int y = MARGIN_HEIGHT;
		if (arrow_on_top)
			y += ARROW_HEIGHT;
		return new Rectangle(x, y, CLOSE_SIZE, CLOSE_SIZE);
	}
	
	private class Painter implements PaintListener {
		public void paintControl(PaintEvent e) {
			if (!refCtrl.isVisible()) { 
				setRedraw(false);
				setVisible(false);
				return;
			}
			Point size = getSize();

			int[] polygon = new int[8];
			if (arrow_on_top) {
				polygon[0] = ARROW_MARGIN;
				polygon[1] = 0;
				polygon[2] = ARROW_MARGIN;
				polygon[3] = ARROW_HEIGHT;
				polygon[4] = ARROW_MARGIN + ARROW_WIDTH;
				polygon[5] = ARROW_HEIGHT;
				polygon[6] = ARROW_MARGIN;
				polygon[7] = 0;
			} else {
				polygon[0] = ARROW_MARGIN;
				polygon[1] = size.y;
				polygon[2] = ARROW_MARGIN;
				polygon[3] = size.y - ARROW_HEIGHT - 1;
				polygon[4] = ARROW_MARGIN + ARROW_WIDTH;
				polygon[5] = size.y - ARROW_HEIGHT - 1;
				polygon[6] = ARROW_MARGIN;
				polygon[7] = size.y;
			}
			e.gc.setForeground(new Color(getDisplay(), 0, 0, 0));
			e.gc.setBackground(new Color(getDisplay(), 255, 255, 220));
			e.gc.fillPolygon(polygon);
			e.gc.drawPolygon(polygon);
				
			Point pt = new Point(0, arrow_on_top ? ARROW_HEIGHT : 0);
			
			e.gc.setForeground(new Color(getDisplay(), 0, 0, 0));
			e.gc.setBackground(new Color(getDisplay(), 255, 255, 220));
			e.gc.fillRectangle(pt.x + 1, pt.y + 1, size.x - 1 - 2, size.y - 1 - ARROW_HEIGHT - 2);
			e.gc.drawRectangle(pt.x, pt.y, size.x - 1, size.y - 1 - ARROW_HEIGHT);
			e.gc.drawImage(icon, pt.x + MARGIN_WIDTH, pt.y + MARGIN_HEIGHT);
			int y = pt.y + MARGIN_HEIGHT;
			if (iconSize.height > textSize.y) y += (iconSize.height - textSize.y) / 2;
			e.gc.drawText(text, pt.x + MARGIN_WIDTH + iconSize.width + SPACE, y, true);

			e.gc.setForeground(new Color(getDisplay(), 255, 255, 220));
			e.gc.drawLine(polygon[2] + 1, polygon[3], polygon[4] - 1, polygon[5]);
			
			e.gc.setForeground(mouseOnClose ? new Color(getDisplay(), 37, 46, 90) : new Color(getDisplay(), 137, 146, 190));
			Rectangle r = getCloseRectangle();
			e.gc.drawRectangle(r);
			e.gc.drawLine(r.x + 2, r.y + 2, r.x + r.width - 2, r.y + r.height - 2);
			e.gc.drawLine(r.x + r.width - 2, r.y + 2, r.x + 2, r.y + r.height - 2);
		}
	}
	
	private class Mouser implements MouseListener, MouseMoveListener {
		public void mouseDoubleClick(MouseEvent e) {
			// nothing to do
		}
		public void mouseDown(MouseEvent e) {
			// nothing to do
		}
		public void mouseUp(MouseEvent e) {
			if (mouseOnClose)
				closeTip();
		}
		public void mouseMove(MouseEvent e) {
			Rectangle r = getCloseRectangle();
			boolean previous = mouseOnClose;
			mouseOnClose = e.x >= r.x && e.x <= r.x + r.width && e.y >= r.y && e.y <= r.y + r.height;
			if (!refCtrl.isVisible() && isVisible())
				Tip.this.setVisible(false);
			else
			if (previous != mouseOnClose && isVisible())
				redraw();
		}
	}
	
	private void closeTip() {
		if (isDisposed()) return;
		close();
	}
	
	private void moveToLocation() {
		setVisible(false);
		setRedraw(false);
		Event e = new Event();
		e.display = getDisplay();
		e.doit = true;
		e.gc = new GC(e.display);
		e.type = SWT.Paint;
		Point pt = getLocation();
		Point size = getSize();
		e.x = pt.x; e.y = pt.y;
		e.width = size.x; e.height = size.y;
		e.display.post(e);
		moveTip();
		if (refCtrl.isVisible()) {
			setRedraw(true);
			setVisible(true);
			setActive();
		}
	}
	
	private void moveTip() {
		Point pt = refCtrl.toDisplay(refX, refY);
		if (!arrow_on_top)
			pt.y -= getSize().y;
		Point loc = getLocation();
		if (!pt.equals(loc))
			setLocation(pt);
	}
}
