package net.lecousin.framework.ui.eclipse.control;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class BorderControl extends Composite {

    public BorderControl(Composite parent) {
    	this(parent, new Color(parent.getDisplay(), 0, 0, 0));
    }
    public BorderControl(Composite parent, Color color) {
        super(parent, SWT.NONE);
        setForeground(color);
        
        FillLayout layout = new FillLayout();
        layout.marginHeight = 1;
        layout.marginWidth = 1;
        setLayout(layout);
        
        addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                paint(e.gc);
            }
        });
    }
    
    private void paint(GC gc) {
        Point size = getSize();
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(getForeground());
        gc.drawRectangle(0, 0, size.x - 1, size.y - 1);
    }
    
}
