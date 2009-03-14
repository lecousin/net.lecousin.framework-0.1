package net.lecousin.framework.thread;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.collections.SortedListFileAccessLong;

public class BackgroundTemporaryWorkThreading {

	public BackgroundTemporaryWorkThreading(String name, long maxThreadInactivityTime, int maxThreads) {
		this.name = name;
		this.maxThreadInactivityTime = maxThreadInactivityTime;
		this.maxThreads = maxThreads;
	}
	
	private String name;
	private long maxThreadInactivityTime;
	private int maxThreads;
	private List<WorkThread> threads = new LinkedList<WorkThread>();
	private SortedListFileAccessLong<Work> works = new SortedListFileAccessLong<Work>();
	
	private static class Work implements SortedListFileAccessLong.LongSortable {
		Work(Runnable work, long priority)
		{ this.work = work; this.priority = priority; }
		Runnable work;
		long priority;
		public long getLongSortValue() { return priority; }
	}
	
	public void newWork(Runnable work, long priority) {
		synchronized (works) {
			works.add(new Work(work, priority));
			if (threads.size() < maxThreads) {
				boolean working = true;
				for (WorkThread t : threads)
					if (!t.working) { working = false; break; }
				if (working) {
					WorkThread t = new WorkThread(name);
					threads.add(t);
					t.start();
				}
			}
		}
	}
	
	private class WorkThread extends Thread {
		public WorkThread(String name) { super(name); }
		private boolean working = true;
		private long inactivityStartTime = System.currentTimeMillis();
		private long sleepTime = 10;
		@Override
		public void run() {
			Work work;
			while (true) {
				work = null;
				synchronized (works) {
					if (!works.isEmpty()) {
						working = true;
						work = works.removeFirst();
					} else {
						working = false;
						if (System.currentTimeMillis() - inactivityStartTime > maxThreadInactivityTime) {
							threads.remove(this);
							break;
						}
					}
				}
				if (work != null) {
					work.work.run();
					inactivityStartTime = System.currentTimeMillis();
				} else {
					if (sleepTime < 160)
						sleepTime *= 2;
					try { sleep(sleepTime); }
					catch (InterruptedException e) { break; }
				}
			};
		}
	}
}
