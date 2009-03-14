package net.lecousin.framework.stats;

import java.util.Iterator;

import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.LinkedArrayList;

public class EventOnOffStatistics implements Statistics {

	@SuppressWarnings("unchecked")
	private LinkedArrayList<Pair<Long,Long>> events = new LinkedArrayList<Pair<Long,Long>>(100, (Class<Pair<Long,Long>>)new Pair<Long,Long>((long)0,(long)0).getClass());
	private long current = -1;
	
	public void on(long time) {
		current = time;
	}
	public void off(long time) {
		if (current != -1)
			events.add(new Pair<Long,Long>(current, time));
	}
	
	public void cleanBefore(long time) {
		for (Iterator<Pair<Long,Long>> it = events.iterator(); it.hasNext(); ) {
			Pair<Long,Long> p = it.next();
			if (p.getValue2() < time) it.remove();
		}
	}

	
	public StatisticsProvider getProvider() { return new Provider(); }
	
	private class Provider implements StatisticsProvider {
		public double getAmountOnRange(long start, long end) {
			double amount = 0;
			for (Pair<Long,Long> time : events)
				if (time.getValue2() >= start && time.getValue1() <= end)
					amount++;
			return amount;
		}
	}
}
