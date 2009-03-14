package net.lecousin.framework.net.http.server;

import java.io.IOException;

import net.lecousin.framework.net.http.server.session.Session;
import net.lecousin.framework.net.http.server.session.SessionManager;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public abstract class HttpSessionRequestHandler implements HttpRequestHandler {

	public final void handle(HttpRequest req, HttpResponse resp, HttpContext ctx)
	throws HttpException, IOException {
		String sessionID = HttpServerUtil.getCookie(resp, "SessionID");
		if (sessionID == null)
			sessionID = HttpServerUtil.getCookie(req, "SessionID");
		Session session = null;
		if (sessionID != null)
			session = SessionManager.instance().get(sessionID);
		if (session == null) {
			System.out.println("Need init session");
			HttpServerUtil.redirect(resp, "/init.session?url=" + req.getRequestLine().getUri());
			return;
			//session = SessionManager.instance().newSession();
			//HttpUtil.setCookie(resp, "SessionID", session.getID());
			//System.out.println("Init cookie: " + session.getID() + " / " + req.getRequestLine().getUri());
		}
		handle(req, resp, ctx, session);
	}
	
	protected abstract void handle(HttpRequest req, HttpResponse resp, HttpContext ctx, Session session) throws HttpException, IOException;
}
