package net.lecousin.framework.ui.eclipse.control.chart;

import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

public class ChartPainter implements PaintListener {

	public ChartPainter(ChartProvider provider) {
		this.provider = provider;
	}
	
	private ChartProvider provider;
	
	public void paintControl(PaintEvent e) {
		Point size = ((Control)e.widget).getSize();
		
		int x = size.x-HorizontalScale.getNeededWidth(e.gc, 
					size.x-2-VerticalScale.getNeededWidth(e.gc, provider)-3, 
					provider);
		int y = size.y-2-HorizontalScale.getNeededHeight(e.gc, provider)-3;
		int w = HorizontalScale.paint(e.gc, x, y, size.x-2-x, size.y-2-y, provider);
		int h = VerticalScale.paint(e.gc, 2, 4, x-3, y-5, x+w, provider);
		
		if (provider.getType().equals(ScaleType.BAR)) {
			int stepX = w/(provider.getMaximumX() - provider.getMinimumX());
			double stepY = ((double)h)/((double)(provider.getMaximumY() - provider.getMinimumY()));
			e.gc.setBackground(provider.getChartColor());
			for (int i = provider.getMinimumX(); i <= provider.getMaximumX(); ++i) {
				int value = provider.getValue(i);
				int index = i - provider.getMinimumX();
				e.gc.fillRectangle(x+stepX*index, (int)(y-1-(int)(value*stepY)), stepX, (int)(value*stepY));
			}
		}
	}
	
	public static interface ChartProvider {
		public int getMinimumX();
		public int getMaximumX();
		public int getMinimumY();
		public int getMaximumY();
		public ScaleType getType();
		public int getValue(int x);
		public Color getScaleColor();
		public Color getScaleTextColor();
		public Color getChartColor();
		public Color getBackgroundColor();
	}

	public static enum ScaleType {
		/** Exact point */
		POINT, 
		/** Bar means the scale bar will be at the middle */
		BAR
	}
	
	static class HorizontalScale {
		
		public static int getMaxValueShown(int min, int max) {
			if ((max % 2) != 0)
				return max-1;
			return max;
		}
		
		public static int getNeededHeight(GC gc, ChartProvider provider) {
			return gc.textExtent(Integer.toString(getMaxValueShown(provider.getMinimumX(), provider.getMaximumX()))).y;
		}
		
		public static int getNeededWidth(GC gc, int maxWidth, ChartProvider provider) {
			int nb = provider.getMaximumX() - provider.getMinimumX();
			if (provider.getType().equals(ScaleType.BAR)) nb++;
			int w = (int)(((double)maxWidth)/((double)nb));
			if (provider.getType().equals(ScaleType.BAR))
				if ((w%2)==0) w--;
			int result = nb*w;
			int maxValue = getMaxValueShown(provider.getMinimumX(), provider.getMaximumX());
			int x = gc.textExtent(Integer.toString(maxValue)).x;
			int x2 = (maxValue-provider.getMinimumX())*w;
			int xMaxText = provider.getType().equals(ScaleType.BAR) ? (x2-w/2)+x : x2+x;
			return Math.max(result, xMaxText);
		}
		
		public static int paint(GC gc, int x, int y, int w, int h, ChartProvider provider) {
			int nb = provider.getMaximumX() - provider.getMinimumX();
			if (provider.getType().equals(ScaleType.BAR)) nb++;
			int stepX = (int)(((double)w)/((double)nb));
			if (provider.getType().equals(ScaleType.BAR))
				if ((stepX%2)==0) stepX--;
			
			gc.setForeground(provider.getScaleColor());
			gc.drawLine(x, y, x+nb*stepX, y);

			// maxValue
			int maxValue = getMaxValueShown(provider.getMinimumX(), provider.getMaximumX());
			int xValue = x + maxValue*stepX;
			if (provider.getType().equals(ScaleType.BAR))
				xValue += stepX/2;
			Point sizeTextMax = gc.textExtent(Integer.toString(maxValue));
			gc.setForeground(provider.getScaleColor());
			gc.drawLine(xValue, y+1, xValue, y+2);
			gc.setForeground(provider.getScaleTextColor());
			gc.drawText(Integer.toString(maxValue), xValue-sizeTextMax.x/2, y+2, false);
			
			// zero
			Point sizeTextZero = gc.textExtent(Integer.toString(provider.getMinimumX()));
			gc.setForeground(provider.getScaleColor());
			gc.drawLine(x, y, x, y+2);
			gc.setForeground(provider.getScaleTextColor());
			gc.drawText(Integer.toString(provider.getMinimumX()), x-sizeTextZero.x/2, y+2, true);
			
			
			tryDrawScale(gc, provider.getMinimumX(), maxValue, stepX, x + sizeTextZero.x/2, xValue-sizeTextMax.x/2, x, y, provider);
			
			return nb*stepX;
		}
		
