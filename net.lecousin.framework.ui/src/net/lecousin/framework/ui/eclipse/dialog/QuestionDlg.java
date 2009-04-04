package net.lecousin.framework.ui.eclipse.dialog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.ControlProvider;
import net.lecousin.framework.ui.eclipse.control.Radio;
import net.lecousin.framework.ui.eclipse.control.UIControlUtil;
import net.lecousin.framework.ui.eclipse.control.buttonbar.OkCancelButtonsPanel;
import net.lecousin.framework.ui.eclipse.control.error.ErrorContainerControl;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;
import net.lecousin.framework.ui.eclipse.dialog.QuestionDlg.ContextualOptions.Option;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class QuestionDlg extends MyDialog {

	public static class ContextualOptions {
		public static class Option {
			public Option(String id, String text, boolean selection)
			{ this.id = id; this.text = text; this.selection = selection; }
			public String id;
			public String text;
			public boolean selection;
			public Object data;
		}
		private List<Option> options = new LinkedList<Option>();
		
		public final List<Option> getOptions() { return options; }
		public final void addOption(Option option) { options.add(option); }
		public final void setOptionSelection(String id, boolean selection) {
			for (Option o : options)
				if (o.id.equals(id)) {
					o.selection = selection;
					break;
				}
		}
		public final void setOptionData(String id, Object data) {
			for (Option o : options)
				if (o.id.equals(id)) {
					o.data = data;
					break;
				}
		}
		public final Option getOption(String id) {
			for (Option o : options)
				if (o.id.equals(id))
					return o;
			return null;
		}
	}
	
	public QuestionDlg(Shell parent, String title, ContextualOptions options) {
		super(parent);
		super.create(title, FLAG_BORDER | FLAG_RESIZABLE | FLAG_TITLE);
	}
	
	private ContextualOptions options;
	private LCMLText message;
//	private ScrolledComposite answerScroll;
	private Composite answerPanel;
	private Map<String,Button> optionsButtons = new HashMap<String,Button>();
	private Label buttonsSeparator;
	private Composite buttonsPanel;
	private String selectedAnswer = null;
	
	@Override
	protected Composite createControl(Composite container) {
		ScrolledComposite scroll = new ScrolledComposite(container, SWT.V_SCROLL | SWT.H_SCROLL) {
			@Override
			public Point computeSize(int hint, int hint2, boolean changed) {
				Point size = getContent().getSize();
				Rectangle r = computeTrim(0, 0, size.x, size.y);
				size.x = r.width;
				size.y = r.height;
				if (hint != SWT.DEFAULT) size.x = hint;
				if (hint2 != SWT.DEFAULT) size.y = hint2;
				return size;
			}
		};
		Composite panel = new Composite(scroll, SWT.NONE);
		scroll.setContent(panel);
		UIUtil.gridLayout(panel, 1);
		message = new LCMLText(panel, false, false);
		UIUtil.gridDataHorizFill(message.getControl());
//		answerScroll = new ScrolledComposite(panel, SWT.H_SCROLL | SWT.V_SCROLL) {
//			@Override
//			public Point computeSize(int hint, int hint2, boolean changed) {
//				Point size = getSize();
//				if (hint != SWT.DEFAULT) size.x = hint;
//				if (hint2 != SWT.DEFAULT) size.y = hint2;
//				return size;
//			}
//		};
		answerPanel = new Composite(panel, SWT.NONE);
//		answerScroll.setContent(answerPanel);
		UIUtil.gridDataHorizFill(answerPanel);
		if (options != null && !options.getOptions().isEmpty()) {
			UIUtil.newSeparator(panel, true, true);
			Composite optionsPanel = new Composite(panel, SWT.NONE);
			UIUtil.gridDataHorizFill(optionsPanel);
			UIUtil.gridLayout(optionsPanel, 1);
			for (Option o : options.getOptions()) {
				Button b = new Button(optionsPanel, SWT.CHECK);
				b.setSelection(o.selection);
				b.setData(o.id);
				optionsButtons.put(o.id, b);
				b.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Button b = (Button)e.widget;
						options.setOptionSelection((String)b.getData(), b.getSelection());
					}
				});
			}
		}
		buttonsSeparator = UIUtil.newSeparator(panel, true, true);
		buttonsPanel = new Composite(panel, SWT.NONE);
		UIUtil.gridDataHorizFill(buttonsPanel);
		return panel;
	}
	
	public void setMessage(String text) {
		message.setText(text);
	}
	public void handleHyperlinkMessage(String href, Runnable listener) {
		message.addLinkListener(href, listener);
	}
	
	public enum AnswerType {
		RADIO_BUTTON,
		PUSHABLE_BUTTON
	}
	
	public static abstract class Answer {
		public Answer(String id) {
			this.id = id;
		}
		public String id;
		protected abstract Control[] createControls(Composite parent);
	}
	public static class AnswerSimple extends Answer {
		public AnswerSimple(String id, String message) { 
			super(id);
			this.message = message;
		}
		private String message;
		@Override
		protected Control[] createControls(Composite parent) {
			return new Control[] { UIUtil.newLabel(parent, message) };
		}
	}
	public static class AnswerControl extends Answer {
		public AnswerControl(String id, ControlProvider controlProvider) { 
			super(id);
			this.controlProvider = controlProvider;
		}
		private ControlProvider controlProvider;
		@Override
		protected Control[] createControls(Composite parent) {
			return new Control[] { controlProvider.create(parent) };
		}
	}
	public static class AnswerText extends Answer {
		public AnswerText(String id, String message, String initalValue, boolean align, IInputValidator validator) {
			super(id);
			this.message = message;
			this.text = initalValue;
			this.align = align;
			this.validator = validator;
		}
		private String message;
		public String text;
		private boolean align;
		private IInputValidator validator;
		@Override
		protected Control[] createControls(Composite parent) {
			Control[] controls;
			ErrorContainerControl ctrl;
			if (align) {
				controls = new Control[2];
				controls[0] = UIUtil.newLabel(parent, message);
				ctrl = new ErrorContainerControl(parent);
				controls[1] = ctrl;
			} else {
				controls = new Control[1];
				Composite panel = new Composite(parent, SWT.NONE);
				controls[0] = panel;
				UIUtil.gridLayout(panel, 2);
				UIUtil.newLabel(panel, message);
				ctrl = new ErrorContainerControl(panel);
				ctrl.setLayoutData(UIUtil.gridDataHoriz(1, true));
			}
			Text text = new Text(ctrl, SWT.BORDER);
			UIUtil.gridDataHorizFill(text);
			text.setText(this.text);
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String s = ((Text)e.widget).getText();
					String err = AnswerText.this.validator.isValid(s);
					((ErrorContainerControl)((Text)e.widget).getParent()).setError(err);
					if (err == null)
						AnswerText.this.text = s;
				}
			});
			return controls;
		}
	}
	
	private Answer[] answers;
	
	public void setAnswers(Answer[] answers) {
		this.answers = answers;
	}
	
	private void createAnswers() {
		UIUtil.gridLayout(answerPanel, 1);
		Radio radio = new Radio(answerPanel, false);
		for (Answer answer : answers)
			radio.addOption(answer.id, answer.createControls(radio));
		radio.addSelectionChangedListener(new Listener<String>() {
			public void fire(String event) {
				selectedAnswer = event;
			}
		});
		UIUtil.gridLayout(buttonsPanel, 1);
		OkCancelButtonsPanel buttons = new OkCancelButtonsPanel(buttonsPanel, false) {
			@Override
			protected boolean handleOk() {
				return ok();
			}
			@Override
			protected boolean handleCancel() {
				cancel();
				return true;
			}
		};
		buttons.centerAndFillInGrid();
//		GridData gd = new GridData();
//		gd.horizontalAlignment = SWT.RIGHT;
//		buttons.setLayoutData(gd);
	}
	
	private void cancel() {
		selectedAnswer = null;
	}
	private boolean ok() {
		if (selectedAnswer == null) return false;
		return true;
	}
	
	public void show() {
		createAnswers();
		answerPanel.layout(true, true);
		UIControlUtil.resize(answerPanel);
		answerPanel.getParent().layout(true, true);
		UIControlUtil.resize(answerPanel.getParent());
//		Point size = answerPanel.getSize();
//		Rectangle rect = answerPanel.getDisplay().getBounds();
//		if (size.x > rect.width*3/4)
//			size.x = rect.width*3/4;
//		if (size.y > rect.height*3/4)
//			size.y = rect.height*3/4;
//		rect = answerScroll.computeTrim(0, 0, size.x, size.y);
//		answerScroll.setSize(rect.width, rect.height);
		resize();
		super.open(true);
//		modal();
	}
	public String getAnswerID() { return selectedAnswer; }
	public Answer getAnswer() {
		if (selectedAnswer == null) return null;
		for (Answer a : answers)
			if (a.id.equals(selectedAnswer))
				return a;
		return null;
	}
}
