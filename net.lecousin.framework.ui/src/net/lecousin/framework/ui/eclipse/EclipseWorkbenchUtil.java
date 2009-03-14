package net.lecousin.framework.ui.eclipse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

public class EclipseWorkbenchUtil {

	@SuppressWarnings("unchecked")
	public static <T> T getSingleNavigationSelection(Class<T> clazz) {
		Object o = getNavigationSelectionObject();
		if (o instanceof Collection) {
			Collection<?> col = (Collection<?>)o;
			if (col.isEmpty()) return null;
			o = col.iterator().next();
		}
		return (T)Platform.getAdapterManager().getAdapter(o, clazz);
	}
	
	/** may return a single object, or a collection */
	public static Object getNavigationSelectionObject() {
		if (!PlatformUI.getWorkbench().hasService(IHandlerService.class)) return null;
		IHandlerService hs = (IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class);
		IEvaluationContext ctx = hs.getCurrentState();
		if (ctx == null) return null;
		return ctx.getDefaultVariable();
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends IViewPart> Collection<T> getViews(String id, Class<T> clazz) {
		List<T> result = new LinkedList<T>();
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; ++i) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int j = 0; j < pages.length; ++j) {
				IViewPart view = pages[j].findView(id);
				if (view != null) {
					if (clazz.isAssignableFrom(view.getClass()))
						result.add((T)view);
				}
			}
		}
		return result;
	}
	
	public static IWorkbenchPage getPage() { return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); }
}
