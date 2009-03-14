package net.lecousin.framework.ui.eclipse.views.tree;

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

public abstract class TreeView 
  extends ViewPart 
{

  public TreeView() {
    super();
  }
  
	private TreeViewer viewer;
  private ViewProvider provider;
	 
  protected abstract Node createInput();
  
  protected TreeViewer getViewer() { return viewer; }
  
  protected final Node[] getSelection() {
    IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
    if (sel == null) return new Node[0];
    Node[] s = new Node[sel.size()];
    int i = 0;
    for (Iterator<?> it = sel.iterator(); it.hasNext(); ++i)
      s[i] = (Node)it.next();
    return s;
  }
  
  protected void init(TreeViewer viewer) {
    // by default, nothing to do
  }
  
	/**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
	@Override
  public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    provider = new ViewProvider();
		viewer.setContentProvider(provider);
		viewer.setLabelProvider(provider);
		viewer.setInput(createInput());
    viewer.getTree().addMouseListener(new Mouse());
    viewer.addSelectionChangedListener(new SelectionListener());
    init(viewer);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
  public void setFocus() {
		viewer.getControl().setFocus();
	}
  
  public void refresh() {
	  if (viewer.getInput() == null)
		  viewer.setInput(createInput());
    viewer.refresh(true);
  }
  
  public void refresh(Node node) {
	  viewer.refresh(node, true);
  }

  protected Node getNode(Point pt) {
    TreeItem item = viewer.getTree().getItem(pt);
    if (item == null) return null;
    return (Node)item.getData();
  }
  
  protected void handleDoubleClick(Node node, Control ctrl, Point pt) {
    if (node != null)
      node.handleDoubleClick(ctrl, pt);
  }
  protected void handleRightClick(Node node, Control ctrl, Point pt) {
    if (node != null)
      node.handleRightClick(ctrl, pt);
  }
  protected void handleSelectionChanged(Node[] sel) {
	  // nothing to do by default
  }
  
  private class Mouse implements MouseListener {
    public void mouseDoubleClick(MouseEvent e)
    {
      Point pt = new Point(e.x, e.y);
      handleDoubleClick(getNode(pt), viewer.getTree(), pt);
    }
    public void mouseDown(MouseEvent e)
    {
      // not used
    }
    public void mouseUp(MouseEvent e)
    {
      if (e.button == 3) {
        Point pt = new Point(e.x, e.y);
        handleRightClick(getNode(pt), viewer.getTree(), pt);
      }
    }
  }
  
  private class SelectionListener implements ISelectionChangedListener {
	  public void selectionChanged(SelectionChangedEvent event) {
		  IStructuredSelection sel = (IStructuredSelection)event.getSelection();
		  Node[] nodes = new Node[sel.size()];
		  int i = 0;
		  for (Iterator<Object> it = sel.iterator(); it.hasNext(); )
			  nodes[i++] = (Node)it.next();
		  handleSelectionChanged(nodes);
	}
  }
}