package net.lecousin.framework.ui.eclipse.views.table;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

public abstract class TableView extends ViewPart
{
  private TableViewer viewer;
  
  protected abstract void createColumns(Table table);
  protected abstract ITableLabelProvider createLabelProvider();
  protected abstract IStructuredContentProvider createContentProvider();
  protected abstract ViewerFilter createFilter();
  protected abstract Object createInput();
  
  protected void init(TableViewer viewer) {
    // by default, nothing to do
  }
  
  @Override
  public void createPartControl(Composite parent)
  {
    viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    Table table = viewer.getTable();
    createColumns(table);
    viewer.setLabelProvider(createLabelProvider());
    viewer.setContentProvider(createContentProvider());
    viewer.addFilter(createFilter());
    viewer.setInput(createInput());
    viewer.getTable().addMouseListener(new Mouse());
    init(viewer);
  }
  
  @Override
  public void setFocus()
  {
    viewer.getTable().setFocus();
  }
  
  protected final Object[] getSelection() {
    IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
    if (sel == null) return new Object[0];
    return sel.toArray();
  }
  public void refresh() {
    viewer.refresh(true);
  }

  protected Object getItem(Point pt) {
    TableItem item = viewer.getTable().getItem(pt);
    if (item == null) return null;
    return item.getData();
  }
  
  protected void handleDoubleClick(Object item, Control ctrl, Point pt) {
    // by default, nothing to do
  }
  protected void handleRightClick(Object item, Control ctrl, Point pt) {
    // by default, nothing to do
  }
  
  private class Mouse implements MouseListener {
    public void mouseDoubleClick(MouseEvent e)
    {
      Point pt = new Point(e.x, e.y);
      handleDoubleClick(getItem(pt), viewer.getTable(), pt);
    }
    public void mouseDown(MouseEvent e)
    {
      // not used
    }
    public void mouseUp(MouseEvent e)
    {
      if (e.button == 3) {
        Point pt = new Point(e.x, e.y);
        handleRightClick(getItem(pt), viewer.getTable(), pt);
      }
    }
  }
}
