package net.lecousin.framework.ui.eclipse.control;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TextList extends Composite {

	public TextList(Composite parent, String title) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 1;
		setLayout(layout);
		
		if (title != null) {
			labelTitle = new Label(this, SWT.NONE);
			labelTitle.setText(title);
			UIUtil.gridDataHorizFill(labelTitle);
		}
		buttonAdd = new LabelButton(this);
		buttonAdd.setImage(SharedImages.getImage(SharedImages.icons.x11.basic.ADD));
		buttonAdd.addClickListener(new Event.Listener<MouseEvent>() {
			public void fire(MouseEvent event) {
				add();
			}
		});
	}
	
	private Label labelTitle;
	private LabelButton buttonAdd;
	private List<Pair<Text,String>> values = new LinkedList<Pair<Text,String>>();
	private Event<String> valueAdded = new Event<String>();
	private Event<Pair<String,String>> valueChanged = new Event<Pair<String,String>>();
	private Event<String> valueRemoved = new Event<String>();
	
	private void add() {
		add("");
	}
	
	public void add(String value) {
		Text text = new Text(this, SWT.BORDER);
		text.moveAbove(buttonAdd);
		text.setText(value);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text text = (Text)e.widget;
				String newValue = text.getText();
				String oldValue = null;
				for (Iterator<Pair<Text,String>> it = values.iterator(); it.hasNext(); ) {
					Pair<Text,String> p = it.next();
					if (p.getValue1() == text) {
						oldValue = p.getValue2();
						p.setValue2(newValue);
						break;
					}
				}
				if (oldValue == null) return;
				valueChanged.fire(new Pair<String,String>(oldValue, newValue));
			}
		});
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));
		LabelButton button = new LabelButton(this);
		button.setImage(SharedImages.getImage(SharedImages.icons.x11.basic.DEL));
		button.moveAbove(buttonAdd);
		button.addClickListener(new Event.Listener<MouseEvent>() {
			public void fire(MouseEvent event) {
				del((LabelButton)event.widget);
			}
		});
		button.setData(text);
		values.add(new Pair<Text,String>(text,value));
		UIControlUtil.autoresize(buttonAdd);
		valueAdded.fire(value);
	}
	
	private void del(LabelButton button) {
		for (Iterator<Pair<Text,String>> it = values.iterator(); it.hasNext(); ) {
			Text text = it.next().getValue1();
			if (button.getData() == text) {
				String value = text.getText();
				button.dispose();
				text.dispose();
				UIControlUtil.autoresize(buttonAdd);
				it.remove();
				valueRemoved.fire(value);
				break;
			}
		}
	}
	
	public void addValueAddedListener(Event.Listener<String> listener) {
		valueAdded.addListener(listener);
	}
	public void removeValueAddedListener(Event.Listener<String> listener) {
		valueAdded.removeListener(listener);
	}
	public void addValueRemovedListener(Event.Listener<String> listener) {
		valueRemoved.addListener(listener);
	}
	public void removeValueRemovedListener(Event.Listener<String> listener) {
		valueRemoved.removeListener(listener);
	}
	public void addValueChangedListener(Event.Listener<Pair<String,String>> listener) {
		valueChanged.addListener(listener);
	}
	public void removeValueChangedListener(Event.Listener<Pair<String,String>> listener) {
		valueChanged.removeListener(listener);
	}
}
