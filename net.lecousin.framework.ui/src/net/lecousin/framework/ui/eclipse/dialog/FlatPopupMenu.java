package net.lecousin.framework.ui.eclipse.dialog;

import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.BorderStyle;
import net.lecousin.framework.ui.eclipse.control.SimpleCrossButton;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.control.button.ButtonStyle;
import net.lecousin.framework.ui.eclipse.control.button.HoverStyle;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class FlatPopupMenu extends PopupMenu {

	public FlatPopupMenu(Control openWith, String title, boolean closable, boolean stayOnTop, boolean hScroll, boolean vScroll) {
		super(openWith, title == null ? false : closable ? stayOnTop : false);
		this.title = title;
		this.closable = title == null ? false : closable;
		this.hScroll = hScroll;
		this.vScroll = vScroll;
		create(null, MyDialog.FLAG_MENU);
	}

	private String title = null;
	private boolean closable = false;
	private boolean hScroll;
	private boolean vScroll;
	private Composite menuPanel = null;
	
	public enum Orientation {
		/** Start from bottom right, and go up-left until there is enough space */
		BOTTOM_RIGHT,
		/** Show on above or below according to where there is the most space */
		TOP_BOTTOM,
		/** Show below, except if there is not enough space. In this case it will be shown above. */
		BOTTOM,
	}
	
	private static class MyControl {}
	private static final MyControl mycontrol = new MyControl();
	@Override
	protected Composite createControl(Composite container) {
		Composite panel = new Composite(container, SWT.NONE) {
			@Override
			public Point computeSize(int hint, int hint2, boolean changed) {
				for (Control c : getChildren()) {
					Object data = c.getData();
					if (data == mycontrol) continue;
					data = c.getLayoutData();
					if (!(data instanceof GridData)) {
						data = new GridData();
						c.setLayoutData(data);
					}
					GridData gd = (GridData)data;
					gd.horizontalAlignment = SWT.FILL;
					gd.grabExcessHorizontalSpace = true;
					gd.horizontalSpan = ((GridLayout)getLayout()).numColumns;
				}
				UIControlUtil.resize(menuPanel);
				return super.computeSize(hint, hint2, changed);
			}
		};
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		panel.setLayout(layout);
		GridData gd;
		
		if (title != null) {
			Label label = new Label(panel, SWT.NONE);
			label.setData(mycontrol);
			label.setBackground(ColorUtil.get(82, 110, 166));
			label.setForeground(ColorUtil.getWhite());
			label.setText(" " + title);
			UIControlUtil.setFontStyle(label, SWT.ITALIC | SWT.BOLD);
			gd = new GridData();
			gd.horizontalAlignment = SWT.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.heightHint = 18;
			gd.horizontalSpan = closable ? 1 : 2;
			label.setLayoutData(gd);
			
			if (closable) {
				SimpleCrossButton button = new SimpleCrossButton(panel, new ButtonStyle().border_normal(BorderStyle.NONE).border_push(BorderStyle.NONE).border_hover(BorderStyle.NONE).hover(HoverStyle.BOLD), 4);
				button.setData(mycontrol);
				button.setBackground(ColorUtil.get(82, 110, 166));
				button.setForeground(ColorUtil.get(173, 190, 216));
				gd = new GridData();
				gd.heightHint = 18;
				gd.widthHint = 18;
				button.setLayoutData(gd);
				button.addClickListener(new Listener<SimpleCrossButton>() {
					public void fire(SimpleCrossButton event) {
						close();
					}
				});
			}
		}
		
		int style = 0;
		if (hScroll) style |= SWT.H_SCROLL;
		if (vScroll) style |= SWT.V_SCROLL;
		if (style != 0) {
			ScrolledComposite scroll = new ScrolledComposite(panel, style);
			gd = UIUtil.gridDataHorizFill(scroll);
			gd.verticalAlignment = SWT.FILL;
			gd.grabExcessVerticalSpace = true;
			menuPanel = new Composite(scroll, SWT.NONE);
			scroll.setContent(menuPanel);
			menuPanel.setData(new UIControlUtil.TopLevelResize());
			if (!hScroll) scroll.setExpandHorizontal(true);
			if (!hScroll) {
				scroll.addControlListener(new ControlListener() {
					public void controlMoved(ControlEvent e) {
					}
					public void controlResized(ControlEvent e) {
						int w = menuPanel.getParent().getClientArea().width-18;
						menuPanel.setSize(menuPanel.computeSize(w, SWT.DEFAULT));
					}
				});
			}
			menuPanel.setBackground(ColorUtil.getWhite());
		} else {
			menuPanel = new Composite(panel, SWT.NONE);
			UIUtil.gridDataHorizFill(menuPanel);
			menuPanel.setBackground(ColorUtil.getWhite());
		}
		UIUtil.gridLayout(menuPanel, 1);
		
		return panel;
	}
	
	public Composite getControl() { return menuPanel; }
	
	public void show(Control relative, Orientation orientation, boolean progressive) {
		resize();
		Rectangle bounds;
		if (relative != null)
			bounds = toDisplay(relative);
		else {
			Point pt = Display.getDefault().getCursorLocation();
			bounds = new Rectangle(pt.x, pt.y, 0, 0);
		}
		Point size = getShell().getSize();
		Rectangle display = getShell().getDisplay().getBounds();
		int x = 0, y = 0;
		OrientationX ox = null;
		OrientationY oy = null;
		switch (orientation) {
		case BOTTOM_RIGHT:
			ox = OrientationX.RIGHT;
			oy = OrientationY.BOTTOM;
			if (size.x + bounds.x + bounds.width <= display.x + display.width)
				// enough place at right
				x = bounds.x + bounds.width;
			else
				x = bounds.x + bounds.width - ((size.x + bounds.x + bounds.width) - (display.x + display.width));
			if (size.y + bounds.y + bounds.height <= display.y + display.height)
				// enough place at bottom
				y = bounds.y + bounds.height;
			else
				y = bounds.y + bounds.height - ((size.y + bounds.y + bounds.height) - (display.y + display.height));
			break;
		case TOP_BOTTOM:
			ox = null;
			if (bounds.y - display.y > (display.y + display.height)-(bounds.y+bounds.height)) {
				// more space on top
				y = bounds.y - size.y;
				if (y < display.y) y = display.y;
				oy = OrientationY.TOP;
			} else {
				// more space on bottom
				y = bounds.y + bounds.height;
				if (y + size.y > display.y + display.height)
					y = (display.y + display.height)-size.y;
				if (y < display.y) y = display.y;
				oy = OrientationY.BOTTOM;
			}
			x = bounds.x;
			if (x + size.x > display.x + display.width)
				x = (display.x + display.width) - size.x;
			if (x < display.x)
				x = display.x;
			break;
		case BOTTOM:
			ox = null;
			if (bounds.y + size.y < display.y + display.height) {
				// enough space on bottom
				y = bounds.y + bounds.height;
				if (y + size.y > display.y + display.height)
					y = (display.y + display.height)-size.y;
				if (y < display.y) y = display.y;
				oy = OrientationY.BOTTOM;
			} else {
				y = bounds.y - size.y;
				if (y < display.y) y = display.y;
				oy = OrientationY.TOP;
			}
			x = bounds.x;
			if (x + size.x > display.x + display.width)
				x = (display.x + display.width) - size.x;
			if (x < display.x)
				x = display.x;
			break;
		}
		getShell().setLocation(x, y);
		
		if (progressive) {
			openProgressive(ox, oy);
			modal();
		} else
			open(true);
	}
	
	private Rectangle toDisplay(Control c) {
		Rectangle r = c.getBounds();
		Point pt = c.getParent().toDisplay(r.x, r.y);
		r.x = pt.x;
		r.y = pt.y;
		return r;
	}
	
	public static class Separator {
		public Separator(FlatPopupMenu menu) {
			UIUtil.newSeparator(menu.getControl(), true, true);
		}
	}
	
	public static class Menu {
		public Menu(FlatPopupMenu menu, String text, Image icon, boolean bold, boolean italic, Runnable action) {
			Composite panel = UIUtil.newGridComposite(menu.getControl(), 1, 1, 2);
			UIUtil.gridDataHorizFill(panel);
			Label label;
			label = UIUtil.newImage(panel, icon);
			label.setSize(16, 16);
			label = UIUtil.newLabel(panel, text, bold, italic);
			label.setLayoutData(UIUtil.gridDataHoriz(1, true));
			UIControlUtil.makeSelectable(panel, new RunnableWithData<Pair<FlatPopupMenu,Runnable>>(new Pair<FlatPopupMenu,Runnable>(menu, action)) {
				public void run() {
					data().getValue1().close();
					data().getValue2().run();
				}
			});
		}
	}
	
	public static class Title {
		public Title(FlatPopupMenu menu, String text) {
			Label label = new Label(menu.getControl(), SWT.NONE);
			label.setBackground(ColorUtil.get(132, 160, 216));
			label.setForeground(ColorUtil.getWhite());
			label.setText(" " + text);
			UIControlUtil.setFontStyle(label, SWT.BOLD);
			GridData gd = UIUtil.gridDataHorizFill(label);
			gd.heightHint = 18;
			label.setLayoutData(gd);
		}
	}
}
