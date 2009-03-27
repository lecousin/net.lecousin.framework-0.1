package net.lecousin.framework.files;

import java.net.URI;

public abstract class TypedFile {

	public TypedFile(URI uri, FileType type, TypedFileInfo info) {
		this.uri = uri;
		this.type = type;
		this.info = info;
	}
	
	private URI uri;
	private FileType type;
	private TypedFileInfo info;
	
	public URI getURI() { return uri; }
	public FileType getType() { return type; }
	public TypedFileInfo getInfo() { return info; }
	
}
