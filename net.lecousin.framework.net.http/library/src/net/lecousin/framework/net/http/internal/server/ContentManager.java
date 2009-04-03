package net.lecousin.framework.net.http.internal.server;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.eclipse.extension.EclipsePluginExtensionUtil;
import net.lecousin.framework.log.Log;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.eclipse.core.runtime.IConfigurationElement;

public class ContentManager implements HttpRequestHandler {

	private static ContentManager instance = null;
	public static ContentManager instance() { if (instance == null) instance = new ContentManager(); return instance; } 
	
	private static final String EXTENSION_ID = "net.lecousin.frameowrk.net.http.ContentInterceptor";
	private static final String INTERCEPTOR_TAG_NAME = "interceptor";
	private static final String PRIORITY_ATTR_NAME = "priority";
	private static final String HANDLER_ATTR_NAME = "handler";
	
	private ContentManager() {
		Collection<IConfigurationElement> exts = EclipsePluginExtensionUtil.getExtensionsSubNode(EXTENSION_ID, INTERCEPTOR_TAG_NAME);
		for (Iterator<IConfigurationElement> it = exts.iterator(); it.hasNext(); ) {
			IConfigurationElement ext = it.next();
			String p = ext.getAttribute(PRIORITY_ATTR_NAME);
			int priority = p != null && p.length() > 0 ? Integer.parseInt(p) : 10000;
			String className = ext.getAttribute(HANDLER_ATTR_NAME);
			try {
				Class<?> clazz = EclipsePluginExtensionUtil.getClass(ext, className);
				HttpRequestHandler handler = (HttpRequestHandler)EclipsePluginExtensionUtil.getInstance(clazz, new Object[][]{ new Object[]{} });
				CollectionUtil.addSorted(interceptors, new Interceptor(priority, handler), new Interceptor.Comparator());
			} catch (Throwable t) {
				if (Log.error(this))
					Log.error(this, "Unable to instantiate HTTPRequestHandler class '"+className+"'.");
			}
		}
	}
	
	static class Interceptor {
		Interceptor(int priority, HttpRequestHandler handler)
		{ this.handler = handler; this.priority = priority; }
		HttpRequestHandler handler;
		int priority;
		static class Comparator implements java.util.Comparator<Interceptor> {
			public int compare(Interceptor o1, Interceptor o2) {
				return o1.priority < o2.priority ? -1 : o1.priority > o2.priority ? 1 : 0;
			}
		}
	}
	
	private List<Interceptor> interceptors = new LinkedList<Interceptor>();
	
	public void handle(HttpRequest req, HttpResponse resp, HttpContext ctx)
	throws HttpException, IOException {
		for (ListIterator<Interceptor> it = interceptors.listIterator(); it.hasNext(); )
			it.next().handler.handle(req, resp, ctx);
	}
}
