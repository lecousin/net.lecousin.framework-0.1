package net.lecousin.framework.ui.eclipse.control.buttonbar;

import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class ButtonsPanel extends Composite {

	public static class ButtonInPanel {
		ButtonInPanel(String id, Image image, String text) 
		{ this.id = id; this.image = image; this.text = text; }
		public String id;
		public Image image;
		public String text;
		private Control button;
	}
	
	public ButtonsPanel(Composite parent, ButtonInPanel[] buttons) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		this.buttons = buttons;
		
		GridLayout layout = new GridLayout();
		layout.numColumns = buttons.length;
		layout.horizontalSpacing = 15;
		setLayout(layout);
		
		for (ButtonInPanel button : buttons) {
			Listener<ButtonInPanel> listener = new Listener<ButtonInPanel>() {
				public void fire(ButtonInPanel event) {
					handleButton(event.id);
				}
			}; 
			if (button.image != null) {
				if (button.text != null)
					button.button = UIUtil.newImageTextButton(this, button.image, button.text, listener, button);
				else
					button.button = UIUtil.newImageButton(this, button.image, listener, button);
			} else
				button.button = UIUtil.newButton(this, button.text, listener, button);
		}
	}
	
	private ButtonInPanel[] buttons;
	
	public void centerInGrid() {
		GridLayout layout = (GridLayout)getParent().getLayout();
		GridData gd = new GridData();
		gd.horizontalSpan = layout.numColumns;
		gd.horizontalAlignment = SWT.CENTER;
		setLayoutData(gd);
	}
	public void centerAndFillInGrid() {
		GridLayout layout = (GridLayout)getParent().getLayout();
		GridData gd = new GridData();
		gd.horizontalSpan = layout.numColumns;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		setLayoutData(gd);
		layout = (GridLayout)getLayout();
		//layout.makeColumnsEqualWidth = true;
		for (Control c : getChildren()) {
			gd = new GridData();
			gd.horizontalAlignment = SWT.CENTER;
			c.setLayoutData(gd);
		}
		addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				Point size = getSize();
				GridLayout layout = (GridLayout)getLayout();
				int x = 0;
				int nb = 0;
				for (Control c : getChildren()) {
					x += c.getSize().x;
					nb++;
				}
				if (nb > 0) {
					nb += 2;
					layout.horizontalSpacing = (size.x-x)/nb;
					layout.marginRight = layout.horizontalSpacing;
					layout.marginLeft = layout.horizontalSpacing;
				}
			}
		});
	}
	
	protected abstract void handleButton(String id);
	
	public void enable(String id, boolean enabled) {
		for (ButtonInPanel button : buttons) {
			if (!button.id.equals(id)) continue;
			button.button.setEnabled(enabled);
			break;
		}
	}
	
	public void show(boolean show) {
		setVisible(show);
		Object o = getLayoutData();
		if (o == null) return;
		if (o instanceof GridData) {
			((GridData)o).exclude = !show;
		} else if (o instanceof RowData) {
			((RowData)o).exclude = !show;
		}
	}
}
