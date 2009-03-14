package net.lecousin.framework.ui.eclipse.event;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.thread.RunnableWithData;

import org.eclipse.ui.PlatformUI;

public class UIEvent<T> extends Event<T> {

	@Override
	public void fire(T event) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new RunnableWithData<T>(event) {
			public void run() {
				fireUI(data());
			}
		});
	}
	
	private void fireUI(T event) {
		super.fire(event);
	}
	
}
