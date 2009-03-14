package net.lecousin.framework.stats;

import java.util.Iterator;

import net.lecousin.framework.Pair;
import net.lecousin.framework.collections.LinkedArrayList;

public class EventDataStatistics<T> implements DataStatistics<T> {

	@SuppressWarnings("unchecked")
	private LinkedArrayList<Pair<Long,T>> events = new LinkedArrayList<Pair<Long,T>>(100, (Class<Pair<Long,T>>)new Pair<Long,T>((long)0,null).getClass());
	
	public void signal(T data, long time) {
		events.add(new Pair<Long,T>(time, data));
	}

	public void cleanBefore(long time) {
		for (Iterator<Pair<Long,T>> it = events.iterator(); it.hasNext(); ) {
			Pair<Long,T> p = it.next();
			if (p.getValue1() < time) it.remove();
		}
	}
	
	public StatisticsProvider getProvider() { return new Provider(); }
	public StatisticsProviderForData<T> getDataProvider() { return new DataProvider(); }
	
	private class Provider implements StatisticsProvider {
		public double getAmountOnRange(long start, long end) {
			double amount = 0;
			for (Pair<Long,T> time : events)
				if (time.getValue1() >= start && time.getValue1() <= end)
					amount++;
			return amount;
		}
	}
	
	private class DataProvider implements StatisticsProviderForData<T> {
		public double getAmountOnRangeForData(T data, long start, long end) {
			double amount = 0;
			for (Pair<Long,T> time : events)
				if (time.getValue2().equals(data) && time.getValue1() >= start && time.getValue1() <= end)
					amount++;
			return amount;
		}
	}
}
