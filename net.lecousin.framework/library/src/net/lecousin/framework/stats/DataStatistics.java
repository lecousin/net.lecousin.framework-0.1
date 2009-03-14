package net.lecousin.framework.stats;

public interface DataStatistics<T> extends Statistics {

	public StatisticsProviderForData<T> getDataProvider();
	
}
