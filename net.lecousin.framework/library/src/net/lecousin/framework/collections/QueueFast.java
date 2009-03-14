package net.lecousin.framework.collections;

import java.util.Queue;

/**
 * Queue for fast access, for use when you don't care of the order (FIFO, LIFO...). In this case the xxxFast methods
 * will perform the operation xxx in the faster way.
 * 
 * @author Guillaume
 *
 * @param <T>
 */
public interface QueueFast<T> extends Queue<T> {

	public void addFast(T element);
	public T pollFast();
	
}
