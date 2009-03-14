package net.lecousin.framework.stats;

import java.util.Iterator;

import net.lecousin.framework.collections.LinkedArrayList;

public class EventStatistics implements Statistics {

	private LinkedArrayList<Long> events = new LinkedArrayList<Long>(100, Long.class);
	
	public void signal(long time) {
		events.add(time);
	}
	
	public void cleanBefore(long time) {
		for (Iterator<Long> it = events.iterator(); it.hasNext(); ) {
			Long l = it.next();
			if (l < time) it.remove();
		}
	}
	
	public StatisticsProvider getProvider() { return new Provider(); }
	
	private class Provider implements StatisticsProvider {
		public double getAmountOnRange(long start, long end) {
			double amount = 0;
			for (Long time : events)
				if (time >= start && time <= end)
					amount++;
			return amount;
		}
	}
}
