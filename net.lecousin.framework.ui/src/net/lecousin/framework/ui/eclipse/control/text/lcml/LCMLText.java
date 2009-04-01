package net.lecousin.framework.ui.eclipse.control.text.lcml;

import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.control.text.lcml.internal.LCMLParser;
import net.lecousin.framework.ui.eclipse.control.text.lcml.internal.Link;
import net.lecousin.framework.ui.eclipse.control.text.lcml.internal.Paragraph;
import net.lecousin.framework.ui.eclipse.control.text.lcml.internal.SectionContainer.GetAllFinder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Supports the following:<ul>
 * <li>b: for bold section</li>
 * <li>i: for italic section</li>
 * <li>br: for break line</li>
 * <li>a: for hyperlink<ul>
 *     <li>href: link url, to be catched by an hyperlink listener</li>
 *     </ul></li>
 * <li>p: for paragraph<ul>
 *     <li>marginTop: spacing before the paragraph (default is 3)</li>
 *     <li>marginBotton: spacing after the paragraph (default is 0)</li>
 *     <li>marginLeft: indentation of the paragraph (default is 0)</li>
 *     </ul></li>
 * </ul> 
 *
 */
public class LCMLText {

	public LCMLText(Composite parent, boolean hScroll, boolean vScroll) {
		this.hScroll = hScroll;
		this.vScroll = vScroll;
		if (hScroll || vScroll) {
			int style = 0;
			if (hScroll) style |= SWT.H_SCROLL;
			if (vScroll) style |= SWT.V_SCROLL;
			scroll = new ScrolledComposite(parent, style);
			if (!hScroll)
				scroll.setExpandHorizontal(true);
			if (!vScroll)
				scroll.setExpandVertical(true);
			panel = new Panel(scroll);
			scroll.setContent(panel);
		} else
			panel = new Panel(parent);
		panel.setBackground(parent.getBackground());
//		panel.addControlListener(new ControlListener() {
//			public void controlMoved(ControlEvent e) {
//			}
//			public void controlResized(ControlEvent e) {
//				if (resizing) return;
//				refreshPanel();
//			}
//		});
	}
	
	private ScrolledComposite scroll;
	private Composite panel;
	private boolean hScroll, vScroll;
	private Paragraph text;
	
	public Control getControl() { return scroll != null ? scroll : panel; }
	
	public void setText(String ml) {
		if (text != null)
			text.removeControls();
		text = LCMLParser.parse(ml);
		refreshPanel();
	}
	
	public void addLinkListener(String href, Runnable listener) {
		Link link = text.findLink(href);
		if (link != null)
			link.addLinkListener(listener);
	}
	public void addLinkListener(Listener<String> listener) {
		List<Link> links = text.findSections(Link.class, new GetAllFinder<Link>());
		for (Link link : links)
			link.addLinkListener(new RunnableWithData<Pair<String,Listener<String>>>(new Pair<String,Listener<String>>(link.getHRef(), listener)) {
				public void run() {
					data().getValue2().fire(data().getValue1());
				}
			});
	}
	
	public void setLayoutData(Object o) { (scroll != null ? scroll : panel).setLayoutData(o); }

//	private boolean resizing = false;
	private void refreshPanel() {
		int width;
		if (scroll == null)
			width = panel.getSize().x;
		else {
			if (hScroll)
				width = -1;
			else
				width = scroll.getClientArea().width;
		}
		if (width == 0) return;
		if (text == null) return;
		Point size = text.refreshControls(panel, width);
//		resizing = true;
		if (!size.equals(panel.getSize())) {
			panel.setSize(size);
		}
		if (!vScroll && scroll != null) {
			// TODO resize scroll vertically ???
		}
//		resizing = false;
	}
	
	private class Panel extends Composite {
		public Panel(Composite parent) {
			super(parent, SWT.NONE);
			setLayout(new Layout());
			addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					scroll = null;
					panel = null;
					text = null;
				}
			});
		}
		@Override
		public Point computeSize(int hint, int hint2, boolean changed) {
			if (text == null)
				return new Point(0,0);
			return text.refreshSize(panel, hint == SWT.DEFAULT ? -1 : hint, false);
		}
		private class Layout extends org.eclipse.swt.widgets.Layout {
			@Override
			protected Point computeSize(Composite composite, int hint, int hint2, boolean flushCache) {
				return Panel.this.computeSize(hint, hint2, flushCache);
			}
			@Override
			protected void layout(Composite composite, boolean flushCache) {
				refreshPanel();
			}
		}
	}
}
