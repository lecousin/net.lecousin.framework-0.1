package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.math.RangeInteger;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LCGrid extends Composite {

	public LCGrid(Composite parent, int numColumns, int borderX, int borderY, Color borderColor) {
		super(parent, SWT.NONE);
		this.borderColor = borderColor;
		setBackground(parent.getBackground());
		layout = UIUtil.gridLayout(this, numColumns, borderY, borderX, borderX, borderY);
		addPaintListener(new BorderPainter());
	}
	
	private Color borderColor;
	private GridLayout layout;
	
	public void fillCell(Control c, Color bgColor) {
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		c.setLayoutData(gd);
		c.setBackground(bgColor);
	}
	public Composite newCell(int hSpace, int vSpace, Color bgColor) {
		Composite cell = UIUtil.newGridComposite(this, hSpace, vSpace, 1);
		fillCell(cell, bgColor);
		return cell;
	}
	public Composite newEmptyCell(Color bgColor) {
		Composite cell = new Composite(this, SWT.NONE) {
			@Override
			public Point computeSize(int hint, int hint2, boolean changed) {
				return new Point(hint == SWT.DEFAULT ? 1 : hint, hint2 == SWT.DEFAULT ? 1 : hint2);
			}
		};
		fillCell(cell, bgColor);
		return cell;
	}
	
	private class BorderPainter implements PaintListener {
		public void paintControl(PaintEvent e) {
			Point size = getSize();
			Control[] children = getChildren();
			
			int nbRows;
			if (children.length == 0)
				nbRows = 1;
			else {
				nbRows = children.length/layout.numColumns;
				if ((children.length%layout.numColumns)!=0) nbRows++;
			}
			
			int rowHeight[] = new int[nbRows];
			int colWidth[] = new int[layout.numColumns];
			for (int i = 0; i < layout.numColumns; ++i) colWidth[i] = 0;
			
			for (int y = 0; y < nbRows; ++y) {
				int h = 0;
				for (int x = 0; x < layout.numColumns; ++x) {
					int i = y*layout.numColumns+x;
					if (i >= children.length) break;
					Point p = children[i].getSize();
					if (p.y > h) h = p.y;
					if (p.x > colWidth[x]) colWidth[x] = p.x;
				}
				rowHeight[y] = h;
			}
			e.gc.setForeground(borderColor);
			paintVerticalBorders(e, size, children, colWidth);
			paintHorizontalBorders(e, size, children, nbRows, rowHeight);
		}
		private void paintVerticalBorders(PaintEvent e, Point size, Control[] children, int colWidth[]) {
			e.gc.setLineWidth(layout.horizontalSpacing);
			int x = 0;
			int y1 = e.y > 0 ? e.y : 0;
			int y2 = e.y+e.height-1 < size.y-1 ? e.y+e.height-1 : size.y-1;
			for (int i = 0; i < layout.numColumns+1; ++i) {
				if (new RangeInteger(x,x+layout.horizontalSpacing-1).intersect(new RangeInteger(e.x, e.x+e.width-1)) != null)
					e.gc.drawLine(x, y1, x, y2);
				if (i < colWidth.length)
					x += colWidth[i] + layout.horizontalSpacing;
			}
		}
		private void paintHorizontalBorders(PaintEvent e, Point size, Control[] children, int nbRows, int rowHeight[]) {
			e.gc.setLineWidth(layout.verticalSpacing);
			int y = 0;
			int x1 = e.x > 0 ? e.x : 0;
			int x2 = e.x+e.width-1 < size.x-1 ? e.x+e.width-1 : size.x-1;
			for (int i = 0; i < nbRows+1; ++i) {
				if (new RangeInteger(y,y+layout.verticalSpacing-1).intersect(new RangeInteger(e.y, e.y+e.height-1)) != null)
					e.gc.drawLine(x1, y, x2, y);
				if (i < rowHeight.length)
					y += rowHeight[i] + layout.verticalSpacing;
			}
		}
	}
}
