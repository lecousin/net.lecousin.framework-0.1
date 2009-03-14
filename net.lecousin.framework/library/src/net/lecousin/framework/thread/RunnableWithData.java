package net.lecousin.framework.thread;

public abstract class RunnableWithData<T> implements Runnable {

	public RunnableWithData(T data) {
		this.data = data;
	}
	private T data;
	public T data() { return data; }
	
}
