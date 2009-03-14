package net.lecousin.framework.ui.eclipse.control;

import java.io.InputStream;

import net.lecousin.framework.lang.MyBoolean;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class AnimatedImage2 extends Label {

	public AnimatedImage2(Composite parent, InputStream stream) {
		super(parent, SWT.NONE);

	    loader = new ImageLoader();
	    imageDataArray = loader.load(stream);
        if (imageDataArray.length > 1)
        	animation();
        else
        	setImage(new Image(getDisplay(), imageDataArray[0]));
	}
	
	@Override
	protected void checkSubclass() { /* allow */ }

	private ImageLoader loader;
	private ImageData[] imageDataArray;
	private int repeatCount;
	private int imageDataIndex;
	private boolean run = false;
	private Thread thread = null;
	private long previousTime = 0;
	private MyBoolean waiting = new MyBoolean(false);

	private void animation() {
    	addDisposeListener(new DisposeListener() {
    		public void widgetDisposed(DisposeEvent e) {
    			run = false;
    		}
    	});
    	runThread();
	}
	
	
	private void runThread() {
		run = true;
        imageDataIndex = 0;
        repeatCount = loader.repeatCount;
        draw(false);
        previousTime = System.currentTimeMillis();
		
		thread = new Thread() {
            public void run() {
                try {
	                while (loader.repeatCount == 0 || repeatCount > 0) {
	                  /* Sleep for the specified delay time (adding commonly-used slow-down fudge factors). */
	                	while (true) {
	                		synchronized (waiting) { if (!waiting.get()) break; }
	                		try { Thread.sleep(10); } catch (InterruptedException e) { break; }
	                	}
	                    int ms = imageDataArray[imageDataIndex].delayTime * 10;
	                    if (ms < 20) ms += 30;
	                    if (ms < 30) ms += 10;
	                	ms -= System.currentTimeMillis() - previousTime;
	                	if (ms > 0) {
	                		try {
	                			Thread.sleep(ms);
	                		} catch (InterruptedException e) { break; }
	                	}
	                	imageDataIndex = (imageDataIndex + 1) % imageDataArray.length;
	                	draw(true);
	              	  	if (!run) break;
	                }
                } catch (SWTException ex) {
            	  if (ex.code != SWT.ERROR_WIDGET_DISPOSED) {
            		  if (Log.error(this))
            			  Log.error(this, "Error on animated image", ex);
            		  //System.out.println("There was an error animating the GIF: " + ex.getMessage());
            		  //ex.printStackTrace(System.out);
            	  }
                } finally {
                	// TODO free resources ??
                }
            }
          };
          thread.setDaemon(true);
          thread.start();
        }
	
	private void draw(boolean inThread) throws SWTException {
		ImageData imageData = imageDataArray[imageDataIndex];
        Image image = new Image(getDisplay(), imageData);
        
        /* Draw the off-screen image to the shell. */
        if (!inThread)
        	setImage(image);
        else
        	try {
        		synchronized (waiting) { waiting.set(true); }
	        	getDisplay().asyncExec(new RunnableWithData<Image>(image) {
	        		public void run() {
	        			previousTime = System.currentTimeMillis();
	        			try { setImage(data()); UIUtil.runPendingEvents(getDisplay()); }
	        			catch (Throwable t) { /* skip */ }
	        			synchronized (waiting) {
	        				waiting.set(false);
	        			}
	        		}
	        	});
        	} catch (Throwable t) { /* skip */ }
		
        /* If we have just drawn the last image, decrement the repeat count and start again. */
        if (imageDataIndex == imageDataArray.length - 1) repeatCount--;
	}
	
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		if (imageDataArray.length > 1) {
			return new Point(loader.logicalScreenWidth, loader.logicalScreenHeight);
		}
		return super.computeSize(wHint, hHint, changed);
	}
	
	public void stopAnimation() {
		if (thread == null) return;
		while (thread.isAlive()) {
			run = false;
			try { Thread.sleep(10); } catch (InterruptedException e) { /* skip */ }
		}
	}
}
