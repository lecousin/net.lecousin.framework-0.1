package net.lecousin.framework.ui.eclipse;

import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.collections.CollectionUtil;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.event.Event.ListenerData;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.control.ImageAndTextButton;
import net.lecousin.framework.ui.eclipse.control.LabelButton;
import net.lecousin.framework.ui.eclipse.control.Radio;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.event.DisposeListenerWithData;
import net.lecousin.framework.ui.eclipse.event.HyperlinkListenerWithData;
import net.lecousin.framework.ui.eclipse.event.SelectionListenerWithData;
import net.lecousin.framework.ui.eclipse.graphics.ColorUtil;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;

public abstract class UIUtil {

    public static MenuManager getMenu(IMenuManager menu, String title) {
        IContributionItem[] items = menu.getItems();
        MenuManager result = null;
        for (int i = 0; i < items.length && result == null; ++i)
            if (items[i] instanceof MenuManager) {
                String text = ((MenuManager)items[i]).getMenuText();
                int j = text.indexOf('&');
                if (j > 0 && j < text.length() - 1) 
                    text = text.substring(0, j) + text.substring(j + 1);
                else if (j > 0)
                    text = text.substring(0, j);
                else if (j == 0)
                    text = text.substring(1);
                if (text.equals(title))
                    result = (MenuManager)items[i];
            }
        return result;
    }

    public static IContributionItem getMenuItem(IMenuManager menu, String title) {
        IContributionItem[] items = menu.getItems();
        IContributionItem result = null;
        for (int i = 0; i < items.length && result == null; ++i) {
            String text = null;
            if (items[i] instanceof IMenuManager)
                text = ((MenuManager)items[i]).getMenuText();
            else if (items[i] instanceof ActionContributionItem)
                text = ((ActionContributionItem)items[i]).getAction().getText();
            if (text == null) continue;
            int j = text.indexOf('&');
            if (j > 0 && j < text.length() - 1) 
                text = text.substring(0, j) + text.substring(j + 1);
            else if (j > 0)
                text = text.substring(0, j);
            else if (j == 0)
                text = text.substring(1);
            if (text.equals(title))
                result = items[i];
        }
        return result;
    }
    
    public static void addSubMenuItem(IMenuManager parentMenu, String subMenuName, String groupName, IAction action) {
    	MenuManager subMenu = getMenu(parentMenu, subMenuName);
    	if (subMenu == null) {
            subMenu = new MenuManager(subMenuName);
            parentMenu.add(subMenu);
    	}
    	addMenuItem(subMenu, groupName, action);
    }
    
    public static void addMenuItem(IMenuManager menu, String groupName, IAction action) {
    	IContributionItem[] items = menu.getItems();
    	boolean found = false;
    	for (int i = 0; i < items.length && !found; ++i) {
    		if (items[i].isGroupMarker() && groupName.equals(items[i].getId()))
    			found = true;
    	}
    	if (!found) {
    		menu.add(new GroupMarker(groupName));
    	}
    	menu.appendToGroup(groupName, action);
    }
    
    public static void runPendingEvents(Display display) {
    	while (display.readAndDispatch()){ /* nothing to do */}
    }
    public static void runPendingEventsIfDisplayThread(Display display) {
    	if (display.getThread() != Thread.currentThread()) return;
    	runPendingEvents(display);
    }
    
	public static void runBackgroundThread(Runnable run, Display display) {
		Thread t = new Thread(new RunnableWithData<Pair<Runnable,Display>>(new Pair<Runnable,Display>(run,display)) {
			public void run() {
				data().getValue1().run();
				data().getValue2().wake();
			}
		});
		t.start();
		do {
			if (!display.readAndDispatch() && t.isAlive()) display.sleep();
		} while (t.isAlive());
	}
    
    public static void resize(Composite panel) {
    	Point size = panel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        panel.setSize(size);
    }
    
    public static org.eclipse.swt.layout.GridLayout gridLayout(Composite panel, int numColumns) {
    	org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout();
    	layout.numColumns = numColumns;
    	panel.setLayout(layout);
    	return layout;
    }
    public static org.eclipse.swt.layout.GridLayout gridLayout(Composite panel, int numColumns, int marginHeight, int marginWidth) {
    	org.eclipse.swt.layout.GridLayout layout = gridLayout(panel, numColumns);
    	layout.marginHeight = marginHeight;
    	layout.marginWidth = marginWidth;
    	return layout;
    }
    public static org.eclipse.swt.layout.GridLayout gridLayout(Composite panel, int numColumns, int marginHeight, int marginWidth, int horizSpace, int vertSpace) {
    	org.eclipse.swt.layout.GridLayout layout = gridLayout(panel, numColumns);
    	layout.marginHeight = marginHeight;
    	layout.marginWidth = marginWidth;
    	layout.horizontalSpacing = horizSpace;
    	layout.verticalSpacing = vertSpace;
    	return layout;
    }
    
