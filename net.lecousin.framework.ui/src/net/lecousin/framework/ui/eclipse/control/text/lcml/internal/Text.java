package net.lecousin.framework.ui.eclipse.control.text.lcml.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class Text extends TextSection {

	public Text(String text, boolean bold, boolean italic) {
		this.text = text;
		this.bold = bold;
		this.italic = italic;
	}
	
	private String text;
	private boolean bold;
	private boolean italic;
	
	@Override
	public String getText() {
		return text;
	}
	@Override
	public Font getFont(Composite parent) {
		FontData data = parent.getFont().getFontData()[0];
		int style = 0;
		if (bold) style |= SWT.BOLD;
		if (italic) style |= SWT.ITALIC;
		return new Font(parent.getDisplay(), data.getName(), data.getHeight(), style);
	}
	@Override
	public void configureLabel(Label label) {
	}
}
