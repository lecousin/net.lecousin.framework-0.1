package net.lecousin.framework.net.http.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import net.lecousin.framework.log.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class HttpServerUtil {

	private static class ErrorHttpRequestHandler implements HttpRequestHandler {
		public ErrorHttpRequestHandler(int status, String message) {
			this.status = status;
			this.message = message;
		}
		private int status;
		private String message;
		public void handle(HttpRequest req, HttpResponse resp, HttpContext ctx)
		throws HttpException, IOException {
			handleError(req, resp, status, message);
		}
	}
	public static HttpRequestHandler getErrorRequestHandler(int status, String message) {
		return new ErrorHttpRequestHandler(status, message);
	}
	
	private static class ErrorEntity extends EntityTemplate {
		public ErrorEntity(HttpRequest req, int status, String message) {
			super(new ErrorContentProducer(req, status, message));
		}
		private static class ErrorContentProducer implements ContentProducer {
			public ErrorContentProducer(HttpRequest req, int status, String message) {
				this.req = req;
				this.status = status;
				this.message = message;
			}
			private HttpRequest req;
			private int status;
			private String message;
			public void writeTo(OutputStream out) throws IOException {
				if (Log.info(this))
					Log.info(this, "ErrorProducer: " + message);
	            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8"); 
	            writer.write("<html><body>");
	            writer.write("<h1>Error ");
	            writer.write(new Integer(status).toString());
	            writer.write("</h1>");
	            writer.write(message);
	            writer.write("<p>Request:<br>");
	            writer.write("<ul>");
	            writer.write("<li>" + "Method: " + req.getRequestLine().getMethod() + "</li>");
	            writer.write("<li>" + "URI: " + req.getRequestLine().getUri() + "</li>");
	            writer.write("<li>" + "Headers:");
	            writer.write("<ul>");
	            Header[] headers = req.getAllHeaders();
	            for (int i = 0; i < headers.length; ++i) {
	            	writer.write("<li>" + headers[i].getName() + ": " + headers[i].getValue() + "</li>");
	            }
	            writer.write("</ul></li>");
	            writer.write("</ul>");
	            writer.write("</p>");
	            writer.write("</body></html>");
	            writer.flush();
			}
		}
	}
	public static HttpEntity generateErrorEntity(HttpRequest req, int status, String message) {
		return new ErrorEntity(req, status, message);
	}
	public static void handleError(HttpRequest req, HttpResponse resp, int status, String message) {
		resp.setStatusCode(status);
		resp.setEntity(generateErrorEntity(req, status, message));
	}
	
	public static String getCookie(HttpRequest req, String name) {
		Header[] headers = req.getHeaders("Cookie");
		for (int i = 0; i < headers.length; ++i) {
			String value = headers[i].getValue();
			if (Log.info(HttpServerUtil.class))
				Log.info(HttpServerUtil.class, "cookie: " + value);
			int j = value.indexOf('=');
			if (j > 0) {
				if (value.substring(0, j).trim().equals(name))
					return extractValue(value.substring(j + 1));
			}
		}
		return null;
	}
	public static String getCookie(HttpResponse resp, String name) {
		Header[] headers = resp.getHeaders("Set-Cookie");
		for (int i = 0; i < headers.length; ++i) {
			String value = headers[i].getValue();
			if (Log.info(HttpServerUtil.class))
				Log.info(HttpServerUtil.class, "set-cookie: " + value);
			int j = value.indexOf('=');
			if (j > 0) {
				if (value.substring(0, j).trim().equals(name))
					return extractValue(value.substring(j + 1));
			}
		}
		return null;
	}
	private static String extractValue(String s) {
		int i = s.indexOf('"', 1);
		return s.substring(1, i);
	}
	public static void setCookie(HttpResponse resp, String name, String value) {
		resp.setHeader("Set-Cookie", name + "=\"" + value + "\"; Version=\"1\"");
	}
	
	public static void redirect(HttpResponse resp, String url) {
		resp.setStatusCode(HttpStatus.SC_TEMPORARY_REDIRECT);
		resp.setHeader("Location", url);
	}
}
