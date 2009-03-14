package net.lecousin.framework.ui.eclipse.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

public class CursorUtil {

	private static Cursor arrow;
	public static Cursor getArrow() { if (arrow == null) arrow = new Cursor(Display.getCurrent(), SWT.CURSOR_ARROW); return arrow; }
	
	private static Cursor wait;
	public static Cursor getWait() { if (wait == null) wait = new Cursor(Display.getCurrent(), SWT.CURSOR_WAIT); return wait; }
	
	private static Cursor hand;
	public static Cursor getHand() { if (hand == null) hand = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND); return hand; }
	
}
