package net.lecousin.framework.geometry;

public class PointDouble {

	public PointDouble(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public PointDouble(PointDouble copy) {
		this(copy.x, copy.y);
	}
	
	public double x, y;
	
	public void goTo(PointDouble newCoord) { goTo(newCoord.x, newCoord.y); }
	public void goTo(double nx, double ny) { x = nx; y = ny; }
	
	public double getDistance(PointDouble c) {
		double xl = (double)(x > c.x ? x-c.x : c.x-x);
		double yl = (double)(y > c.y ? y-c.y : c.y-y);
		return Math.hypot(xl, yl);
	}

	/** return (dx,dy) representing the necessary move to perform d distance to direction */
	public PointDouble getMoveToDirection(PointDouble direction, double d) {
		if (direction.x == x) {
			// vertical
			return direction.y > y ? new PointDouble(0, d) : new PointDouble(0, -d);
		}
		if (direction.y == y) {
			// horizontal
			return direction.x > x ? new PointDouble(d, 0) : new PointDouble(-d, 0);
		}
		double m = (direction.y - y) / (direction.x - x);
		m = m*m; // m²
		d = d*d; // d²
		double dx = Math.sqrt(d/(m+1));
		double dy = Math.sqrt((d*m)/(m+1));
		if (direction.x < x) dx = -dx;
		if (direction.y < y) dy = -dy;
		return new PointDouble(dx, dy);
	}
	
	/** return (x+move.x, y+move.y) representing the new position if the specified move is performed */
	public PointDouble getTargetPositionForMove(PointDouble move) {
		return new PointDouble(x+move.x, y+move.y);
	}
	
	/** execute the specified move: new position is (x+move.x, y+move.y) */
	public void move(PointDouble move) {
		goTo(getTargetPositionForMove(move));
	}
	
	/** return the necessary minimal move to be at distance d from the specified point.
	 * If current distance >= d Then the move has the direction of the point Else it has the opposite direction.
	 * It means this method calculates the minimal move to be at distance d from the speicifed point. 
	 */
	public PointDouble getMinimalMoveToBeAtDistance(PointDouble point, double d) {
		double current_d = getDistance(point);
		if (current_d >= d)
			return getMoveToDirection(point, current_d - d);
		return point.getMoveToDirection(this, d - current_d);
	}
	
	/** move d in the specified direction. Equivalent to move(getMoveToDirection(direction, d)) */ 
	public void moveTo(PointDouble direction, double d) {
		move(getMoveToDirection(direction, d));
	}
	
	/** same as moveTo, but if the final point is outside the borders with a tolerance of 1.0E-10, 
	 * we round the position to be exactly on the border.
	 */  
	public void moveTo_ToleranceOnBorders(PointDouble direction, double d, RectangleDouble borders) {
		move(getMoveToDirection(direction, d));
		checkToleranceBorders(borders, 1.0E-10);
	}
	
	private void checkToleranceBorders(RectangleDouble borders, double tolerance) {
		if (Math.abs(borders.topLeft.x - x) < tolerance) x = borders.topLeft.x;
		if (Math.abs(borders.bottomRight.x - x) < tolerance) x = borders.bottomRight.x;
		if (Math.abs(borders.topLeft.y - y) < tolerance) y = borders.topLeft.y;
		if (Math.abs(borders.bottomRight.y - y) < tolerance) y = borders.bottomRight.y;
	}
	
	/** return the closet position to be at distance d from the specified point. Equivalent to getTargetPositionForMove(getMinimalMoveToBeAtDistance(point, d)) */ 
	public PointDouble getTargetPositionToBeAt(PointDouble point, double d) {
		return getTargetPositionForMove(getMinimalMoveToBeAtDistance(point, d));
	}
	
	/** execute the minimal move to be at distance d from the specified point. Equivalent to move(getMinimalMoveToBeAtDistance(point, d))*/
	public void moveAt(PointDouble point, double d) {
		move(getMinimalMoveToBeAtDistance(point, d));
	}
	
	/** move at distance d from point but limit the move to stay within the given rectangle. */
	public void moveAtStayIn(PointDouble point, double d, PointDouble topLeft, PointDouble bottomRight) {
		moveAtStayIn(point, d, new RectangleDouble(new PointDouble(topLeft), new PointDouble(bottomRight)));
	}
	
	/** move at distance d from point but limit the move to stay within the given rectangle. */
	public void moveAtStayIn(PointDouble point, double d, RectangleDouble r) {
		goTo(getTargetPositionToBeAtStayIn(point, d, r));
	}
	
	/** return the minimal move to do to be at distance d from point while staying within r.
	 * This method assumes this point is contained in the r. */
	public PointDouble getMinimalMoveToBeAtStayIn(PointDouble point, double d, RectangleDouble r) {
		return getMoveToGo(getTargetPositionToBeAtStayIn(point, d, r));
	}
	
	/** return the position where to go to be at distance d from point while staying within r.
	 * This method assumes this point is contained in the r. */
	public PointDouble getTargetPositionToBeAtStayIn(PointDouble point, double d, RectangleDouble r) {
		PointDouble pos = getTargetPositionToBeAt(point, d);
		if (r.contains(pos)) return pos;
		PointDouble[] i = new LineDouble(pos, this).getIntersection(r);
		if (i == null) {
			// we are on a border
			i = null;
			i = new LineDouble(pos, this).getIntersection(r);
			i = new LineDouble(pos, this).getIntersection(r);
		}
		if (i.length != 1) {
			i = null;
			i = new LineDouble(pos, this).getIntersection(r);
			i = new LineDouble(pos, this).getIntersection(r);
		}
		if (!r.contains(i[0])) {
			int ii = 0;
			ii = ii+1;
		}
		return i[0]; // if the assumption is fulfilled, the intersection contains a single point.
	}
	
	/** return the necessary move to go to the specified point (point.x-x, point.y-y). */
	public PointDouble getMoveToGo(PointDouble point) {
		return new PointDouble(point.x - x, point.y - y);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PointDouble)) return false;
		PointDouble c = (PointDouble)obj;
		return x == c.x && y == c.y;
	}
	@Override
	public int hashCode() {
		return (int)(x + y);
	}
	
	@Override
	public String toString() {
		return "[" + x + "," + y + "]";
	}
}