    public static RowLayout rowLayout(Composite panel, int type, int marginWidth, int marginHeight, int spacing, boolean wrap) {
    	RowLayout layout = new RowLayout(type);
    	layout.marginBottom = layout.marginTop = 0;
    	layout.marginHeight = marginHeight;
    	layout.marginLeft = layout.marginRight = 0;
    	layout.marginWidth = marginWidth;
    	layout.spacing = spacing;
    	layout.wrap = wrap;
    	panel.setLayout(layout);
    	return layout;
    }
    
    public static GridData gridDataHorizFill(Control ctrl) {
    	GridLayout layout = (GridLayout)ctrl.getParent().getLayout();
    	GridData gd = gridDataHoriz(layout.numColumns, true);
    	ctrl.setLayoutData(gd);
    	return gd;
    }
    
    public static GridData gridDataHoriz(int colspan, boolean fillHoriz) {
    	return gridData(colspan, fillHoriz, 1, false);
    }
    public static GridData gridDataVert(int rowspan, boolean fillVert) {
    	return gridData(1, false, rowspan, fillVert);
    }
    
    public static GridData gridData(int horizspan, boolean fillHoriz, int vertspan, boolean fillVert) {
    	GridData gd = new GridData();
    	gd.horizontalSpan = horizspan;
    	if (fillHoriz) {
    		gd.horizontalAlignment = SWT.FILL;
    		gd.grabExcessHorizontalSpace = true;
    	}
    	gd.verticalSpan = vertspan;
    	if (fillVert) {
    		gd.verticalAlignment = SWT.FILL;
    		gd.grabExcessVerticalSpace = true;
    	}
    	return gd;
    }
    
    public static GridData gridDataCenterHoriz(Control control) {
    	GridLayout layout = (GridLayout)control.getParent().getLayout();
    	GridData gd = new GridData();
    	gd.horizontalAlignment = SWT.CENTER;
    	gd.horizontalSpan = layout.numColumns;
    	control.setLayoutData(gd);
    	return gd;
    }
    
    public static Color modifyColor(Color color, int amount) {
    	int r = color.getRed() + amount;
    	int g = color.getGreen() + amount;
    	int b = color.getBlue() + amount;
    	if (r < 0) r = 0;
    	if (r > 255) r = 255;
    	if (g < 0) g = 0;
    	if (g > 255) g = 255;
    	if (b < 0) b = 0;
    	if (b > 255) b = 255;
    	return new Color(color.getDevice(), r, g, b);
    }
    
    public static Label newLabel(Composite parent, Object text) {
    	Label label = new Label(parent, SWT.NONE);
    	label.setText(text.toString());
		label.setBackground(parent.getBackground());
    	return label;
    }
    public static Label newLabel(Composite parent, Object text, boolean bold, boolean italic) {
    	Label label = newLabel(parent, text);
    	if (bold || italic) {
    		int style = 0;
    		if (bold) style |= SWT.BOLD;
    		if (italic) style |= SWT.ITALIC;
    		UIControlUtil.setFontStyle(label, style);
    	}
    	return label;
    }
    
    public static Label newLabelAutoRefresh(Composite parent, LabelProvider refresher, int milliseconds) {
    	Label label = newLabel(parent, refresher.getValue().toString());
    	label.getDisplay().timerExec(milliseconds, new RunnableWithData<Triple<Label,LabelProvider,Integer>>(new Triple<Label,LabelProvider,Integer>(label, refresher, milliseconds)) {
    		public void run() {
    			if (data().getValue1().isDisposed()) return;
    			data().getValue1().setText(data().getValue2().getValue().toString());
    			data().getValue1().setForeground(data().getValue2().getColor());
    			UIControlUtil.autoresize(data().getValue1());
    			data().getValue1().getDisplay().timerExec(data().getValue3().intValue(), this);
    		}
    	});
    	return label;
    }
    @SuppressWarnings("unchecked")
	public static Label newLabelAutoRefresh(Composite parent, LabelProvider refresher, Event refreshEvent) {
    	return newLabelAutoRefresh(parent, refresher, CollectionUtil.single_element_list(refreshEvent));
    }
    @SuppressWarnings("unchecked")
    public static Label newLabelAutoRefresh(Composite parent, LabelProvider refresher, List<Event> refreshEvents) {
    	Label label = newLabel(parent, refresher.getValue().toString());
		label.setForeground(refresher.getColor());
    	Runnable updater = new RunnableWithData<Pair<Label,LabelProvider>>(new Pair<Label,LabelProvider>(label, refresher)) {
    		public void run() {
    			data().getValue1().getDisplay().asyncExec(new RunnableWithData<Pair<Label,LabelProvider>>(data()) {
    	    		public void run() {
    	    			data().getValue1().setText(data().getValue2().getValue().toString());
    	    			data().getValue1().setForeground(data().getValue2().getColor());
    	    			UIControlUtil.autoresize(data().getValue1());
    	    		}    				
    			});
    		}
    	};
    	label.addDisposeListener(new DisposeListenerWithData<Pair<Runnable,List<Event>>>(new Pair<Runnable,List<Event>>(updater, refreshEvents)) {
    		public void widgetDisposed(DisposeEvent e) {
    			for (Event<?> event : data().getValue2())
    				event.removeFireListener(data().getValue1());
    		}
    	});
    	for (Event<?> event : refreshEvents)
    		event.addFireListener(updater);
    	return label;
    }
    
