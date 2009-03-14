package net.lecousin.framework.ui.eclipse.dialog;

import net.lecousin.framework.Pair;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class CalloutToolTip {

	private CalloutToolTip() {}
	
	public static void open(Control relative, Orientation ori, String text, long time, int maxSize) {
		Shell shell = new Shell(relative.getShell(), SWT.ON_TOP | SWT.NO_TRIM | SWT.NO_BACKGROUND);
		LCMLText label = new LCMLText(shell, false, false);
		label.setText(text);
		maxSize = computeMaxSize(maxSize, relative, ori);
		Point textSize = label.getControl().computeSize(maxSize, SWT.DEFAULT, true);
		label.getControl().setSize(textSize);
		UIControlUtil.setBackground(label.getControl(), ColorUtil.get(255,255,175));
		
		setShellSizeAndTextLocation(shell, textSize, ori, label.getControl());
		setLocation(shell, relative, ori);
		shell.addPaintListener(new Painter(ori, label.getControl()));
		shell.setAlpha(0);
		shell.setVisible(true);
		showThenHide(shell, time);
	}
	
	public enum Orientation {
		TOP, LEFT, BOTTOM, RIGHT,
		TOP_LEFT, TOP_RIGHT,
		BOTTOM_LEFT, BOTTOM_RIGHT
	}
	
	private static final int MARGIN_WIDTH = 5;
	private static final int MARGIN_HEIGHT = 5;
	private static final int CALLOUT_SIZE = 10;
	
	private static int computeMaxSize(int maxSize, Control relative, Orientation ori) {
		Point relLoc = relative.toDisplay(0, 0);
		Point relSize = relative.getSize();
		int x = relLoc.x + relSize.x;
		Rectangle r = relative.getDisplay().getBounds();
		int max = r.width - x - MARGIN_WIDTH*2 - CALLOUT_SIZE - 20;
		if (maxSize > 0 && maxSize < max)
			max = maxSize;
		return max;
	}
	
	private static void setShellSizeAndTextLocation(Shell shell, Point textSize, Orientation ori, Control label) {
		int x = textSize.x + MARGIN_WIDTH*2;
		int y = textSize.y + MARGIN_HEIGHT*2;
		int xLabel = MARGIN_WIDTH;
		int yLabel = MARGIN_HEIGHT;
		switch (ori) {
		case BOTTOM:
		case TOP:
			y += CALLOUT_SIZE;
			break;
		case RIGHT:
		case LEFT:
			x += CALLOUT_SIZE;
			break;
		case BOTTOM_LEFT:
		case TOP_LEFT:
		case BOTTOM_RIGHT:
		case TOP_RIGHT:
			x += CALLOUT_SIZE;
			y += CALLOUT_SIZE;
			break;
		}
		switch (ori) {
		case BOTTOM: case BOTTOM_RIGHT: case BOTTOM_LEFT: yLabel += CALLOUT_SIZE; break;
		}
		switch (ori) {
		case RIGHT: case TOP_RIGHT: case BOTTOM_RIGHT: xLabel += CALLOUT_SIZE; break;
		}
		shell.setSize(x, y);
		label.setLocation(xLabel, yLabel);
	}
	
	private static void setLocation(Shell shell, Control relative, Orientation ori) {
		Point relLoc = relative.toDisplay(0, 0);
		Point relSize = relative.getSize();
		Point size = shell.getSize();
		int x = 0;
		int y = 0;
		switch (ori) {
		case BOTTOM: x = relLoc.x + relSize.x/2 - size.x/2; y = relLoc.y + relSize.y; break;
		case TOP: x = relLoc.x + relSize.x/2 - size.x/2; y = relLoc.y - size.y; break;
		case LEFT: x = relLoc.x - size.x; y = relLoc.y + relSize.y/2 - size.y/2; break;
		case RIGHT: x = relLoc.x + relSize.x; y = relLoc.y + relSize.y/2 - size.y/2; break;
		case BOTTOM_LEFT: x = relLoc.x - size.x; y = relLoc.y + relSize.y; break;
		case TOP_LEFT: x = relLoc.x - size.x; y = relLoc.y - size.y; break;
		case BOTTOM_RIGHT: x = relLoc.x + relSize.x; y = relLoc.y + relSize.y; break;
		case TOP_RIGHT: x = relLoc.x + relSize.x; y = relLoc.y - size.y; break;
		}
		shell.setLocation(x, y);
	}
	
	private static void showThenHide(Shell shell, long time) {
		shell.getDisplay().timerExec(10, new RunnableWithData<Pair<Shell,Long>>(new Pair<Shell,Long>(shell,time)) {
			private boolean isShowing = true;
			public void run() {
				Shell shell = data().getValue1();
				if (shell.isDisposed()) return;
				if (isShowing) {
					int a = shell.getAlpha();
					a += 20;
					if (a > 255) a = 255;
					shell.setAlpha(a);
					if (a < 255) {
						shell.getDisplay().timerExec(10, this);
						return;
					}
					isShowing = false;
					shell.getDisplay().timerExec(data().getValue2().intValue(), this);
					return;
				}
				int a = shell.getAlpha();
				a -= 20;
				if (a <= 0) {
					shell.close();
					return;
				}
				shell.setAlpha(a);
				shell.getDisplay().timerExec(10, this);
			}
		});
	}
	
	private static class Painter implements PaintListener {
		Painter(Orientation ori, Control label) {
			this.ori = ori;
			this.label = label;
		}
		private Orientation ori;
		private Control label;
		public void paintControl(PaintEvent e) {
			// draw bound around the label
			Point loc = label.getLocation();
			Point size = label.getSize();
			Point fullsize = ((Control)e.widget).getSize();
			
			e.gc.setBackground(ColorUtil.get(255, 255, 175));
			e.gc.fillRectangle(loc.x, loc.y, size.x, size.y);
			e.gc.fillRectangle(loc.x, loc.y-MARGIN_HEIGHT+1, size.x+1, MARGIN_HEIGHT-1); // top
			e.gc.fillRectangle(loc.x, loc.y+size.y, size.x+1, MARGIN_HEIGHT-1); // bottom
			e.gc.fillRectangle(loc.x-MARGIN_WIDTH+1, loc.y, MARGIN_WIDTH-1, size.y+1); // left
			e.gc.fillRectangle(loc.x+size.x, loc.y, MARGIN_WIDTH-1, size.y+1); // right
			
			e.gc.setForeground(ColorUtil.getBlack());
			e.gc.drawLine(loc.x, loc.y-MARGIN_HEIGHT+1, loc.x+size.x, loc.y-MARGIN_HEIGHT+1); // top
			e.gc.drawLine(loc.x, loc.y+size.y+MARGIN_HEIGHT-1, loc.x+size.x, loc.y+size.y+MARGIN_HEIGHT-1); // bottom
			e.gc.drawLine(loc.x-MARGIN_WIDTH+1, loc.y, loc.x-MARGIN_WIDTH+1, loc.y+size.y); // left
			e.gc.drawLine(loc.x+size.x+MARGIN_WIDTH-1, loc.y, loc.x+size.x+MARGIN_WIDTH-1, loc.y+size.y); // right
			
			// top-left
			e.gc.drawPoint(loc.x-MARGIN_WIDTH+2, loc.y-1);
			e.gc.drawPoint(loc.x-MARGIN_WIDTH+2, loc.y-2);
			e.gc.drawPoint(loc.x-MARGIN_WIDTH+3, loc.y-3);
			e.gc.drawPoint(loc.x-MARGIN_WIDTH+4, loc.y-3);
			e.gc.fillRectangle(loc.x-MARGIN_WIDTH+3, loc.y-2, 2, 2);
			// top-right
			e.gc.drawPoint(loc.x+size.x+3, loc.y-1);
			e.gc.drawPoint(loc.x+size.x+3, loc.y-2);
			e.gc.drawPoint(loc.x+size.x+2, loc.y-3);
			e.gc.drawPoint(loc.x+size.x+1, loc.y-3);
			e.gc.fillRectangle(loc.x+size.x+1, loc.y-2, 2, 2);
			// bottom-left
			e.gc.drawPoint(loc.x-MARGIN_WIDTH+2, loc.y+size.y+1);
			e.gc.drawPoint(loc.x-MARGIN_WIDTH+2, loc.y+size.y+2);
			e.gc.drawPoint(loc.x-MARGIN_WIDTH+3, loc.y+size.y+3);
			e.gc.drawPoint(loc.x-MARGIN_WIDTH+4, loc.y+size.y+3);
			e.gc.fillRectangle(loc.x-MARGIN_WIDTH+3, loc.y+size.y+1, 2, 2);
			// bottom-right
			e.gc.drawPoint(loc.x+size.x+3, loc.y+size.y+1);
			e.gc.drawPoint(loc.x+size.x+3, loc.y+size.y+2);
			e.gc.drawPoint(loc.x+size.x+2, loc.y+size.y+3);
			e.gc.drawPoint(loc.x+size.x+1, loc.y+size.y+3);
			e.gc.fillRectangle(loc.x+size.x+1, loc.y+size.y+1, 2, 2);
			
			// callout
			Point target = new Point(0,0);
			Point src1 = new Point(0,0);
			Point src2 = new Point(0,0);
			switch (ori) {
			case BOTTOM:
				target.x = fullsize.x/2;
				target.y = 0;
				src1 = new Point(target.x - 10, loc.y-3);
				src2 = new Point(target.x + 10, loc.y-3);
				break;
			case BOTTOM_LEFT:
				break;
			case BOTTOM_RIGHT:
				target.x = 0;
				target.y = 0;
				src1.x = CALLOUT_SIZE + 10;
				src1.y = loc.y-MARGIN_HEIGHT+2;
				src2.x = CALLOUT_SIZE + 25;
				src2.y = loc.y-MARGIN_HEIGHT+2;
				break;
			case LEFT:
				break;
			case RIGHT:
				break;
			case TOP:
				target.x = fullsize.x/2;
				target.y = fullsize.y;
				src1 = new Point(target.x - 10, loc.y+size.y+MARGIN_HEIGHT-1);
				src2 = new Point(target.x + 10, loc.y+size.y+MARGIN_HEIGHT-1);
				break;
			case TOP_LEFT:
				break;
			case TOP_RIGHT:
				target.x = 0;
				target.y = fullsize.y;
				src1.x = CALLOUT_SIZE + 10;
				src1.y = loc.y+size.y+MARGIN_HEIGHT-1;
				src2.x = CALLOUT_SIZE + 25;
				src2.y = loc.y+size.y+MARGIN_HEIGHT-1;
				break;
			}
			Path path = new Path(e.display);
			path.moveTo(target.x, target.y);
			path.lineTo(src1.x, src1.y);
			path.lineTo(src2.x, src2.y);
			path.lineTo(target.x, target.y);
			e.gc.fillPath(path);
			e.gc.drawLine(target.x, target.y, src1.x, src1.y);
			e.gc.drawLine(target.x, target.y, src2.x, src2.y);
		}
	}
}
