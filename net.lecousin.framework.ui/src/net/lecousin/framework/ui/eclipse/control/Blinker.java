package net.lecousin.framework.ui.eclipse.control;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class Blinker {

	private Blinker(){}
	
	public static final long[] LONG_NO_BLINK__LITTLE_BLINK__SMOOTH_TRANSITION = new long[] { 
		10000, 500, 1000, 500 
	};
	public static final long[] LONG_NO_BLINK__BLINK_BLINK_BLINK_LITTLE_BLINK__SMOOTH_TRANSITION = new long[] { 
		10000, 500, 300, 0, 
		150, 0, 300, 0,
		150, 0, 300, 0,
		150, 0, 600, 500
	};
	
	/**
	 * Make the given control blinking by changing its background color.
	 * 
	 * @param c the control to blink
	 * @param recurseToChildren indicates if the background color should be propagated to the control's children
	 * @param pattern the blink pattern, being a sequence of 4 long indicating 1. time to be with noBlinkColor 2. time to go to the blinkColor 3. time to be with the blinkColor, 4. time to come back to noBlinkColor
	 * @param noBlinkColor the normal color, or null to use the control's parent background color
	 * @param blinkColor the blink color, or null to use the control's parent background color
	 */
	public static void blink(Control c, boolean recurseToChildren, long[] pattern, Color noBlinkColor, Color blinkColor) {
		Blink blink = new Blink();
		blink.c = c;
		blink.recurseToChildren = recurseToChildren;
		blink.pattern = pattern;
		blink.noBlinkColor = noBlinkColor;
		blink.blinkColor = blinkColor;
		boolean torun;
		synchronized (blinks) {
			torun = blinks.isEmpty();
			blinks.add(blink);
		}
		if (torun) c.getDisplay().timerExec(10, blinker);
	}

	private static class Blink {
		Control c;
		boolean recurseToChildren;
		long[] pattern;
		Color noBlinkColor;
		Color blinkColor;
		
		enum State {
			NO_BLINK,
			TO_BLINK,
			BLINK,
			TO_NO_BLINK;
		}
		
		State state = State.NO_BLINK;
		long start = System.currentTimeMillis();
		int patternPos = 0;
	}

	private static final long BLINK_TIMER = 10;
	private static final long WAIT_TIMER = 100;
	private static final long MIN_TIMER = 10;
	
	private static List<Blink> blinks = new LinkedList<Blink>();
	
	private static Runnable blinker = new Runnable() {
		public void run() {
			ArrayList<Blink> blinks;
			synchronized (Blinker.blinks) {
				if (Blinker.blinks.isEmpty()) return;
				blinks = new ArrayList<Blink>(Blinker.blinks); 
			}
			LinkedList<Blink> toRemove = new LinkedList<Blink>();
			long time = System.currentTimeMillis();
			long nextTime = 0;
			long n;
			for (Blink b : blinks) {
				if (b.c.isDisposed()) {
					toRemove.add(b);
					continue;
				}
				switch (b.state) {
				case NO_BLINK:
					if (time - b.start < b.pattern[b.patternPos*4+0]) {
						n = b.start + b.pattern[b.patternPos*4+0];
						if (nextTime == 0 || nextTime > n) nextTime = n;
						continue;
					}
					b.state = Blink.State.TO_BLINK;
					b.start = time;// - (time-b.start-b.pattern[b.patternPos*4+0]);
					doColor(b.c, b.recurseToChildren, b.noBlinkColor, b.blinkColor, b.start, time, b.pattern[b.patternPos*4+1]);
					n = BLINK_TIMER;
					if (nextTime == 0 || nextTime > n) nextTime = n;
					break;
				case TO_BLINK:
					if (time - b.start < b.pattern[b.patternPos*4+1]) {
						doColor(b.c, b.recurseToChildren, b.noBlinkColor, b.blinkColor, b.start, time, b.pattern[b.patternPos*4+1]);
						n = BLINK_TIMER;
						if (nextTime == 0 || nextTime > n) nextTime = n;
					} else {
						b.state = Blink.State.BLINK;
						b.start = time;// - (time - b.start - b.pattern[b.patternPos*4+1]);
						doColor(b.c, b.recurseToChildren, b.blinkColor);
						n = b.start + b.pattern[b.patternPos*4+2];
						if (nextTime == 0 || nextTime > n) nextTime = n;
					}
					break;
				case BLINK:
					if (time - b.start < b.pattern[b.patternPos*4+2]) {
						n = b.start + b.pattern[b.patternPos*4+2];
						if (nextTime == 0 || nextTime > n) nextTime = n;
						continue;
					}
					b.state = Blink.State.TO_NO_BLINK;
					b.start = time;// - (time-b.start-b.pattern[b.patternPos*4+2]);
					doColor(b.c, b.recurseToChildren, b.blinkColor, b.noBlinkColor, b.start, time, b.pattern[b.patternPos*4+3]);
					n = BLINK_TIMER;
					if (nextTime == 0 || nextTime > n) nextTime = n;
					break;
				case TO_NO_BLINK:
					if (time - b.start < b.pattern[b.patternPos*4+3]) {
						n = BLINK_TIMER;
						if (nextTime == 0 || nextTime > n) nextTime = n;
						doColor(b.c, b.recurseToChildren, b.blinkColor, b.noBlinkColor, b.start, time, b.pattern[b.patternPos*4+3]);
					} else {
						b.state = Blink.State.NO_BLINK;
						b.start = time;// - (time - b.start - b.pattern[b.patternPos*4+3]);
						b.patternPos++;
						if (b.patternPos*4 >= b.pattern.length)
							b.patternPos = 0;
						doColor(b.c, b.recurseToChildren, b.noBlinkColor);
						n = b.start + b.pattern[b.patternPos*4+0];
						if (nextTime == 0 || nextTime > n) nextTime = n;
					}
					break;
				}
			}
			synchronized (Blinker.blinks) {
				Blinker.blinks.removeAll(toRemove);
				if (Blinker.blinks.isEmpty()) return;
			}
			time = System.currentTimeMillis();
			if (nextTime < time + MIN_TIMER ) nextTime = time+MIN_TIMER;
			Display.getDefault().timerExec((int)(nextTime == 0 ? WAIT_TIMER : nextTime - time), this);
		}
		
		private void doColor(Control c, boolean rec, Color from, Color to, long start, long now, long period) {
			if (from == null) from = c.getParent().getBackground();
			if (to == null) to = c.getParent().getBackground();
			long time = now-start;
			if (time > period) time = period;
			Color col = period > 0 ? ColorUtil.goTo(from, to, (int)period, (int)time) : to;
			doColor(c, rec, col);
		}
		private void doColor(Control c, boolean rec, Color col) {
			if (rec)
				UIControlUtil.setBackground(c, col);
			else
				c.setBackground(col);
		}
	};
	
}
