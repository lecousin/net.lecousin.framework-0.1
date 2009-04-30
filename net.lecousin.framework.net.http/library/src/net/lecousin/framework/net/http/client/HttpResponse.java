package net.lecousin.framework.net.http.client;

import java.io.IOException;
import java.io.InputStream;

import net.lecousin.framework.io.IOUtil;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.http.Local;
import net.lecousin.framework.net.mime.Mime;
import net.lecousin.framework.net.mime.content.MimeContent;
import net.lecousin.framework.progress.WorkProgress;

public class HttpResponse {

	public HttpResponse(HttpRequest request, InputStream in, WorkProgress progress, int amount, boolean progressIfNotOK) throws IOException {
		this.request = request;
		if (progress != null)
			progress.setSubDescription(Local.Waiting_response+"...");
		String statusLine = IOUtil.readPart(in, "\r\n");
		int i = statusLine.indexOf(' ');
		if (i < 0) {
			if (Log.error(this))
				Log.error(this, "Invalid HTTP status line: " + statusLine);
			if (progressIfNotOK)
				progress.progress(amount);
			return;
		}
		protocol = statusLine.substring(0,i);
		int j = statusLine.indexOf(' ', i+1);
		if (j < 0) {
			if (Log.error(this))
				Log.error(this, "Invalid HTTP status line: " + statusLine);
			if (progressIfNotOK)
				progress.progress(amount);
			return;
		}
		statusCode = Integer.parseInt(statusLine.substring(i+1,j));
		statusDescription = statusLine.substring(j+1);
		if (Log.debug(this))
			Log.debug(this, "HTTP Response status: " + statusCode + " " + statusDescription);
		if (!progressIfNotOK && statusCode/100 != 2)
			progress = null;
		mime = new Mime(in, progress, amount);
	}
	
	private HttpRequest request;
	private String protocol = "";
	private int statusCode = -1;
	private String statusDescription = "";
	private Mime mime;
	
	public HttpRequest getRequest() { return request; }
	public int getStatusCode() { return statusCode; }
	public String getStatusDescription() { return statusDescription; }
	public Mime getContent() { return mime; } 
	
	/** Utility method that provides content as a string or null if not possible */
	public String getContentAsString() throws IOException {
		if (mime == null) return null;
		MimeContent content = mime.getContent();
		if (content == null) return null;
		return content.getAsString();
	}
}
