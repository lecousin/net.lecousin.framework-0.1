package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ImageButton extends Label {

	public ImageButton(Composite parent) {
		super(parent, SWT.NONE);
		Mouse mouse = new Mouse();
		addMouseTrackListener(mouse);
		addMouseListener(mouse);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				normalImage = null;
				hoverImage = null;
				click.free(); click = null;
			}
		});
	}
	
	private Image normalImage, hoverImage;
	private Event<MouseEvent> click = new Event<MouseEvent>();
	
	@Override
	protected void checkSubclass() {
	}
	
	public void setImage(Image img) {
		if (getImage() == normalImage)
			update(img);
		normalImage = img;
		if (getImage() == null)
			update(img);
	}
	public void setHoverImage(Image img) {
		if (getImage() == hoverImage)
			update(img);
		hoverImage = img;
	}
	
	public void addClickListener(Listener<MouseEvent> listener) {
		click.addListener(listener);
	}
	
	private class Mouse implements MouseTrackListener, MouseListener {
		public void mouseEnter(MouseEvent e) {
			update(hoverImage);
			redraw();
		}
		public void mouseExit(MouseEvent e) {
			update(normalImage);
			redraw();
		}
		public void mouseHover(MouseEvent e) {
		}
		public void mouseDoubleClick(MouseEvent e) {
		}
		public void mouseDown(MouseEvent e) {
		}
		public void mouseUp(MouseEvent e) {
			if (e.button == 1)
				click.fire(e);
		}
	}
	
	private void update(Image img) {
		super.setImage(img);
	}
}
