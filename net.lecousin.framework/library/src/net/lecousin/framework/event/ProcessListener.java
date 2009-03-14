package net.lecousin.framework.event;

public interface ProcessListener<T> {

	public void started();
	public void fire(T data);
	public void done();
	
	public static abstract class ProcessListenerWithData<T,TData> implements ProcessListener<T> {
		public ProcessListenerWithData(TData data) { this.data = data; }
		private TData data;
		public TData data() { return data; }
	}
}
