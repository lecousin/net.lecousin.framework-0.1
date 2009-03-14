package net.lecousin.framework.ui.eclipse.browser;

import net.lecousin.framework.ui.eclipse.SharedImages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.widgets.Composite;

public class BrowserControl extends Browser {

	private BrowserManager manager;
	private WindowProvider wp;
	private String homeURL;
	
	public BrowserControl(Composite parent, int style, String homeURL, WindowProvider wp) {
		super(parent, style);
		this.wp = wp;
		this.homeURL = homeURL;
		addToolBarActions();
		addStatusControls();
		addProgressListener(new ProgressStatus());
		addStatusTextListener(new StatusText());
		addTitleListener(new Title());
		addLocationListener(new Location());
		manager = BrowserManager.manage(this);
	}
	
	public static interface WindowProvider {
		public IToolBarManager getToolBar();
		public IStatusLineManager getStatusLine();
		public void setTitle(String title);
	}
	
	@Override
	protected void checkSubclass() {
	}
	
    private void addToolBarActions() {
        IToolBarManager toolBarManager = wp.getToolBar();
        if (toolBarManager == null) return;
        if (homeURL != null)
        	toolBarManager.add(homeAction);
        toolBarManager.add(backAction);
        toolBarManager.add(forwardAction);
        toolBarManager.add(stopAction);
        toolBarManager.add(refreshAction);
        toolBarManager.update(true);
    }
    private void addStatusControls() {
    	IStatusLineManager status = wp.getStatusLine();
    	if (status == null) return;
    }
	
	public void setLocation(String url) {
		manager.setLocation(url);
	}
	
	
    protected Action backAction = new Action() {
        {
            setToolTipText(Local.Back.toString());
            setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.icons.x16.arrows.LEFT));
            setEnabled(false);
        }
        public void run() { back(); }
        @Override
        public boolean isEnabled() {
        	return isBackEnabled();
        }
    };
    protected Action forwardAction = new Action() {
        {
            setToolTipText(Local.Forward.toString());
            setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.icons.x16.arrows.RIGHT));
            setEnabled(false);
        }
        public void run() { forward(); }
        @Override
        public boolean isEnabled() {
        	return isForwardEnabled();
        }
    };
    protected Action homeAction = new Action() {
        {
            setToolTipText(Local.Home.toString());
            setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.icons.x16.basic.HOME));
        }
        public void run() { setLocation(homeURL); }
    };
    protected Action stopAction = new Action() {
        {
            setToolTipText(Local.Stop.toString());
            setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.icons.x16.basic.CANCEL));
            setEnabled(false);
        }
        public void run() {
        	stop();
        }
    };
    protected Action refreshAction = new Action() {
        {
            setToolTipText(Local.Refresh.toString());
            setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.icons.x16.basic.REFRESH));
        }
        public void run() { 
        	refresh();
        }
    };

    private class ProgressStatus implements ProgressListener {
    	private int total = -1;
    	private int worked = 0;
    	public void changed(ProgressEvent event) {
    		if (event.total == 0) { completed(event); return; }
    		IStatusLineManager status = wp.getStatusLine();
    		if (status == null) return;
    		IProgressMonitor progress = status.getProgressMonitor();
    		if (progress == null) return;
    		if (total != event.total) {
    			progress.beginTask(Local.Loading.toString(), event.total);
    			total = event.total;
    			worked = 0;
    		}
    		if (worked != event.current) {
    			progress.worked(event.current - worked);
    			worked = event.current;
    		}
    	}
    	public void completed(ProgressEvent event) {
    		total = -1;
    		worked = 0;
    		IStatusLineManager status = wp.getStatusLine();
    		if (status == null) return;
    		IProgressMonitor progress = status.getProgressMonitor();
    		if (progress == null) return;
    		progress.done();
    	}
    }
    
    private class StatusText implements StatusTextListener {
    	public void changed(StatusTextEvent event) {
    		IStatusLineManager status = wp.getStatusLine();
    		if (status == null) return;
    		status.setMessage(event.text);
    	}
    }
    
    private class Title implements TitleListener {
    	public void changed(TitleEvent event) {
    		wp.setTitle(event.title);
    	}
    }
    
    private class Location implements LocationListener {
    	public void changed(LocationEvent event) {
    		updateActions();
    		stopAction.setEnabled(false);
    	}
    	public void changing(LocationEvent event) {
    		updateActions();
    		stopAction.setEnabled(true);
    	}
    	private void updateActions() {
    		backAction.setEnabled(backAction.isEnabled());
    		forwardAction.setEnabled(forwardAction.isEnabled());
    	}
    }
}
