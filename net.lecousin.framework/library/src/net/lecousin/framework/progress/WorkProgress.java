package net.lecousin.framework.progress;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;

public class WorkProgress {

	public WorkProgress(String description, int amount, boolean cancellable) {
		this.description = description;
		this.workAmount = amount;
		this.cancellable = cancellable;
	}
	
	private String description;
	private String subDescription = null;
	private int workAmount;
	private int workDone = 0;
	private int previousPosition = 0;
	private boolean cancelled = false;
	private boolean cancellable;
	private List<Pair<WorkProgress,Integer>> subWorks = new LinkedList<Pair<WorkProgress,Integer>>();
	private Event<WorkProgress> progressEvent = new Event<WorkProgress>();
	
	public String getDescription() { return description; }
	public void setDescription(String descr) {
		description = descr;
		// TODO add an event ?? (same for sub descr, etc...)
	}
	
	public String getSubDescription() { return subDescription; }
	public void setSubDescription(String descr) { subDescription = descr; progressEvent.fire(this); }
	
	public int getAmount() { return workAmount; }
	public void setAmount(int newAmount) {
//		if (workDone != 0 || !subWorks.isEmpty())
//			throw new IllegalStateException("You cannot set the amount of work if some work has been already done or sub-works have been declared.");
		workAmount = newAmount;
		if (workAmount == 0)
			progressEvent.fire(this); // this is the end so the listeners must be informed
	}
	
	public synchronized int getPosition() {
		long totalPos = 0;
		long totalAmount = 0;
		long totalWork = 0;
		for (Pair<WorkProgress,Integer> p : subWorks) {
			totalPos += ((long)p.getValue1().getPosition())*(long)p.getValue2();
			totalAmount += ((long)p.getValue1().getAmount())*(long)p.getValue2();
			totalWork += p.getValue2();
		}		
		int pos = totalAmount > 0 ? (int)(totalPos*totalWork/totalAmount) : 0;
		pos += workDone;
		return pos;
	}
	
	public int getRemainingWork() {
		return getAmount() - getPosition();
	}

	public void progress(int work) {
		workDone += work;
		int pos = getPosition();
		if (pos != previousPosition) {
			previousPosition = pos;
			progressEvent.fire(this);
		}
	}
	public void undo(int work) {
		progress(-work);
	}
	
	public synchronized void done() {
		if (isFinished()) return;
		subWorks.clear();
		workDone = workAmount;
		progressEvent.fire(this);
	}
	
	public synchronized void reset(String description, int amount) {
		this.description = description;
		subDescription = "";
		workAmount = amount;
		reset();
	}
	public synchronized void reset() {
		workDone = 0;
		subWorks.clear();
		progressEvent.fire(this); // inform the listeners there is a change
	}
	
	public synchronized boolean isStarted() {
		if (workDone > 0) return true;
		for (Pair<WorkProgress,Integer> p : subWorks)
			if (p.getValue1().isStarted()) return true;
		return false;
	}
	
	public boolean isFinished() {
		return getPosition() == workAmount;
	}

	public boolean isCancellable() { return cancellable; }
	public boolean isCancelled() { return cancelled; }
	public synchronized void cancel() {
		if (!cancellable) return;
		cancelled = true;
		for (Pair<WorkProgress,Integer> p : subWorks)
			p.getValue1().cancel();
	}
	
	public synchronized List<WorkProgress> getSubWorks() {
		LinkedList<WorkProgress> list = new LinkedList<WorkProgress>();
		for (Pair<WorkProgress,Integer> p : subWorks)
			list.add(p.getValue1());
		return list;
	}
	
	public WorkProgress addSubWork(String description, int work, int subAmount) {
		WorkProgress subWork = new WorkProgress(description, subAmount, cancellable);
		addSubWork(subWork, work);
		return subWork;
	}
	public synchronized void addSubWork(WorkProgress subWork, int work) {
		subWorks.add(new Pair<WorkProgress,Integer>(subWork, work));
		subWork.addProgressListener(new Listener<WorkProgress>() {
			public void fire(WorkProgress event) {
				int pos = getPosition();
				if (pos != previousPosition) {
					previousPosition = pos;
					progressEvent.fire(WorkProgress.this);
				}
			}
		});
	}
	public synchronized void mergeSubWork(WorkProgress subWork) {
		for (Iterator<Pair<WorkProgress,Integer>> it = subWorks.iterator(); it.hasNext(); ) {
			Pair<WorkProgress,Integer> p = it.next();
			if (p.getValue1() == subWork) {
				workDone += p.getValue2();
				it.remove();
				break;
			}
		}
		progressEvent.fire(this);
	}
	
	public void addProgressListener(Listener<WorkProgress> listener) {
		progressEvent.addListener(listener);
	}
	public void removeProgressListener(Listener<WorkProgress> listener) {
		progressEvent.removeListener(listener);
	}
}
