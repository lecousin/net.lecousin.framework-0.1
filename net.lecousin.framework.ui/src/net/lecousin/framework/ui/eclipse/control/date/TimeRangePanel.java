package net.lecousin.framework.ui.eclipse.control.date;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.math.RangeLong;
import net.lecousin.framework.ui.eclipse.Local;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class TimeRangePanel extends Composite {

	public TimeRangePanel(Composite parent) {
		super(parent, SWT.NONE);
		setBackground(parent.getBackground());
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.wrap = true;
		layout.marginHeight = 0; layout.marginWidth = 0;
		layout.marginBottom = layout.marginLeft = layout.marginRight = layout.marginTop = 0;
		layout.spacing = 2;
		layout.fill = true;
		layout.center = true;
		setLayout(layout);
		
		from = new TimeControl(this);
		RowData rd = new RowData();
		rd.width = 45;
		from.setLayoutData(rd);
		Label label = UIUtil.newLabel(this, Local.to__time.toString());
		label.setAlignment(SWT.CENTER);
		to = new TimeControl(this);
		rd = new RowData();
		rd.width = 45;
		to.setLayoutData(rd);
		
		from.timeChanged().addListener(new Listener<Long>() {
			public void fire(Long event) {
				if (event > min) to.setMinimum(event); else to.setMinimum(min);
				rangeChanged.fire(new RangeLong(from.getTime(), to.getTime()));
			}
		});
		to.timeChanged().addListener(new Listener<Long>() {
			public void fire(Long event) {
				if (event < max && event != 0) from.setMaximum(event); else from.setMaximum(max);
				rangeChanged.fire(new RangeLong(from.getTime(), to.getTime()));
			}
		});
	}
	
	private TimeControl from, to;
	private long min = 0, max = Long.MAX_VALUE;
	private Event<RangeLong> rangeChanged = new Event<RangeLong>();
	
	public Event<RangeLong> rangeChanged() { return rangeChanged; }
	
	public void setMinimum(long min) {
		this.min = min;
		from.setMinimum(this.min);
		to.setMinimum(this.min);
	}
	public void setMaximum(long max) {
		this.max = max;
		from.setMaximum(this.max);
		to.setMaximum(this.max);
	}
	public void setRange(RangeLong range) {
		from.setTime(range.min);
		to.setTime(range.max);
	}
	
	public long getMinimum() { return min; }
	public long getMaximum() { return max; }
	
}
