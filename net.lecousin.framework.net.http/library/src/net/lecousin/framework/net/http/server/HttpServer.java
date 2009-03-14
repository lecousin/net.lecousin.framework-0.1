package net.lecousin.framework.net.http.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.http.HttpURI;
import net.lecousin.framework.net.http.internal.server.ContentManager;
import net.lecousin.framework.net.http.server.session.Session;
import net.lecousin.framework.net.http.server.session.SessionManager;

import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerResolver;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

public class HttpServer extends Thread {

	public static HttpServer open(int port, HttpRequestHandlerResolver handlerResolver) throws IOException {
		HttpServer t = new HttpServer(port, handlerResolver);
        t.setDaemon(false);
        t.start();
        return t;
	}
	
	private HttpServer(int port, HttpRequestHandlerResolver handlerResolver) throws IOException {
        serversocket = new ServerSocket(port);
        params = new BasicHttpParams();
        params
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "net.lecousin.framework.net.http/0.1");
        this.handlerResolver = new HandlerResolver(handlerResolver);
	}

    private final ServerSocket serversocket;
    private final HttpParams params;
    private final HandlerResolver handlerResolver;
    private boolean closing = false;
    
    public void close() {
		int port = serversocket.getLocalPort();
    	try { 
    		closing = true; 
    		serversocket.close();
        	if (Log.info(this))
        		Log.info(this, "HTTP Server closed on port " + port);
    	} catch (IOException e) {
    		if (Log.error(this))
    			Log.error(this, "Error while closing the HTTPServer on port " + port, e);
    	}
    }
	
    public void run() {
    	if (Log.info(this))
    		Log.info(this, "HTTP Server listening on port " + this.serversocket.getLocalPort());
        while (!Thread.interrupted()) {
            try {
                // Set up HTTP connection
                Socket socket = this.serversocket.accept();
                DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
            	if (Log.info(this))
            		Log.info(this, "Incoming HTTP connection from " + socket.getInetAddress());
                conn.bind(socket, this.params);

                // Set up the HTTP protocol processor
                BasicHttpProcessor httpproc = new BasicHttpProcessor();
                httpproc.addInterceptor(new ResponseDate());
                httpproc.addInterceptor(new ResponseServer());
                httpproc.addInterceptor(new ResponseContent());
                httpproc.addInterceptor(new ResponseConnControl());

                // Set up the HTTP service
                HttpService httpService = new HttpService(
                        httpproc, 
                        new DefaultConnectionReuseStrategy(), 
                        new DefaultHttpResponseFactory());
                httpService.setParams(this.params);
                httpService.setHandlerResolver(handlerResolver);
                
                // Start worker thread
                Thread t = new WorkerThread(httpService, conn);
                t.setDaemon(true);
                t.start();
            } catch (InterruptedIOException ex) {
                break;
            } catch (SocketException e) {
            	if (!closing)
                	if (Log.error(this))
                		Log.error(this, "Socket error", e); 
            	break;
            } catch (IOException e) {
            	if (Log.error(this))
            		Log.error(this, "I/O error initializing connection thread", e); 
                break;
            }
        }
    }
    
    static class WorkerThread extends Thread {

        private final HttpService httpservice;
        private final HttpServerConnection conn;
        
        public WorkerThread(
                final HttpService httpservice, 
                final HttpServerConnection conn) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
        }
        
        public void run() {
        	if (Log.info(this))
        		Log.info(this, "New connection thread");
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                }
            } catch (ConnectionClosedException ex) {
            	if (Log.error(this))
            		Log.error(this, "Connection closed");
            } catch (IOException ex) {
            	if (Log.error(this))
            		Log.error(this, "I/O error: " + ex.getMessage());
            } catch (HttpException ex) {
            	if (Log.error(this))
            		Log.error(this, "Unrecoverable HTTP protocol violation", ex);
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {}
            	if (Log.info(this))
            		Log.info(this, "Connection thread closed.");
            }
        }

    }
    
    static class HandlerResolver implements HttpRequestHandlerResolver {
    	HandlerResolver(HttpRequestHandlerResolver resolver) {
    		this.resolver = resolver;
    	}
    	private HttpRequestHandlerResolver resolver;
    	public HttpRequestHandler lookup(String uri) {
    		if (Log.debug(this))
    			Log.debug(this, "URI="+uri);
    		return new Handler(resolver.lookup(uri));
    	}
    }
    
    static class Handler implements HttpRequestHandler {
    	public Handler(HttpRequestHandler handler) {
    		this.handler = handler;
    	}
    	private HttpRequestHandler handler;
    	public void handle(HttpRequest req, HttpResponse resp, HttpContext ctx)
    	throws HttpException, IOException {
    		if (Log.debug(this)) {
    			StringBuilder str = new StringBuilder("HTTP Request Header:\r\n");
    			for (Header h : req.getAllHeaders())
    				str.append("  ").append(h.getName()).append(": ").append(h.getValue()).append("\r\n");
    			Log.debug(this, str.toString());
    		}
    		HttpURI uri = new HttpURI(req.getRequestLine().getUri());
    		if (uri.getPath().equals("init.session")) {
    			Session session = SessionManager.instance().newSession();
    			HttpServerUtil.setCookie(resp, "SessionID", session.getID());
    			HttpServerUtil.redirect(resp, uri.getQuery().get("url"));
    			if (Log.info(this))
    				Log.info(this, "Session initialized: " + session.getID());
    			return;
    		}
    		handler.handle(req, resp, ctx);
    		ContentManager.instance().handle(req, resp, ctx);
    	}
    }
}
