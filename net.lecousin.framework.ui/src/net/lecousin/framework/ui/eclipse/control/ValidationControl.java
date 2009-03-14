package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ValidationControl extends Composite {

    public ValidationControl(Composite parent) {
        super(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 1;
        layout.marginWidth = 1;
        layout.horizontalSpacing = 3;
        layout.verticalSpacing = 0;
        setLayout(layout);
        GridData gd;
        
        icon = new Label(this, SWT.NONE);
        icon.setImage(SharedImages.getImage(SharedImages.icons.x16.basic.ERROR));
        message = new Label(this, SWT.WRAP);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        
        if (parent.getLayout() instanceof GridLayout) {
            GridLayout pl = (GridLayout)parent.getLayout();
            gd = new GridData();
            gd.grabExcessHorizontalSpace = true;
            gd.horizontalAlignment = SWT.FILL;
            gd.horizontalSpan = pl.numColumns;
            this.setLayoutData(gd);
        }
        
        addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                Point size = getSize();
                e.gc.setForeground(new Color(e.display, 255, 64, 64));
                e.gc.setLineStyle(SWT.LINE_DOT);
                e.gc.drawRectangle(0, 0, size.x - 1, size.y - 1);
            }
        });
        
        setVisible(false);
    }

    private Label icon;
    private Label message;
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        Object data = getLayoutData();
        if (data instanceof GridData) {
            ((GridData)data).exclude = !visible;
        }
    }
    
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        UIControlUtil.setBackground(this, color);
    }
    
    public void updateValidation(String msg) {
        if (msg == null) {
            if (isVisible()) {
                setVisible(false);
                UIControlUtil.autoresize(this);
            }
        } else {
            if (!isVisible() || !message.getText().equals(msg)) {
            	message.setText(msg);
            	setVisible(true);
            	UIControlUtil.autoresize(message);
            }
        }
    }
    
    public void extendInGridLayout() {
    	GridLayout layout = (GridLayout)getParent().getLayout();
    	GridData gd = new GridData();
    	gd.horizontalSpan = layout.numColumns;
    	gd.grabExcessHorizontalSpace = true;
    	gd.horizontalAlignment = SWT.FILL;
    	setLayoutData(gd);
    }
}
