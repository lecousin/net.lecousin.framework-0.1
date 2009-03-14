package net.lecousin.framework.ui.eclipse.control;

import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class CollapsableSection extends CollapsableControl {

	public CollapsableSection(Composite parent, Color bgcolor1, Color bgcolor2, Color textColor) {
		super(parent);
		setBackground(parent.getBackground());
		header = new RoundedComposite(this, SWT.NONE, bgcolor1, bgcolor2);
		setHeader(header);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 1;
		layout.marginWidth = 0;
		GridData gd;
		header.setLayout(layout);
		icon = new Label(header, SWT.NONE);
		icon.setBackground(bgcolor1);
		text = new Label(header, SWT.NONE);
		text.setBackground(bgcolor1);
		text.setForeground(textColor);
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));
		toolbar = new Composite(header, SWT.NONE);
		toolbar.setBackground(bgcolor1);
		gd = new GridData();
		gd.exclude = true;
		toolbar.setLayoutData(gd);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		toolbar.setLayout(rl);
	}
	
	public CollapsableSection(Composite parent) {
		this(parent, UIUtil.modifyColor(parent.getBackground(), -10), parent.getBackground(), parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
	}
	
	private RoundedComposite header;
	private Label icon;
	private Label text;
	private Composite toolbar;
	
	public void setIcon(Image icon) { this.icon.setImage(icon); UIControlUtil.autoresize(header); }
	public void setText(String text) { this.text.setText(text); UIControlUtil.autoresize(header); }
	
	public void showToolBar(boolean show) {
		GridData rd = (GridData)toolbar.getLayoutData();
		rd.exclude = !show;
	}
	public Composite getToolBar() { return toolbar; }
}
