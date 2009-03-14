package net.lecousin.framework.stats;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.lecousin.framework.Triple;
import net.lecousin.framework.collections.LinkedArrayList;

public class EventOnOffDataStatistics<T> implements DataStatistics<T> {

	@SuppressWarnings("unchecked")
	private LinkedArrayList<Triple<T,Long,Long>> events = new LinkedArrayList<Triple<T,Long,Long>>(100, (Class<Triple<T,Long,Long>>)new Triple<T,Long,Long>(null, (long)0,(long)0).getClass());
	private Map<T,Long> currents = new HashMap<T,Long>();
	
	public void on(T data, long time) {
		currents.put(data, time);
	}
	public void off(T data, long time) {
		Long on = currents.get(data);
		if (on == null) return;
		events.add(new Triple<T,Long,Long>(data, on, time));
	}
	
	public void cleanBefore(long time) {
		for (Iterator<Triple<T,Long,Long>> it = events.iterator(); it.hasNext(); ) {
			Triple<T,Long,Long> t = it.next();
			if (t.getValue3() < time) it.remove();
		}
	}

	
	public StatisticsProvider getProvider() { return new Provider(); }
	public StatisticsProviderForData<T> getDataProvider() { return new DataProvider(); }
	
	private class Provider implements StatisticsProvider {
		public double getAmountOnRange(long start, long end) {
			double amount = 0;
			for (Triple<T,Long,Long> time : events)
				if (time.getValue3() >= start && time.getValue2() <= end)
					amount++;
			return amount;
		}
	}

	private class DataProvider implements StatisticsProviderForData<T> {
		public double getAmountOnRangeForData(T data, long start, long end) {
			double amount = 0;
			for (Triple<T,Long,Long> time : events)
				if (time.getValue1().equals(data) && time.getValue3() >= start && time.getValue2() <= end)
					amount++;
			return amount;
		}
	}
}
