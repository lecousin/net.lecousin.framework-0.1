package net.lecousin.framework.ui.eclipse.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

public class OnDemandLayoutAndCreate {

	public OnDemandLayoutAndCreate(Composite panel) {
//		this.panel = panel;
		panel.addPaintListener(painter);
	}
	
//	private Composite panel;
	private PaintListener painter = new PaintListener() {
		public void paintControl(PaintEvent e) {
			update(new RectangleInt(e.x, e.y, e.width, e.height));
		}
	};
	
	public static interface ControlsContainer {
		public Control[] getControls();
		public Control[] createControls();
	}
	
	private class Child {
		Child(ControlsContainer container, int controlIndex) {
			this.container = container;
			this.controlIndex = controlIndex;
			Control[] controls = container.getControls();
			if (controls != null) {
				Rectangle r = controls[controlIndex].getBounds();
				bounds = new RectangleInt(r.x, r.y, r.width, r.height);
				controls[controlIndex].addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						List<Child> m = updates.get(Child.this.container);
						if (m != null) {
							m.remove(Child.this);
							if (m.isEmpty())
								updates.remove(Child.this.container);
						}
					}
				});
			}
		}
		ControlsContainer container;
		int controlIndex;
		RectangleInt bounds = null;
		PointInt newLocation = null;
		PointInt newSize = null;
		void setBounds(int x, int y, int w, int h) {
			Control[] controls = container.getControls();
			if (controls == null)
				controls = container.createControls();
			controls[controlIndex].setBounds(x, y, w, h);
			bounds = new RectangleInt(x, y, w, h);
		}
	}

	private Map<ControlsContainer,List<Child>> updates = new HashMap<ControlsContainer,List<Child>>();
	
	private Child getChild(ControlsContainer container, int index) {
		List<Child> list = updates.get(container);
		if (list == null) return null;
		for (Child c : list)
			if (c.controlIndex == index)
				return c;
		return null;
	}
	
	public void setLocation(ControlsContainer container, int controlIndex, int x, int y) { setLocation(container, controlIndex, new PointInt(x, y)); }
	public void setLocation(ControlsContainer container, int controlIndex, PointInt location) {
		Child child = getChild(container, controlIndex);
		if (child != null) {
			child.newLocation = location;
			return;
		}
		child = new Child(container, controlIndex);
		child.newLocation = location;
		List<Child> list = updates.get(container);
		if (list == null) {
			list = new LinkedList<Child>();
			updates.put(container, list);
		}
		list.add(child);
	}
	public void setSize(ControlsContainer container, int controlIndex, int w, int h) { setSize(container, controlIndex, new PointInt(w, h)); }
	public void setSize(ControlsContainer container, int controlIndex, PointInt size) {
		Child child = getChild(container, controlIndex);
		if (child != null) {
			child.newSize = size;
			return;
		}
		child = new Child(container, controlIndex);
		child.newSize = size;
		List<Child> list = updates.get(container);
		if (list == null) {
			list = new LinkedList<Child>();
			updates.put(container, list);
		}
		list.add(child);
	}
	public void setBounds(ControlsContainer container, int controlIndex, int x, int y, int w, int h) { setBounds(container, controlIndex, new PointInt(x, y), new PointInt(w, h)); }
	public void setBounds(ControlsContainer container, int controlIndex, PointInt location, PointInt size) {
		Child child = getChild(container, controlIndex);
		if (child != null) {
			child.newLocation = location;
			child.newSize = size;
			return;
		}
		child = new Child(container, controlIndex);
		child.newLocation = location;
		child.newSize = size;
		List<Child> list = updates.get(container);
		if (list == null) {
			list = new LinkedList<Child>();
			updates.put(container, list);
		}
		list.add(child);
	}
	
	public void update(RectangleInt r) {
		List<ControlsContainer> toRemove = new LinkedList<ControlsContainer>();
		for (Map.Entry<ControlsContainer, List<Child>> e : updates.entrySet()) {
			for (Iterator<Child> it = e.getValue().iterator(); it.hasNext(); ) {
				Child child = it.next();
				boolean needUpdate = child.bounds != null && r.getIntersection(child.bounds) != null;
				RectangleInt nr = null;
				if (!needUpdate) {
					nr = getNewBounds(child);
					needUpdate = r.getIntersection(nr) != null;
				}
				if (!needUpdate) continue;
				it.remove();
				if (nr == null) nr = getNewBounds(child);
				child.setBounds(nr.x, nr.y, nr.width, nr.height);
			}
			if (e.getValue().isEmpty())
				toRemove.add(e.getKey());
		}
		for (ControlsContainer c : toRemove)
			updates.remove(c);
	}
	
	private RectangleInt getNewBounds(Child child) {
		return new RectangleInt(
				child.newLocation != null ? child.newLocation.x : child.bounds != null ? child.bounds.x : 0,
				child.newLocation != null ? child.newLocation.y : child.bounds != null ? child.bounds.y : 0,
				child.newSize != null ? child.newSize.x : child.bounds != null ? child.bounds.width : 0,
				child.newSize != null ? child.newSize.y : child.bounds != null ? child.bounds.height : 0
				);
	}
	
	public PointInt getLocation(ControlsContainer container, int controlIndex) {
		Child child = getChild(container, controlIndex);
		if (child != null) {
			if (child.newLocation != null)
				return child.newLocation;
			if (child.bounds != null)
				return new PointInt(child.bounds.x, child.bounds.y);
			return new PointInt(0,0);
		}
		Control[] controls = container.getControls();
		if (controls == null) return new PointInt(0,0);
		Point pt = controls[controlIndex].getLocation();
		return new PointInt(pt.x, pt.y);
	}
	public PointInt getSize(ControlsContainer container, int controlIndex) {
		Child child = getChild(container, controlIndex);
		if (child != null) {
			if (child.newSize != null)
				return child.newSize;
			if (child.bounds != null)
				return new PointInt(child.bounds.width, child.bounds.height);
			return new PointInt(0,0);
		}
		Control[] controls = container.getControls();
		if (controls == null) return new PointInt(0,0);
		Point pt = controls[controlIndex].getSize();
		return new PointInt(pt.x, pt.y);
	}
	public RectangleInt getBounds(ControlsContainer container, int controlIndex) {
		Child child = getChild(container, controlIndex);
		if (child != null)
			return getNewBounds(child);
		Control[] controls = container.getControls();
		if (controls == null) return new RectangleInt(0,0,0,0);
		Rectangle r = controls[controlIndex].getBounds();
		return new RectangleInt(r.x, r.y, r.width, r.height);
	}
	
	public void reset(ControlsContainer container) {
		List<Child> children = updates.remove(container);
		if (children == null) return;
		if (container.getControls() == null) return;
		for (Child child : children) {
			RectangleInt nr = getNewBounds(child);
			child.setBounds(nr.x, nr.y, nr.width, nr.height);
		}
	}
	public void remove(ControlsContainer container) {
		updates.remove(container);
	}
}