		private static void tryDrawScale(GC gc, int min, int max, int stepX, int xMin, int xMax, int xStart, int y, ChartProvider provider) {
			if (((max-min)%2) != 0) return;
			int value = min+(max-min)/2;
			Point sizeText = gc.textExtent(Integer.toString(value));
			if (xMax-xMin < sizeText.x + /*margin*/ 3) return;
			int xValue = xStart + value*stepX;
			if (provider.getType().equals(ScaleType.BAR))
				xValue += stepX/2;
			
			gc.setForeground(provider.getScaleColor());
			gc.drawLine(xValue, y+1, xValue, y+2);
			gc.setForeground(provider.getScaleTextColor());
			gc.drawText(Integer.toString(value), xValue - sizeText.x/2, y+2);
			
			tryDrawScale(gc, min, value, stepX, xMin, xValue - sizeText.x/2, xStart, y, provider);
			tryDrawScale(gc, value, max, stepX, xValue + sizeText.x/2, xMax, xStart, y, provider);
		}
	}

	static class VerticalScale {
		
		public static int getMaxValueShown(int min, int max) {
			if ((max % 2) != 0)
				return max-1;
			return max;
		}
		
		public static int getNeededWidth(GC gc, ChartProvider provider) {
			return gc.textExtent(Integer.toString(getMaxValueShown(provider.getMinimumY(), provider.getMaximumY()))).x;
		}
		
		public static int getNeededHeight(GC gc, int maxHeight, ChartProvider provider) {
			int nb = provider.getMaximumY() - provider.getMinimumY();
			int h = (int)(((double)maxHeight)/((double)nb));
			int result = nb*h;
			int maxValue = getMaxValueShown(provider.getMinimumY(), provider.getMaximumY());
			int y = gc.textExtent(Integer.toString(maxValue)).y;
			int y2 = (maxValue-provider.getMinimumY())*h;
			int yMaxText = y2+y;
			return Math.max(result, yMaxText);
		}
		
		public static int paint(GC gc, int x, int y, int w, int h, int maxX, ChartProvider provider) {
			int nb = provider.getMaximumY() - provider.getMinimumY();
			double stepY = ((double)h)/((double)nb);

			gc.setForeground(provider.getScaleColor());
			gc.drawLine(x + w, (int)(y+h-nb*stepY), x + w, y+h);

			// maxValue
			int maxValue = getMaxValueShown(provider.getMinimumY(), provider.getMaximumY());
			int yValue = (int)(y+h - maxValue*stepY);
			Point sizeTextMax = gc.textExtent(Integer.toString(maxValue));
			if (maxValue > 0) {
				gc.setForeground(provider.getScaleColor());
				gc.drawLine(x+w-1, yValue, x+w-2, yValue);
				gc.setForeground(ColorUtil.goTo(provider.getScaleColor(), provider.getBackgroundColor(), 2, 1));
				gc.setLineStyle(SWT.LINE_DOT);
				gc.drawLine(x+w+1, yValue, maxX, yValue);
				gc.setLineStyle(SWT.LINE_SOLID);
				gc.setForeground(provider.getScaleTextColor());
				gc.drawText(Integer.toString(maxValue), x+w-2-sizeTextMax.x, yValue-sizeTextMax.y/2);
			}
			
			// zero
			Point sizeTextZero = gc.textExtent(Integer.toString(provider.getMinimumY()));
			gc.setForeground(provider.getScaleColor());
			gc.drawLine(x+w-1, y+h, x+w-2, y+h);
			gc.setForeground(provider.getScaleTextColor());
			gc.drawText(Integer.toString(provider.getMinimumY()), x+w-2-sizeTextZero.x, y+h-sizeTextZero.y/2, true);
			
			if (nb > 0)
				tryDrawScale(gc, provider.getMinimumY(), maxValue, stepY, yValue+sizeTextMax.y/2, y+h-sizeTextZero.y/2, x, w, y, h, maxX, provider);
			
			return (int)(nb*stepY);
		}

		private static void tryDrawScale(GC gc, int min, int max, double stepY, int yMin, int yMax, int x, int w, int yStart, int h, int maxX, ChartProvider provider) {
			if (min == max) return;
			if (((max-min)%2) != 0) return;
			int value = min+(max-min)/2;
			Point sizeText = gc.textExtent(Integer.toString(value));
			if (yMax-yMin < sizeText.y + /*margin*/ 3) return;
			int yValue = (int)(yStart+h - value*stepY);
			
			gc.setForeground(provider.getScaleColor());
			gc.drawLine(x+w-1, yValue, x+w-2, yValue);
			gc.setForeground(provider.getScaleTextColor());
			gc.drawText(Integer.toString(value), x+w-2-sizeText.x, yValue - sizeText.y/2);
			gc.setForeground(ColorUtil.goTo(provider.getScaleColor(), provider.getBackgroundColor(), 2, 1));
			gc.setLineStyle(SWT.LINE_DOT);
			gc.drawLine(x+w+1, yValue, maxX, yValue);
			gc.setLineStyle(SWT.LINE_SOLID);
			
			tryDrawScale(gc, value, max, stepY, yMin, yValue - sizeText.y/2, x, w, yStart, h, maxX, provider);
			tryDrawScale(gc, min, value, stepY, yValue + sizeText.y/2, yMax, x, w, yStart, h, maxX, provider);
		}
	}
	
}
