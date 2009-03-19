package net.lecousin.framework.ui.eclipse.dialog;

import net.lecousin.framework.Pair;
import net.lecousin.framework.thread.RunnableWithData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public abstract class MyDialog extends Dialog {

	public MyDialog(Shell parent) {
		super(parent == null ? getModalShell() : parent);
	}
	
	public static Shell getModalShell() {
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return getModalShell(display.getShells());
	}
	
	public static final int modalStyle = /*SWT.APPLICATION_MODAL |*/ SWT.SYSTEM_MODAL | SWT.PRIMARY_MODAL;

	public static Shell getModalShell(Shell[] shells) {
		for (int i = 0; i < shells.length; ++i) {
            if (shells[i].isVisible() && (shells[i].getStyle() & modalStyle) != 0)
            	return shells[i];
            Shell result = getModalShell(shells[i].getShells());
            if (result != null)
            	return result;
		}
		Display display;
		if (PlatformUI.isWorkbenchRunning())
			display = PlatformUI.getWorkbench().getDisplay();
		else
			display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return new Shell(display, SWT.PRIMARY_MODAL/*SWT.APPLICATION_MODAL*/);
	}
	
	private Shell shell;
	private Composite dialog;
	private int maxWidth = -1, maxHeight = -1, minWidth = -1, minHeight = -1;
	
	public static int FLAG_MODAL = 		0x00000001;
	public static int FLAG_CLOSABLE = 	0x00000002;
	public static int FLAG_RESIZABLE = 	0x00000004;
	public static int FLAG_FULLSCREEN =	0x00000008;
	public static int FLAG_TITLE = 		0x00000010;
	public static int FLAG_BORDER = 	0x00000020;
	public static int FLAG_MENU = 		0x00000040;
	
	public static int FLAGS_DIALOG = FLAG_BORDER | FLAG_CLOSABLE | FLAG_TITLE;
	public static int FLAGS_MODAL_DIALOG = FLAG_MODAL | FLAGS_DIALOG;
	
    protected void open(String title, int flags) {
    	create(title, flags);
        shell.open();
        if ((flags & FLAG_MODAL)!=0)
        	modal();
    }
    protected void open(boolean modal) {
    	if (shell == null) return;
        shell.open();
        if (modal)
        	modal();
    }
    public enum OrientationX { LEFT, RIGHT };
    public enum OrientationY { TOP, BOTTOM };
    static class Progressive {
    	Point size;
    	Point loc;
    	OrientationX x;
    	OrientationY y;
		int stepsSize;
		int stepsAlpha;
		int posX;
		int totalX;
		int posY;
		int totalY;
		int posA;
		int totalA;
    }
    private boolean progressive = false;
    protected void openProgressive(OrientationX x, OrientationY y) {
    	progressive = true;
		Progressive prog = new Progressive();
		prog.x = x;
		prog.y = y;
		prog.size = getShell().getSize();
		prog.loc = getShell().getLocation();
    	int alpha = getShell().getAlpha();
    	progressiveLocation(prog.loc, prog.size, x, y, 0, 0);
    	progressiveSize(prog.loc, prog.size, x, y, 0, 0);
		open(false);
		int max = x != null ? y != null ? Math.max(prog.size.x, prog.size.y) : prog.size.x : prog.size.y; 
		prog.stepsSize = max / 20;
		if (prog.stepsSize == 0) prog.stepsSize = 1;
		else if (prog.stepsSize > 10) prog.stepsSize = 10;
		prog.stepsAlpha = 15;
		prog.posX = 0;
		prog.totalX = prog.size.x;
		prog.posY = 0;
		prog.totalY = prog.size.y;
		prog.posA = 0;
		prog.totalA = alpha-0;
		getShell().getDisplay().timerExec(15, new RunnableWithData<Progressive>(prog) {
			public void run() {
				if (getShell().isDisposed()) return;
				Progressive p = data();
				if (p.stepsSize == 0) {
					p.posX += p.totalX;
					p.posY += p.totalY;
					progressiveLocation(p.loc, p.size, p.x, p.y, p.posX, p.posY);
					progressiveSize(p.loc, p.size, p.x, p.y, p.posX, p.posY);
					p.stepsSize = -1;
				}
				if (p.stepsSize > 0) {
					int stepX = p.totalX/p.stepsSize;
					p.totalX -= stepX;
					p.posX += stepX;
					int stepY = p.totalY/p.stepsSize;
					p.totalY -= stepY;
					p.posY += stepY;
					p.stepsSize--;
					progressiveLocation(p.loc, p.size, p.x, p.y, p.posX, p.posY);
					progressiveSize(p.loc, p.size, p.x, p.y, p.posX, p.posY);
				}
				if (p.stepsAlpha == 0) {
					p.posA += p.totalA;
					getShell().setAlpha(p.posA);
					p.stepsAlpha = -1;
				}
				if (p.stepsAlpha > 0) {
					int stepA = p.totalA/p.stepsAlpha;
					p.totalA -= stepA;
					p.posA += stepA;
					getShell().setAlpha(p.posA);
					p.stepsAlpha--;
				};
				if (p.stepsAlpha < 0 && p.stepsSize < 0)
					return;
				getShell().getDisplay().timerExec(20, this);
			}
		});
    }
    private void progressiveLocation(Point loc, Point size, OrientationX ox, OrientationY oy, int posx, int posy) {
    	int x = loc.x;
    	int y = loc.y;
    	if (ox != null)
    		switch (ox) {
    		case LEFT: x += size.x - posx; break;
    		case RIGHT: break;
    		}
    	if (oy != null)
    		switch (oy) {
    		case TOP: y += size.y - posy; break;
    		}
    	getShell().setLocation(x, y);
    }
    private void progressiveSize(Point loc, Point size, OrientationX ox, OrientationY oy, int posx, int posy) {
    	int x = ox != null ? posx : size.x;
    	int y = oy != null ? posy : size.y;
    	isResizing = true;
    	getShell().setSize(x, y);
    	isResizing = false;
    }
    
    protected void create(String title, int flags) {
    	if (shell != null) return;
        Shell parent = getParent();
        int shellFlags = (flags & FLAG_MENU)!=0 ? SWT.ON_TOP : SWT.PRIMARY_MODAL;
        if ((flags & FLAG_CLOSABLE)!=0) shellFlags |= SWT.CLOSE;
        if ((flags & FLAG_RESIZABLE)!=0) shellFlags |= SWT.RESIZE;
        if ((flags & FLAG_TITLE)!=0) shellFlags |= SWT.TITLE;
        if ((flags & FLAG_BORDER)!=0) shellFlags |= SWT.BORDER;
        shell = new Shell(parent, shellFlags);
        if (title != null)
        	shell.setText(title);
        shell.setLayout(new FillLayout());
        dialog = createControl(shell);
        if ((flags & FLAG_FULLSCREEN)!=0)
        	shell.setBounds(shell.getDisplay().getBounds());
        else
        	resize();
        shell.addControlListener(new ControlListener() {
        	public void controlMoved(ControlEvent e) {
        	}
        	public void controlResized(ControlEvent e) {
        		if (isResizing) return;
        		resize();
        	}
        });
        dialog.addControlListener(new ControlListener() {
        	public void controlMoved(ControlEvent e) {
        	}
        	public void controlResized(ControlEvent e) {
        		if (isResizing) return;
        		resize(false);
        	}
        });
    }
    
    protected abstract Composite createControl(Composite container);

    public void modal() {
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }
    
    public void close() {
    	if (progressive)
    		closeProgressive();
    	else
    		shell.close();
    }
    public void closeProgressive() {
    	if (shell.isDisposed()) return;
    	shell.getDisplay().timerExec(15, new Runnable() {
    		private int steps = 15;
    		public void run() {
    			if (shell.isDisposed()) return;
    			int a = shell.getAlpha();
    			a -= a/steps--;
    			if (steps == 0) {
    				shell.close();
    			} else {
    				shell.setAlpha(a);
    				shell.getDisplay().timerExec(15, this);
    			}
    		}
    	});
    }
    
    public Shell getShell() { return shell; }
    
    public void setMaxWidth(int w) { maxWidth = w; }
    public void setMaxHeight(int h) { maxHeight = h; }
    public void setMinWidth(int w) { minWidth = w; }
    public void setMinHeight(int h) { minHeight = h; }
    
    public static void resizeShell(Shell shell, Point size) {
        Rectangle rect = shell.computeTrim(0, 0, size.x, size.y);
        Rectangle max = shell.getDisplay().getClientArea();
        Point pt = shell.getLocation();
        rect.x = pt.x;
        rect.y = pt.y;
        if (rect.width > max.width) rect.width = max.width;
        if (rect.height > max.height) rect.height = max.height;
        if (rect.x < 0) rect.x = 0;
        if (rect.y < 0) rect.y = 0;
        if (rect.x + rect.width > max.width)
        	rect.x = max.width - rect.width;
        if (rect.y + rect.height > max.height)
        	rect.y = max.height - rect.height;
        shell.setBounds(rect);
        rect = shell.getClientArea();
        size = shell.getChildren()[0].getSize();
        if (size.x != rect.width || size.y != rect.height)
        	shell.getChildren()[0].setSize(rect.width, rect.height);
    }
    public void resize() {
    	resize(true);
    }
    boolean isResizing = false;
    public void resize(boolean computeContentSize) {
    	if (dialog == null) return;
    	isResizing = true;
        Point size = computeContentSize ? dialog.computeSize(SWT.DEFAULT, SWT.DEFAULT, true) : dialog.getSize();
        if (maxWidth != -1 && size.x > maxWidth) {
        	if (computeContentSize) {
        		size =  dialog.computeSize(maxWidth, SWT.DEFAULT, true);
        		if (size.x > maxWidth) size.x = maxWidth;
        	} else
        		size.x = maxWidth;
        }
        if (minWidth != -1 && size.x < minWidth) size.x = minWidth;
        if (minHeight != -1 && size.y < minHeight) size.y = minHeight;
        if (maxHeight != -1 && size.y > maxHeight) size.y = maxHeight;
        resizeShell(shell, size);
        Rectangle r = shell.getBounds();
        Rectangle ra = shell.getClientArea();
        if (ra.width != size.x) {
        	int ecart = shell.getDisplay().getBounds().height - r.height;
        	if (ecart > 0) {
        		Point s = dialog.computeSize(ra.width, SWT.DEFAULT, false);
        		if (s.x != ra.width || s.y != ra.height)
        			resizeShell(shell, s);
        	}
        }
        isResizing = false;
    }
    
    protected Composite getDialogPanel() { return dialog; }

	public enum Orientation {
		/** Start from bottom right, and go up-left until there is enough space */
		BOTTOM_RIGHT,
		/** Show on above or below according to where there is the most space */
		TOP_BOTTOM,
		/** Show below, except if there is not enough space. In this case it will be shown above. */
		BOTTOM,
	}
    
    public Pair<OrientationX,OrientationY> setLocationRelative(Control relative, Orientation orientation) {
		resize();
		Rectangle bounds;
		if (relative != null)
			bounds = toDisplay(relative);
		else {
			Point pt = Display.getDefault().getCursorLocation();
			bounds = new Rectangle(pt.x, pt.y, 0, 0);
		}
		Point size = getShell().getSize();
		Rectangle display = getShell().getDisplay().getBounds();
		int x = 0, y = 0;
		OrientationX ox = null;
		OrientationY oy = null;
		switch (orientation) {
		case BOTTOM_RIGHT:
			ox = OrientationX.RIGHT;
			oy = OrientationY.BOTTOM;
			if (size.x + bounds.x + bounds.width <= display.x + display.width)
				// enough place at right
				x = bounds.x + bounds.width;
			else
				x = bounds.x + bounds.width - ((size.x + bounds.x + bounds.width) - (display.x + display.width));
			if (size.y + bounds.y + bounds.height <= display.y + display.height)
				// enough place at bottom
				y = bounds.y + bounds.height;
			else
				y = bounds.y + bounds.height - ((size.y + bounds.y + bounds.height) - (display.y + display.height));
			break;
		case TOP_BOTTOM:
			ox = null;
			if (bounds.y - display.y > (display.y + display.height)-(bounds.y+bounds.height)) {
				// more space on top
				y = bounds.y - size.y;
				if (y < display.y) y = display.y;
				oy = OrientationY.TOP;
			} else {
				// more space on bottom
				y = bounds.y + bounds.height;
				if (y + size.y > display.y + display.height)
					y = (display.y + display.height)-size.y;
				if (y < display.y) y = display.y;
				oy = OrientationY.BOTTOM;
			}
			x = bounds.x;
			if (x + size.x > display.x + display.width)
				x = (display.x + display.width) - size.x;
			if (x < display.x)
				x = display.x;
			break;
		case BOTTOM:
			ox = null;
			if (bounds.y + size.y < display.y + display.height) {
				// enough space on bottom
				y = bounds.y + bounds.height;
				if (y + size.y > display.y + display.height)
					y = (display.y + display.height)-size.y;
				if (y < display.y) y = display.y;
				oy = OrientationY.BOTTOM;
			} else {
				y = bounds.y - size.y;
				if (y < display.y) y = display.y;
				oy = OrientationY.TOP;
			}
			x = bounds.x;
			if (x + size.x > display.x + display.width)
				x = (display.x + display.width) - size.x;
			if (x < display.x)
				x = display.x;
			break;
		}
		getShell().setLocation(x, y);
		return new Pair<OrientationX,OrientationY>(ox, oy);
    }
	private Rectangle toDisplay(Control c) {
		Rectangle r = c.getBounds();
		Point pt = c.getParent().toDisplay(r.x, r.y);
		r.x = pt.x;
		r.y = pt.y;
		return r;
	}
    
}
