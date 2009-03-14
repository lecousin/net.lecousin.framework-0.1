package net.lecousin.framework.media.util;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.event.ProcessListener;
import net.lecousin.framework.geometry.PointInt;
import net.lecousin.framework.media.MediaPlayer;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.SWT_AWT_Util;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SnapshotTaker {

	private SnapshotTaker() {}
	
	public static void take(URI uri, String pluginID, List<Double> times, PointInt size, Checker checker, ProcessListener<Image> listener) {
		Snapshot snap = new Snapshot();
		snap.uri = uri;
		snap.pluginID = pluginID;
		snap.times = times;
		snap.size = size;
		snap.checker = checker;
		snap.listener = listener;
		synchronized (synchroObject) {
			if (snaps == null) snaps = new LinkedList<Snapshot>();
			snaps.add(snap);
			if (thread == null) {
				thread = new SnapshotTakerThread();
				thread.start();
			}
		}
	}
	
	private static class Snapshot {
		URI uri;
		String pluginID;
		List<Double> times;
		PointInt size;
		Checker checker;
		ProcessListener<Image> listener;
	}
	private static Thread thread = null;
	private static Object synchroObject = new Object();
	private static List<Snapshot> snaps = null;
	
	private static class SnapshotTakerThread extends Thread {
		public SnapshotTakerThread() {
			super("media.snapshot_taker");
		}
		Snapshot snap;
		MediaPlayer player;
		String plugin;

		Robot robot;
		
		@Override
		public void run() {
			do {
				synchronized (synchroObject) {
					snap = snaps.remove(0);
				}
				if (player != null && !plugin.equals(snap.pluginID)) {
					player.free();
					player = null;
				}
				if (player == null) {
					player = MediaPlayer.create(snap.pluginID);
					plugin = snap.pluginID;
				}
				player.addMedia(snap.uri);
				player.setMute(true);
				Display.getDefault().syncExec(new RunnableWithData<SnapshotTakerThread>(this) {
					public void run() {
						snap.listener.started();
						Shell shell = new Shell(Display.getCurrent(), SWT.ON_TOP);
						GridLayout layout = UIUtil.gridLayout(shell, 1);
						layout.marginHeight = 0;
						layout.marginWidth = 0;
						Control ctrl = data().player.createVisual(shell);
						ctrl.setLayoutData(UIUtil.gridData(1, true, 1, true));
						shell.setLocation(Display.getCurrent().getBounds().width, Display.getCurrent().getBounds().height);
						ctrl.setSize(data().snap.size.x, data().snap.size.y);
						shell.layout(true, true);
						shell.setSize(data().snap.size.x, data().snap.size.y);
						if (robot == null)
							try { data().robot = new Robot(); } catch (AWTException e) {}
						shell.setVisible(true);
						long start = System.currentTimeMillis();
						player.start();
						/*while (System.currentTimeMillis() - start < 5000)
							UIUtil.runPendingEvents(Display.getCurrent());*/
						for (Double time : snap.times) {
							player.setPosition(time);
							start = System.currentTimeMillis();
							while (System.currentTimeMillis() - start < 1000)
								UIUtil.runPendingEvents(Display.getCurrent());
							shell.setLocation(0, 0);
							BufferedImage snapAWT = data().robot.createScreenCapture(new Rectangle(0,0,data().snap.size.x, data().snap.size.y));
							shell.setLocation(Display.getCurrent().getBounds().width, Display.getCurrent().getBounds().height);
							UIUtil.runPendingEvents(Display.getCurrent());
							ImageData snapSWT = SWT_AWT_Util.convertToSWT(snapAWT);
							if (snap.checker == null || check(snap.checker, snapSWT)) {
								snap.listener.fire(new Image(Display.getDefault(), snapSWT));
							}
						}
						shell.setVisible(false);
						snap.listener.done();
					}
				});
				synchronized (synchroObject) {
					if (snaps.isEmpty()) {
						snaps = null;
						player.free();
						player = null;
						thread = null;
						break;
					} else {
						player.removeMedia(0);
					}
				}
			} while(true);
		}
	}
	
	public static Image take(MediaPlayer player, Composite visual, double pos, Checker checker) {
		player.setMute(true);
		if (!player.isPlaying())
			player.start();
		player.setPosition(pos);
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < 1000)
			UIUtil.runPendingEvents(Display.getCurrent());
		Robot robot;
		try { robot = new Robot(); } catch (AWTException e) { return null; }
		visual.getShell().forceActive();
		UIUtil.runPendingEvents(visual.getDisplay());
		Point loc = visual.toDisplay(new Point(0,0));
		Point size = visual.getSize();
		BufferedImage snapAWT = robot.createScreenCapture(new Rectangle(loc.x, loc.y, size.x, size.y));
		ImageData snapSWT = SWT_AWT_Util.convertToSWT(snapAWT);
		if (checker == null || check(checker, snapSWT))
			return new Image(Display.getCurrent(), snapSWT);
		return null;
	}
	
	private static interface Checker {
		public void pixel(int x, int y, int pixel);
		public boolean accept();
	}
	public static class LinkedChecker implements Checker {
		public LinkedChecker(Checker check1, Checker check2) {
			this.check1 = check1;
			this.check2 = check2;
		}
		private Checker check1, check2;
		public void pixel(int x, int y, int pixel) {
			check1.pixel(x, y, pixel);
			check2.pixel(x, y, pixel);
		}
		public boolean accept() {
			return check1.accept() && check2.accept();
		}
	}
	
	public static class SameColorChecker implements Checker {
		public SameColorChecker(int red, int green, int blue, int tolerance, double maxAmount) {
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.tolerance = tolerance;
			this.maxAmount = maxAmount;
		}
		private int red, green, blue;
		private int tolerance;
		private double maxAmount;
		private int nbTotal = 0;
		private int nbNotGood = 0;
		public void pixel(int x, int y, int pixel) {
			int r = (((pixel & 0xFF0000) >> 16) & 0xFF);
			int g = (((pixel & 0xFF00) >> 8) & 0xFF);
			int b = (pixel & 0xFF);
			nbTotal++;
			if (r >= red - tolerance && r <= red + tolerance &&
				g >= green - tolerance && g <= green + tolerance &&
				b >= blue - tolerance && b <= blue + tolerance)
				nbNotGood++;
		}
		public boolean accept() {
			return ((double)nbNotGood/(double)nbTotal) <= maxAmount;
		}
	}
	
	private static boolean check(Checker checker, ImageData img) {
		for (int y = 1; y < img.height-1; y++)
	        for (int x = 1; x < img.width-1; x++) {
	        	int pix = img.getPixel(x, y);
	        	checker.pixel(x, y, pix);
	        }
		return checker.accept();
	}
	
}
