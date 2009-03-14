package net.lecousin.framework.monitoring;

import java.util.LinkedList;
import java.util.List;

public class Monitor extends Thread {

	public Monitor(String name) {
		super(name);
	}
	
	private List<Work> works = new LinkedList<Work>();
	
	private class Work {
		Runnable work;
		long interval;
		long lastTime = 0;
	}
	
	public void newWork(Runnable work, long interval) {
		synchronized (works) {
			Work w = new Work();
			w.work = work;
			w.interval = interval;
			works.add(w);
		}
	}
	
	public void close() {
		quit = true;
	}
	
	private boolean quit = false;
	private static final long MAX_SLEEP_TIME = 5000;
	@Override
	public void run() {
		do {
			Work next = null;
			synchronized (works) {
				for (Work w : works) {
					if (next == null || w.lastTime + w.interval < next.lastTime + next.interval)
						next = w;
				}
			}
			long sleepTime;
			if (next == null)
				sleepTime = MAX_SLEEP_TIME;
			else {
				long time = System.currentTimeMillis();
				long nextTime = next.lastTime + next.interval;
				if (nextTime <= time) {
					sleepTime = 0;
					next.work.run();
					next.lastTime = time;
				} else {
					sleepTime = nextTime - time;
					if (sleepTime > MAX_SLEEP_TIME)
						sleepTime = MAX_SLEEP_TIME;
				}
			}
			if (sleepTime > 0)
				try { Thread.sleep(sleepTime); }
				catch (InterruptedException e) { break; }
		} while (!quit);
	}
	
}
