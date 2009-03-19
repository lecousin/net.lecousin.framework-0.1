package net.lecousin.framework.ui.eclipse.control.date;

import java.util.Calendar;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.math.RangeLong;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.Local;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DateRangePanel extends Composite {

	public DateRangePanel(Composite parent) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.wrap = true;
		layout.marginHeight = 0; layout.marginWidth = 0;
		layout.marginBottom = layout.marginLeft = layout.marginRight = layout.marginTop = 0;
		layout.spacing = 0;
		layout.fill = true;
		layout.center = true;
		setLayout(layout);
		
		from = new DateControl(this);
		Label label = UIUtil.newLabel(this, Local.to_date.toString());
		label.setAlignment(SWT.CENTER);
		to = new DateControl(this);
		
		from.dateChanged().addListener(new Listener<Long>() {
			public void fire(Long event) {
				if (event > min) to.setMinimum(event); else to.setMinimum(min);
				rangeChanged.fire(new RangeLong(from.getDate(), to.getDate()));
			}
		});
		to.dateChanged().addListener(new Listener<Long>() {
			public void fire(Long event) {
				if (event < max && event != 0) from.setMaximum(event); else from.setMaximum(max);
				rangeChanged.fire(new RangeLong(from.getDate(), to.getDate()));
			}
		});
	}
	
	private DateControl from, to;
	private long min = 0, max = Long.MAX_VALUE;
	private Event<RangeLong> rangeChanged = new Event<RangeLong>();
	
	public Event<RangeLong> rangeChanged() { return rangeChanged; }
	
	public void setMinimum(long min) {
		this.min = normalize(min);
		from.setMinimum(this.min);
		to.setMinimum(this.min);
	}
	public void setMaximum(long max) {
		this.max = normalize(max);
		to.setMaximum(this.max);
		from.setMaximum(this.max);
	}
	private long normalize(long date) {
		if (date == 0) return 0;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date);
		DateTimeUtil.resetHours(c);
		return c.getTimeInMillis();
	}
	
	public long getMinimum() { return min; }
	public long getMaximum() { return max; }
	
}
