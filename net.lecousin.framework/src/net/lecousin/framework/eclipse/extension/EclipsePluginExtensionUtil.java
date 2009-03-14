package net.lecousin.framework.eclipse.extension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.collections.CollectionUtil;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

public class EclipsePluginExtensionUtil {

	private EclipsePluginExtensionUtil() { /* instantiation not allowed */ }
	
	public static IExtension[] getExtensions(String extensionID) {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(extensionID);
        if (point == null) return new IExtension[] {};
		return point.getExtensions();
	}
	
	public static IExtension[] getExtensions(String pluginID, String extensionID) {
		return getExtensions(pluginID + "." + extensionID);
	}
	
	public static Collection<IConfigurationElement> getExtensionsSubNode(String extensionID, String subNodeName) {
		return getExtensionsSubNode(getExtensions(extensionID), subNodeName);
	}

	public static Collection<IConfigurationElement> getExtensionsSubNode(String pluginID, String extensionID, String subNodeName) {
		return getExtensionsSubNode(getExtensions(pluginID, extensionID), subNodeName);
	}

	public static Collection<IConfigurationElement> getExtensionsSubNode(IExtension[] extensions, String subNodeName) {
		Collection<IConfigurationElement> result = new LinkedList<IConfigurationElement>();
		for (int i = 0; i < extensions.length; ++i) {
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; ++j) {
				if (!elements[j].getName().equals(subNodeName)) continue;
				result.add(elements[j]);
			}
		}
		return result;
	}
	
	public static Collection<IConfigurationElement> filterByElementName(Collection<IConfigurationElement> elements, String name) {
		Collection<IConfigurationElement> result = new LinkedList<IConfigurationElement>();
		for (Iterator<IConfigurationElement> it = elements.iterator(); it.hasNext(); ) {
			IConfigurationElement e = it.next();
			if (e.getName().equals(name))
				result.add(e);
		}
		return result;
	}
	
	public static List<IConfigurationElement> filterByAttribute(Collection<IConfigurationElement> elements, String attributeName, String attributeValue) {
		List<IConfigurationElement> result = new LinkedList<IConfigurationElement>();
		for (Iterator<IConfigurationElement> it = elements.iterator(); it.hasNext(); ) {
			IConfigurationElement e = it.next();
			String value = e.getAttribute(attributeName);
			if (value == null) continue;
			if (attributeValue.equals(value))
				result.add(e);
		}
		return result;
	}
	
	public static IConfigurationElement getExtension(String pluginID, String extensionID, String subNodeName, String attrName, String attrValue) {
		List<IConfigurationElement> col = filterByAttribute(getExtensionsSubNode(pluginID, extensionID, subNodeName), attrName, attrValue);
		if (col.isEmpty()) return null;
		return col.get(0);
	}

    public static Class<?> getClass(IConfigurationElement cfg, String className) 
    throws ClassNotFoundException {
        String ns = cfg.getDeclaringExtension().getContributor().getName();
        //String ns = cfg.getDeclaringExtension().getNamespace();
        return Platform.getBundle(ns).loadClass(className);
    }
	
    @SuppressWarnings("unchecked")
	public static <T> Class<T> getClass(IConfigurationElement cfg, String className, Class<T> clazzNeeded) 
    throws ClassNotFoundException {
        String ns = cfg.getDeclaringExtension().getContributor().getName();
        //String ns = cfg.getDeclaringExtension().getNamespace();
        Class<?> clazz = Platform.getBundle(ns).loadClass(className);
        if (!clazzNeeded.isAssignableFrom(clazz))
        	throw new ClassCastException("Must be of type '" + clazzNeeded.getName() + "': " + clazz.getName());
        return (Class<T>)clazz;
    }
	
    @SuppressWarnings("unchecked")
	public static <T> T getInstance(Class<T> clazz, Object[][] possibleConstructors) 
    throws InvocationTargetException, IllegalAccessException, InstantiationException {
    	Constructor[] constructors = clazz.getConstructors();
    	for (int i = 0; i < possibleConstructors.length; ++i) {
    		Object[] pc = possibleConstructors[i];
    		for (int j = 0; j < constructors.length; ++j) {
    			Class[] args = constructors[j].getParameterTypes();
    			if (args.length != pc.length) continue;
    			boolean ok = true;
    			for (int k = 0; ok && k < args.length; ++k)
    				if (!args[k].isAssignableFrom(pc[k].getClass()))
    					ok = false;
    			if (!ok) continue;
    			return (T)constructors[j].newInstance(pc);
    		}
    	}
    	return null;
    }
    
    public static <T> T createInstance(Class<T> clazz, IConfigurationElement cfg, String classnameAttribute, Object[][] possibleConstructors) 
    throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
    	Class<T> cl = getClass(cfg, cfg.getAttribute(classnameAttribute), clazz);
    	return getInstance(cl, possibleConstructors);
    }
    
    public static Collection<IConfigurationElement> getAllSubNodes(Collection<IConfigurationElement> parents, String elementName) {
    	Collection<IConfigurationElement> result = new LinkedList<IConfigurationElement>();
    	for (Iterator<IConfigurationElement> it = parents.iterator(); it.hasNext(); ) {
    		IConfigurationElement parent = it.next();
    		result.addAll(getAllSubNodes(parent, elementName));
    	}
    	return result;
    }
    
    public static Collection<IConfigurationElement> getAllSubNodes(IConfigurationElement parent, String elementName) {
    	Collection<IConfigurationElement> result = new LinkedList<IConfigurationElement>();
    	CollectionUtil.addAll(result, parent.getChildren(elementName));
    	return result;
    } 
    
    public static <T> T createInstanceFromNode(String pluginID, String extensionID, String node_name, String attr_name, String attr_value, String classname_attr, Class<T> clazz, Object[][] possibleConstructors)
    throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
    	for (IConfigurationElement ext : getExtensionsSubNode(pluginID, extensionID, node_name)) {
    		if (ext.getAttribute(attr_name).equals(attr_value)) {
    			return createInstance(clazz, ext, classname_attr, possibleConstructors);
    		}
    	}
    	return null;
    }
    
	public static <T> T createInstanceFromDeclaringPlugin(Class<T> clazz, String pluginID, String extensionID, String nodeName, String classAttributeName, Object[][] possibleConstructors)
    throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
    	for (IConfigurationElement ext : getExtensionsSubNode(pluginID, extensionID, nodeName)) {
   			return createInstance(clazz, ext, classAttributeName, possibleConstructors);
    	}
		return null;
	}

}
