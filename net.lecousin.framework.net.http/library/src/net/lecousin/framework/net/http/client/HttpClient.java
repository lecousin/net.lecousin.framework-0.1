package net.lecousin.framework.net.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.http.HttpURI;
import net.lecousin.framework.net.http.Local;
import net.lecousin.framework.net.mime.Mime;
import net.lecousin.framework.progress.WorkProgress;

import org.apache.http.HttpStatus;

public class HttpClient {

	public HttpClient(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}
	
	private SocketFactory socketFactory;

	public HttpResponse send(HttpRequest req, boolean followRedirect, WorkProgress progress, int amount) throws UnknownHostException, IOException {
		if (Log.debug(this))
			Log.debug(this, "Send HTTP Request:\r\n" + req.toString());
		int stepSend = amount/10;
		int stepReceive = amount - stepSend;
		
		if (progress != null)
			progress.setSubDescription(Local.Contacting_server+"...");
		String reqStr = req.toString();
		Socket socket = socketFactory.createSocket(req.getHost(), req.getPort());
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();
		out.write(reqStr.getBytes());
		if (progress != null)
			progress.progress(stepSend);
		HttpResponse resp = new HttpResponse(req, in, progress, stepReceive, !followRedirect);
		in.close();
		out.close();
		socket.close();
		if (Log.debug(this))
			Log.debug("HTTP Response received: " + resp.getStatusCode() + " " + resp.getStatusDescription() + "\r\n" + (resp.getContent() != null ? resp.getContent().toString() : ""));
		if (followRedirect) {
			if (resp.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY || resp.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
				Mime mime = resp.getContent();
				if (mime == null) {
					if (Log.warning(this))
						Log.warning(this, "Unable to follow redirection: no MIME.");
					return resp;
				}
				String location = mime.getHeader().getUniqueField("location");
				if (location == null) {
					if (Log.warning(this))
						Log.warning(this, "Unable to follow redirection: no location field in response header.");
					return resp;
				}
				HttpURI uri = new HttpURI(location);
				int port = uri.getPort() == -1 ? 80 : uri.getPort();
				String path = uri.getPathWithQuery();
				if (uri.getHost().equalsIgnoreCase(req.getHost()) && port == req.getPort() && path.equalsIgnoreCase(req.getPath())) {
					if (Log.warning(this))
						Log.warning(this, "Unable to follow redirection: redirection to the same URL.");
					return resp;
				}
				req = new HttpRequest(uri.getHost(), port, path);
				return send(req, true, progress, stepReceive);
			} else if (resp.getStatusCode()/100 != 2) {
				if (progress != null)
					progress.progress(stepReceive);
			}
		}
		return resp;
	}
	
}
