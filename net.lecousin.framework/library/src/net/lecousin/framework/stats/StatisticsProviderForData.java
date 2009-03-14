package net.lecousin.framework.stats;

public interface StatisticsProviderForData<T> {

	public double getAmountOnRangeForData(T data, long start, long end);
	
}