    public static abstract class LabelProvider {
    	public abstract Object getValue();
    	public Color getColor() { return ColorUtil.getBlack(); }
    }
    public static abstract class LabelProviderData<T> extends LabelProvider {
    	public LabelProviderData(T data) { this.data = data; }
    	private T data;
    	public T data() { return data; }
    	public abstract Object getValue();
    	public Color getColor() { return ColorUtil.getBlack(); }
    }
    
    public static Label newImage(Composite parent, Image image) {
    	Label label = new Label(parent, SWT.NONE);
    	label.setImage(image);
    	label.setBackground(parent.getBackground());
    	return label;
    }
    
    public static Label newSeparator(Composite parent, boolean horiz, boolean fill) {
    	Label label = new Label(parent, SWT.SEPARATOR | (horiz ? SWT.HORIZONTAL : SWT.VERTICAL)) {
    		@Override
    		protected void checkSubclass() {
    		}
    		@Override
    		public Point computeSize(int hint, int hint2, boolean changed) {
    			Point size = super.computeSize(hint, hint2, changed);
    			if ((getStyle() & SWT.HORIZONTAL) != 0) {
    				if (hint == SWT.DEFAULT)
    					size.x = 10;
    			} else {
    				if (hint2 == SWT.DEFAULT)
    					size.y = 10;
    			}
    			return size;
    		}
    	};
    	if (fill) {
	    	if (horiz) {
	    		Layout layout = parent.getLayout();
	    		if (layout instanceof GridLayout) {
	    			gridDataHorizFill(label);
	    		}
	    	} else {
	    		Layout layout = parent.getLayout();
	    		if (layout instanceof GridLayout) {
	    			label.setLayoutData(gridDataVert(1, true));
	    		}
	    	}
    	}
    	return label;
    }
    
    public static <T> Button newButton(Composite parent, String text, int style, Event.Listener<Pair<Button,T>> clickListener, T data) {
		Button button = new Button(parent, style);
		button.setBackground(parent.getBackground());
		button.setText(text);
		if (clickListener != null)
			button.addSelectionListener(new SelectionListenerWithData<Pair<Event.Listener<Pair<Button,T>>,T>>(new Pair<Event.Listener<Pair<Button,T>>,T>(clickListener, data)) {
				public void widgetSelected(SelectionEvent e) {
					data().getValue1().fire(new Pair<Button,T>((Button)e.widget, data().getValue2()));
				}
			});
		return button;
    }
    public static <T> Button newButton(Composite parent, String text, Event.Listener<T> clickListener, T data) {
    	return newButton(parent, text, SWT.NONE, new Event.Listener<Pair<Button,Pair<T,Event.Listener<T>>>>() {
    		public void fire(Pair<Button, Pair<T,Event.Listener<T>>> event) {
    			event.getValue2().getValue2().fire(event.getValue2().getValue1());
    		}
    	}, new Pair<T,Event.Listener<T>>(data, clickListener));
    }
    public static <T> Button newCheck(Composite parent, String text, Event.Listener<Pair<Boolean,T>> clickListener, T data) {
    	if (clickListener != null)
	    	return newButton(parent, text, SWT.CHECK, new Event.Listener<Pair<Button,Pair<T,Event.Listener<Pair<Boolean,T>>>>>() {
	    		public void fire(Pair<Button, Pair<T,Event.Listener<Pair<Boolean,T>>>> event) {
	    			event.getValue2().getValue2().fire(new Pair<Boolean,T>(event.getValue1().getSelection(), event.getValue2().getValue1()));
	    		}
	    	}, new Pair<T,Event.Listener<Pair<Boolean,T>>>(data, clickListener));
    	return newButton(parent, text, SWT.CHECK, null, null);
    }
    
