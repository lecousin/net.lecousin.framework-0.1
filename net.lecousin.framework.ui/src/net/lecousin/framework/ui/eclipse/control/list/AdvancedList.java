package net.lecousin.framework.ui.eclipse.control.list;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.control.list.LCMosaic.MosaicConfig;
import net.lecousin.framework.ui.eclipse.control.list.LCMosaic.MosaicProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.ColumnProvider;
import net.lecousin.framework.ui.eclipse.control.list.LCTable.TableConfig;
import net.lecousin.framework.ui.eclipse.control.list.LCViewer.DragListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;

public class AdvancedList<T> extends Composite {

	public AdvancedList(Composite parent, int style, TitleProvider titleProvider, LCContentProvider<T> contentProvider) {
		super(parent, style ^ (style & SWT.MULTI));
		setBackground(parent.getBackground());
		this.titleProvider = titleProvider;
		this.contentProvider = contentProvider;
		UIUtil.gridLayout(this, 1, 0, 0, 0, 0);
		controller = new AdvancedListController<T>(this);
		UIUtil.gridDataHorizFill(controller);
		controller.setTitle(titleProvider.getTitle());
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				for (Event<T> event : addElementEvents)
					event.removeFireListener(changeListener);
				for (Event<T> event : removeElementEvents)
					event.removeFireListener(changeListener);
				for (Event<T> event : elementChangedEvents)
					event.removeFireListener(changeListener);
				for (Event<?> event : contentChangedEvents)
					event.removeFireListener(changeListener);
			}
		});
	}
	
	private TitleProvider titleProvider;
	private LCContentProvider<T> contentProvider;
	private AdvancedListController<T> controller;
	View currentView = null;
	List<View> views = new LinkedList<View>();
	Event<View> viewAdded = new Event<View>();
	
	abstract class View {
		protected LCViewer<T,?> viewer = null;
		String name;
		private void free() { viewer.getControl().dispose(); viewer = null; }
		private void create() {
			createViewer();
			viewer.getControl().setLayoutData(UIUtil.gridData(1, true, 1, true));
			UIControlUtil.traverseKeyEvents(viewer.getControl(), true);
		}
		protected abstract void createViewer();
	}
	class TableView extends View {
		List<Pair<String,List<ColumnProvider<T>>>> allColumns;
		List<String> columnsShown;
		TableConfig config;
		@SuppressWarnings("unchecked")
		@Override
		protected void createViewer() {
			List<ColumnProvider<T>> columns = new ArrayList<ColumnProvider<T>>(columnsShown.size());
			for (String name : columnsShown) {
				for (Pair<String,List<ColumnProvider<T>>> p : allColumns) {
					boolean found = false;
					for (ColumnProvider<T> c : p.getValue2())
						if (c.getTitle().equals(name)) {
							columns.add(c);
							found = true;
							break;
						}
					if (found) break;
				}
			}
			viewer = new LCTable<T>(AdvancedList.this, contentProvider, columns.toArray(new ColumnProvider[columns.size()]), config);
		}
	}
	class MosaicView extends View {
		MosaicProvider<T> mosaicProvider;
		MosaicConfig config;
		@Override
		protected void createViewer() {
			viewer = new LCMosaic<T>(AdvancedList.this, contentProvider, mosaicProvider, config);
		}
	}
	
	public static interface TitleProvider {
		public String getTitle();
	}

	private List<Event<T>> addElementEvents = new LinkedList<Event<T>>();
	private List<Event<T>> removeElementEvents = new LinkedList<Event<T>>();
	private List<Event<T>> elementChangedEvents = new LinkedList<Event<T>>();
	private List<Event<?>> contentChangedEvents = new LinkedList<Event<?>>();
	private Runnable changeListener = new Runnable() { public void run() { updateHeader(); } };
	private List<Listener<List<T>>> selectionListeners = new LinkedList<Listener<List<T>>>();
	private List<Listener<T>> doubleClickListeners = new LinkedList<Listener<T>>();
	private List<Listener<T>> rightClickListeners = new LinkedList<Listener<T>>();
	private List<KeyListener> keyListeners = new LinkedList<KeyListener>();
	
	public void refreshTitle() { controller.setTitle(titleProvider.getTitle()); }
	
	public void addContentChangedEvent(Event<?> event) {
		if (contentChangedEvents.contains(event)) return;
		event.addFireListener(changeListener);
		contentChangedEvents.add(event);
		for (View view : views)
			if (view.viewer != null)
				view.viewer.addContentChangedEvent(event); 
	}
	public void addAddElementEvent(Event<T> event) { 
		if (addElementEvents.contains(event)) return;
		event.addFireListener(changeListener);
		addElementEvents.add(event);
		for (View view : views)
			if (view.viewer != null)
				view.viewer.addAddElementEvent(event); 
	}
	public void addRemoveElementEvent(Event<T> event) { 
		if (removeElementEvents.contains(event)) return;
		event.addFireListener(changeListener);
		removeElementEvents.add(event);
		for (View view : views)
			if (view.viewer != null)
				view.viewer.addRemoveElementEvent(event); 
	}
	public void addElementChangedEvent(Event<T> event) { 
		if (elementChangedEvents.contains(event)) return;
		event.addFireListener(changeListener);
		elementChangedEvents.add(event);
		for (View view : views)
			if (view.viewer != null)
				view.viewer.addElementChangedEvent(event); 
	}
	public void addSelectionChangedListener(Listener<List<T>> listener) { 
		if (selectionListeners.contains(listener)) return;
		selectionListeners.add(listener);
		for (View view : views)
			if (view.viewer != null)
				view.viewer.addSelectionChangedListener(listener); 
	}
	public void addDoubleClickListener(Listener<T> listener) { 
		if (doubleClickListeners.contains(listener)) return;
		doubleClickListeners.add(listener);
		for (View view : views)
			if (view.viewer != null)
				view.viewer.addDoubleClickListener(listener); 
	}
	public void addRightClickListener(Listener<T> listener) { 
		if (rightClickListeners.contains(listener)) return;
		rightClickListeners.add(listener);
		for (View view : views)
			if (view.viewer != null)
				view.viewer.addRightClickListener(listener); 
	}
	public void addKeyListener(KeyListener listener) {
		if (keyListeners.contains(listener)) return;
		keyListeners.add(listener);
		super.addKeyListener(listener);
		for (View view : views)
			if (view.viewer != null)
				view.viewer.addKeyListener(listener);
	}
	
	public void addTableView(String name, TableConfig config, List<Pair<String,List<ColumnProvider<T>>>> allColumns, List<String> columnsShown) {
		TableView view = new TableView();
		view.name = name;
		view.allColumns = allColumns;
		view.columnsShown = columnsShown;
		view.config = config;
		views.add(view);
		viewAdded.fire(view);
		if (views.size() == 1)
			setView(view);
	}
	public void addMosaicView(String name, MosaicProvider<T> mosaicProvider, MosaicConfig config) {
		MosaicView view = new MosaicView();
		view.name = name;
		view.mosaicProvider = mosaicProvider;
		view.config = config;
		views.add(view);
		viewAdded.fire(view);
		if (views.size() == 1)
			setView(view);
	}
	
	void setView(View view) {
		if (currentView == view) return;
		if (currentView != null)
			currentView.free();
		currentView = view;
		controller.viewChanged(view);
		view.create();
		for (Event<T> event : addElementEvents)
			view.viewer.addAddElementEvent(event);
		for (Event<T> event : removeElementEvents)
			view.viewer.addRemoveElementEvent(event);
		for (Event<T> event : elementChangedEvents)
			view.viewer.addElementChangedEvent(event);
		for (Event<?> event : contentChangedEvents)
			view.viewer.addContentChangedEvent(event);
		for (Listener<List<T>> listener : selectionListeners)
			view.viewer.addSelectionChangedListener(listener);
		for (Listener<T> listener : doubleClickListeners)
			view.viewer.addDoubleClickListener(listener);
		for (Listener<T> listener : rightClickListeners)
			view.viewer.addRightClickListener(listener);
		for (KeyListener listener : keyListeners)
			view.viewer.addKeyListener(listener);
		for (Triple<Integer,Transfer[],DragListener<T>> t : drags)
			view.viewer.addDragSupport(t.getValue1(), t.getValue2(), t.getValue3());
		layout(true, true);
	}
	
	public List<T> getSelection() { return currentView.viewer.getSelection(); }
	
	private void updateHeader() {
		controller.getDisplay().asyncExec(new Runnable() {
			public void run() {
				controller.refresh(titleProvider.getTitle());
			}
		});
	}
	
	private List<Triple<Integer,Transfer[],DragListener<T>>> drags = new LinkedList<Triple<Integer,Transfer[],DragListener<T>>>();
	public void addDragSupport(int style, Transfer[] transfers, DragListener<T> listener) {
		drags.add(new Triple<Integer,Transfer[],DragListener<T>>(style, transfers, listener));
		if (currentView != null)
			currentView.viewer.addDragSupport(style, transfers, listener);
	}
	
}
