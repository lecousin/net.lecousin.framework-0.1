package net.lecousin.framework.geometry;

public class RectangleDouble {

	public RectangleDouble(double x1, double y1, double x2, double y2) {
		this(new PointDouble(x1, y1), new PointDouble(x2, y2));
	}
	public RectangleDouble(PointDouble topLeft, PointDouble bottomRight) {
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;
	}
	public RectangleDouble(RectangleDouble copy) { this(new PointDouble(copy.topLeft), new PointDouble(copy.bottomRight)); }
	public static RectangleDouble fromSize(PointDouble topLeft, PointDouble size) {
		return new RectangleDouble(new PointDouble(topLeft), new PointDouble(topLeft.x + size.x, topLeft.y + size.y));
	}
	
	public PointDouble topLeft, bottomRight;
	
	public PointDouble getSize() { return new PointDouble(bottomRight.x-topLeft.x, bottomRight.y-topLeft.y); }
	
	/** return true if the given point is contained within this rectangle. */
	public boolean contains(PointDouble point) {
		return point.x >= topLeft.x && point.x <= bottomRight.x && point.y >= topLeft.y && point.y <= bottomRight.y;
	}

	/** return true if the given point is contained within this rectangle. */
	public boolean contains(PointDouble point, double tolerance) {
		return point.x+tolerance >= topLeft.x && point.x-tolerance <= bottomRight.x && point.y+tolerance >= topLeft.y && point.y-tolerance <= bottomRight.y;
	}
	
	/** return true if this rectangle contains entirely the given rectangle r. */ 
	public boolean contains(RectangleDouble r) {
		return r.topLeft.x >= topLeft.x && r.bottomRight.x <= bottomRight.x && r.topLeft.y >= topLeft.y && r.bottomRight.y <= bottomRight.y;
	}
	
	/** return true if this rectangle contains entirely the given rectangle r. */ 
	public boolean contains(RectangleDouble r, double tolerance) {
		return r.topLeft.x+tolerance >= topLeft.x && r.bottomRight.x-tolerance <= bottomRight.x && r.topLeft.y+tolerance >= topLeft.y && r.bottomRight.y-tolerance <= bottomRight.y;
	}

	/** return the intersection points between this rectangle and the given line: it may returns 0, 1 or 2 points. */
	public PointDouble[] getIntersection(LineDouble line) {
		return line.getIntersection(this);
	}
	
	/** return the intersection points between this rectangle and the given one or null if both are equals: it may return 0 to 2 points (1 point being the case where only one of their corner is common). */
	public PointDouble[] getIntersectionPoints(RectangleDouble r) {
		if (r.equals(this)) return null;
		if (contains(r)) return new PointDouble[0];
		if (r.contains(this)) return new PointDouble[0];
		RectangleDouble left, right;
		if (topLeft.x < r.topLeft.x) {
			left = this; right = r;
		} else {
			left = r; right = this;
		}
		if (right.topLeft.x > left.bottomRight.x) return new PointDouble[0];
		RectangleDouble top, bottom;
		if (topLeft.y < r.topLeft.y) {
			top = this; bottom = r;
		} else {
			top = r; bottom = this;
		}
		if (bottom.topLeft.y > top.bottomRight.y) return new PointDouble[0];
		if (left.bottomRight.x == right.topLeft.x && top.bottomRight.y == bottom.topLeft.y) 
			return new PointDouble[] { new PointDouble(left.bottomRight.x, top.bottomRight.y) };
		return new PointDouble[] {
				new PointDouble(right.topLeft.x, top.bottomRight.y),
				new PointDouble(left.bottomRight.x, bottom.topLeft.y)
		};
	}
	
	/** return the rectangle corresponding to the intersection or null if there is no intersection. */
	public RectangleDouble getIntersection(RectangleDouble r) {
		if (contains(r)) return new RectangleDouble(r);
		if (r.contains(this)) return new RectangleDouble(this);
		RectangleDouble left, right;
		if (topLeft.x < r.topLeft.x) {
			left = this; right = r;
		} else {
			left = r; right = this;
		}
		if (right.topLeft.x > left.bottomRight.x) return null;
		RectangleDouble top, bottom;
		if (topLeft.y < r.topLeft.y) {
			top = this; bottom = r;
		} else {
			top = r; bottom = this;
		}
		if (bottom.topLeft.y > top.bottomRight.y) return null;
		return new RectangleDouble(new PointDouble(right.topLeft.x, bottom.topLeft.y), new PointDouble(left.bottomRight.x, top.bottomRight.y));
	}
	
	public LineDouble getTopLine() { return new LineDouble(new PointDouble(topLeft.x, topLeft.y), new PointDouble(bottomRight.x, topLeft.y)); }
	public LineDouble getBottomLine() { return new LineDouble(new PointDouble(topLeft.x, bottomRight.y), new PointDouble(bottomRight.x, bottomRight.y)); }
	public LineDouble getLeftLine() { return new LineDouble(new PointDouble(topLeft.x, topLeft.y), new PointDouble(topLeft.x, bottomRight.y)); }
	public LineDouble getRightLine() { return new LineDouble(new PointDouble(bottomRight.x, topLeft.y), new PointDouble(bottomRight.x, bottomRight.y)); }
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof RectangleDouble)) return false;
		return topLeft.equals(((RectangleDouble)obj).topLeft) && bottomRight.equals(((RectangleDouble)obj).bottomRight); 
	}
	@Override
	public int hashCode() {
		return topLeft.hashCode() + bottomRight.hashCode();
	}
	@Override
	public String toString() {
		return "[ " + topLeft.toString() + " ; " + bottomRight.toString() + " ]";
	}
}