    public static <T> Radio newRadio(Composite parent, String[] options, Event.Listener<Pair<String,T>> listener, T data) {
    	Radio radio = new Radio(parent, false);
    	for (String s : options)
    		radio.addOption(s, s);
    	radio.addSelectionChangedListener(new ListenerData<String, Pair<Event.Listener<Pair<String,T>>,T>>(new Pair<Event.Listener<Pair<String,T>>,T>(listener, data)) {
    		public void fire(String event) {
    			data().getValue1().fire(new Pair<String,T>(event, data().getValue2()));
    		}
    	});
    	return radio;
    }
    
    public static Spinner newSpinner(Composite parent, int min, int max, int increment, int currentValue, Event.Listener<Integer> changeListener, boolean border) {
    	Spinner spinner = new Spinner(parent, border ? SWT.BORDER : SWT.NONE);
    	spinner.setMinimum(min);
    	spinner.setMaximum(max);
    	spinner.setIncrement(increment);
    	spinner.setSelection(currentValue);
    	if (changeListener != null)
	    	spinner.addSelectionListener(new SelectionListenerWithData<Event.Listener<Integer>>(changeListener) {
	    		public void widgetSelected(SelectionEvent e) {
	    			data().fire(((Spinner)e.widget).getSelection());
	    		}
	    	});
    	return spinner;
    }

	public static Composite compositeLabel(Composite parent, String text) {
		Composite panel = new Composite(parent, SWT.NONE);
		UIUtil.gridLayout(panel, 1);
		UIUtil.newLabel(panel, text);
		return panel;
	}
	
	public static Text newText(Composite parent, String text, ModifyListener listener) {
		Text ctrl = new Text(parent, SWT.BORDER);
		ctrl.setText(text != null ? text : "");
		if (listener != null)
			ctrl.addModifyListener(listener);
		return ctrl;
	}
	
	public static Hyperlink newLink(Composite parent, String text, IHyperlinkListener listener) {
		Hyperlink link = new Hyperlink(parent, SWT.NONE);
		link.setText(text);
		link.addHyperlinkListener(listener);
		link.setBackground(parent.getBackground());
		if (ColorUtil.isDark(parent.getBackground()))
			link.setForeground(ColorUtil.getWhite());
		return link;
	}
	public static Color getLinkSoftNetStyleColorHover() { return ColorUtil.get(0, 0, 160); }
	public static Color getLinkSoftNetStyleColorNormal() { return ColorUtil.get(0, 0, 60); }
	/** a link, soft blue in normal mode, more blue and underlined when hover */
	public static Hyperlink newLinkSoftNetStyle(Composite parent, String text, Listener<HyperlinkEvent> listener) {
		Hyperlink link = newLink(parent, text, new HyperlinkListenerWithData<Listener<HyperlinkEvent>>(listener) {
			public void linkActivated(HyperlinkEvent e) {
				data().fire(e);
			}
			public void linkEntered(HyperlinkEvent e) {
				((Hyperlink)e.widget).setForeground(getLinkSoftNetStyleColorHover());
				((Hyperlink)e.widget).setUnderlined(true);
			}
			public void linkExited(HyperlinkEvent e) {
				((Hyperlink)e.widget).setForeground(getLinkSoftNetStyleColorNormal());
				((Hyperlink)e.widget).setUnderlined(false);
			}
		});
		link.setForeground(getLinkSoftNetStyleColorNormal());
		return link;
	}
	public static Hyperlink newLink(Composite parent, String text, Listener<HyperlinkEvent> listener, Color colorNormal, Color colorHover) {
		Hyperlink link = newLink(parent, text, new HyperlinkListenerWithData<Triple<Listener<HyperlinkEvent>,Color,Color>>(new Triple<Listener<HyperlinkEvent>,Color,Color>(listener, colorNormal, colorHover)) {
			public void linkActivated(HyperlinkEvent e) {
				data().getValue1().fire(e);
			}
			public void linkEntered(HyperlinkEvent e) {
				((Hyperlink)e.widget).setForeground(data().getValue3());
				((Hyperlink)e.widget).setUnderlined(true);
			}
			public void linkExited(HyperlinkEvent e) {
				((Hyperlink)e.widget).setForeground(data().getValue2());
				((Hyperlink)e.widget).setUnderlined(false);
			}
		});
		link.setForeground(colorNormal);
		return link;
	}
	/** a link, blue and underlined */
	public static Hyperlink newLinkNetStyle(Composite parent, String text, Listener<HyperlinkEvent> listener) {
		Hyperlink link = newLink(parent, text, new HyperlinkListenerWithData<Listener<HyperlinkEvent>>(listener) {
			public void linkActivated(HyperlinkEvent e) {
				data().fire(e);
			}
			public void linkEntered(HyperlinkEvent e) {
			}
			public void linkExited(HyperlinkEvent e) {
			}
		});
		link.setForeground(ColorUtil.getBlue());
		link.setUnderlined(true);
		return link;
	}
	
