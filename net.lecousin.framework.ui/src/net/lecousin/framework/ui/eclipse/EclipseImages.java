package net.lecousin.framework.ui.eclipse;

import java.util.HashMap;
import java.util.Map;

import net.lecousin.framework.ui.eclipse.graphics.GraphicsUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class EclipseImages {

	private static Map<String,Map<String,Image>> images = null;
	private static int total = 0;
	
	public static Image getImage(String pluginID, String path) {
		Map<String,Image> map = null;
		if (images == null)
			images = new HashMap<String,Map<String,Image>>();
		else
			map = images.get(pluginID);
		if (map == null) {
			map = new HashMap<String,Image>();
			images.put(pluginID, map);
		}
		if (map.containsKey(path)) return map.get(path);
		if (++total > 200) {
			images = null;
			total = 0;
			return getImage(pluginID, path);
		}
		ImageDescriptor descr = AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, path);
		Image img = null;
		if (descr != null)
			img = descr.createImage();
		map.put(path, img);
		return img;
	}
	
	public static Image resize(Image image, int width, int height) {
      if (image == null)
         return null;
      Image scaled = new Image(image.getDevice(), width, height);
      GC gc = new GC(scaled);
      gc.setAntialias(SWT.ON);
      gc.setInterpolation(SWT.HIGH);
      gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height);
      gc.dispose();

      return scaled;
   }

	public static Image resizeMax(Image image, int maxWidth, int maxHeight) {
		Point size = GraphicsUtil.getSize(image.getBounds());
		double ratioX = (double)maxWidth / (double)size.x;
		double ratioY = (double)maxHeight / (double)size.y;
		double ratio = Math.min(ratioX, ratioY);
		if (ratio >= 1) return image;
		return resize(image, (int)(size.x*ratio), (int)(size.y*ratio));
	}
}
