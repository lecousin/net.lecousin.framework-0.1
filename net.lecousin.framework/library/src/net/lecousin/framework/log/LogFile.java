package net.lecousin.framework.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class LogFile extends Log {

	public LogFile(Severity maxLevel, String filename) throws IOException {
		this.maxLevel = maxLevel;
		File file = new File(filename);
		if (!file.exists())
			file.createNewFile();
		out = new FileOutputStream(file);
	}
	
	private Severity maxLevel;
	private FileOutputStream out;
	
	@Override
	public boolean enabled(Severity sev) {
		return sev.level() <= maxLevel.level();
	}

	@Override
	public void logMessage(Severity sev, Class<?> cl, String message) {
		try {
			out.write(message.getBytes());
			out.write("\r\n".getBytes());
			out.flush();
		} catch (IOException e) {
			System.err.println("Cannot log: " + e.getClass().getName() + ":" + e.getMessage());
		}
	}

}
