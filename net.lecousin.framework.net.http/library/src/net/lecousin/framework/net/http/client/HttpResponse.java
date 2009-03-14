package net.lecousin.framework.net.http.client;

import java.io.IOException;
import java.io.InputStream;

import net.lecousin.framework.io.IOUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.mime.Mime;
import net.lecousin.framework.progress.WorkProgress;

public class HttpResponse {

	public HttpResponse(InputStream in, WorkProgress progress, int amount) throws IOException {
		String statusLine = IOUtil.readPart(in, "\r\n");
		int i = statusLine.indexOf(' ');
		if (i < 0) {
			if (Log.error(this))
				Log.error(this, "Invalid HTTP status line: " + statusLine);
			return;
		}
		protocol = statusLine.substring(0,i);
		int j = statusLine.indexOf(' ', i+1);
		if (j < 0) {
			if (Log.error(this))
				Log.error(this, "Invalid HTTP status line: " + statusLine);
			return;
		}
		statusCode = Integer.parseInt(statusLine.substring(i+1,j));
		statusDescription = statusLine.substring(j+1);
		if (Log.debug(this))
			Log.debug(this, "HTTP Response status: " + statusCode + " " + statusDescription);
		mime = new Mime(in, progress, amount);
	}
	
	private String protocol = "";
	private int statusCode = -1;
	private String statusDescription = "";
	private Mime mime;
	
	public int getStatusCode() { return statusCode; }
	public String getStatusDescription() { return statusDescription; }
	public Mime getContent() { return mime; } 
	
}
