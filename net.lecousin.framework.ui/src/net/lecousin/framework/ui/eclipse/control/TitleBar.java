package net.lecousin.framework.ui.eclipse.control;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.lecousin.framework.event.Event;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class TitleBar extends Canvas implements PaintListener, MouseListener, MouseTrackListener, MouseMoveListener {

	public TitleBar(Composite parent) {
		super(parent, SWT.NONE);
		setToolTipText(null);
		addMouseListener(this);
        addMouseTrackListener(this);
        addMouseMoveListener(this);
        addPaintListener(this);
	}

	private Color borderColor = new Color(getDisplay(), 255, 255, 255);
	private Image icon = null;
	private String title = "";
	private List<Tool> tools = new LinkedList<Tool>();
	private Tool toolHover = null;
	private boolean mouseDown = false;
	
	public int MARGIN_WIDTH = 2;
	public int MARGIN_HEIGHT = 2;
	public int ICON_TEXT_SPACE = 5;
	public int TEXT_TOOLS_SPACE = 5;
	public int TOOL_SPACING = 2;
	public int TOOL_MARGIN = 2;
	
	protected static class Tool {
		Tool(String name, String tooltip, Image icon, Event.Listener<MouseEvent> clickListener, boolean shown)
		{ this.name= name; this.tooltip = tooltip; this.icon = icon; this.clickListener = clickListener; this.shown = shown; }
		String name;
		String tooltip;
		Image icon;
		Event.Listener<MouseEvent> clickListener;
		boolean shown;
		int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
	}
	
	public void setRoundedBorderColor(Color color) { borderColor = color; }
	public void setIcon(Image icon) { this.icon = icon; }
	public void setTitle(String title) { this.title = title; }
	public void addTool(String name, String tooltip, Image icon, Event.Listener<MouseEvent> clickListener, boolean shown) {
		tools.add(new Tool(name, tooltip, icon, clickListener, shown));
	}
	
	public String getTitle() { return title; }
	
	public void paintControl(PaintEvent e) {
		Point size = getSize();
		DrawingUtil.drawRoundedRectangle(e.gc, e.display, new Point(0, 0), size, getBackground(), borderColor);
		int x = MARGIN_WIDTH;
		if (icon != null) {
			e.gc.drawImage(icon, x, MARGIN_HEIGHT);
			x += icon.getBounds().width + ICON_TEXT_SPACE;
		}
		if (hasTitleToDisplay()) {
			x = drawTitleText(e.gc, x);
			x += TEXT_TOOLS_SPACE;
		}
		
		x = size.x - MARGIN_WIDTH;
		if (tools.size() > 0) {
			boolean first = true;
			for (ListIterator<Tool> it = tools.listIterator(tools.size()); it.hasPrevious(); ) {
				Tool tool = it.previous();
				if (!tool.shown) continue;
				if (first)
					first = false;
				else
					x -= TOOL_SPACING;
				tool.x2 = x;
				tool.y1 = MARGIN_HEIGHT;
				drawTool(e.gc, tool);
				x = tool.x1;
			}
		}
	}
	
	protected int drawTitleText(GC gc, int x) {
		gc.setForeground(getForeground());
		gc.drawText(title, x, MARGIN_HEIGHT);
		return x + getTitleSize(gc).x;
	}
	protected Point computeTitleTextSize() {
		return getTitleSize(null);
	}
	private Point lastTitleSize = null;
	private String lastString = null;
	private Font lastFont = null;
	private Point getTitleSize(GC gc) {
		if (lastTitleSize != null) {
			if (lastString.equals(title) && lastFont.equals(getFont()))
				return lastTitleSize;
		}
		if (gc != null)
			lastTitleSize = gc.textExtent(title);
		else {
			gc = new GC(this);
			lastTitleSize = gc.textExtent(title);
			gc.dispose();
		}
		lastString = title;
		lastFont = getFont();
		return lastTitleSize;
	}
	protected boolean hasTitleToDisplay() {
		return title.length() > 0;
	}
	
	private Color toolPushDownBorderColor = new Color(getDisplay(), 128, 128, 128);
	private Color toolPushUpBorderColor = new Color(getDisplay(), 240, 240, 240);

	
	protected void drawTool(GC gc, Tool tool) {
		Rectangle r = tool.icon.getBounds();
		tool.x1 = tool.x2 - r.width - TOOL_MARGIN * 2;
		tool.y2 = tool.y1 + r.height + TOOL_MARGIN * 2;
		gc.drawImage(tool.icon, tool.x1 + TOOL_MARGIN, tool.y1 + TOOL_MARGIN);
		DrawingUtil.drawPushButtonBorder(gc, new Point(tool.x1, tool.y1), new Point(tool.x2 - tool.x1, tool.y2 - tool.y1), 1, tool == toolHover && mouseDown, tool == toolHover, getBackground(), toolPushDownBorderColor, toolPushUpBorderColor);
	}
	protected Point computeToolSize(Tool tool) {
		if (tool.x1 != -1)
			return new Point(tool.x2 - tool.x1, tool.y2 - tool.y1);
		Rectangle r = tool.icon.getBounds();
		return new Point(r.width + TOOL_MARGIN * 2, r.height + TOOL_MARGIN * 2);
	}
	
	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
		int x = MARGIN_WIDTH;
		int y = 0;
		if (icon != null) {
			Rectangle r = icon.getBounds();
			x += r.width + ICON_TEXT_SPACE;
			if (y < r.height) y = r.height;
		}
		if (hasTitleToDisplay()) {
			Point p = computeTitleTextSize();
			x += p.x + TEXT_TOOLS_SPACE;
			if (y < p.y) y = p.y;
		}
		boolean first = true;
		for (Iterator<Tool> it = tools.iterator(); it.hasNext(); ) {
			Tool tool = it.next();
			if (!tool.shown) continue;
			if (first)
				first = false;
			else
				x += TOOL_SPACING;
			Point p = computeToolSize(tool);
			x += p.x;
			if (y < p.y) y = p.y;
		}
		x += MARGIN_WIDTH;
		y += MARGIN_HEIGHT * 2;
		return new Point(x,y);
	}
	
	public void mouseDoubleClick(MouseEvent e) {
		// nothing to do
	}
	public void mouseDown(MouseEvent e) {
		mouseDown = true;
		if (toolHover != null)
			redraw(toolHover.x1, toolHover.y1, toolHover.x2 - toolHover.x1, toolHover.y2 - toolHover.y1, false);
	}
	public void mouseUp(MouseEvent e) {
		mouseDown = false;
		if (toolHover != null) {
			redraw(toolHover.x1, toolHover.y1, toolHover.x2 - toolHover.x1, toolHover.y2 - toolHover.y1, false);
			update();
			toolHover.clickListener.fire(e);
		}
	}
	public void mouseEnter(MouseEvent e) {
		Tool previousHover = toolHover;
		toolHover = null;
		int startx = -1, endx = -1, endy = -1;
		for (Iterator<Tool> it = tools.iterator(); it.hasNext(); ) {
			Tool tool = it.next();
			if (toolHover == null && tool.x1 <= e.x && tool.x2 >= e.x && tool.y1 <= e.y && tool.y2 >= e.y) {
				toolHover = tool;
				if (previousHover != tool) {
					if (startx == -1)
						startx = tool.x1;
					endx = tool.x2;
					if (endy < tool.y2) endy = tool.y2;
				}
			} else {
				if (tool == previousHover) {
					if (startx == -1)
						startx = tool.x1;
					endx = tool.x2;
					if (endy < tool.y2) endy = tool.y2;
				}
			}
		}

		setToolTipText(toolHover != null ? toolHover.tooltip : null);
		if (startx != -1)
			redraw(startx, MARGIN_HEIGHT, endx - startx, endy - MARGIN_HEIGHT, false);
	}
	public void mouseExit(MouseEvent e) {
		setToolTipText(null);
		int startx = -1, endx = -1, endy = -1;
		for (Iterator<Tool> it = tools.iterator(); it.hasNext(); ) {
			Tool tool = it.next();
			if (toolHover == tool) {
				toolHover = null;
				if (startx == -1)
					startx = tool.x1;
				endx = tool.x2;
				if (endy < tool.y2) endy = tool.y2;
			}
		}
		if (startx != -1)
			redraw(startx, MARGIN_HEIGHT, endx - startx, endy - MARGIN_HEIGHT, false);
	}
	public void mouseHover(MouseEvent e) {
		mouseEnter(e);
	}
	public void mouseMove(MouseEvent e) {
		mouseEnter(e);
	}
	
	protected Tool getTool(String name) {
		for (Iterator<Tool> it = tools.iterator(); it.hasNext(); ) {
			Tool tool = it.next();
			if (tool.name.equals(name))
				return tool;
		}
		return null;
	}
	
	public void setToolShown(String toolName, boolean shown) {
		Tool tool = getTool(toolName);
		if (tool != null) {
			if (tool.shown != shown) {
				tool.shown = shown;
				Tool tool1 = tools.get(0);
				redraw(tool1.x1, tool1.y1, tool.x2 - tool1.x1, tool.y2 - tool1.y1, false);
			}
		}
	}
	public void setTool(String toolName, String tooltip, Image icon) {
		Tool tool = getTool(toolName);
		if (tool == null) return;
		boolean change = !tool.tooltip.equals(tooltip) || tool.icon != icon;
		if (!change) return;
		tool.tooltip = tooltip;
		tool.icon = icon;
		redraw(tool.x1, tool.y1, tool.x2 - tool.x1, tool.y2 - tool.y1, false);
	}
	public Point getToolLocation(String toolName) {
		Tool tool = getTool(toolName);
		if (tool == null) return new Point(0, 0);
		return new Point(tool.x1, tool.y1);
	}
	public Point getToolSize(String toolName) {
		Tool tool = getTool(toolName);
		if (tool == null) return new Point(0, 0);
		return new Point(tool.x2 - tool.x1, tool.y2 - tool.y1);
	}
}
