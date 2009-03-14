package net.lecousin.framework.ui.eclipse.dialog;

import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public abstract class FlatDialog extends MyDialog {

	public FlatDialog(Shell shell, String title, boolean hScroll, boolean vScroll) {
		super(shell);
		this.title = title;
		this.hScroll = hScroll;
		this.vScroll = vScroll;
	}

	private String title = null;
	private boolean hScroll;
	private boolean vScroll;
	private Composite contentPanel = null;
	
	private static class MyControl {}
	private static final MyControl mycontrol = new MyControl();
	@Override
	protected final Composite createControl(Composite container) {
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
				UIControlUtil.resize(contentPanel);
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
			gd.horizontalSpan = 1;
			label.setLayoutData(gd);
			
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
		
		int style = 0;
		if (hScroll) style |= SWT.H_SCROLL;
		if (vScroll) style |= SWT.V_SCROLL;
		if (style != 0) {
			ScrolledComposite scroll = new ScrolledComposite(panel, style);
			gd = UIUtil.gridDataHorizFill(scroll);
			gd.verticalAlignment = SWT.FILL;
			gd.grabExcessVerticalSpace = true;
			contentPanel = new Composite(scroll, SWT.NONE);
			scroll.setContent(contentPanel);
			contentPanel.setData(new UIControlUtil.TopLevelResize());
			if (!hScroll) scroll.setExpandHorizontal(true);
			if (!hScroll) {
				scroll.addControlListener(new ControlListener() {
					public void controlMoved(ControlEvent e) {
					}
					public void controlResized(ControlEvent e) {
						int w = contentPanel.getParent().getClientArea().width-18;
						contentPanel.setSize(contentPanel.computeSize(w, SWT.DEFAULT));
					}
				});
			}
		} else {
			contentPanel = new Composite(panel, SWT.NONE);
			UIUtil.gridDataHorizFill(contentPanel);
		}
		UIUtil.gridLayout(contentPanel, 1);
		
		createContent(contentPanel);
		
		return panel;
	}
	
	protected abstract void createContent(Composite container);
	
	public Composite getControl() { return contentPanel; }
	
	public void openRelative(Control relative, Orientation o, boolean progressive, boolean modal) {
		super.create(null, 0);
		Pair<OrientationX,OrientationY> p = super.setLocationRelative(relative, o);
		if (progressive) {
			super.openProgressive(p.getValue1(), p.getValue2());
			if (modal)
				super.modal();
		} else
			super.open(modal);
	}
	public void openProgressive(OrientationX ox, OrientationY oy, boolean modal) {
		super.create(null, 0);
		super.openProgressive(ox, oy);
		if (modal)
			super.modal();
	}
	public void open(boolean modal) {
		super.open(null, 0);
		if (modal)
			super.modal();
	}
}
