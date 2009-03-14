package net.lecousin.framework.geometry;

public class RectangleInt {

	public RectangleInt(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	public RectangleInt(RectangleInt copy) {
		this(copy.x, copy.y, copy.width, copy.height);
	}
	
	public int x, y, width, height;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof RectangleInt)) return false;
		RectangleInt o = (RectangleInt)obj;
		return x == o.x && y == o.y && width == o.width && height == o.height; 
	}
	@Override
	public int hashCode() {
		return x + y + width + height;
	}
	@Override
	public String toString() {
		return "[x:" + x + ",y:" + y + ",w:" + width + ",h:" + height + "]";
	}
	
	/** return true if this rectangle contains entirely the given rectangle r. */ 
	public boolean contains(RectangleInt r) {
		return r.x >= x && r.x+r.width <= x+width && r.y >= y && r.y+r.height <= y+height;
	}

	/** return the rectangle corresponding to the intersection or null if there is no intersection. */
	public RectangleInt getIntersection(RectangleInt r) {
		if (contains(r)) return new RectangleInt(r);
		if (r.contains(this)) return new RectangleInt(this);
		RectangleInt left, right;
		if (x < r.x) {
			left = this; right = r;
		} else {
			left = r; right = this;
		}
		if (right.x > left.x+left.width) return null;
		RectangleInt top, bottom;
		if (y < r.y) {
			top = this; bottom = r;
		} else {
			top = r; bottom = this;
		}
		if (bottom.y > top.y+top.height) return null;
		return new RectangleInt(right.x, bottom.y, left.x+left.width, top.y+top.height);
	}
	
}
