package net.lecousin.framework.ui.eclipse.control;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class DrawingUtil {

	private DrawingUtil() { /* instantiation not allowed */ }
	
	public static void drawRoundedRectangle(GC gc, Device device, Point pos, Point size, Color background, Color roundColor) {
		gc.setForeground(background);
		gc.drawRectangle(pos.x, pos.y, size.x - 1, size.y - 1);
		
		int r = (background.getRed() * 2 + roundColor.getRed()) / 3;
		int g = (background.getGreen() * 2 + roundColor.getGreen()) / 3;
		int b = (background.getBlue() * 2 + roundColor.getBlue()) / 3;
		Color roundColor2 = new Color(device, r, g, b);
		
		gc.setForeground(roundColor);
		gc.drawPoint(pos.x, pos.y);
		gc.drawPoint(pos.x, pos.y + size.y - 1);
		gc.drawPoint(pos.x + size.x - 1, pos.y);
		gc.drawPoint(pos.x + size.x - 1, pos.y + size.y - 1);
		
		gc.setForeground(roundColor2);
		gc.drawPoint(pos.x + 1, pos.y + 0);
		gc.drawPoint(pos.x + 0, pos.y + 1);
		gc.drawPoint(pos.x + 1, pos.y + size.y - 1);
		gc.drawPoint(pos.x + 0, pos.y + size.y - 2);
		gc.drawPoint(pos.x + size.x - 2, pos.y + 0);
		gc.drawPoint(pos.x + size.x - 1, pos.y + 1);
		gc.drawPoint(pos.x + size.x - 2, pos.y + size.y - 1);
		gc.drawPoint(pos.x + size.x - 1, pos.y + size.y - 2);
	}
	
	public static void drawPushButtonBorder(GC gc, Point pos, Point size, int borderSize, boolean pushed, boolean isOver, Color borderColor, Color pushDownBorderColor, Color pushUpBorderColor) {
        if (borderSize > 0) {
            gc.setLineWidth(borderSize);
            gc.setForeground(borderColor);
            gc.drawRectangle(pos.x, pos.y, size.x - 1, size.y - 1);
        }
        if (pushed) {
            gc.setLineWidth(1);
            gc.setForeground(pushDownBorderColor);
            gc.drawLine(pos.x + borderSize, pos.y + borderSize, pos.x + size.x - borderSize, pos.y + borderSize);
            gc.drawLine(pos.x + borderSize, pos.y + borderSize, pos.x + borderSize, pos.y + size.y - 1 - borderSize);
            gc.setForeground(pushUpBorderColor);
            gc.drawLine(pos.x + size.x - borderSize, pos.y + borderSize + 1, pos.x + size.x - borderSize, pos.y + size.y + borderSize);
            gc.drawLine(pos.x + borderSize, pos.y + size.y - borderSize, pos.x + size.x - borderSize, pos.y + size.y - borderSize);
        } else if (isOver) {
            gc.setLineWidth(1);
            gc.setForeground(pushUpBorderColor);
            gc.drawLine(pos.x + borderSize, pos.y + borderSize, pos.x + size.x - 1 - borderSize, pos.y + borderSize);
            gc.drawLine(pos.x + borderSize, pos.y + borderSize, pos.x + borderSize, pos.y + size.y - 2 - borderSize);
            gc.setForeground(pushDownBorderColor);
            gc.drawLine(pos.x + size.x - 1 - borderSize, pos.y + borderSize + 1, pos.x + size.x - 1 - borderSize, pos.y + size.y - 1 - borderSize);
            gc.drawLine(pos.x + borderSize, pos.y + size.y - 1 - borderSize, pos.x + size.x - 1 - borderSize, pos.y + size.y - 1 - borderSize);
        }
	}
}
