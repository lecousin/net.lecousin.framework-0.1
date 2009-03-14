package net.lecousin.framework.ui.eclipse.control.button;

import net.lecousin.framework.ui.eclipse.control.BorderStyle;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.swt.graphics.Color;


public class ButtonStyle {

	public boolean pushable = true;
	public ButtonStyle pushable(boolean value) { pushable = value; return this; }
	
	public BorderStyle border_normal = BorderStyle.NONE;
	public ButtonStyle border_normal(BorderStyle style) { border_normal = style; return this; }
	public BorderStyle border_push = BorderStyle.SHADOW_DOWN;
	public ButtonStyle border_push(BorderStyle style) { border_push = style; return this; }
	public BorderStyle border_hover = BorderStyle.SHADOW_UP;
	public ButtonStyle border_hover(BorderStyle style) { border_hover = style; return this; }
	
	public HoverStyle hover = HoverStyle.NONE;
	public ButtonStyle hover(HoverStyle style) { hover = style; return this; }
	
    public Color shadowDownColor = ColorUtil.get(128, 128, 128);
    public Color shadowUpColor = ColorUtil.get(240, 240, 240);
	
}
