package net.lecousin.framework.ui.eclipse.helper;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.thread.RunnableWithData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class MyTable<T> {

	public MyTable(Composite parent, int style) {
		table = new Table(parent, style);
		table.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				unsubscribeEvents();
			}
		});
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
	}
	
	private Table table;
	private ContentProvider<T> provider = null;
	private List<SelectionListener> selectionListeners = new LinkedList<SelectionListener>();
	private Event<T> doubleClick = new Event<T>();

	private List<Event<?>> eventsUpdate = null;
	private Runnable updater = null;
	private List<Event<T>> eventsElementAdded = null;
	private Listener<T> elementAdder = null;
	private List<Event<T>> eventsElementRemoved = null;
	private Listener<T> elementRemover= null;
	private List<Event<T>> eventsElementChanged = null;
	private Listener<T> elementUpdater = null;
	
	public Table getControl() { return table; }
	
	private void unsubscribeEvents() {
		if (eventsUpdate != null && updater != null) {
			for (Event<?> event : eventsUpdate)
				event.removeFireListener(updater);
		}
		if (eventsElementAdded != null && elementAdder != null) {
			for (Event<T> event : eventsElementAdded)
				event.removeListener(elementAdder);
		}
		if (eventsElementRemoved != null && elementRemover != null) {
			for (Event<T> event : eventsElementRemoved)
				event.removeListener(elementRemover);
		}
		if (eventsElementChanged != null && elementUpdater != null) {
			for (Event<T> event : eventsElementChanged)
				event.removeListener(elementUpdater);
		}
		eventsUpdate = null;
		updater = null;
	}
	
	public void addColumn(String title, int width, Comparator<String> comparator) {
		TableColumn col = new TableColumn(table, SWT.NONE, table.getColumnCount());
		col.setText(title);
		col.setWidth(width);
		if (comparator != null)
			TableHelper.makeColumnSortable(col, comparator);
	}
	
	public void setContent(ContentProvider<T> provider, int millis) {
		setContent(provider);
		table.getDisplay().timerExec(millis, new RunnableWithData<Integer>(millis) {
			public void run() {
				if (table.isDisposed()) return;
				update();
				table.getDisplay().timerExec(data(), this);
			}
		});
	}
	public void setContent(ContentProvider<T> provider, List<Event<?>> autoUpdateEvents) {
		unsubscribeEvents();
		this.provider = provider;
		update();
		if (autoUpdateEvents != null) {
			updater = new Runnable() {
				public void run() {
					update();
				}
			};
			eventsUpdate = autoUpdateEvents;
			for (Event<?> event : autoUpdateEvents)
				event.addFireListener(updater);
		}
	}
	public void setContent(ContentProvider<T> provider, Event<?> autoUpdateEvent) {
		List<Event<?>> list;
		if (autoUpdateEvent == null)
			list = null;
		else {
			list = new LinkedList<Event<?>>();
			list.add(autoUpdateEvent);
		}
		setContent(provider, list);
	}
	public void setContent(ContentProvider<T> provider) {
		setContent(provider, (List<Event<?>>)null);
	}
	
	public static interface ContentProvider<T> {
		public Iterable<? extends T> getElements();
		public String[] getTexts(T element);
		public Image[] getImages(T element);
		public Control[] getControls(T element, Composite parent);
		public Comparator<String>[] getComparators();
	}
	public static abstract class ContentProviderWithData<T,TData> implements ContentProvider<T> {
		public ContentProviderWithData(TData data) { this.data = data; }
		private TData data;
		public TData data() { return data; }
	}
	
	public void addElementChangedEvent(Event<T> event) {
		if (eventsElementChanged == null) eventsElementChanged = new LinkedList<Event<T>>();
		eventsElementChanged.add(event);
		if (elementUpdater == null) elementUpdater = new ElementUpdater();
		event.addListener(elementUpdater);
	}
	public void addElementRemovedEvent(Event<T> event) {
		if (eventsElementRemoved == null) eventsElementRemoved = new LinkedList<Event<T>>();
		eventsElementRemoved.add(event);
		if (elementRemover == null) elementRemover = new ElementRemover();
		event.addListener(elementRemover);
	}
	public void addElementAddedEvent(Event<T> event) {
		if (eventsElementAdded == null) eventsElementAdded = new LinkedList<Event<T>>();
		eventsElementAdded.add(event);
		if (elementAdder == null) elementAdder = new ElementAdder();
		event.addListener(elementAdder);
	}
	
	
	public void update() {
		table.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (table.isDisposed()) return;
				int vscroll = table.getVerticalBar().getSelection();
				int hscroll = table.getHorizontalBar().getSelection();
				Iterable<? extends T> elements = provider.getElements();
				TableItem[] items = table.getItems();
				TableItem[] sel = table.getSelection();
				Object[] selected = new Object[sel.length];
				for (int i = 0; i < sel.length; ++i)
					selected[i] = sel[i].getData();
				for (T element : elements) {
					boolean found = false;
					for (int i = 0; i < items.length; ++i) {
						if (items[i] == null) continue;
						if (items[i].getData().equals(element)) {
							updateRow(items[i], element);
							items[i] = null;
							found = true;
							break;
						}
					}
					if (!found)
						createRow(element);
				}
				for (int i = 0; i < items.length; ++i)
					if (items[i] != null)
						removeRow(items[i]);
				TableHelper.refreshSorting(table, provider.getComparators());
				items = table.getItems();
				List<TableItem> selItems = new LinkedList<TableItem>();
				for (int i = 0; i < items.length; ++i) {
					for (int j = 0; j < selected.length; ++j)
						if (items[i].getData().equals(selected[j])) {
							selItems.add(items[i]);
							break;
						}
				}
				table.setSelection(selItems.toArray(new TableItem[selItems.size()]));
				if (selItems.isEmpty())
					fireSelection();
				table.getVerticalBar().setSelection(vscroll);
				table.getHorizontalBar().setSelection(hscroll);
			}
		});
	}
	
	private void createRow(T element) {
		TableItem item = new TableItem(table, SWT.NONE);
		updateRow(item, element);
	}
	private void updateRow(TableItem item, T element) {
		item.setText(provider.getTexts(element));
		item.setImage(provider.getImages(element));
		item.setData(element);
		
		// remove previous editors
		for (Control c : table.getChildren())
			if (c.getData() instanceof TableEditor) {
				TableEditor editor = (TableEditor)c.getData();
				if (editor.getItem() == item) {
					editor.getEditor().dispose();
					editor.dispose();
				}
			}
		// create new editors
		Control[] controls = provider.getControls(element, table);
		if (controls == null) return;
		for (int i = 0; i < controls.length; ++i)
			if (controls[i] != null) {
				TableEditor editor = new TableEditor(table);
				editor.grabHorizontal = true;
				editor.grabVertical = true;
				editor.setEditor(controls[i], item, i);
				controls[i].setData(editor);
			}
	}
	
	private void removeRow(TableItem item) {
		item.dispose();
		for (Control c : table.getChildren())
			if (c.getData() instanceof TableEditor) {
				TableEditor editor = (TableEditor)c.getData();
				if (editor.getItem() == item) {
					editor.getEditor().dispose();
					editor.dispose();
				}
			}
	}
	
	public void addSelectionListener(SelectionListener listener) {
		if (!selectionListeners.contains(listener))
			selectionListeners.add(listener);
	}
	
	private void fireSelection() {
		for (SelectionListener listener : selectionListeners)
			listener.widgetSelected(null);
	}
	
	public void addDoubleClickListener(Event.Listener<T> listener) { doubleClick.addListener(listener); }
	public void removeDoubleClickListener(Event.Listener<T> listener) { doubleClick.removeListener(listener); }
	
	@SuppressWarnings("unchecked")
	public T getSingleSelection() {
		TableItem[] sel = table.getSelection();
		if (sel == null || sel.length != 1) return null;
		return (T)sel[0].getData();
	}
	
	public TableItem getItem(T element) {
		for (TableItem item : table.getItems())
			if (item.getData() == element)
				return item;
		return null;
	}
	
	private class ElementUpdater implements Listener<T> {
		public void fire(T element) {
			TableItem item = getItem(element);
			if (item == null) return;
			updateRow(item, element);
		}
	}
	private class ElementAdder implements Listener<T> {
		public void fire(T element) {
			TableItem item = getItem(element);
			if (item != null)
				updateRow(item, element);
			else
				createRow(element);
		}
	}
	private class ElementRemover implements Listener<T> {
		public void fire(T element) {
			TableItem item = getItem(element);
			if (item == null) return;
			removeRow(item);
		}
	}
}
