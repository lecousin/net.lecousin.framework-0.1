package net.lecousin.framework.ui.eclipse.control.text.lcml.internal;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;
import net.lecousin.framework.ui.eclipse.graphics.CursorUtil;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class Link extends TextSection {

	public Link(String text, String href) {
		this.text = text;
		this.href = href;
	}
	
	private String text;
	private String href;
	private Event<Object> selected = new Event<Object>();
	
	private Color normalColor = ColorUtil.get(0, 0, 255);
	
	public String getHRef() { return href; }
	
	public void addLinkListener(Runnable listener) {
		selected.addFireListener(listener);
	}
	
	@Override
	public String getText() {
		return text;
	}
	@Override
	public Font getFont(Composite parent) {
		return parent.getFont();
	}
	@Override
	public void configureLabel(Label label) {
		label.setForeground(normalColor);
		label.setCursor(CursorUtil.getHand());
		label.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Point size = ((Label)e.widget).getSize();
				e.gc.drawLine(0, size.y-1, size.x-1, size.y-1);
			}
		});
		label.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
			}
			public void mouseUp(MouseEvent e) {
				if (e.button == 1) {
					selected.fire(null);
				}
			}
		});
		label.addMouseTrackListener(new MouseTrackListener() {
			public void mouseEnter(MouseEvent e) {
			}
			public void mouseExit(MouseEvent e) {
			}
			public void mouseHover(MouseEvent e) {
			}
		});
	}
}
