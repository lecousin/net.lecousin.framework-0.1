package net.lecousin.framework.ui.eclipse.dialog;

import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.buttonbar.CloseButtonPanel;
import net.lecousin.framework.ui.eclipse.control.list.LCTable;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class LCTableDialog<T> extends MyDialog {

	public LCTableDialog(Shell shell, String title, String message, LCTable.LCTableProvider<T> provider, int maxWidth) {
		super(shell);
		this.title = title;
		this.message = message;
		setMaxWidth(maxWidth);
	}
	
	private String title;
	private String message;
	private LCTable.LCTableProvider<T> provider;
	
	@Override
	protected Composite createControl(Composite container) {
		Composite panel = UIUtil.newGridComposite(container, 2, 2, 1);
		
		LCMLText text = new LCMLText(panel, false, false);
		text.setLayoutData(UIUtil.gridDataHoriz(1, true));
		text.setText(message);
		
		LCTable<T> table = new LCTable<T>(panel, provider);
		UIUtil.gridDataHorizFill(table.getControl());
		
		new CloseButtonPanel(panel, true) {
			@Override
			protected void handleClose() {
			}
		}.centerAndFillInGrid();

		return panel;
	}
	
	public void open() {
		super.open(title, MyDialog.FLAGS_MODAL_DIALOG);
	}
}
