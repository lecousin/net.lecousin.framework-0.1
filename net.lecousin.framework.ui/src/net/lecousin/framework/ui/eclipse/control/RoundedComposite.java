package net.lecousin.framework.ui.eclipse.control;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class RoundedComposite extends Composite {

	public RoundedComposite(Composite parent, int style, Color background, Color roundColor) {
		super(parent, style);
		this.background = background;
		this.roundColor = roundColor;
		
		setBackground(background);
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				DrawingUtil.drawRoundedRectangle(e.gc, e.display, new Point(0, 0), getSize(), RoundedComposite.this.background, RoundedComposite.this.roundColor);
			}
		});
	}
	
	private Color background, roundColor;
}
