package net.lecousin.framework.math;

import net.lecousin.framework.event.Event;

public class Scale<T extends Number> {

	public Scale(T min, T max, T pos) {
		this.min = min;
		this.max = max;
		this.pos = pos;
	}
	
	private T min, max, pos;
	private Event<T> changed = new Event<T>();
	
	public T getMinimum() { return min; }
	public void setMinimum(T min) { this.min = min; } 
	public T getMaximum() { return max; }
	public void setMaximum(T max) { this.max = max; } 
	public T getPosition() { return pos; }
	public void setPosition(T pos) {
		if (pos.equals(this.pos)) return;
		this.pos = pos; 
		changed.fire(pos);
	} 
	
	public Event<T> changed() { return changed; }
	
}
