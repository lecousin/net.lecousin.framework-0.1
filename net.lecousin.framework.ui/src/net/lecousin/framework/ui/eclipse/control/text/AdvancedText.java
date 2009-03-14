package net.lecousin.framework.ui.eclipse.control.text;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledFormText;

public class AdvancedText {

	public AdvancedText(Composite parent, int style, boolean scrollable) {
		text = scrollable ? new ScrolledFormText(parent, style, true).getFormText() : new FormText(parent, style);
		text.addHyperlinkListener(new HyperlinkHandler());
		text.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				((Control)e.widget).getParent().layout(true);
			}
		});
	}
	
	private FormText text;
	private Map<String,Runnable> hyperlinkListeners = new HashMap<String,Runnable>();
	
	public Composite getControl() { return text; }
	public void setText(String text) { this.text.setText("<form>" + text + "</form>", true, false); }
	public void handleHyperlink(String href, Runnable listener) {
		hyperlinkListeners.put(href, listener);
	}
	
	private class HyperlinkHandler implements IHyperlinkListener {
		public void linkActivated(HyperlinkEvent e) {
			Runnable listener = hyperlinkListeners.get(e.getHref());
			if (listener != null)
				listener.run();
		}
		public void linkEntered(HyperlinkEvent e) {
		}
		public void linkExited(HyperlinkEvent e) {
		}
	}
	
	public void setLayoutData(Object data) { text.setLayoutData(data); }
}
