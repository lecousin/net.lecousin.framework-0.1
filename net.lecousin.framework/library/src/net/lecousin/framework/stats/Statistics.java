package net.lecousin.framework.stats;

public interface Statistics {

	public void cleanBefore(long time);
	
	public StatisticsProvider getProvider();
}
