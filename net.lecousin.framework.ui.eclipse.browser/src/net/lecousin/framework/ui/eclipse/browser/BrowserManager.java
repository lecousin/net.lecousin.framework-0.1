package net.lecousin.framework.ui.eclipse.browser;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.WindowEvent;

public class BrowserManager implements LocationListener {

	public static BrowserManager manage(Browser browser) {
		BrowserManager m = new BrowserManager(browser);
		m.init();
		return m;
	}
	
	private BrowserManager(Browser browser) {
		this.browser = browser;
	}
	
	private Browser browser;
	
	private void init() {
		browser.addLocationListener(this);
		browser.addProgressListener(new ProgressListener() {
			public void changed(ProgressEvent event) {
				System.out.println("PROGRESS CHANGED: " + event.current + "/" + event.total + "[" + event.data + "]");
			}
			public void completed(ProgressEvent event) {
				System.out.println("PROGRESS COMPLETED: " + event.current + "/" + event.total + "[" + event.data + "]");
			}
		});
		browser.addOpenWindowListener(new OpenWindowListener() {
			public void open(WindowEvent event) {
				System.out.println("WINDOW OPEN: " + event.location + "[" + event.data + "]");
			}
		});
	}

	public void setLocation(String url) {
		browser.setUrl(url);
	}
	
	public void changed(LocationEvent event) {
		System.out.println("Location changed: " + event.location);
	}
	public void changing(LocationEvent event) {
		System.out.println("Changing location: " + event.location);
	}
}
