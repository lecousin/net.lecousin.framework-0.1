package net.lecousin.framework.ui.eclipse.graphics;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class GraphicsUtil {

	public static Point getSize(Rectangle rect) {
		return new Point(rect.width, rect.height);
	}
	
}
