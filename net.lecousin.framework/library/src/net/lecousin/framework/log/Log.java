package net.lecousin.framework.log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import net.lecousin.framework.Pair;

public abstract class Log
{
  public enum Severity {
	DEBUG(4),
    INFO(3),
    WARNING(2),
    ERROR(1),
    FATAL(0);
    
    Severity(int level) { this.level = level; }
	private int level;
	public int level() { return level; }
  }
  
  private static Collection<Pair<String,Log>> logs = null;
  private static Map<String,Log> knownLogs = null;
  private static Log defaultLog = null;
  
  public static void setDefaultLog(Log log) {
	  System.out.println("Default log set.");
	  defaultLog = log; 
  }
  public static void setLog(String qnameStart, Log log) {
	  if (logs == null) logs = new LinkedList<Pair<String,Log>>();
	  logs.add(new Pair<String, Log>(qnameStart, log));
	  System.out.println("Log set for " + qnameStart);
  }
  
  public static boolean log(Severity sev, Object obj) {
	  Log log = getLog(obj.getClass());
	  if (log == null) return false;
	  return log.enabled(sev);
  }
  public static boolean log(Severity sev, Class<?> cl) {
	  Log log = getLog(cl);
	  if (log == null) return false;
	  return log.enabled(sev);
  }
  
  public static boolean debug(Object obj) { return log(Severity.DEBUG, obj); }
  public static boolean info(Object obj) { return log(Severity.INFO, obj); }
  public static boolean warning(Object obj) { return log(Severity.WARNING, obj); }
  public static boolean error(Object obj) { return log(Severity.ERROR, obj); }
  public static boolean fatal(Object obj) { return log(Severity.FATAL, obj); }

  public static boolean debug(Class<?> cl) { return log(Severity.DEBUG, cl); }
  public static boolean info(Class<?> cl) { return log(Severity.INFO, cl); }
  public static boolean warning(Class<?> cl) { return log(Severity.WARNING, cl); }
  public static boolean error(Class<?> cl) { return log(Severity.ERROR, cl); }
  public static boolean fatal(Class<?> cl) { return log(Severity.FATAL, cl); }
  
  public static void log(Severity sev, Object obj, String message) {
	  log(sev, obj.getClass(), message, null);
  }
  public static void log(Severity sev, Class<?> cl, String message) {
	  log(sev, cl, message, null);
  }
  public static void log(Severity sev, Object obj, String message, Throwable t) {
	  Log log = getLog(obj.getClass());
	  if (log != null)
		  log.logMessage(sev, obj.getClass(), message, t);
  }
  public static void log(Severity sev, Class<?> cl, String message, Throwable t) {
	  Log log = getLog(cl);
	  if (log != null)
		  log.logMessage(sev, cl, message, t);
  }
  
  public static void debug(Object obj, String message) { log(Severity.DEBUG, obj, message); }
  public static void info(Object obj, String message) { log(Severity.INFO, obj, message); }
  public static void warning(Object obj, String message) { log(Severity.WARNING, obj, message); }
  public static void warning(Object obj, String message, Throwable t) { log(Severity.WARNING, obj, message, t); }
  public static void error(Object obj, String message) { log(Severity.ERROR, obj, message); }
  public static void fatal(Object obj, String message) { log(Severity.FATAL, obj, message); }
  public static void error(Object obj, String message, Throwable t) { log(Severity.ERROR, obj, message, t); }
  public static void fatal(Object obj, String message, Throwable t) { log(Severity.FATAL, obj, message, t); }
  public static void debug(Object obj, String message, Throwable t) { log(Severity.DEBUG, obj, message, t); }
  
  public static void debug(Class<?> cl, String message) { log(Severity.DEBUG, cl, message); }
  public static void info(Class<?> cl, String message) { log(Severity.INFO, cl, message); }
  public static void warning(Class<?> cl, String message) { log(Severity.WARNING, cl, message); }
  public static void warning(Class<?> cl, String message, Throwable t) { log(Severity.WARNING, cl, message, t); }
  public static void error(Class<?> cl, String message) { log(Severity.ERROR, cl, message); }
  public static void fatal(Class<?> cl, String message) { log(Severity.FATAL, cl, message); }
  public static void error(Class<?> cl, String message, Throwable t) { log(Severity.ERROR, cl, message, t); }
  public static void fatal(Class<?> cl, String message, Throwable t) { log(Severity.FATAL, cl, message, t); }
  
  public static Log getLog(Class<?> clazz) {
	  String qname = clazz.getName();
	  Log log = null;
	  if (knownLogs != null) {
		  log = knownLogs.get(qname);
		  if (log != null) return log;
	  }
	  if (logs != null) {
		  Pair<String,Log> best = null;
		  for (Iterator<Pair<String,Log>> it = logs.iterator(); it.hasNext(); ) {
			  Pair<String,Log> p = it.next();
			  if (qname.startsWith(p.getValue1())) {
				  if (best == null || best.getValue1().length() < p.getValue1().length())
					  best = p;
			  }
		  }
		  if (best != null)
			  log = best.getValue2();
	  }
	  if (log == null)
		  log = defaultLog;
	  if (log != null) {
		  if (knownLogs == null) knownLogs = new HashMap<String,Log>();
		  knownLogs.put(qname, log);
	  }
	  return log;
  }
  
  public abstract boolean enabled(Severity sev);
  public abstract void logMessage(Severity sev, Class<?> cl, String message);
  public final void logMessage(Severity sev, Object obj, String message) {
	  logMessage(sev, obj.getClass(), message);
  }
  public final void logMessage(Severity sev, Class<?> cl, String message, Throwable t) {
	  if (t == null) {
		  logMessage(sev, cl, message);
		  return;
	  }
	  StringBuilder str = new StringBuilder(message);
	  str.append(": ").append(t.getClass().getName()).append(": ").append(t.getMessage()).append("\r\n");
	  for (StackTraceElement stack : t.getStackTrace()) {
		  str.append("\tat ").append(stack.getClassName()).append(".").append(stack.getMethodName()).append("(").append(stack.getFileName()).append(":").append(stack.getLineNumber()).append(")\r\n");
	  }
	  logMessage(sev, cl, str.toString());
  }
}
