package net.lecousin.framework.net.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpURI {

	public HttpURI(String uri) {
		int i;
		i = uri.indexOf('#');
		if (i >= 0) {
			fragment = uri.substring(i + 1);
			uri = uri.substring(0, i);
		}
		i = uri.indexOf('?');
		if (i >= 0) {
			parseQuery(uri.substring(i + 1));
			uri = uri.substring(0, i);
		}
		i = uri.indexOf("://");
		if (i < 0) {
			host = null;
			i = uri.indexOf(':');
			if (i >= 0) {
				protocol = uri.substring(0, i);
				uri = uri.substring(i+1);
			} else
				protocol = null;
		} else {
			protocol = uri.substring(0, i);
			uri = uri.substring(i + 3);
			i = uri.indexOf('/');
			if (i == 0) {
				uri = uri.substring(1);
				i = uri.indexOf('/');
			}
			if (i < 0) {
				host = uri;
				uri = "";
			} else {
				host = uri.substring(0, i);
				uri = uri.substring(i);
			}
			i = host.indexOf(':');
			if (i >= 0) {
				try {
					port = Integer.parseInt(host.substring(i + 1));
				} catch (NumberFormatException e) {
					// skip
				}
				host = host.substring(0, i);
			}
		}
		path = uri;
	}
	
	private String protocol;
	private String host;
	private int port = -1;
	private String path;
	private Map<String,String> query = new HashMap<String,String>();
	private String fragment;
	
	private void parseQuery(String s) {
		int i;
		do {
			i = s.indexOf('&');
			String str;
			if (i < 0)
				str = s;
			else {
				str = s.substring(0, i);
				s = s.substring(i + 1);
			}
			int j = str.indexOf('=');
			String name, value;
			if (j < 0) {
				name = str;
				value = "";
			} else {
				name = str.substring(0, j);
				value = str.substring(j + 1);
			}
			query.put(name, value);
		} while (i >= 0);
	}
	
	public String getProtocol() { return protocol; }
	public String getHost() { return host; }
	public int getPort() { return port; }
	public String getPath() { return path; }
	public Map<String,String> getQuery() { return query; }
	
	public String getFileName() {
		int i = path.lastIndexOf('/');
		if (i < 0)
			return path;
		return path.substring(i + 1);
	}
	
	public String getFileExtension() {
		String fn = getFileName();
		int i = fn.lastIndexOf('.');
		if (i < 0)
			return "";
		return fn.substring(i + 1);
	}
	
	public void setPath(String path) { this.path = path; }
	public void setHost(String host) {
		int i = host.indexOf(':');
		if (i < 0)
			this.host = host;
		else {
			this.host = host.substring(0, i);
			try { port = Integer.parseInt(host.substring(i+1)); }
			catch (NumberFormatException e) { port = -1; }
		}
	}
	public void setPort(int port) { this.port = port; }
	public void setProtocol(String protocol) { this.protocol = protocol; }
	
	public String getPathWithQuery() {
		StringBuilder s = new StringBuilder(path);
		if (!query.isEmpty()) {
			s.append('?');
			for (Iterator<Map.Entry<String,String>> it = query.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<String,String> e = it.next();
				s.append(e.getKey()).append('=').append(e.getValue());
				if (it.hasNext())
					s.append('&');
			}
		}
		return s.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		if (protocol != null)
			s.append(protocol).append(':');
		if (protocol != null && protocol.equals("file"))
			s.append('/');
		if (host != null)
			s.append("//").append(host);
		if (port != -1)
			s.append(':').append(Integer.toString(port));
		if (protocol != null && protocol.equals("file"))
			s.append(':');
		s.append('/');
		s.append(getPathWithQuery());
		if (fragment != null)
			s.append('#').append(fragment);
		return s.toString();
	}
}
