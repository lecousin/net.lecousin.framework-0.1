package net.lecousin.framework.files.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.framework.eclipse.extension.EclipsePluginExtensionUtil;
import net.lecousin.framework.files.FileTypeDetector;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.TypedFileRegister;
import net.lecousin.framework.log.Log;

import org.eclipse.core.runtime.IConfigurationElement;

public class TypedFileRegistry {

	private TypedFileRegistry() {}

	private static Map<String,List<FileTypeDetector>> byScheme = new HashMap<String,List<FileTypeDetector>>(10);
	private static Map<String,List<FileTypeDetector>> byExtension = new HashMap<String,List<FileTypeDetector>>(50);
	private static List<FileTypeDetector> all = new ArrayList<FileTypeDetector>(100);
	
	public static List<FileTypeDetector> getByScheme(String scheme) { return byScheme.get(scheme); }
	public static List<FileTypeDetector> getByExtension(String ext) { return byExtension.get(ext); }
	public static List<FileTypeDetector> getAll() { return all; }
	
	static void init() {
		for (IConfigurationElement ext : EclipsePluginExtensionUtil.getExtensionsSubNode(EclipsePlugin.ID, "register", "register")) {
			try {
				TypedFileRegister register = EclipsePluginExtensionUtil.createInstance(TypedFileRegister.class, ext, "class", new Object[][]{ new Object[]{} });
				register.registerTypes(FileTypeRegistry.instance());
				FileTypeDetector[] detectors = register.getDetectors();
				for (FileTypeDetector d : detectors) {
					String[] schemes = d.getSpecificURISchemeSupported();
					if (schemes != null)
						for (String scheme : schemes) {
							scheme = scheme.toLowerCase();
							List<FileTypeDetector> list = byScheme.get(scheme);
							if (list == null) {
								list = new LinkedList<FileTypeDetector>();
								byScheme.put(scheme, list);
							}
							list.add(d);
						}
					if (d.isSupportingOnlyGivenURIScheme()) continue;
					String[] extensions = d.getSupportedExtensions();
					if (extensions != null)
						for (String e : extensions) {
							e = e.toLowerCase();
							List<FileTypeDetector> list = byExtension.get(e);
							if (list == null) {
								list = new LinkedList<FileTypeDetector>();
								byExtension.put(e, list);
							}
							list.add(d);
						}
					all.add(d);
				}
			} catch (ClassNotFoundException e) {
				if (Log.error(TypedFileRegistry.class))
					Log.error(TypedFileRegistry.class, "Unable to register file types from plugin " + ext.getDeclaringExtension().getContributor().getName() + ": class not found. Please check your plug-in configuration.", e);
			} catch (InstantiationException e) {
				if (Log.error(TypedFileRegistry.class))
					Log.error(TypedFileRegistry.class, "Unable to register file types from plugin " + ext.getDeclaringExtension().getContributor().getName() + ": class not instantiable. Please check your plug-in configuration or that your class is neither an interface nor an abstract class.", e);
			} catch (IllegalAccessException e) {
				if (Log.error(TypedFileRegistry.class))
					Log.error(TypedFileRegistry.class, "Unable to register file types from plugin " + ext.getDeclaringExtension().getContributor().getName() + ": class not accessible. Please check your plug-in configuration or that your class is accessible (public and published by the plugin).", e);
			} catch (InvocationTargetException e) {
				if (Log.error(TypedFileRegistry.class))
					Log.error(TypedFileRegistry.class, "Unable to register file types from plugin " + ext.getDeclaringExtension().getContributor().getName() + ": constructor threw an exception.", e.getTargetException());
			}
		}
	}
	
}
