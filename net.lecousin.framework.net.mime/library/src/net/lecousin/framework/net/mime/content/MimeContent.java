package net.lecousin.framework.net.mime.content;

import java.io.IOException;
import java.io.OutputStream;

public abstract class MimeContent {

	public abstract String getAsString() throws IOException;
	public abstract void write(OutputStream out) throws IOException;
	
	@Override
	public String toString() {
		try { return getAsString(); }
		catch (IOException e) {
			return "";
		}
	}
}
