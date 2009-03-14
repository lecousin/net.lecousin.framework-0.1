package net.lecousin.framework.net.http.client;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lecousin.framework.Pair;
import net.lecousin.framework.net.http.HttpURI;

public class HttpRequest {

	public HttpRequest(String host) {
		this(host, 80, "/");
	}
	public HttpRequest(String host, String path) {
		this(host, 80, path);
	}
	public HttpRequest(String host, int port) {
		this(host, port, "/");
	}
	public HttpRequest(String host, int port, String path) {
		this.host = host;
		this.port = port;
		this.path = path;
	}
	
	public static HttpRequest fromURL(String url, String defaultHost, int defaultPort) {
		HttpURI uri = new HttpURI(url);
		String host = uri.getHost();
		int port = uri.getPort();
		if (host == null) {
			host = defaultHost;
			port = defaultPort;
		} else if (port == -1)
			port = 80;
		String path = uri.getPath();
		HttpRequest req = new HttpRequest(host, port, path);
		for (Map.Entry<String, String> e : uri.getQuery().entrySet())
			req.addParameter(e.getKey(), e.getValue());
		return req;
	}
	
	public enum Method {
		GET,
	}
	
	private String host;
	private int port;
	private String path;
	private List<Pair<String,Object>> parameters = new LinkedList<Pair<String,Object>>();
	
	private Method method = Method.GET;
	
	private Map<String,String> header = new HashMap<String,String>();
	
	public String getHost() { return host; }
	public int getPort() { return port; }
	public String getPath() { return path; }
	public Method getMethod() { return method; }
	public void setMethod(Method m) { method = m; }
	public String getHeader(String name) { return header.get(name); }
	public Set<Map.Entry<String,String>> getHeaders() { return header.entrySet(); }
	public void setHeader(String name, String value) {
		if (name.equalsIgnoreCase("host")) return;
		header.put(name, value);
	}
	public void addParameter(String name, Object value) {
		parameters.add(new Pair<String,Object>(name, value));
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(method.toString()).append(' ').append(path);
		if (method.equals(Method.GET)) {
			if (!parameters.isEmpty()) {
				str.append('?');
				boolean first = true;
				for (Pair<String,Object> p : parameters) {
					if (first) first = false;
					else str.append('&');
					str.append(URLEncoder.encode(p.getValue1()));
					str.append('=');
					str.append(URLEncoder.encode(p.getValue2().toString()));
				}
			}
		}
		str.append(" HTTP/1.1\r\n");
		if (!header.containsKey("User-Agent")) header.put("User-Agent", "net.lecousin.framework.http.client.HttpClient/0.0.1");
		str.append("Host: ").append(host);
		if (port != 80) str.append(':').append(port);
		str.append("\r\n");
		for (Map.Entry<String,String> h : header.entrySet())
			str.append(h.getKey()).append(": ").append(h.getValue()).append("\r\n");
		str.append("\r\n");
		return str.toString();
	}
	
}
