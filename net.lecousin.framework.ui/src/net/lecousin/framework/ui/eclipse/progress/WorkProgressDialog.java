package net.lecousin.framework.ui.eclipse.progress;

import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.ui.eclipse.Local;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class WorkProgressDialog extends MyDialog {

	public WorkProgressDialog(Shell parent, WorkProgress progress) {
		super(parent);
		wp = new EmbeddedWorkProgressControl(progress);
		open(Local.Operation_in_progress.toString(), MyDialog.FLAG_BORDER | MyDialog.FLAG_TITLE);
	}
	
	private EmbeddedWorkProgressControl wp;

	private class DialogResizer implements EmbeddedWorkProgressControl.Resizer {
		public void resize() {
			WorkProgressDialog.this.resize();
		}
	}
	
	@Override
	protected Composite createControl(Composite container) {
		return wp.create(container, new DialogResizer());
	}
	
	public Composite getCustomizePanel() { return wp.getCustomizePanel(); }
	public void ensureCustomPanelVisibleIfNeeded() { wp.ensureCustomPanelVisibleIfNeeded(); }
	
	public void forceRefresh() { wp.forceRefresh(); }
}
