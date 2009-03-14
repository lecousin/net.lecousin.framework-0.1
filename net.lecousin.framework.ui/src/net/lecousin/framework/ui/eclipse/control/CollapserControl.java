package net.lecousin.framework.ui.eclipse.control;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class CollapserControl extends Canvas {

    public CollapserControl(Composite parent) {
        super(parent, SWT.NONE);
        addPaintListener(new Painter());
        addMouseListener(new Mouser());
    }
    
    private boolean collapsed = false;
    private Collection<CollapseListener> listeners = new LinkedList<CollapseListener>();
    
    public static interface CollapseListener {
        public void collapseChanged(boolean collapsed);
    }
    
    public void registerCollpaseListener(CollapseListener listener) {
        if (listeners.contains(listener)) return;
        listeners.add(listener);
    }
    public void unregisterCollapseListener(CollapseListener listener) {
        listeners.remove(listener);
    }

    class Painter implements PaintListener {
        public void paintControl(PaintEvent e) {
            paint(e.gc);
        }
    }
    class Mouser implements MouseListener {
        public void mouseDoubleClick(MouseEvent e) {
          // nothing to do
        }
        public void mouseDown(MouseEvent e) {
          // nothing to do
        }
        public void mouseUp(MouseEvent e) {
            mouseClick(new Point(e.x, e.y));
        }
    }
    
    private void paint(GC gc) {
        Point size = getSize();
        //Color lineColor = new Color(getDisplay(), 30, 30, 30);
        
        // draw button
/*        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(lineColor);
        gc.drawOval(1, 1, WIDTH_BOX, HEIGHT_BOX);
        gc.drawLine(3, 1 + HEIGHT_BOX / 2, 1 + WIDTH_BOX - 2, 1 + HEIGHT_BOX / 2);
        if (collapsed)
            gc.drawLine(1 + WIDTH_BOX / 2, 3, 1 + WIDTH_BOX / 2, 1 + HEIGHT_BOX - 2);
*/
        
        Point headerSize = ((CollapsableControl)getParent()).getHeader().getSize();
        int yImg = headerSize.y <= HEIGHT_BOX ? 0 : (headerSize.y - HEIGHT_BOX) / 2;
        
        Image img = SharedImages.getImage(collapsed ? SharedImages.icons.x16.arrows.EXPAND : SharedImages.icons.x16.arrows.COLLAPSE);
        gc.drawImage(img, 0, yImg);
        
        // draw expand line
        if (!collapsed) {
        	/*
            gc.setLineStyle(SWT.LINE_DOT);
            Color lineColor = new Color(getDisplay(), 101, 113, 156);
            gc.setForeground(lineColor);
            gc.drawLine(size.x / 2, HEIGHT_BOX + 1, size.x / 2, size.y - 1);
            gc.drawLine(size.x / 2, size.y - 1, size.x, size.y - 1);
            */

        	Color lineColor = new Color(getDisplay(), 101, 113, 156);
            gc.setForeground(lineColor);
        	for (int y = HEIGHT_BOX + 1 + yImg; y < size.y - 1; y += 2)
        		gc.drawPoint(size.x / 2, y);
        	for (int x = size.x / 2; x < size.x; x += 2)
        		gc.drawPoint(x, size.y - 1);
        }
    }
    
    //private static int WIDTH_BOX = 8;
    //private static int HEIGHT_BOX = 8;
    private static int WIDTH_BOX = 14;
    private static int HEIGHT_BOX = 14;
    
    public Point computeSize(int wHint, int hHint, boolean changed) {
/*        Point pt = new Point(WIDTH_BOX + 2, hHint == SWT.DEFAULT ? 10 : hHint);
        if (collapsed)
            pt.y = HEIGHT_BOX;
        return pt;
*/    
        return new Point(WIDTH_BOX, HEIGHT_BOX);
        }
    
    private void mouseClick(Point pt) {
        if (pt.y < HEIGHT_BOX + 1)
            changeCollapse(!collapsed);
    }
    
    public void changeCollapse(boolean collapsed) {
        if (this.collapsed == collapsed) return;
        this.collapsed = collapsed;
        getParent().layout(true, true);
        redraw();
        for (Iterator<CollapseListener> it = listeners.iterator(); it.hasNext(); )
            it.next().collapseChanged(this.collapsed);
    }
}
