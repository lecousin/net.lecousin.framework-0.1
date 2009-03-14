package net.lecousin.framework.net.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.net.SocketFactory;

import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.http.client.HttpClient;
import net.lecousin.framework.net.http.client.HttpRequest;
import net.lecousin.framework.net.http.client.HttpResponse;
import net.lecousin.framework.net.mime.Mime;
import net.lecousin.framework.net.mime.content.MimeContent;
import net.lecousin.framework.progress.WorkProgress;

public class HttpUtil {

	public static boolean retrieveFile(String host, int port, String path, File target, boolean followRedirect, WorkProgress progress, int amount) {
		return retrieveFile(new HttpRequest(host, port, path), target, followRedirect, progress, amount);
	}

	public static boolean retrieveFile(HttpRequest req, File target, boolean followRedirect, WorkProgress progress, int amount) {
		try {
			HttpClient client = new HttpClient(SocketFactory.getDefault());
			HttpResponse resp = client.send(req, followRedirect, progress, amount);
			if (resp.getStatusCode() != 200) {
				if (Log.info(HttpUtil.class))
					Log.info(HttpUtil.class, "Unable to retrieve " + req.getPath() + " on " + req.getHost() + ":" + req.getPort() + ": " + resp.getStatusCode() + " " + resp.getStatusDescription());
				return false;
			}
			Mime mime = resp.getContent();
			if (mime == null) return false;
			MimeContent content = mime.getContent();
			if (content == null) return false;
			if (!target.exists()) target.createNewFile();
			FileOutputStream out = new FileOutputStream(target);
			try { content.write(out); } catch (IOException e) { out.close(); throw e; }
			out.flush();
			out.close();
			return true;
		} catch (IOException e) {
			if (Log.info(HttpUtil.class))
				Log.info(HttpUtil.class, "Unable to retrieve " + req.getPath() + " on " + req.getHost() + ":" + req.getPort() + " to " + target.getAbsolutePath() + " : " + e.getMessage());
			return false;
		}
	}
}
