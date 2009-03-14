package net.lecousin.framework.ui.eclipse.control.stats;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.stats.Statistics;
import net.lecousin.framework.stats.StatisticsProvider;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.time.DateTimeUtil;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.Radio;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

public abstract class StatisticsControl extends Composite implements PaintListener {

	public StatisticsControl(Composite parent, int style) {
		super(parent, style);
		UIUtil.gridLayout(this, 1);
		canvas = new Canvas(this, SWT.NONE);
		canvas.setLayoutData(UIUtil.gridData(1, true, 1, true));
		canvas.setBackground(ColorUtil.getBlack());
		panel = new Composite(this, SWT.NONE);
		panel.setLayoutData(UIUtil.gridData(1, true, 1, false));
		createPanelContent(panel);
		canvas.addPaintListener(this);
	}

	private Canvas canvas;
	private Composite panel;
	private long endTime = System.currentTimeMillis();
	private long startTime = 0;
	private List<Stat> stats = new LinkedList<Stat>();
	
	private static class Stat {
		Stat(Statistics s, String n, Color c)
		{ stat = s; name = n; color = c; }
		Statistics stat;
		String name;
		Color color;
	}
	
	public void setStartTime(long startTime) { this.startTime = startTime; }
	public void setEndTime(long endTime) { this.endTime = endTime; }
	
