package net.lecousin.framework.ui.eclipse.views.tree;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public abstract class Node
{

  public Node(Node parent) {
    this.parent = parent;
  }
  public Node(TreeViewer viewer) {
	  this.viewer = viewer;
  }
  
  private TreeViewer viewer = null;
  private Node parent;
  
  public Node getParent() { return parent; }
  public TreeViewer getViewer() { return parent == null ? viewer : parent.getViewer(); }
  
  public abstract String getName();
  public Image getImage() { return null; }
  
  public abstract Node[] getChildren();
  public abstract boolean hasChildren();
  
  public void handleDoubleClick(Control ctrl, Point pt) {
    // by default, nothing to do
  }
  public void handleRightClick(Control ctrl, Point pt) {
    // by default, show the menu
    Menu menu = new Menu(ctrl);
    fillContextMenu(menu);
    if (menu.getItemCount() == 0) return;
    menu.setLocation(ctrl.toDisplay(pt));
    menu.setVisible(true);
  }
  public void fillContextMenu(Menu menu) {
    // by default, no menu
  }
  
  public void refresh() {
	  getViewer().refresh(this, true);
  }
  
  @Override
	public String toString() {
	  return "Node: " + getName();
	}
}
