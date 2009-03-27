package net.lecousin.framework.media;

import net.lecousin.framework.media.internal.EclipsePlugin;
import net.lecousin.framework.ui.eclipse.EclipseImages;

import org.eclipse.swt.graphics.Image;

public class Icons {

	public static Image getIcon() { return EclipseImages.getImage(EclipsePlugin.ID, "images/icons/icon.gif"); }
	public static Image getIconAdd() { return EclipseImages.getImage(EclipsePlugin.ID, "images/icons/icon_add.gif"); }
	public static Image getIconPlay() { return EclipseImages.getImage(EclipsePlugin.ID, "images/icons/icon_play.gif"); }
	public static Image getIconApplication() { return EclipseImages.getImage(EclipsePlugin.ID, "images/icons/application.gif"); }
	
}
