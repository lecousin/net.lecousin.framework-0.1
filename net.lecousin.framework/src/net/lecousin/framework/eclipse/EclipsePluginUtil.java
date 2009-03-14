package net.lecousin.framework.eclipse;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class EclipsePluginUtil {

	public static InputStream openResource(String pluginID, String path) throws IOException {
		return FileLocator.openStream(Platform.getBundle(pluginID), new Path(path), false);
	}
	
}
