package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class ImageAndTextButton extends Composite implements PaintListener {

	public ImageAndTextButton(Composite parent, Image icon, String text) {
		this(parent, icon, text, 0, 0);
	}
	
    public ImageAndTextButton(Composite parent, Image icon, String text, int marginWidth, int marginHeight) {
        super(parent, SWT.NONE);
        super.setBackground(parent.getBackground());
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = SPACE;
        layout.marginHeight = MARGIN + marginHeight;
        layout.marginWidth = MARGIN + marginWidth;
        setLayout(layout);
        GridData gd;
        
        this.icon = UIUtil.newImage(this, icon);
        this.text = UIUtil.newLabel(this, text);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.CENTER;
        gd.verticalAlignment = SWT.CENTER;
        this.text.setLayoutData(gd);
        gd = new GridData();
        gd.verticalAlignment = SWT.CENTER;
        this.icon.setLayoutData(gd);
        
        pushDownBorderColor = new Color(getDisplay(), 128, 128, 128);
        pushUpBorderColor = new Color(getDisplay(), 240, 240, 240);
        
        addPaintListener(this);
        new LabelListener(this.icon);
        new LabelListener(this.text);
        new LabelListener(this);
    }

    private Label icon;
    private Label text;

    private int borderSize = 0;
    private Color borderColor = null;
    private boolean borderInsideAsWell = false;
    private boolean pushed = false;
    private boolean isOver = false;
    private Color pushDownBorderColor;
    private Color pushUpBorderColor;

    private static final int MARGIN = 2;
    private static final int SPACE = 2;
    
    private Event<MouseEvent> clickEvent = new Event<MouseEvent>();
    
    public void addClickListener(Event.Listener<MouseEvent> listener) {
        clickEvent.addListener(listener);
    }
    public void removeClickListener(Event.Listener<MouseEvent> listener) {
        clickEvent.removeListener(listener);
    }
    
    public void setBorder(int borderSize, boolean borderInsideAsWell, Color color) {
        borderColor = color;
        int diff = (borderSize + (borderInsideAsWell ? 1 : 0)) - (this.borderSize + (this.borderInsideAsWell ? 1 : 0));
        this.borderInsideAsWell = borderInsideAsWell;
        if (diff == 0) return;
        this.borderSize = borderSize;
        Point size = getSize();
        size.x += diff * 2;
        size.y += diff * 2;
        setSize(size);
    }
    
    private Color getBorderColor() {
    	if (!isEnabled()) return new Color(getDisplay(), 128, 128, 128);
    	if (borderColor != null) return borderColor;
    	Color bg = getBackground();
    	int r = bg.getRed();
    	int g = bg.getGreen();
    	int b = bg.getBlue();
    	if (r > 35) r -= 35; else r = 0;
    	if (g > 35) g -= 35; else g = 0;
    	if (b > 35) b -= 35; else b = 0;
    	return new Color(getDisplay(), r, g, b);
    }
    
    public void paintControl(PaintEvent e) {
        Point size = getSize();
        if (borderSize > 0) {
            e.gc.setLineWidth(borderSize);
            e.gc.setForeground(getBorderColor());
            e.gc.drawRectangle(0, 0, size.x - 1, size.y - 1);
        }
        if (pushed) {
            e.gc.setLineWidth(1);
            e.gc.setForeground(pushDownBorderColor);
            e.gc.drawLine(borderSize, borderSize, size.x - 1 - borderSize, borderSize);
            e.gc.drawLine(borderSize, borderSize, borderSize, size.y - 2 - borderSize);
            e.gc.setForeground(pushUpBorderColor);
            e.gc.drawLine(size.x - 1 - borderSize, borderSize + 1, size.x - 1 - borderSize, size.y - 1 - borderSize);
            e.gc.drawLine(borderSize, size.y - 1 - borderSize, size.x - 1 - borderSize, size.y - 1 - borderSize);
        } else if (isOver) {
            e.gc.setLineWidth(1);
            e.gc.setForeground(pushUpBorderColor);
            e.gc.drawLine(borderSize, borderSize, size.x - 1 - borderSize, borderSize);
            e.gc.drawLine(borderSize, borderSize, borderSize, size.y - 2 - borderSize);
            e.gc.setForeground(pushDownBorderColor);
            e.gc.drawLine(size.x - 1 - borderSize, borderSize + 1, size.x - 1 - borderSize, size.y - 1 - borderSize);
            e.gc.drawLine(borderSize, size.y - 1 - borderSize, size.x - 1 - borderSize, size.y - 1 - borderSize);
        } else {
        	if (borderInsideAsWell) {
	        	e.gc.setForeground(getBorderColor());
	            e.gc.setLineWidth(1);
	            e.gc.drawLine(borderSize, borderSize, size.x - 1 - borderSize, borderSize);
	            e.gc.drawLine(borderSize, borderSize, borderSize, size.y - 2 - borderSize);
	            e.gc.drawLine(size.x - 1 - borderSize, borderSize + 1, size.x - 1 - borderSize, size.y - 1 - borderSize);
	            e.gc.drawLine(borderSize, size.y - 1 - borderSize, size.x - 1 - borderSize, size.y - 1 - borderSize);
        	}
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        Point size = super.computeSize(wHint, hHint, changed);
        int border = borderSize;
        if (borderInsideAsWell) border++;
        size.x += border * 2;
        size.y += border * 2;
        return size;
    }
    
    private class LabelListener implements MouseListener, MouseTrackListener {

        public LabelListener(Control control) {
            control.addMouseListener(this);
            control.addMouseTrackListener(this);
        }
        
        public void mouseDoubleClick(MouseEvent e) {
            // nothing
        }
        public void mouseDown(MouseEvent e) {
            pushed = true; redraw();
        }
        public void mouseUp(MouseEvent e) {
            pushed = false; redraw();
            clickEvent.fire(e);
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
    
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        icon.setBackground(color);
        text.setBackground(color);
    }
    
    public void setImage(Image image) {
        icon.setImage(image);
    }
    public void setText(String text) {
        this.text.setText(text);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
    	super.setEnabled(enabled);
    	icon.setEnabled(enabled);
    	text.setEnabled(enabled);
    }
}
