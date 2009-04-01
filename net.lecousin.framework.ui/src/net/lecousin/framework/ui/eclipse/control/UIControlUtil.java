package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.event.DisposeListenerWithData;
import net.lecousin.framework.ui.eclipse.event.ListenerWithData;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Hyperlink;

public abstract class UIControlUtil {

  public static class TopLevelResize { /* marker */ }
  
    public static void autoresize(Control ctrl) {
    	if (ctrl.getData() instanceof TopLevelResize) {
            if (ctrl instanceof Composite)
                ((Composite)ctrl).layout(true, true);
            resize(ctrl);
            return;
    	}
        Control p = ctrl.getParent();
        if (p!= null && p.getData() instanceof TopLevelResize) {
        	resize(p);
            if (ctrl instanceof Composite)
            	((Composite)ctrl).layout(true, true);
        } else if (p != null && !(p instanceof Shell)) autoresize(p);
        else {
            if (ctrl instanceof Composite)
                ((Composite)ctrl).layout(true, true);
            resize(ctrl);
        }
    }
    
    public static void resize(Control ctrl) {
        Point ns = ctrl.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        Point cs = ctrl.getSize();
        if (!cs.equals(ns))
        	ctrl.setSize(ns);
    }
    
    public static void setBackground(Control c, Color color) {
    	c.setBackground(color);
    	if (c instanceof Composite)
    		for (Control child : ((Composite)c).getChildren())
    			setBackground(child, color);
    }
    
    public static void clear(Composite panel) {
        Control[] children = panel.getChildren();
        for (int i = 0; i < children.length; ++i)
            children[i].dispose();
    }
    
    public static void increaseFontSize(Control ctrl, int inc) {
    	Font font = UIUtil.increaseFontSize(ctrl.getFont(), inc);
    	ctrl.setFont(font);
    	ctrl.addDisposeListener(new DisposeListenerWithData<Font>(font) {
    		public void widgetDisposed(DisposeEvent e) {
    			data().dispose();
    		}
    	});
    }
    
    public static void setFontStyle(Control ctrl, int style) {
    	Font font = UIUtil.setFontStyle(ctrl.getFont(), style);
    	ctrl.setFont(font);
    	ctrl.addDisposeListener(new DisposeListenerWithData<Font>(font) {
    		public void widgetDisposed(DisposeEvent e) {
    			data().dispose();
    		}
    	});
    }
    
    public static void recursiveMouseListener(Control c, MouseListener listener, boolean includeListened) {
    	if (includeListened ||
    		(!(c instanceof Hyperlink) &&
    			(c.getListeners(SWT.MouseUp).length == 0 ||
    			 c.getListeners(SWT.MouseDown).length == 0 ||
    		     c.getListeners(SWT.MouseDoubleClick).length == 0)
    		))
    		c.addMouseListener(listener);
    	if (c instanceof Composite)
    		for (Control child : ((Composite)c).getChildren())
    			recursiveMouseListener(child, listener, includeListened);
    }
    public static void recursiveMouseTrackListener(Control c, MouseTrackListener listener, boolean includeListened) {
    	if (includeListened ||
    		(c.getListeners(SWT.MouseEnter).length == 0 ||
    		 c.getListeners(SWT.MouseExit).length == 0 ||
    		 c.getListeners(SWT.MouseHover).length == 0))
    		c.addMouseTrackListener(listener);
    	if (c instanceof Composite)
    		for (Control child : ((Composite)c).getChildren())
    			recursiveMouseTrackListener(child, listener, includeListened);
    }
    public static void recursiveKeyListener(Control c, KeyListener listener) {
   		c.addKeyListener(listener);
    	if (c instanceof Composite)
    		for (Control child : ((Composite)c).getChildren())
    			recursiveKeyListener(child, listener);
    }
    public static void recursiveFocusListener(Control c, FocusListener listener) {
   		c.addFocusListener(listener);
    	if (c instanceof Composite)
    		for (Control child : ((Composite)c).getChildren())
    			recursiveFocusListener(child, listener);
    }
    
