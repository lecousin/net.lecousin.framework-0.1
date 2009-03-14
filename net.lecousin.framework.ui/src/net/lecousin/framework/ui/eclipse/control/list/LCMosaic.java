package net.lecousin.framework.ui.eclipse.control.list;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.lang.MyBoolean;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.event.DragSourceListenerWithData;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class LCMosaic<T> implements LCViewer<T,Composite> {

	public LCMosaic(Composite parent, LCContentProvider<T> contentProvider, MosaicProvider<T> mosaicProvider, MosaicConfig config) {
		this.contentProvider = contentProvider;
		this.mosaicProvider = mosaicProvider;
		this.config = config;
		scroll = new ScrolledComposite(parent, SWT.V_SCROLL);
		panel = new Composite(scroll, SWT.NONE);
		scroll.setContent(panel);
		scroll.setExpandHorizontal(true);
		scroll.getVerticalBar().setIncrement(50);
//		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
//		layout.wrap = true;
//		layout.fill = true;
//		panel.setLayout(layout);
		layout = new MosaicLayout();
		scroll.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				updatePanel();
				Point size = scroll.getSize();
				int h = size.y*2/3;
				if (h == 0) h = 1;
				scroll.getVerticalBar().setPageIncrement(h);
			}
		});
		refresh(true);
		panel.addKeyListener(keyListener);
		panel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				for (Event<T> ev : addElementEvents)
					ev.removeListener(addElementListener);
				for (Event<T> ev : removeElementEvents)
					ev.removeListener(removeElementListener);
				for (Event<T> ev : elementChangedEvents)
					ev.removeListener(elementChangedListener);
				for (Event<?> ev : contentChangedEvents)
					ev.removeFireListener(contentChangedListener);
			}
		});
	}
	
	public static interface MosaicProvider<T> {
		public Control getImageControl(Composite parent, T element);
		public Control refreshImageControl(T element, Control current);
		public String getText(T element);
	}
	
	public static class MosaicConfig {
		public boolean multiSelection = true;
	}
	
	private MosaicLayout layout;
	private ScrolledComposite scroll;
	private Composite panel;
	private int elementWidth = 150;
	private Comparator<T> comparator = null;
	private Sort sortDirection = null;
	private List<MosaicItem> selection = new LinkedList<MosaicItem>();
	
	private LCContentProvider<T> contentProvider;
	private MosaicProvider<T> mosaicProvider;
	private MosaicConfig config;
	
	private List<Event<T>> addElementEvents = new LinkedList<Event<T>>();
	private Listener<T> addElementListener = new Listener<T>() { public void fire(T element) { createRow(element); updatePanel(); } };
	private List<Event<T>> removeElementEvents = new LinkedList<Event<T>>();
	private Listener<T> removeElementListener = new Listener<T>() { public void fire(T element) { MosaicItem i = getItem(element); if (i != null) { removeRow(i); updatePanel(); } } };
	private List<Event<T>> elementChangedEvents = new LinkedList<Event<T>>();
	private Listener<T> elementChangedListener = new Listener<T>() { public void fire(T element) { MosaicItem i = getItem(element); if (i != null) { updateRow(i); updatePanel(); } } };
	private List<Event<?>> contentChangedEvents = new LinkedList<Event<?>>();
	private Runnable contentChangedListener = new Runnable() { public void run() { refresh(true); } };
	
	private Event<List<T>> selectionChanged = new Event<List<T>>();
	private Event<T> doubleClick = new Event<T>();
	private Event<T> rightClick = new Event<T>();
	
	private List<KeyListener> keyListeners = new LinkedList<KeyListener>();
	private KeyListener keyListener = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if (e.keyCode == 'a' || e.keyCode == 'A') {
				if ((e.stateMask & SWT.CTRL) != 0) {
					selectAll();
					e.doit = false;
				}
			}
			if (e.doit)
				for (KeyListener listener : keyListeners)
					listener.keyPressed(e);
		}
		public void keyReleased(KeyEvent e) {
			for (KeyListener listener : keyListeners)
				listener.keyReleased(e);
		}
	};
	public void addKeyListener(KeyListener listener) {
		if (keyListeners.contains(listener)) return;
		keyListeners.add(listener);
	}
		
	
	public Composite getControl() { return scroll; }
	
	public void setSorting(Comparator<T> comparator, Sort direction) {
		this.comparator = comparator;
		this.sortDirection = direction;
	}
	
	public enum Sort { ASCENDING, DESCENDING; }
	private class MosaicItem extends Composite implements MouseListener {
		MosaicItem(T element, Control position) {
			super(panel, SWT.NONE);
			setData(element);
			moveBelow(position);
			UIUtil.gridLayout(this, 1);
			label = new Label(this, SWT.WRAP);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			gd.widthHint = elementWidth;
			label.setLayoutData(gd);
			label.setAlignment(SWT.CENTER);
			addMouseListener(this);
			label.addMouseListener(this);
			UIControlUtil.traverseKeyEvents(this, true);
			updateContent();
			setSelected(false);
			addDragSupport(this);
		}
		private Control imageControl;
		private Label label;
		private boolean selected;
		@SuppressWarnings("unchecked")
		public T getData() { return (T)super.getData(); }
		public void updateContent() {
			label.setText(mosaicProvider.getText(getData()));
			Control c;
			if (imageControl == null)
				c = mosaicProvider.getImageControl(this, getData());
			else
				c = mosaicProvider.refreshImageControl(getData(), imageControl);
			if (c != imageControl) {
				if (imageControl != null)
					imageControl.dispose();
				c.moveAbove(label);
				imageControl = c;
				GridData gd = new GridData();
				gd.horizontalAlignment = SWT.CENTER;
				gd.grabExcessVerticalSpace = true;
				gd.verticalAlignment = SWT.CENTER;
				imageControl.setLayoutData(gd);
				UIControlUtil.recursiveMouseListener(imageControl, this, false);
				UIControlUtil.traverseKeyEvents(imageControl, true);
			}
		}
		public void mouseDoubleClick(MouseEvent e) {
			panel.setFocus();
			doubleClick.fire(getData());
		}
		public void mouseDown(MouseEvent e) {
			panel.setFocus();
			if (e.button == 1) {
				if ((e.stateMask & SWT.CTRL) != 0)
					switchSelection(this);
				else
					setSelection(this);
			} else if (e.button == 3) {
			}
		}
		public void mouseUp(MouseEvent e) {
			if (e.button == 1) {
			} else if (e.button == 3) {
				rightClick.fire(getData());
			}
		}
		public void setSelected(boolean sel) {
			selected = sel;
			Color col = sel ? ColorUtil.get(150, 150, 255) : ColorUtil.getWhite();
			setBackground(col);
			label.setBackground(col);
			UIControlUtil.setBackground(imageControl, col);
			redraw();
		}
		public boolean getSelected() { return selected; }
	}
	
	private MyBoolean isRefreshing = new MyBoolean(false);
	private boolean needNewRefresh = false;
	private boolean newRefreshBackground = false;
	
	public void refresh(boolean background) {
		synchronized (isRefreshing) {
			if (panel.isDisposed()) return;
			if (isRefreshing.get()) {
				needNewRefresh = true;
				newRefreshBackground = background;
				return;
			}
			isRefreshing.set(true);
		}
		try {
			ArrayList<Control> list = CollectionUtil.list(panel.getChildren());
			LinkedList<T> toCreate = new LinkedList<T>();
			LinkedList<MosaicItem> toUpdate = new LinkedList<MosaicItem>();
			for (T element : contentProvider.getElements()) {
				if (element == null) continue;
				Control item = null;
				for (Iterator<Control> it = list.iterator(); it.hasNext(); ) {
					Control i = it.next();
					if (i.getData().equals(element)) {
						item = i;
						it.remove();
						break;
					}
				}
				if (item == null)
					toCreate.add(element);
				else
					toUpdate.add((MosaicItem)item);
			}
			if (panel.isDisposed()) {
				endRefresh();
				return;
			}
			for (Control i : list)
				removeRow((MosaicItem)i);
			if (panel.isDisposed()) {
				endRefresh();
				return;
			}
			if (background) {
				updatePanel();
				panel.getDisplay().asyncExec(new RunnableWithData<Pair<LinkedList<T>,LinkedList<MosaicItem>>>(new Pair<LinkedList<T>,LinkedList<MosaicItem>>(toCreate,toUpdate)) {
					public void run() {
						try {
							if (panel.isDisposed()) {
								endRefresh();
								return;
							}
							UIUtil.runPendingEvents(panel.getDisplay());
							if (panel.isDisposed()) {
								endRefresh();
								return;
							}
							if (!data().getValue2().isEmpty())
								for (int i = 0; i < 10 && !data().getValue2().isEmpty(); ++i)
									updateRow(data().getValue2().removeFirst());
							else
								for (int i = 0; i < 10 && !data().getValue1().isEmpty(); ++i)
									createRow(data().getValue1().removeFirst());
							updatePanel();
							if (!data().getValue2().isEmpty() || !data().getValue1().isEmpty())
								panel.getDisplay().asyncExec(this);
							else
								endRefresh();
						} catch (Throwable t) {
							if (Log.error(this))
								Log.error(this, "Error while updating table", t);
							endRefresh();
						}
					}
				});
			} else {
				for (MosaicItem item : toUpdate)
					updateRow(item);
				for (T element : toCreate)
					createRow(element);
				updatePanel();
				endRefresh();
			}
		} catch (Throwable t) {
			if (Log.error(this))
				Log.error(this, "Error while updating table", t);
			endRefresh();
		}
	}
	private void endRefresh() {
		synchronized (isRefreshing) {
			isRefreshing.set(false);
		}
		if (needNewRefresh) {
			needNewRefresh = false;
			refresh(newRefreshBackground);
		}
	}
	
	public void refresh(T element) {
		MosaicItem item = getItem(element);
		if (item != null) updateRow(item);
	}
	
	private MosaicItem getItem(T element) {
		for (Control i : panel.getChildren())
			if (i.getData().equals(element))
				return (MosaicItem)i;
		return null;
	}
	
	private void createRow(T element) {
		Control position = getItemToInsert(element);
		MosaicItem item = new MosaicItem(element, position);
		layout.addedItem(item);
	}
	private void removeRow(MosaicItem item) {
		layout.removedItem(item);
		item.dispose();
	}
	private void updateRow(MosaicItem item) {
		layout.changedItem(item);
		item.updateContent();
	}
	
	private Control getItemToInsert(T element) {
		return getItemToInsert(element, panel.getChildren(), 0, panel.getChildren().length-1);
	}
	@SuppressWarnings("unchecked")
	private Control getItemToInsert(T element, Control[] items, int start, int end) {
		if (comparator == null || sortDirection == null) return null;
		if (end < start) return items[start];
		if (start > end) return end == items.length-1 ? null : items[end];
		int pivotIndex = (end-start)/2 + start;
		T pivot = (T)items[pivotIndex].getData();
		int pivotCompare = comparator.compare(element, pivot);
		if (pivotCompare == 0) return items[pivotIndex];
		if ((pivotCompare < 0 && sortDirection.equals(Sort.ASCENDING)) || (pivotCompare > 0 && sortDirection.equals(Sort.DESCENDING)))
			return getItemToInsert(element, items, start, pivotIndex-1);
		return getItemToInsert(element, items, pivotIndex+1, end);
	}
	
	public void setLayoutData(Object data) {
		scroll.setLayoutData(data);
	}
	
	public void addAddElementEvent(Event<T> event) {
		addElementEvents.add(event);
		event.addListener(addElementListener);
	}
	public void addRemoveElementEvent(Event<T> event) {
		removeElementEvents.add(event);
		event.addListener(removeElementListener);
	}
	public void addElementChangedEvent(Event<T> event) {
		elementChangedEvents.add(event);
		event.addListener(elementChangedListener);
	}
	public void addContentChangedEvent(Event<?> event) {
		contentChangedEvents.add(event);
		event.addFireListener(contentChangedListener);
	}
	public void addSelectionChangedListener(Listener<List<T>> listener) { selectionChanged.addListener(listener); }
	public void addDoubleClickListener(Listener<T> listener) { doubleClick.addListener(listener); }
	public void addRightClickListener(Listener<T> listener) { rightClick.addListener(listener); }
	
	private void updatePanel() {
//		panel.layout(false, false);
//        Point ns = panel.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
//        Point cs = panel.getSize();
//        if (!cs.equals(ns))
//        	panel.setSize(ns);
		layout.layout();
	}
	
	private void setSelection(MosaicItem sel) {
		for (MosaicItem i : selection)
			i.setSelected(false);
		selection.clear();
		if (sel != null)
			sel.setSelected(true);
		selection.add(sel);
		selectionChanged.fire(CollectionUtil.single_element_list(sel.getData()));
	}
	private void switchSelection(MosaicItem sel) {
		boolean selected = !sel.getSelected();
		if (selected && !config.multiSelection) {
			setSelection(sel);
			return;
		}
		sel.setSelected(selected);
		if (selected)
			selection.add(sel);
		else
			selection.remove(sel);
		List<T> event = new ArrayList<T>(selection.size());
		for (MosaicItem i : selection)
			event.add(i.getData());
		selectionChanged.fire(event);
	}
	public List<T> getSelection() {
		List<T> event = new ArrayList<T>(selection.size());
		for (MosaicItem i : selection)
			event.add(i.getData());
		return event;
	}
	public void selectAll() {
		if (!config.multiSelection) return;
		List<T> event = new LinkedList<T>();
		selection.clear();
		for (Control child : panel.getChildren())
			if (child instanceof LCMosaic<?>.MosaicItem) {
				MosaicItem i = (MosaicItem)child;
				selection.add(i);
				event.add(i.getData());
				i.setSelected(true);
			}
		selectionChanged.fire(event);
	}
	
	private class MosaicLayout {
		private int itemsByRow = 1;
		private int itemWidth = 1;
		private int totalWidth = 1;
		
		private List<MosaicItem> newItems = new LinkedList<MosaicItem>();
		private List<MosaicItem> removedItems = new LinkedList<MosaicItem>();
		private List<MosaicItem> changedItems = new LinkedList<MosaicItem>();
		
		private IdentityHashMap<MosaicItem,Integer> cacheItemRow = new IdentityHashMap<MosaicItem, Integer>();
		private List<Integer> cacheRowHeight = new LinkedList<Integer>();
		private Control[] cacheItems = new Control[0];
		
		private static final int horizontalSpacing = 5;
		private static final int verticalSpacing = 5;
		
		synchronized void addedItem(MosaicItem item) { newItems.add(item); }
		synchronized void changedItem(MosaicItem item) { if (!newItems.contains(item)) changedItems.add(item); }
		synchronized void removedItem(MosaicItem item) { 
			if (!newItems.remove(item)) {
				changedItems.remove(item);
				removedItems.add(item);
			}
		}
		
		synchronized void layout() {
			internal_layout();
			changedItems.clear();
			newItems.clear();
			removedItems.clear();
			updateSize();
		}
		private void updateSize() {
			int height = 0;
			for (int h : cacheRowHeight)
				height += h + verticalSpacing;
			Point size = panel.getSize();
			if (size.y != height) {
				size.y = height;
				panel.setSize(size);
			}
		}
		private int computeNbItemsByRow() {
			int nb = (totalWidth+horizontalSpacing)/(itemWidth+horizontalSpacing);
			if (nb == 0)
				nb = 1;
			return nb;
		}
		private void internal_layout() {
			int width = scroll.getSize().x;
			if (newItems.isEmpty() && removedItems.isEmpty()) {
				if (changedItems.isEmpty()) {
					if (width == totalWidth) return;
					totalWidth = width;
					int nb = computeNbItemsByRow();
					if (nb == itemsByRow)
						return;
					itemsByRow = nb;
					// itemsByRow changed => updateAll
					updateAll();
					return;
				} else {
					boolean itemWidthChanged = false;
					boolean rowHeightChanged[] = new boolean[cacheRowHeight.size()];
					for (int i = 0; i < rowHeightChanged.length; ++i)
						rowHeightChanged[i] = false;
					for (MosaicItem item : changedItems) {
						Point size = item.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
						if (size.x > itemWidth) {
							itemWidth = size.x;
							itemWidthChanged = true;
						}
						int row = cacheItemRow.get(item);
						int rowHeight = cacheRowHeight.get(row);
						if (size.y > rowHeight) {
							rowHeightChanged[row] = true;
							cacheRowHeight.set(row, size.y);
						}
					}
					totalWidth = width;
					int nb = computeNbItemsByRow();
					if (nb != itemsByRow) {
						updateAll();
						return;
					}
					if (!itemWidthChanged) {
						updateRowHeights(rowHeightChanged, false);
						return;
					}
					updateRowHeights(rowHeightChanged, true);
					return;
				}
			}
			// there are new or removed items
			boolean itemWidthChanged = false;
			boolean rowHeightChanged[] = new boolean[cacheRowHeight.size()];
			for (MosaicItem item : changedItems) {
				Point size = item.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				if (size.x > itemWidth) {
					itemWidth = size.x;
					itemWidthChanged = true;
				}
				int row = cacheItemRow.get(item);
				int rowHeight = cacheRowHeight.get(row);
				if (size.y > rowHeight) {
					rowHeightChanged[row] = true;
					cacheRowHeight.set(row, size.y);
				}
			}
			for (MosaicItem item : newItems) {
				Point size = item.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				if (size.x > itemWidth) {
					itemWidth = size.x;
					itemWidthChanged = true;
				}
			}
			for (MosaicItem item : removedItems) {
				cacheItemRow.remove(item);
			}
			
			totalWidth = width;
			int nb = computeNbItemsByRow();
			if (nb != itemsByRow) {
				itemsByRow = nb;
				updateAll();
				return;
			}
			
			if (changedItems.isEmpty()) {
				updateAllFromFirstNew(itemWidthChanged, null);
			} else {
				updateAllFromFirstNew(itemWidthChanged, rowHeightChanged);
			}
		}
		
		private void updateRowHeights(boolean[] rowHeightChanged, boolean updatePosOnRow) {
			int row = 0;
			int y = 0;
			boolean started = false;
			Control[] children = panel.getChildren();
			for (int index = 0; index < children.length; ) {
				if (!started) {
					if (rowHeightChanged[row])
						started = true;
					else {
						if (updatePosOnRow) {
							for (int col = 1; col < itemsByRow && index + col < children.length; ++col)
								children[index+col].setLocation(col*(itemWidth+horizontalSpacing), y);
						}
						index += itemsByRow;
						y += cacheRowHeight.get(row) + verticalSpacing;
						row++;
						continue;
					}
				}
				int height = cacheRowHeight.get(row); 
				for (int col = 0; col < itemsByRow && index + col < children.length; ++col) {
					MosaicItem item = (MosaicItem)children[index+col];
					if (rowHeightChanged[row]) {
						Point size = item.getSize();
						size.y = height;
						item.setSize(size);
					}
					Point loc = item.getLocation();
					loc.y = y;
					if (updatePosOnRow)
						loc.x = col*(itemWidth+horizontalSpacing);
					item.setLocation(loc);
				}
				index += itemsByRow;
				y += height + verticalSpacing;
				row++;
			}
		}
		
		private void updateAll() {
			updateAll(0, 0);
		}
		private void updateAllFromFirstNew(boolean itemWidthChanged, boolean[] rowHeightChanged) {
			Control[] children = panel.getChildren();
			int y = 0;
			for (int index = 0; index < children.length; ++index) {
				if (index == cacheItems.length || children[index] != cacheItems[index]) {
					updateAll(index, y);
					return;
				}
				if (itemWidthChanged) {
					int col = index % itemsByRow;
					if (col > 0) {
						int x = col*(itemWidth+horizontalSpacing);
						Point loc = children[index].getLocation();
						if (loc.x != x) {
							loc.x = x;
							children[index].setLocation(loc);
						}
					}
				}
				int row = index / itemsByRow;
				if (rowHeightChanged != null && rowHeightChanged[row]) {
					Point size = children[index].getSize();
					int height = cacheRowHeight.get(row);
					if (size.y != height) {
						size.y = height;
						children[index].setSize(size);
					}
				}
				if ((index % itemsByRow) == (itemsByRow-1)) {
					y += cacheRowHeight.get(row) + verticalSpacing;
				}
			}
		}

		private void updateAll(int index, int y) {
			int row = index / itemsByRow;
			cacheItems = panel.getChildren();
			while (row*itemsByRow < cacheItems.length) {
				int height = 0;
				for (int i = 0; i < itemsByRow && row*itemsByRow+i < cacheItems.length; ++i) {
					Point size = cacheItems[row*itemsByRow+i].computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
					if (size.y > height)
						height = size.y;
				}
				for (int i = 0; i < itemsByRow && row*itemsByRow+i < cacheItems.length; ++i) {
					Control c = cacheItems[row*itemsByRow+i];
					c.setLocation(i*(itemWidth+horizontalSpacing), y);
					c.setSize(itemWidth, height);
					cacheItemRow.put((MosaicItem)c, row);
				}
				if (cacheRowHeight.size() < row)
					cacheRowHeight.set(row, height);
				else
					cacheRowHeight.add(height);
				y += height + verticalSpacing;
				row++;
			}
			while (cacheRowHeight.size() > row)
				cacheRowHeight.remove(row);
		}
	}
	
	private List<Triple<Integer,Transfer[],DragListener<T>>> drags = new LinkedList<Triple<Integer,Transfer[],DragListener<T>>>();
	public void addDragSupport(int style, Transfer[] transfers, DragListener<T> listener) {
		drags.add(new Triple<Integer,Transfer[],DragListener<T>>(style, transfers, listener));
		for (Control c : panel.getChildren())
			addDragSupport((MosaicItem)c, style, transfers, listener);
	}
	
	private void addDragSupport(MosaicItem item) {
		for (Triple<Integer,Transfer[],DragListener<T>> t : drags)
			addDragSupport(item, t.getValue1(), t.getValue2(), t.getValue3());
	}
	
	private void addDragSupport(Control c, int style, Transfer[] transfers, DragListener<T> listener) {
		DragSource drag = new DragSource(c, style);
		drag.setTransfer(transfers);
		drag.addDragListener(new DragSourceListenerWithData<DragListener<T>>(listener) {
			public void dragStart(DragSourceEvent event) {
				data().dragStart(event, getSelection());
			}
			public void dragSetData(DragSourceEvent event) {
				data().dragSetData(event, getSelection());
			}
			public void dragFinished(DragSourceEvent event) {
				data().dragFinished(event, getSelection());
			}
		});
		if (c instanceof Composite)
			for (Control child : ((Composite)c).getChildren())
				addDragSupport(child, style, transfers, listener);
	}
}
