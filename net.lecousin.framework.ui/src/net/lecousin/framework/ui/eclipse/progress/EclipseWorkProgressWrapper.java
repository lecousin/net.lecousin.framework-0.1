package net.lecousin.framework.ui.eclipse.progress;

import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.progress.WorkProgress;

import org.eclipse.core.runtime.IProgressMonitor;

public class EclipseWorkProgressWrapper implements Listener<WorkProgress>
{

  public EclipseWorkProgressWrapper(IProgressMonitor eclipseProgress, WorkProgress progress) {
	  this.eclipseProgress = eclipseProgress;
	  this.progress = progress;
	  eclipseProgress.beginTask(progress.getDescription(), progress.getAmount());
	  progress.addProgressListener(this);
  }
  
  private IProgressMonitor eclipseProgress;
  private WorkProgress progress;
  private int given = 0;
 
  public void fire(WorkProgress event) {
	  int pos = progress.getPosition();
	  if (pos > given) {
		  eclipseProgress.worked(pos - given);
		  given = pos;
	  }
  }
}
