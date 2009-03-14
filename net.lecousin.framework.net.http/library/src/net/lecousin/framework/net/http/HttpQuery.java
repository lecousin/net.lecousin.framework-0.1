package net.lecousin.framework.net.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpRequest;

public class HttpQuery {

	public HttpQuery(HttpRequest req) {
		HttpURI uri = new HttpURI(req.getRequestLine().getUri());
		data.putAll(uri.getQuery());
	}
	
	private Map<String,Object> data = new HashMap<String,Object>();
	
	public boolean has(String name) {
		return data.containsKey(name);
	}
	
	public String getString(String name) {
		return (String)data.get(name);
	}
	
	public Map<String,Object> getData() {
		return data;
	}
}