	public static Composite newComposite(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setBackground(parent.getBackground());
		return panel;
	}
	public static Composite newGridComposite(Composite parent, int marginWidth, int marginHeight, int numColumns) {
		Composite panel = newComposite(parent);
		GridLayout layout = gridLayout(panel, numColumns);
		layout.marginHeight = marginHeight;
		layout.marginWidth = marginWidth;
		return panel;
	}
	public static Composite newGridComposite(Composite parent, int marginWidth, int marginHeight, int numColumns, int horizSpacing, int vertSpacing) {
		Composite panel = newComposite(parent);
		GridLayout layout = gridLayout(panel, numColumns);
		layout.marginHeight = marginHeight;
		layout.marginWidth = marginWidth;
		layout.horizontalSpacing = horizSpacing;
		layout.verticalSpacing = vertSpacing;
		return panel;
	}
	public static Composite newRowComposite(Composite parent, int type, int marginHeight, int marginWidth, int spacing, boolean wrap) {
		Composite panel = newComposite(parent);
		rowLayout(panel, type, marginHeight, marginWidth, spacing, wrap);
		return panel;
	}
	
	public static <T> LabelButton newImageButton(Composite parent, Image image, Listener<T> listener, T data) {
		LabelButton button = new LabelButton(parent);
		button.setImage(image);
		button.setBackground(parent.getBackground());
		button.addClickListener(new ListenerData<MouseEvent,Pair<Listener<T>, T>>(new Pair<Listener<T>, T>(listener, data)) {
			public void fire(MouseEvent event) {
				data().getValue1().fire(data().getValue2());
			}
		});
		return button;
	}
	
	public static <T> Button newImageToggleButton(Composite parent, Image image, Listener<Pair<T,Boolean>> listener, T data) {
		Button button = new Button(parent, SWT.TOGGLE);
		button.setBackground(parent.getBackground());
		button.setImage(image);
		button.addSelectionListener(new SelectionListenerWithData<Pair<Listener<Pair<T,Boolean>>,T>>(new Pair<Listener<Pair<T,Boolean>>,T>(listener, data)) {
			public void widgetSelected(SelectionEvent e) {
				data().getValue1().fire(new Pair<T,Boolean>(data().getValue2(), ((Button)e.widget).getSelection()));
			}
		});
		return button;
	}
	
	public static <T> ImageAndTextButton newImageTextButton(Composite parent, Image image, String text, Listener<T> listener, T data) {
		ImageAndTextButton button = new ImageAndTextButton(parent, image, text);
		button.setBackground(parent.getBackground());
		if (listener != null)
			button.addClickListener(new ListenerData<MouseEvent,Pair<Listener<T>,T>>(new Pair<Listener<T>,T>(listener, data)) {
				public void fire(MouseEvent event) {
					data().getValue1().fire(data().getValue2());
				}
			});
		return button;
	}
	
	public static Font increaseFontSize(Font font, int inc) {
    	FontData data = font.getFontData()[0];
    	return new Font(font.getDevice(), data.getName(), data.getHeight() + inc, data.getStyle());
	}
    public static Font setFontStyle(Font font, int style) {
    	FontData data = font.getFontData()[0];
    	return new Font(font.getDevice(), data.getName(), data.getHeight(), style);
    }
    public static Font copyFont(Font font) {
    	FontData data = font.getFontData()[0];
    	return new Font(font.getDevice(), data.getName(), data.getHeight(), data.getStyle());
    }
    
    public static GridData indentOnGrid(Control c, int indent) {
    	GridData gd = (GridData)c.getLayoutData();
    	if (gd == null) {
    		gd = new GridData();
    		c.setLayoutData(gd);
    	}
    	gd.horizontalIndent = indent;
    	return gd;
    }
    
    public static void execAsync(Control c, Runnable r) {
    	Display d = c.getDisplay();
    	if (d.getThread() == Thread.currentThread())
    		r.run();
    	else
    		d.asyncExec(r);
    }
}
