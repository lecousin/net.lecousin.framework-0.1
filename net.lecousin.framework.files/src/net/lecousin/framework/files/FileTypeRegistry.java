package net.lecousin.framework.files;

import java.util.Set;

import net.lecousin.framework.collections.SelfMap;
import net.lecousin.framework.collections.SelfMapLinkedList;

public class FileTypeRegistry {

	private FileTypeRegistry() {}
	private static FileTypeRegistry instance = new FileTypeRegistry();
	public static FileTypeRegistry instance() { return instance; }
	
	private SelfMap<String,FileType> roots = new SelfMapLinkedList<String,FileType>(10);

	public FileType register(FileType parent, String name) {
		if (parent == null) {
			FileType type = roots.get(name);
			if (type != null) return type;
			type = new FileType(null, name);
			roots.put(type);
			return type;
		}
		if (parent.subTypes != null) {
			FileType type = parent.subTypes.get(name);
			if (type != null) return type;
		} else
			parent.subTypes = new SelfMapLinkedList<String,FileType>(5);
		FileType type = new FileType(parent, name);
		parent.subTypes.put(type);
		return type;
	}
	
	public FileType getType(String path) {
		int i = path.indexOf('/');
		if (i < 0)
			return getType(path, roots);
		FileType type = getType(path.substring(0, i), roots);
		if (type == null) return null;
		return getType(type, path.substring(i+1));
	}
	
	private FileType getType(String name, SelfMap<String,FileType> children) {
		if (children == null) return null;
		return children.get(name);
	}
	
	private FileType getType(FileType parent, String subpath) {
		int i = subpath.indexOf('/');
		if (i < 0)
			return getType(subpath, parent.subTypes);
		FileType type = getType(subpath.substring(0, i), parent.subTypes);
		if (type == null) return null;
		return getType(type, subpath.substring(i+1));
	}
	
	void fillAllTypes(Set<FileType> result) {
		for (FileType type : roots)
			type.fillAllTypes(result);
	}
}
