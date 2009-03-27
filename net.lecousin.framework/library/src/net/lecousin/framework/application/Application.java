package net.lecousin.framework.application;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.memory.AutoFreeMemoryGroup;
import net.lecousin.framework.monitoring.Monitor;

public class Application {

	private static String name = "net.lecousin.framework.Application";
	public static void setName(String s) { name = s; }
	
	public static enum Language {
		ENGLISH, FRENCH;
	}
	public static Language language = Language.ENGLISH;
	
	public static File deployPath = null;
	
	public static boolean isDebugEnabled = false;
	
	private static Monitor monitor = null;
	public static Monitor getMonitor() {
		if (monitor == null) {
			monitor = new Monitor(name + " - Monitoring");
			monitor.start();
		}
		return monitor;
	}
	
	private static AutoFreeMemoryGroup autoFreeMemoryGroup = null;
	public static AutoFreeMemoryGroup getAutoFreeMemoryGroup() {
		if (autoFreeMemoryGroup == null)
			autoFreeMemoryGroup = new AutoFreeMemoryGroup(getMonitor());
		return autoFreeMemoryGroup;
	}
	
	public static void close() {
		if (monitor != null) { monitor.close(); monitor = null; }
		for (Pair<Process,Listener<Process>> process : processes.values())
			try { 
				process.getValue1().exitValue();
			} catch (IllegalThreadStateException e) {
				if (process.getValue2() != null)
					process.getValue2().fire(process.getValue1());
				process.getValue1().destroy();
			}
	}
	
	private static Map<String,Pair<Process,Listener<Process>>> processes = new HashMap<String,Pair<Process,Listener<Process>>>();
	
	public static boolean isProcessRunning(String id) {
		Pair<Process,Listener<Process>> p = processes.get(id);
		if (p == null) return false;
		try { p.getValue1().exitValue(); return false; }
		catch (IllegalThreadStateException e) { return true; }
	}
	
	public static Process ensureRunningProcess(String id, String commandLine, String[] envp, ProcessChecker checkStarted, Listener<Process> closer, Listener<Integer> exitedListener) throws IOException {
		Pair<Process,Listener<Process>> process = processes.get(id);
		if (process != null) {
			try { process.getValue1().exitValue(); }
			catch (IllegalThreadStateException e) {
				return process.getValue1();
			}
			process = null;
		}
		if (Log.info(Application.class))
			Log.info(Application.class, "Launch new application process: " + commandLine);
		Process p = Runtime.getRuntime().exec(commandLine, envp);
		process = new Pair<Process, Listener<Process>>(p, closer);
		processes.put(id, process);
		new ProcessHandler(p, id, checkStarted, exitedListener).start();
		if (checkStarted != null) checkStarted.waitReady();
		return p;
	}
	public static Process ensureRunningJAR(String id, String jarPath, String classPath, String vmArgs, String arguments, String[] envp, ProcessChecker checkStarted, Listener<Process> closer, Listener<Integer> exitedListener) throws IOException {
//		for (Object key : System.getProperties().keySet())
//			System.out.println("Property " + key.toString() + "=" + System.getProperty((String)key));
//		for (String key : System.getenv().keySet())
//			System.out.println("Env " + key + "=" + System.getenv(key));
		return ensureRunningProcess(id, System.getProperty("java.home") + "/bin/java.exe -cp \"" + classPath + "\" " + vmArgs + " -jar \"" + jarPath + "\" " + arguments, envp, checkStarted, closer, exitedListener);
	}
	
	public static interface ProcessChecker {
		public ProcessChecker processOutput(String line);
		public void waitReady();
	}
	
	private static class ProcessHandler extends Thread {
		ProcessHandler(Process process, String id, ProcessChecker checker, Listener<Integer> exitedListener) {
			super("ProcessHandler:" + id);
			this.process = process; this.id = id; this.checker = checker; this.exitedListener = exitedListener;
		}
		private Process process;
		private String id;
		private ProcessChecker checker;
		private Listener<Integer> exitedListener;
		@Override
		public void run() {
			LineNumberReader out = new LineNumberReader(new InputStreamReader(process.getInputStream()));
			LineNumberReader err = new LineNumberReader(new InputStreamReader(process.getErrorStream()));
			do {
				boolean read = false;
				try {
					if (out.ready()) {
						String line = out.readLine();
						if (checker != null)
							checker = checker.processOutput(line);
						if (line != null && line.length() > 0) {
							if (Log.info(this))
								Log.info(this, "<Process:"+id+"> " + line);
							read = true;
						}
					}
					if (err.ready()) {
						String line = err.readLine();
						if (line != null && line.length() > 0) {
							if (Log.error(this))
								Log.error(this, "<Process:"+id+"> " + line);
							read = true;
						}
					}
				} catch (IOException e) {
					if (Log.error(this))
						Log.error(this, "Unable to correctly read out/err stream from external process", e);
				}
				try { 
					int i = process.exitValue();
					if (Log.info(this))
						Log.info(this, "<Process:"+id+"> Process exited: " + i);
					if (exitedListener != null)
						exitedListener.fire(i);
					break;
				} catch (IllegalThreadStateException e) {}
				if (!read)
					try { Thread.sleep(150); }
					catch (InterruptedException e) { break; }
			} while (true);
			try {
				if (out.ready()) {
					String line = out.readLine();
					if (line != null && line.length() > 0) {
						if (Log.info(this))
							Log.info(this, "<Process:"+id+"> " + line);
					}
				}
				if (err.ready()) {
					String line = err.readLine();
					if (line != null && line.length() > 0) {
						if (Log.error(this))
							Log.error(this, "<Process:"+id+"> " + line);
					}
				}
			} catch (IOException e) {
				if (Log.error(this))
					Log.error(this, "Unable to correctly read out/err stream from external process", e);
			}
		}
	}
}
