package net.lecousin.framework.math;

public class RangeLong {

	public RangeLong(long min, long max) {
		this.min = min;
		this.max = max;
	}
	
	public long min;
	public long max;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof RangeLong)) return false;
		RangeLong r = (RangeLong)obj;
		return r.min == min && r.max == max;
	}

	@Override
	public int hashCode() {
		return (int)(min + max);
	}
}
