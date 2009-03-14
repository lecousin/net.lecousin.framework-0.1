package net.lecousin.framework.ui.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.lecousin.framework.log.Log;
import net.lecousin.framework.ui.Images;
import net.lecousin.framework.ui.eclipse.internal.EclipsePlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public abstract class SharedImages implements Images {

    public static ImageDescriptor getImageDescriptor(String path) {
    	if (path == null) return null;
    	ImageDescriptor descr = descriptors.get(path);
    	if (descr == null) {
    		descr = AbstractUIPlugin.imageDescriptorFromPlugin(EclipsePlugin.ID, "library/" + path);
    		if (descr == null) {
    			if (Log.error(SharedImages.class))
    				Log.error(SharedImages.class, "Unable to get image '" + path + "'.");
    			return null;
    		}
    		descriptors.put(path, descr);
    	}
    	return descr;
    }
    
    public static Image getImage(String path) {
        return getImage(getImageDescriptor(path));
    }
    public static Image getImage(ImageDescriptor descr) {
        if (descr == null)
            return null;
        if (!images.containsKey(descr)) {
            Image img = descr.createImage();
            images.put(descr,img);
            return img;
        }
        return images.get(descr);
    }
    
    private static Map<ImageDescriptor, Image> images = new HashMap<ImageDescriptor, Image>(); 
    private static Map<String,ImageDescriptor> descriptors = new HashMap<String, ImageDescriptor>();
    
    public static InputStream getStream(String path) {
    	try {
    		//return EclipsePlugin.getDefault().openStream(new Path(path));
    		return FileLocator.openStream(EclipsePlugin.getDefault().getBundle(), new Path(path), false);
    	} catch (IOException e) {
    		Log.error(SharedImages.class, "Unable to open resource '" + path + "'.", e);
    		return null;
    	}
    }
}
