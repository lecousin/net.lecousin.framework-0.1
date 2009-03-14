package net.lecousin.framework.ui.eclipse.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ResourceKindFilter extends ViewerFilter {

	public ResourceKindFilter(int kinds) {
		this.kinds = kinds;
	}
	
	private int kinds;
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof IResource)) return false;
		IResource res = (IResource)element;
		if ((res.getType() & kinds) != 0)
			return true;
		return false;
	}

}
