package net.lecousin.framework.media.ui;

import net.lecousin.framework.math.Scale;
import net.lecousin.framework.media.internal.EclipsePlugin;
import net.lecousin.framework.ui.eclipse.EclipseImages;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;
import net.lecousin.framework.ui.eclipse.graphics.GraphicsUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class ScaleControl extends Canvas {

	public ScaleControl(Composite parent, Scale<Double> scale, boolean horiz, LabelProvider labelProvider) {
		super(parent, SWT.NONE);
		this.scale = scale;
		this.horiz = horiz;
		this.labelProvider = labelProvider;
		img_button = EclipseImages.getImage(EclipsePlugin.ID, horiz ? "images/player/button_horiz.gif" : "images/player/button_vert.gif");
		setBackground(ColorUtil.getBlack());
		addPaintListener(new Painter());
		scale.changed().addFireListener(changedListener);
		Mouse mouse = new Mouse();
		addMouseListener(mouse);
		addMouseMoveListener(mouse);
		addMouseTrackListener(mouse);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				ScaleControl.this.scale.changed().removeFireListener(changedListener);
			}
		});
	}
	
	private Scale<Double> scale;
	private boolean horiz;
	private LabelProvider labelProvider;
	private Image img_button;
	private static Color col_border1 = ColorUtil.get(55,55,55);
	private static Color col_border2 = ColorUtil.get(80,80,80);
	private static Color col_border3 = ColorUtil.get(60,60,60);
	private static Color col_bar1 = ColorUtil.get(110,110,110);
	private static Color col_bar2 = ColorUtil.get(240,240,240);
	private static Color col_bar3 = ColorUtil.get(165,165,165);
	private static Color col_bar4 = ColorUtil.get(80,80,80);
	private Runnable changedListener = new Runnable() {
		public void run() {
			if (isDisposed()) return;
			Point size = ScaleControl.this.getSize();
			Point bsize = GraphicsUtil.getSize(img_button.getBounds());
			int newX = getCursorX(size, bsize);
			if (newX != lastCursorXDrawn)
				redraw(Math.min(lastCursorXDrawn, newX), cursorY-1, Math.max(lastCursorXDrawn, newX)+bsize.x, bsize.y+2, false);
			if (labelProvider != null) {
				String text = labelProvider.getLabel(scale.getPosition(), scale.getMinimum(), scale.getMaximum());
				if (!text.equals(lastText))
					redraw(0, textY, size.x, size.y-textY, false);
			}
		}
	};
	
	public interface LabelProvider {
		public String getLabel(double pos, double min, double max);
	}
	
	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
		Point size = new Point(hint, hint2);
		if (hint == SWT.DEFAULT)
			size.x = horiz ? 75 : img_button.getBounds().x + 15;
		if (hint2 == SWT.DEFAULT)
			size.y = horiz ? img_button.getBounds().y + 15 : 75;
		return size;
	}
	
	private class Painter implements PaintListener {
		public void paintControl(PaintEvent e) {
			Point size = ScaleControl.this.getSize();
			Point bsize = GraphicsUtil.getSize(img_button.getBounds());
			
			if (horiz) {
				Point text_size;
				String text;
				if (labelProvider != null) {
					text = labelProvider.getLabel(scale.getPosition(), scale.getMinimum(), scale.getMaximum());
					text_size = e.gc.textExtent(text);
				} else {
					text = null;
					text_size = new Point(0,0);
				}
				if (e.y < textY || textY == 0) {
					int ym = (size.y-text_size.y)/2;
					int x1 = bsize.x/2;
					int x2 = size.x - bsize.x/2 - 1;
					e.gc.setForeground(col_border1);
					e.gc.drawPoint(x1, ym-2);
					e.gc.drawPoint(x2, ym-2);
					e.gc.setForeground(col_border2);
					e.gc.drawPoint(x1, ym-1);
					e.gc.drawPoint(x2, ym-1);
					e.gc.drawPoint(x1, ym);
					e.gc.drawPoint(x2, ym);
					e.gc.setForeground(col_border3);
					e.gc.drawPoint(x1, ym+1);
					e.gc.drawPoint(x2, ym+1);
					e.gc.setForeground(col_bar1);
					e.gc.drawLine(x1+1, ym-2, x2-1, ym-2);
					e.gc.setForeground(col_bar2);
					e.gc.drawLine(x1+1, ym-1, x2-1, ym-1);
					e.gc.setForeground(col_bar3);
					e.gc.drawLine(x1+1, ym, x2-1, ym);
					e.gc.setForeground(col_bar4);
					e.gc.drawLine(x1+1, ym+1, x2-1, ym+1);
					lastCursorXDrawn = getCursorX(size, bsize);
					cursorY = ym-bsize.y/2;
					e.gc.drawImage(img_button, lastCursorXDrawn, cursorY);
					e.gc.setForeground(col_bar2);
					e.gc.drawPoint(lastCursorXDrawn+bsize.x/2, cursorY-1);
					e.gc.drawPoint(lastCursorXDrawn+bsize.x/2, cursorY+bsize.y);
				}
				if (e.y+e.height >= textY) {
					if (labelProvider != null) {
						e.gc.setForeground(ColorUtil.getWhite());
						textY = size.y-text_size.y;
						e.gc.drawText(text, size.x/2 - text_size.x/2, textY);
					}
				}
			} else {
				// TODO vertical
			}
		}
	}
	
	private int lastCursorXDrawn = 0;
	private int cursorY = 0;
	private String lastText = "";
	private int textY = 0;
	private int getCursorX(Point size, Point bsize) {
		return (int)((size.x - bsize.x)*(scale.getPosition().doubleValue()-scale.getMinimum().doubleValue())/(scale.getMaximum().doubleValue()-scale.getMinimum().doubleValue()));
	}
	
	private boolean editing = false;
	public boolean isEditingPosition() { return editing; }
	
	private class Mouse implements MouseListener, MouseTrackListener, MouseMoveListener {
		private boolean down = false;
		public void mouseDoubleClick(MouseEvent e) {
		}
		public void mouseDown(MouseEvent e) {
			editing = true;
			double pos;
			if (horiz) {
				pos = (double)(e.x-img_button.getBounds().width/2);
				if (pos < 0) pos = 0;
				pos = (double)pos / (double)(ScaleControl.this.getSize().x-img_button.getBounds().width);
				if (pos > 1) pos = 1;
			} else {
				pos = (double)(e.y-img_button.getBounds().height/2);
				if (pos < 0) pos = 0;
				pos = (double)pos / (double)(ScaleControl.this.getSize().y-img_button.getBounds().height);
				if (pos > 1) pos = 1;
			}
			pos = pos*(scale.getMaximum()-scale.getMinimum())+scale.getMinimum();
			if (pos < scale.getMinimum()) pos = scale.getMinimum();
			else if (pos > scale.getMaximum()) pos = scale.getMaximum();
			scale.setPosition(pos);
			down = true;
		}
		public void mouseUp(MouseEvent e) {
			down = false;
			editing = false;
		}
		public void mouseMove(MouseEvent e) {
			if (down) {
				mouseDown(e);
			}
		}
		public void mouseEnter(MouseEvent e) {
		}
		public void mouseExit(MouseEvent e) {
			down = false;
			editing = false;
		}
		public void mouseHover(MouseEvent e) {
		}
	}
}
