package net.lecousin.framework.memory;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.collections.LinkedArrayList;

public abstract class ObjectBank<T> {

	public ObjectBank(Class<T> clazz, int maxBankSize) {
		int arraySize = maxBankSize < 20 ? maxBankSize : 20;
		bank = new LinkedArrayList<T>(arraySize, clazz);
		this.maxSize = maxBankSize;
		Application.getMonitor().newWork(new Freer(), 1000);
	}
	
	private LinkedArrayList<T> bank;
	private int maxSize;
	private boolean used = false;
	
	public T create() {
		T result;
		synchronized (this) {
			used = true;
			result = bank.pollFast();
		}
		if (result != null) return result;
		return _create();
	}
	protected abstract T _create();
	
	public void free(T element) {
		if (bank.size() >= maxSize) return;
		bank.addFast(element);
	}
	
	private class Freer implements Runnable {
		public void run() {
			synchronized (ObjectBank.this) {
				if (used) return;
				used = false;
				int i = bank.size()/10; // free 10%
				if (i==0) i = 1;
				while (i-- > 0)
					bank.pollFast();
			}
		}
	}
}
