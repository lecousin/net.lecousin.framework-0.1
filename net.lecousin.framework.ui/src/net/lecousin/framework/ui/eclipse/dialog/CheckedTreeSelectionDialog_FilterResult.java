package net.lecousin.framework.ui.eclipse.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;

/**
 * Same as CheckedTreeSelectionDialog, except for the returned results:
 * If all the sub-elements of a container is checked, then only the container
 * is returned in the results. If only part of the sub-elements of a container is
 * checked, only these sub-elements are returned. 
 */
public class CheckedTreeSelectionDialog_FilterResult 
  extends CheckedTreeSelectionDialog {

	public CheckedTreeSelectionDialog_FilterResult(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider) {
		super(parent, labelProvider, contentProvider);
	}

	private boolean filtered(Object parent, Object element, ViewerFilter[] filters) {
		for (int i = 0 ; i < filters.length; ++i)
			if (!filters[i].select(getTreeViewer(), parent, element))
				return true;
		return false;
	}
	
	@Override
	protected void computeResult() {
		super.computeResult();
		Object[] result = getResult();
		List<Object> newResult = new ArrayList<Object>(result.length);
		for (int i = 0; i < result.length; ++i)
			newResult.add(result[i]);
		if (result == null) return;
		for (int i = 0; i < result.length; ++i) {
			if (result[i] == null) continue;
			// all sub-elements are checked ?
			Object[] children = ((ITreeContentProvider)getTreeViewer().getContentProvider()).getChildren(result[i]);
			ViewerFilter[] filters = getTreeViewer().getFilters();
			boolean all = true;
			for (int iChild = 0; all && iChild < children.length; ++iChild) {
				if (filtered(result[i], children[iChild], filters)) {
					children[iChild] = null;
					continue;
				}
				boolean found = false;
				for (int iResult = 0; !found && iResult < result.length; ++iResult)
					if (children[iChild].equals(result[iResult]))
						found = true;
				if (!found)
					all = false;
			}
			if (all) {
				// remove children from the result
				for (int iChild = 0; iChild < children.length; ++iChild) {
					if (children[iChild] == null) continue;
					for (Iterator<Object> it = newResult.iterator(); it.hasNext(); )
						if (children[iChild].equals(it.next())) {
							it.remove();
							break;
						}
				}
			} else {
				// remove parent
				newResult.remove(result[i]);
			}
		}
		setResult(newResult);
	}
}
