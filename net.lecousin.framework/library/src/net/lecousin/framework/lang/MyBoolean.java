package net.lecousin.framework.lang;

public class MyBoolean {

	public MyBoolean(boolean value) {
		this.value = value;
	}
	
	private boolean value;
	
	public boolean get() { return value; }
	public void set(boolean value) { this.value = value; }
	
}