	public void addStatistics(Statistics stats, String name, Color color) {
		this.stats.add(new Stat(stats, name, color));
		Button button = new Button(statsGroup, SWT.CHECK);
		button.setText(name);
		button.setBackground(color);
		button.setLayoutData(UIUtil.gridDataHoriz(1, true));
		button.setSelection(false);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				canvas.redraw();
			}
		});
		UIControlUtil.autoresize(button);
		canvas.redraw();
	}
	
	private List<Stat> getActivatedStats() {
		List<Stat> list = new LinkedList<Stat>();
		for (Stat s : stats)
			for (Control c : statsGroup.getChildren()) {
				if (c instanceof Button && ((Button)c).getText().equals(s.name)) {
					if (((Button)c).getSelection())
						list.add(s);
					break;
				}
			}
		return list;
	}
	
	protected abstract Calendar getCalendar(long time);
	protected abstract long getTime(Calendar c);
	private double getAmount(Statistics stat, long start, long end) {
		StatisticsProvider provider = stat.getProvider();
		if (scaleRadio.getSelection().equals(SCALE_FULL))
			return provider.getAmountOnRange(start, end);
		if (scaleRadio.getSelection().equals(SCALE_WEEK)) {
			Calendar c = getCalendar(startTime);
			int day = c.get(Calendar.DAY_OF_MONTH)-1;
			DateTimeUtil.resetHours(c);
			c.add(Calendar.DAY_OF_MONTH, 1);
			long t2 = getTime(c);
			double amount = 0;
			if (day >= start && day <= end)
				amount = provider.getAmountOnRange(startTime, t2 > endTime ? endTime : t2);
			long t1;
			while (t2 < endTime) {
				t1 = t2;
				c.add(Calendar.DAY_OF_MONTH, 1);
				t2 = getTime(c);
				if (++day == 7) day = 0;
				if (day >= start && day <= end)
					amount += provider.getAmountOnRange(t1, t2 > endTime ? endTime : t2);
			}
			return amount;
		}
		if (scaleRadio.getSelection().equals(SCALE_DAY)) {
			return 0; // TODO start and end are in minutes of the day
		}
		return 0;
	}
	private long getStart() {
		if (scaleRadio.getSelection().equals(SCALE_FULL))
			return startTime;
		if (scaleRadio.getSelection().equals(SCALE_WEEK))
			return 0;
		if (scaleRadio.getSelection().equals(SCALE_DAY))
			return 0;
		return 0;
	}
	private long getEnd() {
		if (scaleRadio.getSelection().equals(SCALE_FULL))
			return endTime;
		if (scaleRadio.getSelection().equals(SCALE_WEEK))
			return 7;
		if (scaleRadio.getSelection().equals(SCALE_DAY))
			return 24*60;
		return 0;
	}
	private double getTimeIncrement(Rectangle bounds) {
		if (scaleRadio.getSelection().equals(SCALE_FULL))
			return ((double)(endTime - startTime)) / bounds.width;
		if (scaleRadio.getSelection().equals(SCALE_WEEK))
			return 1;
		if (scaleRadio.getSelection().equals(SCALE_DAY))
			return 1;
		return 0;
	}
	private double getPixelIncrement(Rectangle bounds) {
		if (scaleRadio.getSelection().equals(SCALE_FULL))
			return 1;
		if (scaleRadio.getSelection().equals(SCALE_WEEK))
			return (double)bounds.width/(double)7;
		if (scaleRadio.getSelection().equals(SCALE_DAY))
			return (double)bounds.width/(double)(24*60);
		return 0;
	}
	
	private static final String[] dayName = new String[] { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	public void paintControl(PaintEvent e) {
		Rectangle boundsCanvas = canvas.getBounds();
		List<Stat> stats = getActivatedStats();
		double incXTime = getTimeIncrement(boundsCanvas);
		double incXPixel = getPixelIncrement(boundsCanvas);
		
		// paint scale
		double max = 0;
		for (Stat stat : stats) {
			double start = getStart();
			double end = start;
			int lastX = -1;
			for (double x = 0; x < boundsCanvas.width; x += incXPixel) {
				end += incXTime;
				if (((int)x) == lastX)
					continue;
				if (((long)start) == ((long)end))
					continue;
				double amount = getAmount(stat.stat, (long)start, (long)end);
				if (amount > max) max = amount;
				lastX = (int)x;
				start = end;
			}
		}
		
		int top;
		if (max < 1) top = 1;
		else if (max < 5) top = 5;
		else if (max < 10) top = 10;
		else if (max < 100)	for (top = 20; top < max; top += 10);
		else if (max < 1000) for (top = 200; top < max; top += 100);
		else if (max < 10000) for (top = 2000; top < max; top += 1000);
		else if (max < 100000) for (top = 20000; top < max; top += 10000);
		else if (max < 1000000) for (top = 200000; top < max; top += 100000);
		else if (max < 10000000) for (top = 2000000; top < max; top += 1000000);
		else if (max < 100000000) for (top = 20000000; top < max; top += 10000000);
		else if (max < 1000000000) for (top = 200000000; top < max; top += 100000000);
		else top = 1000000000;
		
		e.gc.setForeground(ColorUtil.getWhite());
		Point pt = e.gc.textExtent(Integer.toString(top));
		Rectangle boundsContent = new Rectangle(boundsCanvas.x + pt.x + 5, boundsCanvas.y + pt.y, 0, 0);
		pt = e.gc.textExtent(DateTimeUtil.getDateTimeString(endTime, 1));
		boundsContent.width = boundsCanvas.width - (boundsContent.x - boundsCanvas.x) - pt.x/2;
		boundsContent.height = boundsCanvas.height - (boundsContent.y - boundsCanvas.y) - pt.y - 10;
		
		e.gc.drawLine(boundsContent.x, boundsContent.y, boundsContent.x, boundsContent.y + boundsContent.height);
		e.gc.drawLine(boundsContent.x, boundsContent.y + boundsContent.height, boundsContent.x + boundsContent.width, boundsContent.y + boundsContent.height);
		
		for (int i = 0; i <= 10; ++i) {
			int y = boundsContent.y + boundsContent.height - (i*boundsContent.height)/10;
			e.gc.setForeground(ColorUtil.getWhite());
			String str = top >= 10 ? Integer.toString(i*top/10) : Integer.toString(i*top/10)+"."+Integer.toString(i*top%10); 
			pt = e.gc.textExtent(str);
			e.gc.drawText(str, boundsContent.x - 4 - pt.x, y-pt.y/2);
			e.gc.drawLine(boundsContent.x - 3, y, boundsContent.x, y);
			e.gc.setForeground(ColorUtil.get(120, 120, 120));
			e.gc.drawLine(boundsContent.x+1, y, boundsContent.x+boundsContent.width, y);
		}
		if (scaleRadio.getSelection().equals(SCALE_FULL)) {
			// TODO horizontal axis
		} else if (scaleRadio.getSelection().equals(SCALE_WEEK)) {
			int w = boundsContent.width/7;
			for (int i = 0; i < 8; ++i) {
				int x = boundsContent.x+i*w;
				e.gc.setForeground(ColorUtil.getWhite());
				e.gc.drawLine(x, boundsContent.y + boundsContent.height, x, boundsContent.y + boundsContent.height + 3);
				e.gc.setForeground(ColorUtil.get(120, 120, 120));
				e.gc.drawLine(x, boundsContent.y + boundsContent.height, x, boundsContent.y);
				if (i < 7) {
					pt = e.gc.textExtent(dayName[i]);
					e.gc.setForeground(ColorUtil.getWhite());
					e.gc.drawText(dayName[i], x+w/2-pt.x/2, boundsContent.y + boundsContent.height + 4);
				}
			}
		} else if (scaleRadio.getSelection().equals(SCALE_DAY)) {
			// TODO horizontal axis
		}
		
		
		double incY = ((double)boundsContent.height)/(double)top;
		incXTime = getTimeIncrement(boundsContent);
		incXPixel = getPixelIncrement(boundsContent);

		int statIndex = 0;
		for (Stat stat : stats) {
			Point lastPoint = new Point(boundsContent.x, boundsContent.y + boundsContent.height);
			double start = getStart();
			double end = start;
			int lastX = -1;
			int xIndex = 0;
			for (double x = 0; x < boundsContent.width; x += incXPixel, ++xIndex) {
				end += incXTime;
				if (((int)x) == lastX)
					continue;
				if (((long)start) == ((long)end))
					continue;
				double amount = getAmount(stat.stat, (long)start, (long)end);
				int y = boundsContent.y + boundsContent.height - ((int)(amount*incY));
				if (scaleRadio.getSelection().equals(SCALE_FULL)) {
					Point newPoint = new Point(boundsContent.x + (int)x, y);
					if (lastPoint.x != boundsContent.x) {
						e.gc.setForeground(stat.color);
						e.gc.drawLine(lastPoint.x, lastPoint.y, newPoint.x, newPoint.y);
					}
					lastPoint = newPoint;
				} else if (scaleRadio.getSelection().equals(SCALE_WEEK)) {
					Point newPoint = new Point(boundsContent.x + (int)(x+incXPixel), y);
					Rectangle r = new Rectangle((int)(lastPoint.x+statIndex*(incXPixel/stats.size())), y, (int)(incXPixel/stats.size()-1), boundsContent.height-(y-boundsContent.y));
					e.gc.setBackground(stat.color);
					e.gc.fillRectangle(r);
					String str = StringUtil.toStringSep(amount, 2);
					pt = e.gc.textExtent(str);
					e.gc.setBackground(canvas.getBackground());
					e.gc.setForeground(stat.color);
					e.gc.drawText(str, r.x+r.width/2-pt.x/2, r.y-pt.y);
					lastPoint = newPoint;
					if (xIndex == 6) break;
				} else if (scaleRadio.getSelection().equals(SCALE_DAY)) {
					// TODO
				}
				
				lastX = (int)x;
				start = end;
			}
			statIndex++;
		}
	}
	
	private Group statsGroup;
	private Radio scaleRadio;
	private void createPanelContent(Composite panel) {
		RowLayout layout = new RowLayout();
		layout.wrap = true;
		layout.pack = true;
		layout.type = SWT.HORIZONTAL;
		panel.setLayout(layout);
		
		statsGroup = new Group(panel, SWT.SHADOW_IN);
		statsGroup.setText("Statistics");
		UIUtil.gridLayout(statsGroup, 1);
		
		Group scaleGroup = new Group(panel, SWT.SHADOW_IN);
		scaleGroup.setText("Scale");
		UIUtil.gridLayout(scaleGroup, 1);
		scaleRadio = new Radio(scaleGroup, false);
		scaleRadio.setLayoutData(UIUtil.gridDataHoriz(1, true));
		scaleRadio.addOption(SCALE_FULL, "Full time");
		scaleRadio.addOption(SCALE_WEEK, "Week");
		scaleRadio.addOption(SCALE_DAY, "Day");
		scaleRadio.setSelection(SCALE_FULL);
		scaleRadio.addSelectionChangedListener(new Listener<String>() {
			public void fire(String event) {
				canvas.redraw();
			}
		});
	}
	private static final String SCALE_FULL = "full";
	private static final String SCALE_WEEK = "week";
	private static final String SCALE_DAY = "day";
}
