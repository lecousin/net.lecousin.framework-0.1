package net.lecousin.framework.files;

import java.util.Set;

import net.lecousin.framework.collections.SelfMap;

public class FileType implements SelfMap.Entry<String> {

	FileType(FileType parent, String name) {
		this.parent = parent;
		this.name = name;
	}
	
	private FileType parent;
	private String name;
	SelfMap<String,FileType> subTypes = null;
	
	public FileType getParent() { return parent; }
	public String getName() { return name; }
	public String getFullName() {
		return parent == null ? name : parent.getFullName()+'/'+name;
	}
	
	public String getHashObject() { return name; }
	
	void fillAllTypes(Set<FileType> result) {
		if (!result.add(this)) return;
		if (subTypes == null) return;
		for (FileType type : subTypes)
			type.fillAllTypes(result);
	}
}
