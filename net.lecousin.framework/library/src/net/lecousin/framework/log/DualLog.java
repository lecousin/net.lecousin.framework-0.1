package net.lecousin.framework.log;

public class DualLog extends Log {

	public DualLog(Log log1, Log log2) {
		l1 = log1;
		l2 = log2;
	}
	
	private Log l1, l2;

	@Override
	public boolean enabled(Severity sev) {
		return l1.enabled(sev) || l2.enabled(sev);
	}

	@Override
	public void logMessage(Severity sev, Class<?> cl, String message) {
		if (l1.enabled(sev))
			l1.logMessage(sev, cl, message);
		if (l2.enabled(sev))
			l2.logMessage(sev, cl, message);
	}

}
