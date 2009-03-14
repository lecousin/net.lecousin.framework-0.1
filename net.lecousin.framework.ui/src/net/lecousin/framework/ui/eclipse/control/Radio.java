package net.lecousin.framework.ui.eclipse.control;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class Radio extends Composite {

	public Radio(Composite parent, boolean horiz) {
		super(parent, SWT.NONE);
		horizontal = horiz;
		UIUtil.gridLayout(this, 1);
	}
	
	private boolean horizontal;
	private Map<String, Button> options = new HashMap<String, Button>();
	private Map<String, LinkedList<Control>> optionControls = new HashMap<String, LinkedList<Control>>();
	private Event<String> selectionEvent = new Event<String>();
	
	public void addOption(String id, Control[] controls) {
		GridLayout layout = (GridLayout)getLayout();
		if (horizontal) {
			layout.numColumns = options.size() + 1;
		} else {
			if (layout.numColumns < controls.length + 1) {
				for (String optionID : options.keySet()) {
					LinkedList<Control> list = optionControls.get(optionID);
					Control last = list.isEmpty() ? options.get(optionID) : list.getLast();
					for (int i = layout.numColumns; i < controls.length + 1; ++i) {
						Label label = UIUtil.newLabel(this, "");
						label.moveBelow(last);
						list.add(label);
					}
				}
				layout.numColumns = controls.length + 1;
			}
		}
		Button button = new Button(this, SWT.RADIO);
		button.setBackground(getBackground());
		GridData gd = new GridData();
		button.setLayoutData(gd);
		options.put(id, button);
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button)e.widget;
				if ((button.getStyle() & SWT.PUSH) != 0 || button.getSelection())
					selectionEvent.fire(getOptionID((Button)e.widget));
			}
		});
		Control previous = button;
		LinkedList<Control> list = new LinkedList<Control>();
		for (Control c : controls) {
			c.moveBelow(previous);
			c.setLayoutData(UIUtil.gridDataHoriz(1, true));
			previous = c;
			list.add(c);
		}
		for (int i = controls.length; i < layout.numColumns-1; ++i)
			list.add(UIUtil.newLabel(this, ""));
		optionControls.put(id, list);
		UIControlUtil.autoresize(button);
	}
	
	public void addOption(String id, String text) {
		addOption(id, new Control[] { UIUtil.newLabel(this, text) });
	}
	
	public void setSelection(String id) {
		for (Iterator<Map.Entry<String,Button>> it = options.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String,Button> e = it.next();
			boolean selected = id.equals(e.getKey());
			e.getValue().setSelection(selected);
			LinkedList<Control> list = optionControls.get(e.getKey());
			if (list != null)
				for (Control ctrl : list)
					ctrl.setEnabled(selected);
		}
	}
	
	public String getSelection() {
		for (Iterator<Map.Entry<String,Button>> it = options.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String,Button> e = it.next();
			if (e.getValue().getSelection())
				return e.getKey();
		}
		return null;
	}
	
	private String getOptionID(Button button) {
		for (Iterator<Map.Entry<String,Button>> it = options.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String,Button> e = it.next();
			if (e.getValue() == button)
				return e.getKey();
		}
		return null;
	}
	
	public void addSelectionChangedListener(Event.Listener<String> listener) {
		selectionEvent.addListener(listener);
	}
}
