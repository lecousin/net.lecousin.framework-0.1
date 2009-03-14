package net.lecousin.framework.ui.eclipse.graphics;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ColorUtil {

	private static Map<Long,Color> colors = null;
	
	public static Color get(int r, int g, int b) {
		long v = r + (g*256) + (b*65536);
		if (colors == null) colors = new HashMap<Long,Color>();
		Color c = colors.get(v);
		if (c != null) return c;
		c = new Color(Display.getDefault(), r, g, b);
		colors.put(v, c);
		return c;
	}
	
	public static Color getBlack() { return get(0, 0, 0); }
	public static Color getWhite() { return get(255, 255, 255); }
	public static Color getGray() { return get(128,128,128); }
	public static Color getDarkGrey() { return get(80, 80, 80); }
	public static Color getBlue() { return get(0, 0, 255); }
	public static Color getRed() { return get(255, 0, 0); }
	public static Color getGreen() { return get(0, 255, 0); }
	public static Color getRose() { return get(200, 100, 100); }
	public static Color getOrange() { return get(255, 128, 0); }
	public static Color getYellow() { return get(255, 255, 0); }
	
	public static Color goTo(Color from, Color to, int remainingSteps) {
		return get(
				from.getRed()+(to.getRed()-from.getRed())/remainingSteps,
				from.getGreen()+(to.getGreen()-from.getGreen())/remainingSteps,
				from.getBlue()+(to.getBlue()-from.getBlue())/remainingSteps
				);
	}
	
	public static Color goTo(Color from, Color to, int steps, int step) {
		return get(
				from.getRed()+(to.getRed()-from.getRed())*step/steps,
				from.getGreen()+(to.getGreen()-from.getGreen())*step/steps,
				from.getBlue()+(to.getBlue()-from.getBlue())*step/steps
				);
	}
	
	public static Color darkerOrClearer(Color c, int amount) {
		if (c.getRed() > amount || c.getGreen() > amount || c.getBlue() > amount)
			return darker(c, amount);
		return clearer(c, amount);
	}
	
	public static Color darker(Color c, int amount) {
		int r = c.getRed()-amount;
		int g = c.getGreen()-amount;
		int b = c.getBlue()-amount;
		return get(r >= 0 ? r : 0, g >= 0 ? g : 0, b >= 0 ? b: 0);
	}
	public static Color clearer(Color c, int amount) {
		int r = c.getRed()+amount;
		int g = c.getGreen()+amount;
		int b = c.getBlue()+amount;
		return get(r <= 255 ? r : 255, g <= 255 ? g : 255, b <= 255 ? b: 255);
	}
	
	public static boolean isDark(Color c) {
		return c.getBlue() < 80 && c.getGreen() < 80 && c.getRed() < 80; 
	}
	public static boolean isClear(Color c) {
		return c.getBlue() > 160 && c.getGreen() > 160 && c.getRed() > 160; 
	}
	
	public static boolean isSame(Color c1, Color c2) {
		return c1.getRed() == c2.getRed() && c1.getGreen() == c2.getGreen() && c1.getBlue() == c2.getBlue();
	}
}