    public static void traverseEvent(Control c, int event, boolean includeChildren) {
    	c.addListener(event, new ListenerWithData<Composite>(c.getParent()) {
    		public void handleEvent(Event event) {
    			data().notifyListeners(event.type, event);
    		}
    	});
    	if (includeChildren && c instanceof Composite)
    		for (Control child : ((Composite)c).getChildren())
    			traverseEvent(child, event, true);
    }
    
    public static void traverseKeyEvents(Control c, boolean includeChildren) {
    	traverseEvent(c, SWT.KeyDown, includeChildren);
    	traverseEvent(c, SWT.KeyUp, includeChildren);
    }
    
    public static boolean isVisible(Control c) {
    	if (c.getParent() == null) return true;
    	Rectangle displayRect = c.getDisplay().getBounds();
    	Point loc = c.getLocation();
    	loc = c.getParent().toDisplay(loc);
    	Point size = c.getSize();
    	if (!displayRect.intersects(loc.x, loc.y, size.x, size.y)) return false;
    	if (c.getParent() == null) return true;
    	Rectangle r = getVisibleRectangleInParent(c);
    	return r != null;
    }
    private static Rectangle getVisibleRectangleInParent(Control c) {
    	Rectangle rect = c.getBounds();
    	if (rect.x < 0) {
    		rect.width += rect.x;
    		rect.x = 0;
    	}
    	if (rect.y < 0) {
    		rect.height += rect.y;
    		rect.y = 0;
    	}
    	if (rect.width <= 0 || rect.height <= 0) return null;
    	Rectangle prect = c.getParent() != null ? c.getParent().getBounds() : c.getDisplay().getBounds();
    	if (rect.x + rect.width > prect.width)
    		rect.width -= (rect.x + rect.width) - prect.width;
    	if (rect.y + rect.height > prect.height)
    		rect.height -= (rect.y + rect.height) - prect.height;
    	if (rect.width <= 0 || rect.height <= 0) return null;
    	if (c.getParent() == null) return rect;
    	Rectangle prectVisible = getVisibleRectangleInParent(c.getParent());
    	if (prectVisible == null) return null;
    	// TODO improvment to do here
    	return rect;
    }
    
    public static void makeSelectable(Control ctrl, Runnable action) {
    	recursiveMouseListener(ctrl, new SelectableMouseListener(action), true);
    	recursiveMouseTrackListener(ctrl, new SelectableMouseTrackListener(ctrl), true);
    }
    
    private static class SelectableMouseListener implements MouseListener {
    	SelectableMouseListener(Runnable action) {
    		this.action = action;
    	}
    	Runnable action;
    	public void mouseDoubleClick(MouseEvent e) {
    	}
    	public void mouseDown(MouseEvent e) {
    	}
    	public void mouseUp(MouseEvent e) {
    		if (e.button == 1)
    			action.run();
    	}
    }
    private static class SelectableMouseTrackListener implements MouseTrackListener {
    	SelectableMouseTrackListener(Control root) {
    		this.root = root;
    		initialBg = root.getBackground();
    		root.addDisposeListener(new DisposeListener() {
    			public void widgetDisposed(DisposeEvent e) {
    				SelectableMouseTrackListener.this.root = null;
    				border = null;
    				painter = null;
    				initialBg = null;
    			}
    		});
    	}
    	private Control root;
    	private Color initialBg;
    	private BorderStyle border = new BorderStyle.SimpleLine(ColorUtil.get(180, 180, 255), 1);
    	private PaintListener painter = new PaintListener() {
    		public void paintControl(PaintEvent e) {
    			border.paint(e, 0);
    		}
    	};
    	private int entered = 0;
    	public void mouseEnter(MouseEvent e) {
    		if (entered == 0) {
    			root.addPaintListener(painter);
    			setBackground(root, ColorUtil.get(220, 220, 255));
    		}
    		entered++;
    	}
    	public void mouseExit(MouseEvent e) {
    		if (--entered == 0) {
    			root.removePaintListener(painter);
    			setBackground(root, initialBg);
    		}
    	}
    	public void mouseHover(MouseEvent e) {
    	}
    }
}
