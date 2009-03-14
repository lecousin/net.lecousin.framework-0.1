package net.lecousin.framework.media.ui;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class MediaPlayerWindow extends ApplicationWindow {

	public MediaPlayerWindow() {
		super(null);
	}
	
	private MediaPlayerControl player = null;
	
	@Override
	protected final Control createContents(Composite container) {
		player = createMediaPlayer(container);
		getShell().setSize(700, 500);
		Rectangle r = getShell().getDisplay().getBounds();
		getShell().setLocation(r.x + r.width/2 - 350, r.y + r.height/2 - 250);
		return player.getControl();
	}
	
	@Override
	protected final void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getTitle());
	}
	
	public MediaPlayerControl getPlayer() { return player; }
	
	protected MediaPlayerControl createMediaPlayer(Composite parent) {
		return new MediaPlayerControl(parent);
	}
	protected String getTitle() { return "Media Player"; }
}
