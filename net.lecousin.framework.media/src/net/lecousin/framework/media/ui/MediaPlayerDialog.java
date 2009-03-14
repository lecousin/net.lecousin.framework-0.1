package net.lecousin.framework.media.ui;

import java.net.URI;

import net.lecousin.framework.media.Media;
import net.lecousin.framework.ui.eclipse.dialog.MyDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class MediaPlayerDialog extends MyDialog {

	public MediaPlayerDialog() {
		super(new Shell(PlatformUI.getWorkbench().getDisplay(), SWT.APPLICATION_MODAL));
	}
	
	@Override
	protected Composite createControl(Composite container) {
		MediaPlayerControl control = new MediaPlayerControl(container);
		return control;
	}
	
	public void open() {
		super.open("Media Player", FLAG_BORDER | FLAG_CLOSABLE | FLAG_RESIZABLE | FLAG_TITLE);
	}
	
	public MediaPlayerControl getControl() { return (MediaPlayerControl)getDialogPanel(); }
	
	public void add(URI uri) {
		getControl().add(uri);
	}
	public void start() {
		Media media = getControl().start();
//		PointInt size = media.getSize();
//		Point pt = getControl().getSizeForVisualSize(size.x, size.y);
//		Rectangle rect = getShell().computeTrim(0, 0, pt.x, pt.y);
//        getShell().setSize(rect.width, rect.height);
		getShell().setSize(700, 500);
	}
}
