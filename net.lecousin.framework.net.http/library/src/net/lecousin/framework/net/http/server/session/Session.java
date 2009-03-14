package net.lecousin.framework.net.http.server.session;

import java.util.HashMap;
import java.util.Map;

public class Session {

	public Session(String id) {
		this.id = id;
	}
	
	private String id;
	private Map<String,Object> data = new HashMap<String,Object>();
	
	public String getID() { return id; }
	
	public boolean hasData(String key) { return data.containsKey(key); }
	public Object getData(String key) { return data.get(key); }
	public void setData(String key, Object data) { this.data.put(key, data); }
	public void clearData(String key) { data.remove(key); }
}
