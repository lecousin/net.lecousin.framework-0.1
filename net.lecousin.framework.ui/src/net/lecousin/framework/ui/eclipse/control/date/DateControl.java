package net.lecousin.framework.ui.eclipse.control.date;

import java.util.Calendar;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.dialog.FlatPopupMenu;
import net.lecousin.framework.ui.eclipse.event.SelectionListenerWithData;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class DateControl extends Composite {

	public DateControl(Composite parent) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		GridLayout layout = UIUtil.gridLayout(this, 2, 0, 0);
		layout.horizontalSpacing = 1;
		text = UIUtil.newText(this, "", new TextChanged());
		UIControlUtil.increaseFontSize(text, -2);
		GridData gd = new GridData();
		gd.widthHint = 50;
		text.setLayoutData(gd);
		button = UIUtil.newImageButton(this, SharedImages.getImage(SharedImages.icons.x16.basic.CALENDAR_POPUP), new ButtonListener(), null);
	}
	
	private Text text;
	private Label button;
	private Event<Long> dateChanged = new Event<Long>();
	private long date = 0;
	private long min = 0, max = Long.MAX_VALUE;
	private boolean allowEmpty = true;
	
	public Event<Long> dateChanged() { return dateChanged; }
	
	public long getDate() { return date; }
	
	public void setMinimum(long min) { this.min = normalize(min); if (max < min) max = min; if (date != 0 && date < min) setDateText(min); }
	public void setMaximum(long max) { this.max = normalize(max); if (min > max) min = max; if (date > max) setDateText(max); }
	public void setAllowEmpty(boolean value) { allowEmpty = value; setDateText(date);  }
	
	private void setDateText(long date) {
		if (date == 0)
			text.setText("");
		else
			text.setText(DateTimeUtil.getDateString(date));
	}
	
	private long normalize(long date) {
		if (date == 0) return 0;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date);
		DateTimeUtil.resetHours(c);
		return c.getTimeInMillis();
	}
	
	private class ButtonListener implements Listener<Object> {
		public void fire(Object event) {
			FlatPopupMenu dlg = new FlatPopupMenu(button, null, true, false, false, false);
			DateTime ctrl = new DateTime(dlg.getControl(), SWT.CALENDAR | SWT.LONG);
			long date = DateTimeUtil.getDateFromString(text.getText());
			if (date != 0) {
				Calendar c = Calendar.getInstance(); 
				c.setTimeInMillis(date);
				ctrl.setYear(c.get(Calendar.YEAR));
				ctrl.setMonth(c.get(Calendar.MONTH));
				ctrl.setDay(c.get(Calendar.DAY_OF_MONTH));
			}
			ctrl.addSelectionListener(new SelectionListenerWithData<DateTime>(ctrl) {
				public void widgetSelected(SelectionEvent e) {
					Calendar c = Calendar.getInstance();
					c.set(Calendar.YEAR, data().getYear());
					c.set(Calendar.MONTH, data().getMonth());
					c.set(Calendar.DAY_OF_MONTH, data().getDay());
					text.setText(DateTimeUtil.getDateString(c.getTimeInMillis()));
				}
			});
			dlg.show(button, FlatPopupMenu.Orientation.BOTTOM, true);
		}
	}
	
	private class TextChanged implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			String s = text.getText();
			long date;
			if (s.length() == 0) {
				date = 0;
				setValid(allowEmpty);
				if (!allowEmpty) return;
			} else {
				date = DateTimeUtil.getDateFromString(s);
				if (date == 0 || date < min || date > max) {
					setValid(false);
					return;
				} else
					setValid(true);
			}
			if (DateControl.this.date != date) {
				DateControl.this.date = date;
				dateChanged.fire(date);
			}
		}
	}
	
	private void setValid(boolean valid) {
		text.setBackground(valid ? ColorUtil.getWhite() : ColorUtil.getOrange());
	}
}
