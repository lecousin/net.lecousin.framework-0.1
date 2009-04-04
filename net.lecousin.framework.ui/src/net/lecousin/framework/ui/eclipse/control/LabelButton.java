package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.event.Event;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LabelButton extends Label implements MouseListener, PaintListener, MouseTrackListener {

    public LabelButton(Composite parent) {
        super(parent, SWT.NONE);
        setBackground(parent.getBackground());
        
        borderColor = new Color(getDisplay(), 0, 0, 0);
        pushDownBorderColor = new Color(getDisplay(), 128, 128, 128);
        pushUpBorderColor = new Color(getDisplay(), 240, 240, 240);
        
        addMouseListener(this);
        addMouseTrackListener(this);
        addPaintListener(this);
        
        setAlignment(SWT.CENTER);
        
        addDisposeListener(new DisposeListener() {
        	public void widgetDisposed(DisposeEvent e) {
        		clickEvent.free(); clickEvent = null;
        		removeMouseListener(LabelButton.this);
        		removeMouseTrackListener(LabelButton.this);
        		removePaintListener(LabelButton.this);
        		removeDisposeListener(this);
        	}
        });
    }

    @Override
    protected void checkSubclass() {
        // allowed
    }
    
    private int borderSize = 0;
    private Color borderColor;
    private boolean pushed = false;
    private boolean isOver = false;
    private Color pushDownBorderColor;
    private Color pushUpBorderColor;
    
    private static final int MARGIN = 2;
    
    private Event<MouseEvent> clickEvent = new Event<MouseEvent>();
    
    public void addClickListener(Event.Listener<MouseEvent> listener) {
        clickEvent.addListener(listener);
    }
    public void removeClickListener(Event.Listener<MouseEvent> listener) {
        clickEvent.removeListener(listener);
    }
    
    public void mouseDoubleClick(MouseEvent e) {
        // nothing
    }
    public void mouseDown(MouseEvent e) {
        pushed = true; redraw();
    }
    public void mouseUp(MouseEvent e) {
    	if (isDisposed()) return;
        pushed = false; redraw();
        clickEvent.fire(e);
    }
    
    public void setBorderSize(int borderSize) {
        int diff = borderSize - this.borderSize;
        if (diff == 0) return;
        this.borderSize = borderSize;
        Point size = getSize();
        size.x += diff * 2;
        size.y += diff * 2;
        setSize(size);
    }

    public void paintControl(PaintEvent e) {
    	DrawingUtil.drawPushButtonBorder(e.gc, new Point(0, 0), getSize(), borderSize, pushed, isOver, borderColor, pushDownBorderColor, pushUpBorderColor);
    }
    
    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        Point size = super.computeSize(wHint, hHint, changed);
        size.x += MARGIN * 2;
        size.y += MARGIN * 2;
        size.x += borderSize * 2;
        size.y += borderSize * 2;
        return size;
    }
    
    public void mouseEnter(MouseEvent e) {
        if (!isOver) {
            isOver = true;
            redraw();
        }
    }

    public void mouseExit(MouseEvent e) {
        if (isOver) {
            isOver = false;
            redraw();
        }
    }
    
    public void mouseHover(MouseEvent e) {
        // nothing
    }

}
