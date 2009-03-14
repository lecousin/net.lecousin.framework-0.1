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
}
