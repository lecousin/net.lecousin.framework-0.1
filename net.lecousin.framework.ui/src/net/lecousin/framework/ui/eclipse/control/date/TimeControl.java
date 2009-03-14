package net.lecousin.framework.ui.eclipse.control.date;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class TimeControl extends Text {

	public TimeControl(Composite parent) {
		super(parent, SWT.BORDER);
		//setBackground(parent.getBackground());
		addModifyListener(new TextChanged());
	}
	
	private Event<Long> timeChanged = new Event<Long>();
	private long time = 0;
	private long min = 0, max = Long.MAX_VALUE;
	private boolean allowEmpty = true;
	
	public Event<Long> timeChanged() { return timeChanged; }
	
	public long getTime() { return time; }
	public void setTime(long time) { setTimeText(time); }
	
	public void setMinimum(long min) { this.min = min; if (max < min) max = min; if (time != 0 && time < min) setTimeText(min); }
	public void setMaximum(long max) { this.max = max; if (min > max) min = max; if (time > max) setTimeText(max); }
	public void setAllowEmpty(boolean value) { allowEmpty = value; setTimeText(time);  }
	
	@Override
	protected void checkSubclass() {
	}
	
	private void setTimeText(long date) {
		if (time == 0)
			setText("");
		else
			setText(DateTimeUtil.getTimeString(time, true, true, true, false));
	}
	
	private class TextChanged implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			String s = getText();
			long time;
			if (s.length() == 0) {
				time = 0;
				setValid(allowEmpty);
				if (!allowEmpty) return;
			} else {
				time = DateTimeUtil.getTimeFromString(s, true, true, true, false);
				if (time == 0 || time < min || time > max) {
					setValid(false);
					return;
				} else
					setValid(true);
			}
			if (TimeControl.this.time != time) {
				TimeControl.this.time = time;
				timeChanged.fire(time);
			}
		}
	}
	
	private void setValid(boolean valid) {
		setBackground(valid ? ColorUtil.getWhite() : ColorUtil.getOrange());
	}
}
