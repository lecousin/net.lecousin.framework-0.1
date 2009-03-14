package net.lecousin.framework.ui.eclipse.views.tree;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

public class ViewProvider implements ILabelProvider, IStructuredContentProvider, ITreeContentProvider
{

  public Image getImage(Object element)
  {
    return ((Node)element).getImage();
  }

  public String getText(Object element)
  {
    return ((Node)element).getName();
  }
  
  public Object getParent(Object element)
  {
    return ((Node)element).getParent();
  }
  
  public Object[] getChildren(Object parentElement)
  {
    return ((Node)parentElement).getChildren();
  }
  
  public boolean hasChildren(Object element)
  {
    return ((Node)element).hasChildren();
  }
  
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    // nothing to do
  }
  
  public Object[] getElements(Object inputElement)
  {
    return getChildren(inputElement);
  }

  public void addListener(ILabelProviderListener listener)
  {
    // not yet supported
  }
  public void removeListener(ILabelProviderListener listener)
  {
    // not yet supported
  }

  public void dispose()
  {
    // nothing to do
  }

  public boolean isLabelProperty(Object element, String property)
  {
    return false;
  }

}
