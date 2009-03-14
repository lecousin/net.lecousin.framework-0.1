package net.lecousin.framework.net.http.server.session;

import java.util.HashMap;
import java.util.Map;

import net.lecousin.framework.random.Random;

public class SessionManager {

	private static SessionManager instance = null;
	public static SessionManager instance() { if (instance == null) instance = new SessionManager(); return instance; }

	private SessionManager() {
	}
	
	private Map<String,Session> sessions = new HashMap<String,Session>();
	
	public Session get(String id) {
		return sessions.get(id);
	}
	public Session newSession() {
		String id;
		while (sessions.containsKey(id = generateID()));
		Session s = new Session(id);
		sessions.put(id, s);
		return s;
	}
	
	private String generateID() {
		return Random.randAlphaNum(16);
	}
}
