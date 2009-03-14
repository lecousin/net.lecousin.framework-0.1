package net.lecousin.framework.ui.eclipse.control;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CollapsableControl extends Composite implements CollapserControl.CollapseListener {

    public CollapsableControl(Composite parent) {
        super(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 1;
        layout.verticalSpacing = 1;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);
        
        collapser = new CollapserControl(this);
        GridData gd = new GridData();
        gd.verticalSpan = 2;
        gd.verticalAlignment = SWT.FILL;
        collapser.setLayoutData(gd);
        collapser.registerCollpaseListener(this);
    }
    
    private CollapserControl collapser;
    private Control header = null;
    private Control body = null;
    
    public void setHeader(Control header) {
        this.header = header;
        GridData gd = (GridData)header.getLayoutData();
        if (gd == null) {
            gd = new GridData();
            header.setLayoutData(gd);
        }
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
    }
    public void setBody(Control body) {
        this.body = body;
        GridData gd = (GridData)body.getLayoutData();
        if (gd == null) {
            gd = new GridData();
            body.setLayoutData(gd);
        }
    }
    
    public void collapseChanged(boolean collapsed) {
        if (body.isVisible() == !collapsed) return;
        if (collapsed) animation();
        body.setVisible(!collapsed);
        GridData gd = (GridData)body.getLayoutData();
        gd.exclude = collapsed;
        UIControlUtil.autoresize(this);
    }
    
    public void setBackground(Color color) {
        super.setBackground(color);
        collapser.setBackground(color);
    }
    
    public Control getHeader() { return header; }
    public Control getBody() { return body; }
    
    public void collapse() {
        collapser.changeCollapse(true);
    }
    public void expand() {
        collapser.changeCollapse(false);
    }
    
    private void animation() {
/*    	GC gc = new GC(getDisplay());
    	Point pt = body.toDisplay(0, 0);
    	Point size = body.getSize();

//    	gc.setForeground(new Color(getDisplay(), 160, 160, 160));
//    	for (int y = 0; y < size.y; ++y)
//    		for (int x = y % 2; x < size.x; x += 2)
//    			gc.drawPoint(x + pt.x, y + pt.y);

    	int step = size.y / 50 + 1;
    	gc.setForeground(getBackground());
    	for (int y = size.y - step; y >= 0; y -= step) {
    		if (y < 0) y = 0;
    		for (int i = 0; i < step; ++i)
    			gc.drawLine(pt.x, pt.y + y + i, pt.x + size.x - 1, pt.y + y + i);
    		
    		try { Thread.sleep(5 - (y * 5 / size.y)); }
    		catch (InterruptedException e) { break; }
    	}*/
    }
}
