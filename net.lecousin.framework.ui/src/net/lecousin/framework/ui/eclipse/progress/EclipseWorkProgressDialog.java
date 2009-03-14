package net.lecousin.framework.ui.eclipse.progress;

import net.lecousin.framework.progress.WorkProgress;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;

public class EclipseWorkProgressDialog extends ProgressMonitorDialog
{
  public EclipseWorkProgressDialog(WorkProgress progress) {
    super(null);
    new EclipseWorkProgressWrapper(getProgressMonitor(), progress);
  }
}
