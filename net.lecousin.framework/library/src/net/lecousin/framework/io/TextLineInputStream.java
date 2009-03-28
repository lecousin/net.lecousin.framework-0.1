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
			c = in.read();
		} else
			pushBack = -1;
		if (c == -1)
			return null;
		while (c != '\n') {
			if (c == '\r') {
				lineNumber++;
				pushBack = in.read();
				if (pushBack == -1)
					return s.toString();
				if (pushBack == '\n')
					pushBack = -1;
				return s.toString();
			}
			s.append((char)c);
			c = in.read();
			if (c == -1) break;
		}
		lineNumber++;
		return s.toString();
	}
	
	public int getLineNumber() { return lineNumber; }
	
	public void close() throws IOException { in.close(); }
}
