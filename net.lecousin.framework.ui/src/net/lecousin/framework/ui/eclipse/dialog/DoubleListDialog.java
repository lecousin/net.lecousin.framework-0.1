package net.lecousin.framework.ui.eclipse.dialog;

import java.util.List;

import net.lecousin.framework.ui.eclipse.UIUtil;
import net.lecousin.framework.ui.eclipse.control.buttonbar.OkCancelButtonsPanel;
import net.lecousin.framework.ui.eclipse.control.list.DoubleListControl;
import net.lecousin.framework.ui.eclipse.control.list.LCTableWithControls.Provider;
import net.lecousin.framework.ui.eclipse.control.text.lcml.LCMLText;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class DoubleListDialog<T> extends MyDialog {

	/** list1Name and list2Name may be null */
	public DoubleListDialog(Shell parent, Provider<T> provider1, String list1Name, boolean list1Orderable, Provider<T> provider2, String list2Name, boolean list2Orderable) {
		super(parent);
		this.provider1 = provider1;
		this.provider2 = provider2;
		this.list1Name = list1Name;
		this.list2Name = list2Name;
		this.list1Orderable = list1Orderable;
		this.list2Orderable = list2Orderable;
	}
	
	private boolean list1Orderable, list2Orderable;
	private Provider<T> provider1, provider2;
	private String list1Name;
	private String list2Name;
	private String message = null;
	private DoubleListControl<T> control;
	private boolean ok = false;
	private List<T> resultList1, resultList2;
	
	public void setMessage(String message) { this.message = message; }
	
	@Override
	protected Composite createControl(Composite container) {
		Composite panel = new Composite(container, SWT.NONE);
		UIUtil.gridLayout(panel, 1);
		if (message != null) {
			LCMLText text = new LCMLText(panel, false, false);
			UIUtil.gridDataHorizFill(text.getControl());
			text.setText(message);
		}
		
		control = new DoubleListControl<T>(panel, provider1, list1Name, list1Orderable, provider2, list2Name, list2Orderable);
		control.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				resize();
			}
		});
		
		new OkCancelButtonsPanel(panel, true) {
			@Override
			protected boolean handleOk() {
				resultList1 = control.getList1();
				resultList2 = control.getList2();
				ok = true;
				return true;
			}
			@Override
			protected boolean handleCancel() {
				resultList1 = null;
				resultList2 = null;
				return true;
			}
		}.centerAndFillInGrid();

		return panel;
	}
	
	public boolean open(String title) {
		super.open(title, MyDialog.FLAGS_MODAL_DIALOG);
		return ok;
	}
	
	public List<T> getList1() { return resultList1; }
	public List<T> getList2() { return resultList2; }
}
