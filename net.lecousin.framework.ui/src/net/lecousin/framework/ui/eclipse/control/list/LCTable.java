package net.lecousin.framework.ui.eclipse.control.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
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
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Slider;

public class LCTable<T> implements LCViewer<T,Composite> {

	public LCTable(Composite parent, LCTableProvider<T> provider) {
		this(parent, provider.getContentProvider(), provider.getColumns(), provider.getConfig());
	}
	public LCTable(Composite parent, LCContentProvider<T> contentProvider, ColumnProvider<T>[] columnsProvider, TableConfig config) {
		this.contentProvider = contentProvider;
		this.config = config;

		panel = new Composite(parent, SWT.BORDER);
		panel.setBackground(ColorUtil.getWhite());
		UIUtil.gridLayout(panel, 2, 0, 0);
		ScrolledComposite contentScroll = new ScrolledComposite(panel, SWT.H_SCROLL);
		contentScroll.setBackground(panel.getBackground());
		contentScroll.setLayoutData(UIUtil.gridData(1, true, 1, true));
		verticalBar = new Slider(panel, SWT.VERTICAL);
		verticalBar.setLayoutData(UIUtil.gridDataVert(1, true));
		verticalBar.setMinimum(0);
		verticalBar.setIncrement(20);

		Composite content = UIUtil.newGridComposite(contentScroll, 0, 0, columnsProvider.length, 1, 0);
		contentScroll.setContent(content);
		contentScroll.setExpandVertical(true);
		resizer.register(content);
        columns = new ArrayList<Column>(columnsProvider.length);
		for (int i = 0; i < columnsProvider.length; ++i)
			columns.add(new Column(columnsProvider[i]));
		boolean first = true;
		for (Column c : columns) {
			c.header = new ColumnHeader(content, c);
			if (first) {
				first = false;
				GridData gd = new GridData();
				gd.horizontalIndent = 1;
				c.header.setLayoutData(gd);
			}
		}
		
		scrollRows = UIUtil.newComposite(content);
		scrollRows.setLayoutData(UIUtil.gridData(columnsProvider.length, true, 1, true));
		scrollRows.setLayout(new Layout() {
			private boolean done = false;
			@Override
			protected Point computeSize(Composite composite, int hint, int hint2, boolean flushCache) {
				return new Point(hint, hint2);
			}
			@Override
			protected void layout(Composite composite, boolean flushCache) {
				if (!done) {
					done = true;
					layout.refreshAll();
				}
				//layout.layout(panelRows, true);
			}
		});
		panelRows = UIUtil.newComposite(scrollRows);
		panelRows.setLocation(0, 0);
		layout = new TableLayout();
		panelRows.setLayout(layout);
		panelRows.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				refreshVerticalBar();
			}
		});
		scrollRows.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				refreshScrollRowsSize();
			}
		});
		scrollRows.addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent e) {
				if (e.button == 0) {
					int i = verticalBar.getSelection();
					i -= e.count * verticalBar.getIncrement();
					if (i < 0) i = 0;
					if (i > verticalBar.getMaximum()) i = verticalBar.getMaximum();
					verticalBar.setSelection(i);
					verticalScrollChanged();
				}
			}
		});
		verticalBar.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				verticalScrollChanged();
			}
		});
		UIControlUtil.recursiveKeyListener(panel, keyListener);
		refresh(true);
		content.setSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
		panelRows.addPaintListener(new RowsPainter());
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
				verticalBar = null;
				panel = null;
				scrollRows = null;
				panelRows = null;
				layout = null;
				columns.clear(); columns = null;
				rows.clear(); rows = null;
				LCTable.this.contentProvider = null;
				LCTable.this.config = null;
				addElementEvents.clear(); addElementEvents = null;
				addElementListener = null;
				backgroundAdd = null;
				LCTable.this.contentChangedEvents.clear(); LCTable.this.contentChangedEvents = null;
				LCTable.this.contentChangedListener = null;
				LCTable.this.doubleClick.free(); LCTable.this.doubleClick = null;
				LCTable.this.drags.clear(); LCTable.this.drags = null;
				for (DragSource ds : dragSources.values())
					ds.dispose();
				LCTable.this.dragSources.clear(); LCTable.this.dragSources = null;
				LCTable.this.elementChangedEvents.clear(); LCTable.this.elementChangedEvents = null;
				LCTable.this.elementChangedListener = null;
				LCTable.this.keyListener = null;
				LCTable.this.keyListeners.clear(); LCTable.this.keyListeners = null;
				LCTable.this.removeElementEvents.clear(); LCTable.this.removeElementEvents = null;
				LCTable.this.removeElementListener = null;
				LCTable.this.resizer = null;
				LCTable.this.rightClick.free(); LCTable.this.rightClick = null;
				LCTable.this.selectionChanged.free(); LCTable.this.selectionChanged = null;
			}
		});
	}

	private Slider verticalBar;
	private Composite panel;
	private Composite scrollRows;
	private Composite panelRows;
	private TableLayout layout;
	private ArrayList<Column> columns;
	private List<Row> rows = new LinkedList<Row>();
	
	private LCContentProvider<T> contentProvider;
	private TableConfig config;
	
	public Composite getControl() { return panel; }
	
	public interface ColumnProvider<T> {
		public String getTitle();
		public int getDefaultWidth();
		public int getAlignment();
	}
	public interface ColumnProviderText<T> extends ColumnProvider<T> {
		public String getText(T element);
		public Font getFont(T element);
		public Image getImage(T element);
		public int compare(T element1, String text1, T element2, String text2);
	}
	public interface ColumnProviderControl<T> extends ColumnProvider<T> {
		public Control getControl(Composite parent, T element);
		public int compare(T element1, T element2);
	}
	
	public static class TableConfig {
		public boolean multiSelection = true;
		public int fixedRowHeight = 18;
		public boolean sortable = true;
	}
	
	public static interface LCTableProvider<T> {
		public LCContentProvider<T> getContentProvider();
		public ColumnProvider<T>[] getColumns();
		public TableConfig getConfig();
	}
	public static abstract class LCTableProvider_SingleColumnText<T> implements LCTableProvider<T> {
		public LCTableProvider_SingleColumnText(List<T> data) {
			this(data, "", SWT.LEFT, true);
		}
		public LCTableProvider_SingleColumnText(List<T> data, String columnTitle, int alignment, boolean multiSelection) { 
			this.data = data;
			this.columnTitle = columnTitle;
			this.alignment = alignment;
			this.multiSelection = multiSelection;
		}
		private List<T> data;
		private String columnTitle;
		private int alignment;
		boolean multiSelection;
		public LCContentProvider<T> getContentProvider() {
			return new LCContentProvider<T>() {
				public Iterable<T> getElements() { return data; }
			};
		}
		@SuppressWarnings("unchecked")
		public ColumnProvider<T>[] getColumns() {
			return new ColumnProvider[] {
				new ColumnProviderText<T>() {
					public String getTitle() { return columnTitle; }
					public String getText(T element) { return LCTableProvider_SingleColumnText.this.getText(element); }
					public Image getImage(T element) { return LCTableProvider_SingleColumnText.this.getImage(element); }
					public int getAlignment() { return alignment; }
					public int getDefaultWidth() { return 200; }
					public Font getFont(T element) { return null; }
					public int compare(T element1, String text1, T element2, String text2) { return text1.compareTo(text2); }
				}
			};
		}
		public TableConfig getConfig() {
			TableConfig config = new TableConfig();
			config.fixedRowHeight = 18;
			config.multiSelection = multiSelection;
			return config;
		}
		public abstract String getText(T element);
		public abstract Image getImage(T element);
	}
	
	private class Column {
		Column(ColumnProvider<T> provider) {
			this.provider = provider;
			width = provider.getDefaultWidth();
		}
		ColumnProvider<T> provider;
		int width;
		int sort = 0;
		ColumnHeader header;
	}
	private int lastSelection = -1;
	private class Row {
		Row(T element) {
			this.element = element;
			controls = new Control[columns.size()];
			for (int i = 0; i < columns.size(); ++i)
				controls[i] = createControl(columns.get(i), i);
			UIControlUtil.recursiveMouseListener(controls[0], new MouseListener() {
				public void mouseDoubleClick(MouseEvent e) {
					panelRows.setFocus();
					doubleClick.fire(Row.this.element);
				}
				public void mouseDown(MouseEvent e) {
					panelRows.setFocus();
					if (e.button == 1) {
						int index = rows.indexOf(Row.this);
						if ((e.stateMask & SWT.CTRL) != 0) {
							if (config.multiSelection) {
								if ((e.stateMask & SWT.SHIFT) != 0) {
									if (lastSelection >= 0 && lastSelection < index && !selected) {
										for (int i = lastSelection+1; i <= index; ++i) {
											Row r = rows.get(i);
											if (!r.selected)
												r.setSelected(true);
										}
										lastSelection = index;
									} else
										if (setSelected(!selected))
											lastSelection = index;
										else
											lastSelection = -1;
								} else
									if (setSelected(!selected))
										lastSelection = index;
									else
										lastSelection = -1;
							} else if (selected)
								setSelection((T)null);
							else
								setSelection(Row.this.element);
							fireSelection();
						} else if (getSelection().size() < 2) {
							setSelection(Row.this.element);
							lastSelection = index;
						}
					}
				}
				public void mouseUp(MouseEvent e) {
					panelRows.setFocus();
					if (e.button == 1) {
						if ((e.stateMask & SWT.CTRL) == 0 && getSelection().size() > 1) {
							setSelection(Row.this.element);
							lastSelection = rows.indexOf(Row.this);
						}
					} else if (e.button == 3) {
						rightClick.fire(Row.this.element);
					}
				}
			}, false);
			UIControlUtil.recursiveKeyListener(controls[0], keyListener);
			addDragSupport(this);
			panel.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					Row.this.element = null;
					controls = null;
				}
			});
		}
		T element;
		Control[] controls;
		boolean selected = false;
		int height = 0;
		private Control createControl(Column col, int index) {
			if (col.provider instanceof ColumnProviderText)
				return createCompositeText((ColumnProviderText<T>)col.provider, element);
			return ((ColumnProviderControl<T>)col.provider).getControl(panelRows, element);
		}
		void update() {
			for (int i = 0; i < columns.size(); ++i) {
				ColumnProvider<T> provider = columns.get(i).provider;
				if (provider instanceof ColumnProviderText)
					updateText(controls[i], (ColumnProviderText<T>)provider, element);
			}
		}
		void remove() {
			for (Control c : controls)
				dispose(c);
		}
		boolean setSelected(boolean value) {
			if (selected == value) return false;
			selected = value;
			controls[0].setBackground(selected ? ColorUtil.get(192, 192, 255) : panelRows.getBackground());
			Rectangle r = controls[0].getBounds();
			panelRows.redraw(r.x-HORIZ_SPACE, r.y-VERT_SPACE, r.width+2*HORIZ_SPACE, r.height+2*VERT_SPACE, false);
			return true;
		}
	}
	
	private List<KeyListener> keyListeners = new LinkedList<KeyListener>();
	private KeyListener keyListener = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if (e.keyCode == 'a' || e.keyCode == 'A') {
				if ((e.stateMask & SWT.CTRL) != 0) {
					selectAll();
					e.doit = false;
				}
			} else if (e.keyCode == SWT.ARROW_DOWN && rows.size() > 0) {
				List<Row> sel = getSelectedRows();
				if (sel == null || sel.size() == 0) {
					setSelection(rows.get(0).element);
					makeVisible(0);
					e.doit = false;
				} else {
					Row r = sel.get(0);
					int i = rows.indexOf(r);
					if (i < rows.size() - 1) {
						setSelection(rows.get(i+1).element);
						makeVisible(i+1);
						e.doit = false;
					}
				}
			} else if (e.keyCode == SWT.ARROW_UP && rows.size() > 0) {
				List<Row> sel = getSelectedRows();
				if (sel == null || sel.size() == 0) {
					setSelection(rows.get(0).element);
					makeVisible(0);
					e.doit = false;
				} else {
					Row r = sel.get(0);
					int i = rows.indexOf(r);
					if (i > 0) {
						setSelection(rows.get(i-1).element);
						makeVisible(i-1);
						e.doit = false;
					}
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
	
	private Event<List<T>> selectionChanged = new Event<List<T>>();
	private Event<T> doubleClick = new Event<T>();
	private Event<T> rightClick = new Event<T>();
	
	public void addSelectionChangedListener(Listener<List<T>> listener) { selectionChanged.addListener(listener); }
	public void addDoubleClickListener(Listener<T> listener) { doubleClick.addListener(listener); }
	public void addRightClickListener(Listener<T> listener) { rightClick.addListener(listener); }

	private List<Event<T>> addElementEvents = new LinkedList<Event<T>>();
	private Listener<T> addElementListener = new Listener<T>() { public void fire(T element) { add(element); } };
	private List<Event<T>> removeElementEvents = new LinkedList<Event<T>>();
	private Listener<T> removeElementListener = new Listener<T>() { public void fire(T element) { remove(element); } };
	private List<Event<T>> elementChangedEvents = new LinkedList<Event<T>>();
	private Listener<T> elementChangedListener = new Listener<T>() { public void fire(T element) { Row i = getRow(element); if (i != null) updateRow(i, false); } };
	private List<Event<?>> contentChangedEvents = new LinkedList<Event<?>>();
	private Runnable contentChangedListener = new Runnable() { public void run() { refresh(true); } };

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
	
	private List<Triple<Integer,Transfer[],DragListener<T>>> drags = new LinkedList<Triple<Integer,Transfer[],DragListener<T>>>();
	public void addDragSupport(int style, Transfer[] transfers, DragListener<T> listener) {
		drags.add(new Triple<Integer,Transfer[],DragListener<T>>(style, transfers, listener));
		for (Row r : rows)
			addDragSupport(r, style, transfers, listener);
	}
	
	private void addDragSupport(Row r) {
		for (Triple<Integer,Transfer[],DragListener<T>> t : drags)
			addDragSupport(r, t.getValue1(), t.getValue2(), t.getValue3());
	}
	
	private void addDragSupport(Row r, int style, Transfer[] transfers, DragListener<T> listener) {
		addDragSupport(r.controls[0], style, transfers, listener);
	}

	private Map<Control, DragSource> dragSources = new HashMap<Control, DragSource>();
	private void addDragSupport(Control c, int style, Transfer[] transfers, DragListener<T> listener) {
		DragSource drag = new DragSource(c, style);
		dragSources.put(c, drag);
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
	private void disposeDrag(Control c) {
		DragSource src = dragSources.remove(c);
		if (src != null)
			src.dispose();
		if (c instanceof Composite)
			for (Control child : ((Composite)c).getChildren())
				disposeDrag(child);
	}
	
	
	public void add(T element) {
		createRow(element, false);
	}
	public void add(List<T> elements) {
		for (T element : elements)
			createRow(element, true);
		panelRows.setSize(panelRowsSize);
	}
	public boolean remove(T element) {
		Row i = getRow(element);
		if (i == null) return false;
		boolean selected = i.selected; 
		removeRow(i, false); 
		if (selected) fireSelection(); 
		return true;
	}
	
	
	private MyBoolean isRefreshing = new MyBoolean(false);
	private boolean needNewRefresh = false;
	private boolean newRefreshBackground = false;
	private LinkedList<T> backgroundAdd = new LinkedList<T>();
	
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
			ArrayList<Row> list = new ArrayList<Row>(rows);
			LinkedList<Row> toUpdate = new LinkedList<Row>();
			for (T element : contentProvider.getElements()) {
				if (element == null) continue;
				Row item = null;
				for (Iterator<Row> it = list.iterator(); it.hasNext(); ) {
					Row i = it.next();
					if (i.element.equals(element)) {
						item = i;
						it.remove();
						break;
					}
				}
				if (item == null)
					backgroundAdd.add(element);
				else
					toUpdate.add(item);
			}
			boolean selChanged = false;
			for (Row i : list)
				if (getSelectedRows().contains(i)) { selChanged = true; break; }
			if (!list.isEmpty())
				removeRows(list, true);
			if (selChanged)
				fireSelection();
			if (background) {
				if (panelRowsSize != null)
					panelRows.setSize(panelRowsSize);
				panel.getDisplay().asyncExec(new RunnableWithData<Pair<LinkedList<T>,LinkedList<Row>>>(new Pair<LinkedList<T>,LinkedList<Row>>(backgroundAdd,toUpdate)) {
					public void run() {
						try {
							if (panel == null || panel.isDisposed()) {
								endRefresh();
								return;
							}
							UIUtil.runPendingEvents(panel.getDisplay());
							if (panel.isDisposed()) {
								endRefresh();
								return;
							}
							if (!data().getValue2().isEmpty()) {
								for (int i = 0; i < 10 && !data().getValue2().isEmpty(); ++i)
									updateRow(data().getValue2().removeFirst(), true);
								panelRows.setSize(panelRowsSize);
							} else {
								for (int i = 0; i < 10 && !data().getValue1().isEmpty(); ++i)
									createRow(data().getValue1().removeFirst(), true);
								panelRows.setSize(panelRowsSize);
							}
							if (!needNewRefresh && (!data().getValue2().isEmpty() || !data().getValue1().isEmpty()))
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
				for (Row item : toUpdate)
					updateRow(item, true);
				for (T element : backgroundAdd)
					createRow(element, true);
				if (panelRowsSize != null)
					panelRows.setSize(panelRowsSize);
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
		Row i = getRow(element); 
		if (i != null) 
			updateRow(i, false);
	}
	
	public void resizeColumn(int colIndex, int width) {
		if (colIndex >= columns.size()) return;
		int diff = width - columns.get(colIndex).width;
		columns.get(colIndex).width = width;
		layout.columnResized(colIndex, diff, false);
	}
	
	private void createRow(T element, boolean deferResize) {
		Row row = new Row(element);
		int index = getIndexToInsert(element);
		layout.rowAdded(row, deferResize);
		if (index != rows.size())
			layout.rowMoved(row, rows.size(), index);
		rows.add(index, row);
	}
	private void updateRow(Row row, boolean deferResize) {
		row.update();
		layout.rowUpdated(row, deferResize);
		int current = rows.indexOf(row);
		int index = getSortIndex(row, current);
		if (current != index)
			moveRow(row, current, index);
	}
	private void removeRow(Row r, boolean deferResize) {
		layout.rowRemoved(r, deferResize);
		rows.remove(r);
		r.remove();
	}
	private void removeRows(List<Row> list, boolean deferResize) {
		layout.rowsRemoved(list, deferResize);
		rows.removeAll(list);
		for (Row r : list)
			r.remove();
	}
	private void moveRow(Row r, int srcIndex, int dstIndex) {
		layout.rowMoved(r, srcIndex, dstIndex);
		rows.remove(srcIndex);
		if (dstIndex >= rows.size())
			rows.add(r);
		else
			rows.add(dstIndex, r);
		panelRows.redraw();
	}
	private Row getRow(T element) {
		for (Row r : rows)
			if (r.element == element)
				return r;
		return null;
	}
	
	private int getIndexToInsert(T element) {
		if (!config.sortable) return rows.size();
		Column sortCol = null;
		for (Column col : columns)
			if (col.sort != 0) {
				sortCol = col;
				break;
			}
		if (sortCol == null) return rows.size();
		int i = 0;
		for (Row r : rows) {
			int cmp = compare(sortCol.provider, element, r.element);
			if (cmp <= 0 && sortCol.sort < 0)
				return i;
			if (cmp >= 0 && sortCol.sort > 0)
				return i;
			i++;
		}
		return i;
	}
	private int getSortIndex(Row row, int current) {
		if (!config.sortable) return rows.indexOf(row);
		Column sortCol = null;
		for (Column col : columns)
			if (col.sort != 0) {
				sortCol = col;
				break;
			}
		if (sortCol == null) return current;
		int i = 0;
		for (Row r : rows) {
			if (r == row) continue;
			int cmp = compare(sortCol.provider, row.element, r.element);
			if (cmp <= 0 && sortCol.sort < 0)
				return i;
			if (cmp >= 0 && sortCol.sort > 0)
				return i;
			i++;
		}
		return i;
	}
	private int compare(ColumnProvider<T> provider, T e1, T e2) {
		if (provider instanceof ColumnProviderText) {
			ColumnProviderText<T> p = (ColumnProviderText<T>)provider;
			return p.compare(e1, p.getText(e1), e2, p.getText(e2));
		} else if (provider instanceof ColumnProviderControl) {
			ColumnProviderControl<T> p = (ColumnProviderControl<T>)provider;
			return p.compare(e1, e2);
		}
		return 0;
	}
	private void sort(Column col) {
		LinkedList<Row> sorted = new LinkedList<Row>();
		for (Row r : rows)
			sort(col, r, sorted, 0, sorted.size()-1);
		rows = sorted;
	}
	private void sort(Column col, Row r, List<Row> list, int min, int max) {
		if (max < min) { list.add(min, r); return; }
		if (min > max) { list.add(max+1, r); return; }
		int pivot = (max-min)/2 + min;
		int cmp = compare(col.provider, r.element, list.get(pivot).element);
		if (col.sort > 0) cmp = -cmp;
		if (cmp == 0) { list.add(pivot, r); return; }
		if (cmp < 0) 
			sort(col, r, list, min, pivot-1);
		else
			sort(col, r, list, pivot+1, max);
	}
	
	
	private List<Row> getSelectedRows() {
		List<Row> result = new LinkedList<Row>();
		for (Row r : rows)
			if (r.selected)
				result.add(r);
		return result;
	}
	public List<T> getSelection() {
		List<T> result = new LinkedList<T>();
		for (Row r : rows)
			if (r.selected)
				result.add(r.element);
		return result;
	}
	public void setSelection(List<T> list) {
		boolean changed = false;
		for (Row r : rows)
			changed |= r.setSelected(list.contains(r.element));
		if (changed) fireSelection();
	}
	public void setSelection(T element) {
		boolean changed = false;
		for (Row r : rows)
			changed |= r.setSelected(r.element == element);
		if (changed) fireSelection();
	}
	public void selectAll() {
		if (!config.multiSelection) return;
		boolean changed = false;
		for (Row r : rows)
			changed |= r.setSelected(true);
		if (changed) fireSelection();
	}
	private void fireSelection() {
		selectionChanged.fire(getSelection());
	}
	
	public List<T> removeSelected() {
		List<T> sel = getSelection();
		removeRows(getSelectedRows(), false);
		return sel;
	}
	
	public int indexOf(T element) {
		int i = 0;
		for (Row r : rows) {
			if (r.element == element) return i;
			i++;
		}
		return -1;
	}
	
	public List<T> getElements() {
		List<T> result = new ArrayList<T>(rows.size());
		for (Row r : rows)
			result.add(r.element);
		for (T element : new ArrayList<T>(backgroundAdd))
			result.add(element);
		return result;
	}
	
	public boolean move(T element, int index) {
		int i = 0;
		Row row = null;
		for (Row r : rows) {
			if (r.element == element) {
				row = r;
				break;
			} else
				i++;
		}
		if (row == null) return false;
		if (i == index) return true;
		moveRow(row, i, index);
		return true;
	}
	
	public void makeVisible(int rowIndex) {
		if (rowIndex >= rows.size()) return;
		int y = VERT_SPACE;
		int i = 0;
		int startY = 0, endY = 0;
		for (Row r : rows) {
			if (i == rowIndex) {
				startY = y;
				endY = y+r.height;
				break;
			}
			y += r.height + VERT_SPACE;
			i++;
		}
		if (startY < panelRowsScrolled) {
			verticalBar.setSelection(startY);
			verticalScrollChanged();
		} else if (endY > panelRowsScrolled + scrollRowsSize.y) {
			verticalBar.setSelection(endY-scrollRowsSize.y);
			verticalScrollChanged();
		}
	}
	
	private Point panelRowsSize = null;
	private int panelRowsScrolled = 0;
	private Point scrollRowsSize = null;
	private void refreshScrollRowsSize() {
		scrollRowsSize = scrollRows.getSize();
		verticalBar.setPageIncrement(scrollRowsSize.y);
		if (panelRowsSize != null)
			refreshVerticalBar();
	}
	private void refreshVerticalBar() {
		int h = panelRowsSize.y;
		int vh = scrollRowsSize.y;
		int ecart = h - vh;
		if (ecart < 0) ecart = 1;
		verticalBar.setMaximum(ecart);
		if (ecart < panelRowsScrolled)
			verticalScrollChanged();
	}
	private void verticalScrollChanged() {
		int vb = verticalBar.getSelection();
		if (panelRowsScrolled != vb) {
			panelRows.setLocation(0, -vb);
			panelRowsScrolled = vb;
		}
	}
	
	private static int COLUMN_HEADER_HEIGHT = 25;
	private class ColumnHeader extends Canvas implements PaintListener, MouseListener {
		public ColumnHeader(Composite parent, Column col) {
			super(parent, SWT.NO_BACKGROUND);
			this.col = col;
			addPaintListener(this);
			resizer.register(this);
			addMouseListener(this);
		}
		Column col;
		public void paintControl(PaintEvent e) {
			Point size = ((Control)e.widget).getSize();
//			e.gc.setBackgroundPattern(new Pattern(e.display, 0, 0, 0, size.y/2, ColorUtil.get(192, 192, 255), ColorUtil.get(240, 240, 255)));
//			e.gc.fillRectangle(0, 0, size.x, size.y/2);
//			e.gc.setBackgroundPattern(new Pattern(e.display, 0, size.y/2, 0, size.y, ColorUtil.get(240, 240, 255), ColorUtil.get(192, 192, 255)));
//			e.gc.fillRectangle(0, size.y/2, size.x, size.y/2);
			int y1 = size.y/3;
			e.gc.setBackgroundPattern(new Pattern(e.display, 0, 0, 0, y1, ColorUtil.get(230, 230, 255), ColorUtil.get(210, 210, 255)));
			e.gc.fillRectangle(0, 0, size.x, y1);
			e.gc.setBackgroundPattern(new Pattern(e.display, 0, y1, 0, size.y, ColorUtil.get(192, 192, 255), ColorUtil.get(240, 240, 255)));
			e.gc.fillRectangle(0, y1, size.x, size.y-y1);
			int x, y;
			int align = col.provider.getAlignment();
			String title = col.provider.getTitle();
			Point tsize = e.gc.textExtent(title);
			List<Font> fonts = new LinkedList<Font>();
			Font initialFont = e.gc.getFont();
			while (tsize.x > size.x+4 && e.gc.getFont().getFontData()[0].height > 5) {
				Font font = UIUtil.increaseFontSize(e.gc.getFont(), -1);
				fonts.add(font);
				e.gc.setFont(font);
				tsize = e.gc.textExtent(title);
			}
			y = size.y - tsize.y - 2;
			if (align == SWT.CENTER)
				x = size.x/2 - tsize.x/2;
			else if (align == SWT.RIGHT)
				x = size.x - 2 - tsize.x;
			else
				x = 2;
			e.gc.drawText(title, x, y, true);
			e.gc.setForeground(ColorUtil.get(150, 150, 150));
			e.gc.drawLine(0, size.y-1, size.x-1, size.y-1);
			
			if (col.sort < 0) {
				e.gc.setForeground(ColorUtil.get(100, 100, 200));
				e.gc.drawPoint(size.x/2, 2);
				e.gc.drawLine(size.x/2-1, 3, size.x/2+1, 3);
				e.gc.drawLine(size.x/2-2, 4, size.x/2+2, 4);
			} else if (col.sort > 0) {
				e.gc.setForeground(ColorUtil.get(100, 100, 200));
				e.gc.drawLine(size.x/2-2, 2, size.x/2+2, 2);
				e.gc.drawLine(size.x/2-1, 3, size.x/2+1, 3);
				e.gc.drawPoint(size.x/2, 4);
			}
			e.gc.setFont(initialFont);
			for (Font f : fonts) f.dispose();
		}
		public void mouseDoubleClick(MouseEvent e) {
		}
		public void mouseDown(MouseEvent e) {
		}
		public void mouseUp(MouseEvent e) {
			if (!config.sortable) return;
			if (resizer.isResizing) return;
			if (col.sort < 0)
				col.sort = 1;
			else if (col.sort > 0)
				col.sort = -1;
			else {
				for (Column c : columns) {
					if (c.sort != 0) {
						c.sort = 0;
						c.header.redraw();
						break;
					}
				}
				col.sort = -1;
			}
			redraw();
			sort(col);
			layout.refreshAll();
		}
		@Override
		public Point computeSize(int hint, int hint2, boolean changed) {
			return new Point(col.width, COLUMN_HEADER_HEIGHT);
		}
	}
	
	private static int HORIZ_SPACE = 1;
	private static int VERT_SPACE = 1;
	
	private class RowsPainter implements PaintListener {
		public void paintControl(PaintEvent e) {
			int y = 0;
			for (Row r : rows) {
				if (y+r.height+1 >= e.y) {
					if (r.selected) {
						int w = columns.get(0).width+1; 
						e.gc.setForeground(ColorUtil.get(128, 128, 255));
						e.gc.drawLine(0, y+1, 0, y+r.height);
						e.gc.drawLine(1, y, w-1, y);
						e.gc.drawLine(1, y+r.height+1, w-1, y+r.height+1);
						e.gc.drawLine(w, y+1, w, y+r.height);
						e.gc.setForeground(ColorUtil.get(200, 200, 255));
						e.gc.drawPoint(0, y);
						e.gc.drawPoint(0, y+r.height+1);
						e.gc.drawPoint(w, y);
						e.gc.drawPoint(w, y+r.height+1);
						//e.gc.drawRectangle(0, y, w, r.height+1);
					}
				}
				y += r.height + VERT_SPACE;
				if (y > e.y + e.height - 1) break;
			}
			
			
//			Point size = panelRowsSize;
//			if (size == null) return;
//			e.gc.setForeground(ColorUtil.get(200, 200, 255));
//			int y = 0;
//			for (Row r : rows) {
//				e.gc.drawLine(0, y, size.x, y);
//				y += r.height+1;
//			}
			
			
//			int x = 0;
//			for (Column c : columns) {
//				if (x > 0)
//					e.gc.drawLine(x-1, 0, x-1, size.y);
//				x += c.width+1;
//			}
//			e.gc.drawLine(x-1, 0, x-1, size.y);
		}
	}
	
	private class TableLayout extends Layout {
		@Override
		protected Point computeSize(Composite composite, int hint, int hint2, boolean flushCache) {
//			int y = 1;
//			for (Row r : rows) {
//				int h = 0;
//				if (flushCache) {
//					for (int i = 0; i < columns.size(); ++i) {
//						Point size = r.controls[i].computeSize(columns.get(i).width, SWT.DEFAULT);
//						if (size.y > h)
//							h = size.y;
//					}
//				} else
//					h = r.height;
//				y += h+1;
//			}
//			int w = 0;
//			for (int i = 0; i < columns.size(); ++i)
//				w += columns.get(i).width + 1;
//			w--;
//			return new Point(w, y);
			return new Point(100,100);
		}
		@Override
		protected void layout(Composite composite, boolean flushCache) {
		}
		private void refreshAll() {
			int y = VERT_SPACE;
			for (Row r : rows) {
				int h;
				if (config.fixedRowHeight <= 0) {
					h = 0;
					Point[] sizes = new Point[columns.size()];
					for (int i = 0; i < columns.size(); ++i) {
						sizes[i] = r.controls[i].computeSize(columns.get(i).width, SWT.DEFAULT);
						if (sizes[i].y > h)
							h = sizes[i].y;
					}
				} else
					h = config.fixedRowHeight;
				r.height = h;
				int x = HORIZ_SPACE;
				for (int i = 0; i < columns.size(); ++i) {
//					int ecart = h-sizes[i].y;
//					r.controls[i].setBounds(x, y+ecart/2, columns.get(i).width, sizes[i].y);
					r.controls[i].setBounds(x, y, columns.get(i).width, h);
					x += columns.get(i).width + HORIZ_SPACE;
				}
				y += h+VERT_SPACE;
			}
			int w = HORIZ_SPACE;
			for (int i = 0; i < columns.size(); ++i)
				w += columns.get(i).width + HORIZ_SPACE;
			w-=HORIZ_SPACE;
			panelRowsSize = new Point(w, y);
			panelRows.setSize(panelRowsSize);
		}
		
		void rowAdded(Row r, boolean deferResize) {
			if (panelRowsSize == null) return;
			int h;
			if (config.fixedRowHeight <= 0) {
				h = 0;
				Point[] sizes = new Point[columns.size()];
				for (int i = 0; i < columns.size(); ++i) {
					sizes[i] = r.controls[i].computeSize(columns.get(i).width, SWT.DEFAULT);
					if (sizes[i].y > h)
						h = sizes[i].y;
				}
			} else
				h = config.fixedRowHeight;
			r.height = h;
			int x = HORIZ_SPACE;
			for (int i = 0; i < columns.size(); ++i) {
//				int ecart = h-sizes[i].y;
//				r.controls[i].setBounds(x, size.y+ecart/2, columns.get(i).width, sizes[i].y);
				r.controls[i].setBounds(x, panelRowsSize.y, columns.get(i).width, h);
				x += columns.get(i).width + HORIZ_SPACE;
			}
			panelRowsSize.y += h+VERT_SPACE;
			if (!deferResize)
				panelRows.setSize(panelRowsSize);
		}
		void rowUpdated(Row r, boolean deferResize) {
			if (config.fixedRowHeight > 0) return;
			int h = 0;
			Point[] sizes = new Point[columns.size()];
			for (int i = 0; i < columns.size(); ++i) {
				sizes[i] = r.controls[i].computeSize(columns.get(i).width, SWT.DEFAULT);
				if (sizes[i].y > h)
					h = sizes[i].y;
			}
			if (r.height == h) return;
			int y = VERT_SPACE;
			Iterator<Row> it;
			for (it = rows.iterator(); it.hasNext(); ) {
				Row rr = it.next();
				if (rr == r) break;
				y += rr.height+VERT_SPACE;
			}
			int diff = h - r.height;
			r.height = h;
			int x = HORIZ_SPACE;
			for (int i = 0; i < columns.size(); ++i) {
//				int ecart = h-sizes[i].y;
//				r.controls[i].setBounds(x, y+ecart/2, columns.get(i).width, sizes[i].y);
				r.controls[i].setBounds(x, y, columns.get(i).width, h);
				x += columns.get(i).width + HORIZ_SPACE;
			}
			while (it.hasNext()) {
				Row rr = it.next();
				for (int i = 0; i < columns.size(); ++i) {
					Point loc = rr.controls[i].getLocation();
					rr.controls[i].setLocation(loc.x, loc.y + diff);
				}
			}
			panelRowsSize.y += diff;
			if (!deferResize)
				panelRows.setSize(panelRowsSize);
		}
		void rowMoved(Row r, int srcIndex, int dstIndex) {
			int y = VERT_SPACE;
			Iterator<Row> it;
			int index = 0;
			// go to the first row where change is needed
			int startIndex = Math.min(srcIndex, dstIndex);
			for (it = rows.iterator(); index < startIndex && it.hasNext(); ++index) {
				Row rr = it.next();
				y += rr.height+VERT_SPACE;
			}
			// if we are at the destination, we put the row
			if (index == dstIndex) {
				int x = HORIZ_SPACE;
				for (int i = 0; i < columns.size(); ++i) {
					r.controls[i].setBounds(x, y, columns.get(i).width, r.height);
					x += columns.get(i).width + HORIZ_SPACE;
				}
				y += r.height+VERT_SPACE;
			} else {
				it.next();
			}
			// update until the last row where change is needed
			startIndex = Math.max(srcIndex, dstIndex);
			for (; index < startIndex && it.hasNext(); ++index) {
				Row rr = it.next();
				int x = HORIZ_SPACE;
				for (int i = 0; i < columns.size(); ++i) {
					rr.controls[i].setLocation(x, y);
					x += columns.get(i).width + HORIZ_SPACE;
				}
				y += rr.height+VERT_SPACE;
			}
			// if we are at the destination, we put the row
			if (index == dstIndex) {
				int x = HORIZ_SPACE;
				for (int i = 0; i < columns.size(); ++i) {
					r.controls[i].setBounds(x, y, columns.get(i).width, r.height);
					x += columns.get(i).width + HORIZ_SPACE;
				}
				y += r.height+VERT_SPACE;
			}
		}
		void rowRemoved(Row r, boolean deferResize) {
			Iterator<Row> it;
			for (it = rows.iterator(); it.hasNext(); ) {
				Row rr = it.next();
				if (rr == r) break;
			}
			while (it.hasNext()) {
				Row rr = it.next();
				for (int i = 0; i < columns.size(); ++i) {
					Point loc = rr.controls[i].getLocation();
					rr.controls[i].setLocation(loc.x, loc.y - r.height - 1);
				}
			}
			panelRowsSize.y -= r.height+VERT_SPACE;
			if (!deferResize)
				panelRows.setSize(panelRowsSize);
		}
		void rowsRemoved(List<Row> list, boolean deferResize) {
			Iterator<Row> it;
			int diff = 0;
			for (it = rows.iterator(); it.hasNext(); ) {
				Row r = it.next();
				if (list.contains(r)) {
					diff -= r.height+VERT_SPACE;
				} else {
					if (diff != 0)
						for (int i = 0; i < columns.size(); ++i) {
							Point loc = r.controls[i].getLocation();
							r.controls[i].setLocation(loc.x, loc.y + diff);
						}
				}
			}
			panelRowsSize.y += diff;
			if (!deferResize)
				panelRows.setSize(panelRowsSize);
		}
		
		void columnResized(int colIndex, int diff, boolean deferResize) {
			if (diff == 0) return;
			Composite p = columns.get(colIndex).header.getParent();
			p.layout(true, false);
			UIControlUtil.resize(p);
			int y = VERT_SPACE;
			for (Row r : rows) {
				int x = HORIZ_SPACE;
				for (int i = 0; i < r.controls.length; ++i) {
					int w = columns.get(i).width;
					if (i >= colIndex) {
						r.controls[i].setBounds(x, y, w, r.height);
					}
					x += w + HORIZ_SPACE;
				}
				y += r.height + VERT_SPACE;
			}
			panelRowsSize.x += diff;
			if (!deferResize)
				panelRows.setSize(panelRowsSize);
		}
	}
	
	private static class CompositeText<T> extends Composite {
		public CompositeText(Composite parent, ColumnProviderText<T> provider, T element) {
			super(parent, SWT.NONE);
			super.setBackground(parent.getBackground());
			Image image = provider.getImage(element);
			setLayout(layout = new MyLayout());
			if (image != null)
				this.image = UIUtil.newImage(this, image);
			text = createTextLabel(this, provider, element);
			text.setLayoutData(UIUtil.gridData(1, true, 1, true));
			if (image != null) {
				this.image.setLocation(0, 0);
				this.image.setSize(16, 16);
				text.setLocation(16, 0);
			} else
				text.setLocation(0, 0);
		}
		private MyLayout layout;
		private Label image;
		private Label text;
		@Override
		public void setBackground(Color color) {
			image.setBackground(color);
			text.setBackground(color);
			super.setBackground(color);
		}
		@Override
		public Point computeSize(int hint, int hint2, boolean changed) {
			return layout.computeSize(this, hint, hint2, changed);
		}
		private class MyLayout extends Layout {
			@Override
			protected Point computeSize(Composite composite, int hint, int hint2, boolean flushCache) {
				int x;
				if (hint == SWT.DEFAULT) {
					x = text.computeSize(SWT.DEFAULT, 16, flushCache).x;
					if (image != null)
						x += 16;
				} else
					x = hint;
				return new Point(x, 16);
			}
			@Override
			protected void layout(Composite composite, boolean flushCache) {
				Point size = composite.getSize();
				if (image != null)
					size.x -= 16;
				text.setSize(size);
			}
		}
	}
	
	private static <T> Label createTextLabel(Composite parent, ColumnProviderText<T> provider, T element) {
		String text = provider.getText(element);
		Label label = UIUtil.newLabel(parent, text);
		label.setAlignment(provider.getAlignment());
		label.setFont(provider.getFont(element));
		//label.setToolTipText(text);
		return label;
	}
	
//	private LinkedList<Label> labelBank = new LinkedList<Label>();
//	private LinkedList<CompositeText<T>> compositeBank = new LinkedList<CompositeText<T>>();
	private Control createCompositeText(ColumnProviderText<T> provider, T element) {
		Image img = provider.getImage(element);
		if (img == null) {
//			if (labelBank.isEmpty())
				return createTextLabel(panelRows, provider, element);
//			Label label = labelBank.removeFirst();
//			updateText(label, provider, element);
//			label.setVisible(true);
//			return label;
		}
//		if (compositeBank.isEmpty())
			return new CompositeText<T>(panelRows, provider, element);
//		CompositeText<T> c = compositeBank.removeFirst();
//		updateText(c, provider, element);
//		c.setVisible(true);
//		return c; 
	}
	
//	@SuppressWarnings("unchecked")
	private void dispose(Control c) {
		disposeDrag(c);
//		if (c instanceof CompositeText) {
//			c.setVisible(false);
//			compositeBank.add((CompositeText<T>)c);
//		} else if (c instanceof Label) {
//			c.setVisible(false);
//			labelBank.add((Label)c);
//		} else
			c.dispose();
	}
	
	@SuppressWarnings("unchecked")
	private void updateText(Control c, ColumnProviderText<T> provider, T element) {
		if (c instanceof CompositeText)
			updateText((CompositeText<T>)c, provider, element);
		else
			updateText((Label)c, provider, element);
	}
	private void updateText(CompositeText<T> c, ColumnProviderText<T> provider, T element) {
		c.image.setImage(provider.getImage(element));
		updateText(c.text, provider, element);
	}
	private void updateText(Label label, ColumnProviderText<T> provider, T element) {
		String text = provider.getText(element);
		label.setText(text);
		label.setAlignment(provider.getAlignment());
		label.setFont(provider.getFont(element));
		//label.setToolTipText(text);
	}
	
	private Resizer resizer = new Resizer();
	private class Resizer {
		void register(Control c) {
			c.addMouseListener(mouse);
			c.addMouseMoveListener(mouse);
			c.addMouseTrackListener(mouse);
		}
		
		private static final int SIZE = 5;
		Mouse mouse = new Mouse();
		Cursor cursor = new Cursor(Display.getDefault(), SWT.CURSOR_SIZEWE);
		Column colToResize = null;
		boolean isResizing = false;
		private class Mouse implements MouseListener, MouseTrackListener, MouseMoveListener {
			public void mouseDoubleClick(MouseEvent e) {
				Control c = ((Control)e.widget);
				if (c instanceof Canvas) {
					ColumnHeader header = (ColumnHeader)c;
					int index = columns.indexOf(header.col);
					int w = 0;
					for (Row r : rows) {
						int x = r.controls[index].computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
						if (x > w)
							w = x;
					}
					if (w < 10) w = 10;
					resizeColumn(index, w);
				}
			}
			public void mouseDown(MouseEvent e) {
				if (((Control)e.widget).getCursor() == cursor)
					isResizing = true;
			}
			public void mouseUp(MouseEvent e) {
				isResizing = false;
			}
			public void mouseEnter(MouseEvent e) {
			}
			public void mouseExit(MouseEvent e) {
				((Control)e.widget).setCursor(null);
			}
			public void mouseHover(MouseEvent e) {
			}
			public void mouseMove(MouseEvent e) {
				Control c = ((Control)e.widget);
				if (isResizing) {
					int x;
					if (c instanceof Canvas)
						x = c.getLocation().x + e.x;
					else
						x = e.x;
					int px = HORIZ_SPACE;
					int index = 0;
					for (Column col: columns) {
						if (col == colToResize)
							break;
						px += col.width + HORIZ_SPACE;
						index++;
					}
					x -= px;
					if (x < 10) x = 10;
					resizeColumn(index, x);
					return;
				}
				Cursor current = c.getCursor();
				if (c instanceof Canvas) {
					ColumnHeader header = (ColumnHeader)c;
					boolean ok = false;
					if (e.x < SIZE && header.col != columns.get(0)) {
						if (current != cursor) {
							c.setCursor(cursor);
							colToResize = columns.get(columns.indexOf(header.col)-1);
						}
						ok = true;
					} else if (e.x > header.col.width-SIZE) {
						if (current != cursor) {
							c.setCursor(cursor);
							colToResize = header.col;
						}
						ok = true;
					}
					if (!ok && current == cursor)
						c.setCursor(null);
				} else {
					boolean ok = false;
					if (e.y <= COLUMN_HEADER_HEIGHT) {
						int x = HORIZ_SPACE;
						for (Column col : columns) {
							if (e.x >= x + col.width - SIZE && e.x <= x + col.width + SIZE) {
								if (current != cursor) {
									c.setCursor(cursor);
									colToResize = col;
								}
								ok = true;
								break;
							}
							x += col.width + HORIZ_SPACE;
						}
					}
					if (!ok && current == cursor)
						c.setCursor(null);
				}
			}
		}
	}
}
