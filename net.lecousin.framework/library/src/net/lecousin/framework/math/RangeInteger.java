package net.lecousin.framework.math;

public class RangeInteger {

	public RangeInteger(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	public int min;
	public int max;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof RangeInteger)) return false;
		RangeInteger r = (RangeInteger)obj;
		return r.min == min && r.max == max;
	}

	@Override
	public int hashCode() {
		return min + max;
	}
	
	public boolean contains(int value) {
		return value >= min && value <= max;
	}
	public RangeInteger intersect(RangeInteger r) {
		if (min > r.max) return null;
		if (max < r.min) return null;
		return new RangeInteger(Math.max(min, r.min), Math.min(max, r.max));
	}
}
