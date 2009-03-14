package net.lecousin.framework.ui.eclipse.control.chart;

import net.lecousin.framework.ui.eclipse.control.chart.ChartPainter.ChartProvider;
import net.lecousin.framework.ui.eclipse.control.chart.ChartPainter.ScaleType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class BarChart extends Canvas {

	public BarChart(Composite parent, int minimum, int maximum, Color backgroundColor, Color scaleColor, Color barColor) {
		super(parent, SWT.NONE);
		setBackground(backgroundColor);
		min = minimum;
		max = maximum;
		values = new int[max-min+1];
		this.scaleColor = scaleColor;
		this.barColor = barColor;
		addPaintListener(new ChartPainter(new Provider()));
	}
	
	private int min, max;
	private int[] values;
	private Color scaleColor, barColor;
	
	public int getMinimum() { return min; }
	public int getMaximum() { return max; }
	
	public void addValue(int value, int amount) {
		values[value] += amount;
	}
	
	private int getMaximumValue() {
		int max = 0;
		for (int v : values)
			if (v > max) max = v;
		return max;
	}
	
	private class Provider implements ChartProvider {
		public int getMinimumX() { return min; }
		public int getMaximumX() { return max; }
		public int getMinimumY() { return 0; }
		public int getMaximumY() { return getMaximumValue(); }
		public ScaleType getType() { return ScaleType.BAR; }
		public Color getScaleColor() { return scaleColor; }
		public Color getScaleTextColor() { return scaleColor; }
		public Color getChartColor() { return barColor; }
		public Color getBackgroundColor() { return BarChart.this.getBackground(); }
		public int getValue(int x) { return values[x]; }
	}
	
}
