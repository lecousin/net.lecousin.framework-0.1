package net.lecousin.framework.geometry;

public class PointInt {

	public PointInt(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int x, y;
	
	public int getX() { return x; }
	public int getY() { return y; }
	
	public double getDistance(PointInt c) {
		double xl = (double)(x > c.x ? x-c.x : c.x-x);
		double yl = (double)(y > c.y ? y-c.y : c.y-y);
		return Math.hypot(xl, yl);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PointInt)) return false;
		PointInt c = (PointInt)obj;
		return x == c.x && y == c.y;
	}
	@Override
	public int hashCode() {
		return x + y;
	}
	
	@Override
	public String toString() {
		return "[" + x + "," + y + "]";
	}
}
