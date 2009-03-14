package net.lecousin.framework.ui.eclipse.menu;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.ui.eclipse.event.SelectionListenerWithData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class MyMenu {

	public MyMenu() {
	}
	
	private List<Item> items = new LinkedList<Item>();
	
	public void add(String text, Runnable run) {
		add(text, null, run);
	}
	public void add(String text, Image icon, Runnable run) {
		items.add(new Item(text, icon, run));
	}
	
	private static class Item {
		Item(String text, Image icon, Runnable run)
		{ this.text = text; this.icon = icon; this.run = run; }
		String text;
		Image icon;
		Runnable run;
	}
	
	public void show(Control parent, int x, int y) {
		Menu menu = new Menu(parent);
		for (Item i : items) {
			MenuItem mi = new MenuItem(menu, SWT.PUSH);
			mi.setText(i.text);
			mi.setImage(i.icon);
			mi.addSelectionListener(new SelectionListenerWithData<Runnable>(i.run) {
				public void widgetSelected(SelectionEvent e) {
					data().run();
				}
			});
		}
		menu.setLocation(parent.toDisplay(x, y));
		menu.setVisible(true);
	}
}
