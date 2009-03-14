package net.lecousin.framework.log;

import java.io.PrintStream;

public class LogConsole extends Log {

	public LogConsole(Severity maxLevel) {
		this.maxLevel = maxLevel;
	}
	
	private Severity maxLevel;
	
	@Override
	public boolean enabled(Severity sev) {
		return sev.level() <= maxLevel.level();
	}

	@Override
	public void logMessage(Severity sev, Class<?> cl, String message) {
		PrintStream stream;
		switch (sev) {
		case DEBUG:
		case INFO:
		case WARNING: 
		default:
			stream = System.out; break;
		case ERROR:
		case FATAL:
			stream = System.err; break;
		}
		stream.println(message);
	}

}
