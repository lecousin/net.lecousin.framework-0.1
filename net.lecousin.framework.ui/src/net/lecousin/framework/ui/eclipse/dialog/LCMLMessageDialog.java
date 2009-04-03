package net.lecousin.framework.ui.eclipse.dialog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.framework.ui.eclipse.SharedImages;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.buttonbar.ButtonsPanel;
import net.lecousin.framework.ui.eclipse.control.buttonbar.CloseButtonPanel;
import net.lecousin.framework.ui.eclipse.control.buttonbar.YesNoButtonsPanel;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class LCMLMessageDialog extends MyDialog {

	public LCMLMessageDialog(Shell parent, String message, Type type) {
		super(parent);
		this.message = message;
		this.type = type;
		setMaxWidth(500);
	}
	
	public enum Type {
		INFORMATION,
		WARNING,
		ERROR,
		QUESTION
	}
	
	public static final int RESULT_QUESTION_NO = 0;
	public static final int RESULT_QUESTION_YES = 1;
	
	private String message;
	private Type type;
	private Map<String,List<Runnable>> linkListeners = new HashMap<String,List<Runnable>>(5);
	private int result = 0;
	
	private LCMLText text;
	
	public void addLinkListener(String href, Runnable listener) {
		List<Runnable> list = linkListeners.get(href);
		if (list == null) {
			list = new LinkedList<Runnable>();
			linkListeners.put(href, list);
		}
		list.add(listener);
	}
	
	@Override
	protected Composite createControl(Composite container) {
		Composite panel = new Composite(container, SWT.NONE);
		UIUtil.gridLayout(panel, 2);
		UIUtil.newImage(panel, getImage());
		text = new LCMLText(panel, false, false);
		text.setText(message);
		for (String href : linkListeners.keySet())
			for (Runnable r : linkListeners.get(href))
				text.addLinkListener(href, r);
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));
		ButtonsPanel buttons = createButtons(panel);
		buttons.centerAndFillInGrid();
		return panel;
	}
	
	private Image getImage() {
		switch (type) {
		default:
		case INFORMATION: return SharedImages.getImage(SharedImages.icons.x48.basic.INFO);
		case ERROR: return SharedImages.getImage(SharedImages.icons.x48.basic.ERROR);
		case WARNING: return SharedImages.getImage(SharedImages.icons.x48.basic.WARNING);
		case QUESTION: return SharedImages.getImage(SharedImages.icons.x48.basic.QUESTION);
		}
	}
	
	private ButtonsPanel createButtons(Composite container) {
		switch (type) {
		default:
		case INFORMATION:
		case ERROR:
		case WARNING:
			return new CloseButtonPanel(container, false);
		case QUESTION:
			return new YesNoButtonsPanel(container, false) {
				@Override
				protected boolean handleYes() {
					result = RESULT_QUESTION_YES;
					return true;
				}
				@Override
				protected boolean handleNo() {
					result = RESULT_QUESTION_NO;
					return true;
				}
			};
		}
	}
	
	public int open(String title) {
		super.open(title, MyDialog.FLAGS_MODAL_DIALOG);
		return result;
	}
}
