package net.lecousin.framework.io;

import java.io.IOException;
import java.io.InputStream;

public class TextLineInputStream {

	public TextLineInputStream(InputStream in) {
		this.in = in;
	}
	
	private InputStream in;
	private int lineNumber = 1;
	private int pushBack = -1;
	
	public String readLine() throws IOException {
		StringBuilder s = new StringBuilder();
		int c = pushBack;
		if (c == -1) {
			if (in.available() == 0) return "";
			c = in.read();
		} else
			pushBack = -1;
		while (c != '\n') {
			if (c == '\r') {
				lineNumber++;
				if (in.available() == 0)
					return s.toString();
				pushBack = in.read();
				if (pushBack == '\n')
					pushBack = -1;
				return s.toString();
			}
			s.append((char)c);
			if (in.available() == 0) break;
			c = in.read();
		}
		lineNumber++;
		return s.toString();
	}
	
	public boolean isEndOfStream() { try { return in.available() == 0; } catch (IOException e) { return true; } }
	public int getLineNumber() { return lineNumber; }
	
	public void close() throws IOException { in.close(); }
}
