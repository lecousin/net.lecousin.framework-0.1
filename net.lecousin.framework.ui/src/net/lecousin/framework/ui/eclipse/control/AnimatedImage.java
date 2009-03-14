package net.lecousin.framework.ui.eclipse.control;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class AnimatedImage extends Label {

	public AnimatedImage(Composite parent, InputStream stream) {
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
	private Image image;
	private boolean run = false;
	private boolean exit = false;
	private Point loc = new Point(0, 0);
	private Thread thread = null;

	private void animation() {
    	addPaintListener(new PaintListener() {
    		public void paintControl(PaintEvent e) {
    			loc = toDisplay(new Point(0, 0));
    			if (run || exit) return;
    			run = true;
    			runThread();
    		}
    	});
    	addControlListener(new ControlListener() {
    		public void controlMoved(org.eclipse.swt.events.ControlEvent e) {
    			loc = toDisplay(new Point(0, 0));
    		}
    		public void controlResized(org.eclipse.swt.events.ControlEvent e) {
    			// nothing to do
    		}
    	});
    	addDisposeListener(new DisposeListener() {
    		public void widgetDisposed(DisposeEvent e) {
    			run = false;
    			exit = true;
    		}
    	});
	}
	
	private Image offScreenImage = null; 
	private GC offScreenImageGC = null;
	private ImageData imageData;
	private int repeatCount;
	private int imageDataIndex;
	
	private void runThread() {
        /* Create an off-screen image to draw on, and fill it with the shell background. */
        offScreenImage = new Image(getDisplay(), loader.logicalScreenWidth, loader.logicalScreenHeight);
        offScreenImageGC = new GC(offScreenImage);
        /* Create the first image and draw it on the off-screen image. */
        imageDataIndex = 0;
        imageData = imageDataArray[imageDataIndex];
        if (image != null && !image.isDisposed()) image.dispose();
        image = new Image(getDisplay(), imageData);
        offScreenImageGC.drawImage(
          image,
          0,
          0,
          imageData.width,
          imageData.height,
          imageData.x,
          imageData.y,
          imageData.width,
          imageData.height);

        repeatCount = loader.repeatCount;
        draw(false);
		
		thread = new Thread() {
            public void run() {
                try {
	                /* Now loop through the images, creating and drawing each one
	                 * on the off-screen image before drawing it on the shell. */
	                while (loader.repeatCount == 0 || repeatCount > 0) {
	                  /* Sleep for the specified delay time (adding commonly-used slow-down fudge factors). */
	                  try {
	                    int ms = imageData.delayTime * 10;
	                    if (ms < 20) ms += 30;
	                    if (ms < 30) ms += 10;
	                    Thread.sleep(ms);
	                  } catch (InterruptedException e) {
	                	  // continue
	                  }
	                  draw(true);
	              	  if (!run) break;
	                }
                } catch (SWTException ex) {
            	  if (ex.code != SWT.ERROR_WIDGET_DISPOSED) {
            		  //System.out.println("There was an error animating the GIF: " + ex.getMessage());
            		  //ex.printStackTrace(System.out);
            	  }
                } finally {
                  if (offScreenImage != null && !offScreenImage.isDisposed()) offScreenImage.dispose();
                  if (offScreenImageGC != null && !offScreenImageGC.isDisposed()) offScreenImageGC.dispose();
                  if (image != null && !image.isDisposed()) image.dispose();
                }
            }
          };
          thread.setDaemon(true);
          thread.start();
        }
	
	private void draw(boolean inThread) throws SWTException {
        switch (imageData.disposalMethod) {
        case SWT.DM_FILL_BACKGROUND:
          /* Fill with the background color before drawing. */
          //Color bgColor = null;
          //if (useGIFBackground && loader.backgroundPixel != -1) {
          //  bgColor = new Color(display, imageData.palette.getRGB(loader.backgroundPixel));
          //}
          //offScreenImageGC.setBackground(bgColor != null ? bgColor : shellBackground);
          //offScreenImageGC.fillRectangle(imageData.x, imageData.y, imageData.width, imageData.height);
          //if (bgColor != null) bgColor.dispose();
          break;
        case SWT.DM_FILL_PREVIOUS:
          /* Restore the previous image before drawing. */
          offScreenImageGC.drawImage(
            image,
            0,
            0,
            imageData.width,
            imageData.height,
            imageData.x,
            imageData.y,
            imageData.width,
            imageData.height);
          break;
        }
                  
    	imageDataIndex = (imageDataIndex + 1) % imageDataArray.length;
        imageData = imageDataArray[imageDataIndex];
        image.dispose();
        image = new Image(getDisplay(), imageData);
        offScreenImageGC.drawImage(
          image,
          0,
          0,
          imageData.width,
          imageData.height,
          imageData.x,
          imageData.y,
          imageData.width,
          imageData.height);
        
		loc = toDisplay(new Point(0, 0));
        
        /* Draw the off-screen image to the shell. */
        if (!inThread)
            new GC(getDisplay()).drawImage(offScreenImage, loc.x, loc.y);
        else
        	try {
	        	getDisplay().asyncExec(new Runnable() {
	        		public void run() {
	        			try { new GC(getDisplay()).drawImage(offScreenImage, loc.x, loc.y); }
	        			catch (Throwable t) { /* skip */ }
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
			exit = true;
			try { Thread.sleep(10); } catch (InterruptedException e) { /* skip */ }
		}
	}
}
