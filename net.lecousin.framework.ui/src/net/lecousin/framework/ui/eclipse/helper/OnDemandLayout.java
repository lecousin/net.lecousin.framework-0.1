package net.lecousin.framework.ui.eclipse.helper;

import java.util.Iterator;

import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.collections.SelfMapLinkedList;
import net.lecousin.framework.geometry.PointInt;
import net.lecousin.framework.geometry.RectangleInt;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class OnDemandLayout {

	public OnDemandLayout(Composite panel) {
//		this.panel = panel;
		panel.addPaintListener(painter);
	}
	
//	private Composite panel;
	private PaintListener painter = new PaintListener() {
		public void paintControl(PaintEvent e) {
			update(new RectangleInt(e.x, e.y, e.width, e.height));
		}
	};
	
	private class Child implements SelfMap.Entry<Control> {
		Child(Control control) {
			this.control = control;
			Rectangle r = control.getBounds();
			bounds = new RectangleInt(r.x, r.y, r.width, r.height);
			control.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					updates.removeEntry(Child.this);
				}
			});
		}
		Control control;
		RectangleInt bounds;
		PointInt newLocation = null;
		PointInt newSize = null;
		public Control getHashObject() { return control; }
	}

	private SelfMap<Control,Child> updates = new SelfMapLinkedList<Control,Child>();
	
	public void setLocation(Control control, int x, int y) { setLocation(control, new PointInt(x, y)); }
	public void setLocation(Control control, PointInt location) {
		Child child = updates.get(control);
		if (child != null) {
			child.newLocation = location;
			return;
		}
		child = new Child(control);
		child.newLocation = location;
		updates.add(child);
	}
	public void setSize(Control control, int w, int h) { setSize(control, new PointInt(w, h)); }
	public void setSize(Control control, PointInt size) {
		Child child = updates.get(control);
		if (child != null) {
			child.newSize = size;
			return;
		}
		child = new Child(control);
		child.newSize = size;
		updates.add(child);
	}
	public void setBounds(Control control, int x, int y, int w, int h) { setBounds(control, new PointInt(x, y), new PointInt(w, h)); }
	public void setBounds(Control control, PointInt location, PointInt size) {
		Child child = updates.get(control);
		if (child != null) {
			child.newLocation = location;
			child.newSize = size;
			return;
		}
		child = new Child(control);
		child.newLocation = location;
		child.newSize = size;
		updates.add(child);
	}
	
	public void update(RectangleInt r) {
		for (Iterator<Child> it = updates.iterator(); it.hasNext(); ) {
			Child child = it.next();
			boolean needUpdate = r.getIntersection(child.bounds) != null;
			RectangleInt nr = null;
			if (!needUpdate) {
				nr = getNewBounds(child);
				needUpdate = r.getIntersection(nr) != null;
			}
			if (!needUpdate) continue;
			it.remove();
			if (nr == null) nr = getNewBounds(child);
			child.control.setBounds(nr.x, nr.y, nr.width, nr.height);
		}
	}
	
	private RectangleInt getNewBounds(Child child) {
		return new RectangleInt(
				child.newLocation != null ? child.newLocation.x : child.bounds.x,
				child.newLocation != null ? child.newLocation.y : child.bounds.y,
				child.newSize != null ? child.newSize.x : child.bounds.width,
				child.newSize != null ? child.newSize.y : child.bounds.height
				);
	}
	
	public PointInt getLocation(Control control) {
		Child child = updates.get(control);
		if (child != null) {
			if (child.newLocation != null)
				return child.newLocation;
			return new PointInt(child.bounds.x, child.bounds.y);
		}
		Point pt = control.getLocation();
		return new PointInt(pt.x, pt.y);
	}
	public PointInt getSize(Control control) {
		Child child = updates.get(control);
		if (child != null) {
			if (child.newSize != null)
				return child.newSize;
			return new PointInt(child.bounds.width, child.bounds.height);
		}
		Point pt = control.getSize();
		return new PointInt(pt.x, pt.y);
	}
}
