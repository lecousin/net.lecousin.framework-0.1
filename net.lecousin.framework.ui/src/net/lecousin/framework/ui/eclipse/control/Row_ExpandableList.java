package net.lecousin.framework.ui.eclipse.control;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Represents a list of controls in a row, with the specificity that according to the size of this control,
 * some items may be hidden. Only the first controls are displayed (limited by the size of this control itself),
 * and in case there is not enough place, an optional control is display to allow the user to access to the 
 * complete list.
 */
public class Row_ExpandableList extends Composite {

	public Row_ExpandableList(Composite parent) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.marginHeight = layout.marginWidth = layout.marginBottom = layout.marginTop = 0;
		layout.wrap = false;
		layout.spacing = spacing;
		setLayout(layout);
		addControlListener(new SizeListener());
	}
	
	private Control header = null;
	private Control footer = null;
	private static int spacing = 3;
	
	/** the header is always shown, whatever the size of the control */
	public void setHeader(Control control) {
		header = control;
		header.moveAbove(null);
	}

	/** the footer is always shown, whatever the size of the control */
	public void setFooter(Control control) {
		footer = control;
	}
	
	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
//		Point size = new Point(hint, hint2);
//		RowLayout l = (RowLayout)getLayout();
//		if (hint == SWT.DEFAULT || hint2 == SWT.DEFAULT) {
//			int x = 0;
//			int y = 0;
//			for (Control c : getChildren()) {
//				Point csize = c.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
//				if (x > 0) x += l.spacing;
//				x += csize.x;
//				if (csize.y > y) y = csize.y;
//			}
//			if (hint == SWT.DEFAULT) size.x = x;
//			if (hint2 == SWT.DEFAULT) size.y = y;
//		}
//		return size;
		return super.computeSize(hint, hint2, changed);
	}
	
	private class SizeListener implements ControlListener {
		public void controlMoved(ControlEvent e) {
		}
		public void controlResized(ControlEvent e) {
			Point size = getSize();
			int used = 0;
			if (header != null) used += header.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + spacing;
			if (footer != null) used += footer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + spacing;
			boolean changed = false;
			boolean hide = false;
			for (Control c : getChildren()) {
				if (c == header) continue;
				if (c == footer) continue;
				Point csize = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				RowData rd = (RowData)c.getLayoutData();
				if (rd == null) {
					rd = new RowData();
					c.setLayoutData(rd);
				}
				if (used + csize.x > size.x)
					hide = true;
				else
					used += csize.x + spacing;
				if (hide) {
					if (!rd.exclude) {
						rd.exclude = true;
						c.setVisible(false);
						changed = true;
					}
				} else {
					if (rd.exclude) {
						rd.exclude = false;
						c.setVisible(true);
						changed = true;
					}
				}
			}
			if (changed) layout(true, true);
		}
	}
	
}
