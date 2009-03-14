package net.lecousin.framework.eclipse.progress;

import net.lecousin.framework.progress.WorkProgress;

import org.eclipse.core.runtime.IProgressMonitor;

public class ProgressMonitor_WorkProgressWrapper implements IProgressMonitor {

	public ProgressMonitor_WorkProgressWrapper(WorkProgress progress) {
		this.progress = progress;
	}
	
	private WorkProgress progress;

	public void beginTask(String name, int totalWork) {
		progress.setDescription(name);
		progress.setAmount(totalWork);
	}

	public void done() {
		progress.done();
	}

	public void internalWorked(double work) {
		// not necessary...
	}

	public boolean isCanceled() {
		return progress.isCancelled();
	}

	public void setCanceled(boolean value) {
		if (value)
			progress.cancel();
	}

	public void setTaskName(String name) {
		progress.setDescription(name);
	}

	public void subTask(String name) {
		progress.setSubDescription(name);
	}

	public void worked(int work) {
		progress.progress(work);
	}

}
