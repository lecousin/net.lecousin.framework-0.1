package net.lecousin.framework.ui.eclipse.control.list;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.ArrayUtil;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.collections.SortedList;
import net.lecousin.framework.collections.SortedListTree;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.lang.MyBoolean;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.event.DragSourceListenerWithData;
import net.lecousin.framework.ui.eclipse.helper.TableHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class LCTable<T> implements LCViewer<T,Table> {

	public LCTable(Composite parent, LCContentProvider<T> contentProvider, ColumnProvider<T>[] columnsProvider, boolean multiSelection) {
		this.table = new Table(parent, multiSelection ? SWT.MULTI : SWT.NONE);
		this.contentProvider = contentProvider;
		
		for (ColumnProvider<T> cp : columnsProvider) {
			TableColumn col = new TableColumn(table, SWT.NONE, table.getColumnCount());
			col.setText(cp.getTitle());
			col.setWidth(cp.getDefaultWidth());
			col.setAlignment(cp.getAlignment());
			col.setData(cp);
			col.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					TableColumn col = (TableColumn)e.widget;
					if (col.getParent().getSortColumn() == col) {
						if (col.getParent().getSortDirection() == SWT.UP)
							col.getParent().setSortDirection(SWT.DOWN);
						else
							col.getParent().setSortDirection(SWT.UP);
					}
					col.getParent().setSortColumn(col);
					if (col.getParent().getSortDirection() == SWT.NONE)
						col.getParent().setSortDirection(SWT.UP);
					sort(col, col.getParent().getSortDirection() == SWT.UP);
				}
			});
		}
		refresh(true);
		table.setHeaderVisible(true);
		
		table.addSelectionListener(new SelectionListener() {
			@SuppressWarnings("unchecked")
			public void widgetDefaultSelected(SelectionEvent e) {
				TableItem[] sel = table.getSelection();
				for (TableItem i : sel)
					doubleClick.fire((T)i.getData());
			}
			public void widgetSelected(SelectionEvent e) {
				fireSelection();
			}
		});
		table.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
			}
			@SuppressWarnings("unchecked")
			public void mouseUp(MouseEvent e) {
				if (e.button == 3) {
					TableItem item = table.getItem(new Point(e.x, e.y));
					rightClick.fire(item != null ? (T)item.getData() : null);
				}
			}
		});
		
		table.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				for (Event<T> event : addElementEvents)
					event.removeListener(addElementListener);
				for (Event<T> event : removeElementEvents)
					event.removeListener(removeElementListener);
				for (Event<T> event : elementChangedEvents)
					event.removeListener(elementChangedListener);
				for (Event<?> event : contentChangedEvents)
					event.removeFireListener(contentChangedListener);
			}
		});
	}
	
	public interface ColumnProvider<T> {
		public String getTitle();
		public int getDefaultWidth();
		public int getAlignment();
		public String getText(T element);
		public Font getFont(T element);
		public Image getImage(T element);
		public Control getControl(Composite parent, T element);
		public int compare(T element1, String text1, T element2, String text2);
	}
	
	private Table table;
	private LCContentProvider<T> contentProvider;
	
	private List<Event<T>> addElementEvents = new LinkedList<Event<T>>();
	private Listener<T> addElementListener = new Listener<T>() { public void fire(T element) { createRow(element); } };
	private List<Event<T>> removeElementEvents = new LinkedList<Event<T>>();
	private Listener<T> removeElementListener = new Listener<T>() { public void fire(T element) { TableItem i = getItem(element);  if (i != null) { boolean selected = checkSelection(i); removeRow(i); if (selected) fireSelection(); } } };
	private List<Event<T>> elementChangedEvents = new LinkedList<Event<T>>();
	private Listener<T> elementChangedListener = new Listener<T>() { public void fire(T element) { TableItem i = getItem(element); if (i != null) updateRow(i); } };
	private List<Event<?>> contentChangedEvents = new LinkedList<Event<?>>();
	private Runnable contentChangedListener = new Runnable() { public void run() { refresh(true); } };
	
	private Event<List<T>> selectionChanged = new Event<List<T>>();
	private Event<T> doubleClick = new Event<T>();
	private Event<T> rightClick = new Event<T>();
	
	public Table getControl() {
		return table;
	}
	
	private MyBoolean isRefreshing = new MyBoolean(false);
	private boolean needNewRefresh = false;
	private boolean newRefreshBackground = false;
	
	public void refresh(boolean background) {
		synchronized (isRefreshing) {
			if (table.isDisposed()) return;
			if (isRefreshing.get()) {
				needNewRefresh = true;
				newRefreshBackground = background;
				return;
			}
			isRefreshing.set(true);
		}
		try {
			ArrayList<TableItem> list = CollectionUtil.list(table.getItems());
			LinkedList<T> toCreate = new LinkedList<T>();
			LinkedList<TableItem> toUpdate = new LinkedList<TableItem>();
			for (T element : contentProvider.getElements()) {
				if (element == null) continue;
				TableItem item = null;
				for (Iterator<TableItem> it = list.iterator(); it.hasNext(); ) {
					TableItem i = it.next();
					if (i.getData().equals(element)) {
						item = i;
						it.remove();
						break;
					}
				}
				if (item == null)
					toCreate.add(element);
				else
					toUpdate.add(item);
			}
			TableItem[] sel = table.getSelection();
			boolean selChanged = false;
			for (TableItem i : list)
				if (ArrayUtil.contains(sel, i)) { selChanged = true; break; }
			if (list.size() > 10)
				table.showItem(table.getItem(0));
			removeRows(list);
			if (selChanged)
				fireSelection();
			if (background) {
				table.getDisplay().asyncExec(new RunnableWithData<Pair<LinkedList<T>,LinkedList<TableItem>>>(new Pair<LinkedList<T>,LinkedList<TableItem>>(toCreate,toUpdate)) {
					public void run() {
						try {
							if (table.isDisposed()) {
								endRefresh();
								return;
							}
							UIUtil.runPendingEvents(table.getDisplay());
							if (table.isDisposed()) {
								endRefresh();
								return;
							}
							if (!data().getValue2().isEmpty())
								for (int i = 0; i < 5 && !data().getValue2().isEmpty(); ++i)
									updateRow(data().getValue2().removeFirst());
							else
								for (int i = 0; i < 5 && !data().getValue1().isEmpty(); ++i)
									createRow(data().getValue1().removeFirst());
							if (!data().getValue2().isEmpty() || !data().getValue1().isEmpty())
								table.getDisplay().asyncExec(this);
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
				for (TableItem item : toUpdate)
					updateRow(item);
				for (T element : toCreate)
					createRow(element);
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
		TableItem item = getItem(element);
		if (item != null) updateRow(item);
	}
	
	private TableItem getItem(T element) {
		for (TableItem i : table.getItems())
			if (i.getData().equals(element))
				return i;
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void sort(TableColumn col, boolean ascending) {
		SortedList<TableItem> list = new SortedListTree<TableItem>(new ItemComparator<T>((ColumnProvider<T>)col.getData(), ascending));
		for (TableItem item : table.getItems())
			list.add(item);
		int i = 0;
		Set<Integer> columnsToReset = new HashSet<Integer>();
		for (TableItem item : list) {
			TableItem item2 = TableHelper.moveItem(item, i);
			if (item2 != item) {
				for (TableEditor editor : getEditors(item)) {
					editor.setItem(item2);
					columnsToReset.add(editor.getColumn());
				}
			}
			++i;
		}
		for (int icol : columnsToReset) {
			TableColumn c = table.getColumn(icol); 
			c.setWidth(c.getWidth());
		}
	}
	private static class ItemComparator<T> implements Comparator<TableItem> {
		ItemComparator(ColumnProvider<T> column, boolean ascending) {
			this.column = column;
			this.ascending = ascending;
		}
		private ColumnProvider<T> column;
		private boolean ascending;
		@SuppressWarnings("unchecked")
		public int compare(TableItem i1, TableItem i2) {
			T e1 = (T)i1.getData();
			String s1 = i1.getText();
			T e2 = (T)i2.getData();
			String s2 = i2.getText();
			int result = column.compare(e1, s1, e2, s2);
			return ascending ? result : -result;
		}
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
	
	public void addAutoRefreshTimer(int millis) {
		table.getDisplay().timerExec(millis, new RunnableWithData<Integer>(millis) {
			public void run() {
				if (table.isDisposed()) return;
				refresh(true);
				table.getDisplay().timerExec(data(), this);
			}
		});
	}
	
	private List<TableEditor> getEditors(TableItem item) {
		List<TableEditor> list = new ArrayList<TableEditor>(table.getColumnCount());
		for (Control c : table.getChildren())
			if (c.getData() instanceof TableEditor) {
				TableEditor editor = (TableEditor)c.getData();
				if (editor.getItem() == item)
					list.add(editor);
			}
		return list;
	}
	
	private void createRow(T element) {
		int index = getIndexToInsert(element);
		TableItem item = new TableItem(table, SWT.NONE, index);
		item.setData(element);
		updateRow(item);
	}
	private void removeRow(TableItem item) {
		item.dispose();
		for (TableEditor editor : getEditors(item)) {
			editor.getEditor().dispose();
			editor.dispose();
		}
	}
	private void removeRows(List<TableItem> items) {
		List<TableEditor> editors = new ArrayList<TableEditor>(items.size());
		for (Control c : table.getChildren())
			if (c.getData() instanceof TableEditor) {
				TableEditor editor = (TableEditor)c.getData();
				if (items.contains(editor.getItem()))
					editors.add(editor);
			}
		int[] indexes = new int[items.size()];
		int i = 0;
		for (TableItem item : items)
			indexes[i++] = table.indexOf(item);
		table.remove(indexes);
		for (TableEditor editor : editors) {
			editor.getEditor().dispose();
			editor.dispose();
		}
	}
	@SuppressWarnings("unchecked")
	private void updateRow(TableItem item) {
		// remove previous editors
		for (TableEditor editor : getEditors(item)) {
			editor.getEditor().dispose();
			editor.dispose();
		}

		T element = (T)item.getData();
		for (int i = 0; i < table.getColumnCount(); ++i) {
			TableColumn col = table.getColumn(i);
			ColumnProvider<T> cp = (ColumnProvider<T>)col.getData();

			item.setText(i, cp.getText(element));
			Font font = cp.getFont(element);
			if (font != null)
				item.setFont(i, font);
			item.setImage(i, cp.getImage(element));
			Control c = cp.getControl(table, element);
			if (c != null) {
				TableEditor editor = new TableEditor(table);
				editor.grabHorizontal = true;
				editor.grabVertical = true;
				editor.setEditor(c, item, i);
				c.setData(editor);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private int getIndexToInsert(T element) {
		TableColumn col = table.getSortColumn();
		if (col == null) return table.getItemCount();
		boolean asc = table.getSortDirection() == SWT.UP;
		ColumnProvider<T> cp = (ColumnProvider<T>)col.getData();
		return getIndexToInsert(element, table.getItems(), 0, table.getItemCount()-1, cp, asc);
	}
	@SuppressWarnings("unchecked")
	private int getIndexToInsert(T element, TableItem[] items, int start, int end, ColumnProvider<T> cp, boolean asc) {
		if (end < start) return start;
		if (start > end) return end+1;
		int pivotIndex = (end-start)/2 + start;
		T pivot = (T)items[pivotIndex].getData();
		int pivotCompare = cp.compare(element, cp.getText(element), pivot, cp.getText(pivot));
		if (pivotCompare == 0) return pivotIndex;
		if ((pivotCompare < 0 && asc) || (pivotCompare > 0 && !asc))
			return getIndexToInsert(element, items, start, pivotIndex-1, cp, asc);
		return getIndexToInsert(element, items, pivotIndex+1, end, cp, asc);
	}
	
	public void setLayoutData(Object data) {
		table.setLayoutData(data);
	}
	
	@SuppressWarnings("unchecked")
	private void fireSelection() {
		TableItem[] items = table.getSelection();
		List<T> list = new ArrayList<T>(items.length);
		for (TableItem i : items)
			list.add((T)i.getData());
		selectionChanged.fire(list);
	}
	
	public void addSelectionChangedListener(Listener<List<T>> listener) { selectionChanged.addListener(listener); }
	public void addDoubleClickListener(Listener<T> listener) { doubleClick.addListener(listener); }
	public void addRightClickListener(Listener<T> listener) { rightClick.addListener(listener); }
	
	@SuppressWarnings("unchecked")
	public List<T> getSelection() {
		TableItem[] sel = table.getSelection();
		List<T> result = new ArrayList<T>(sel != null ? sel.length : 0);
		if (sel == null) return result;
		for (TableItem i : sel)
			result.add((T)i.getData());
		return result;
	}
	
	private boolean checkSelection(TableItem item) {
		return ArrayUtil.contains(table.getSelection(), item);
	}
	
	public void addDragSupport(int style, Transfer[] transfers, DragListener<T> listener) {
		DragSource drag = new DragSource(table, style);
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
	}
}
