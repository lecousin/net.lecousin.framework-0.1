package net.lecousin.framework.ui.eclipse.control.list;

import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.Radio;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

class AdvancedListController<T> extends Composite {

	public AdvancedListController(AdvancedList<T> list) {
		super(list, SWT.NONE);
		this.list = list;
		bgColor = ColorUtil.get(192, 192, 255);
		setBackground(bgColor);
		GridLayout layout = UIUtil.gridLayout(this, 3);
		layout.marginHeight = 1;
		GridData gd;
		title = new Label(this, SWT.NONE);
		title.setBackground(bgColor);
		gd = new GridData();
		gd.horizontalAlignment = SWT.LEFT;
		title.setLayoutData(gd);
		radioViews = new Radio(this, true);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.CENTER;
		gd.verticalAlignment = SWT.CENTER;
		radioViews.setLayoutData(gd);
		radioViews.setBackground(bgColor);
		layout = (GridLayout)radioViews.getLayout();
		layout.verticalSpacing = 0;
		controlPanel = new Composite(this, SWT.NONE);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.CENTER;
		gd.verticalAlignment = SWT.CENTER;
		controlPanel.setLayoutData(gd);
		controlPanel.setSize(10, 10);
		controlPanel.setBackground(bgColor);
		RowLayout rlayout = new RowLayout(SWT.HORIZONTAL);
		rlayout.marginHeight = 0;
		rlayout.marginWidth = 0;
		controlPanel.setLayout(rlayout);
		
		list.viewAdded.addListener(new Listener<AdvancedList<T>.View>() {
			public void fire(AdvancedList<T>.View view) {
				radioViews.addOption(view.name, view.name);
			}
		});
		radioViews.addSelectionChangedListener(new Listener<String>() {
			public void fire(String viewName) {
				AdvancedList<T>.View view = null;
				for (AdvancedList<T>.View v : AdvancedListController.this.list.views)
					if (v.name.equals(viewName)) {
						view = v;
						break;
					}
				if (view != null)
					AdvancedListController.this.list.setView(view);
			}
		});
	}
	
	private AdvancedList<T> list;
	private Color bgColor;
	private Label title;
	private Radio radioViews;
	private Composite controlPanel;
	
	public void setTitle(String title) {
		this.title.setText(title);
		layout(true, true);
	}
	
	public void refresh(String title) {
		this.title.setText(title);
		// TODO ??
		layout(true, true);
	}
	
	void viewChanged(AdvancedList<T>.View view) {
		radioViews.setSelection(view.name);
	}
}
