package net.lecousin.eclipse.filesystem.apache_vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;

public class VFSFileInfo implements IFileInfo {

	VFSFileInfo(FileObject file) {
		this.file = file;
	}
	
	private FileObject file;

	public boolean exists() { try { return file.exists(); } catch (FileSystemException e) { return false; } }

	public boolean getAttribute(int attribute) {
		return false;
	}

	public long getLastModified() {
		try { return file.getContent().getLastModifiedTime(); }
		catch (FileSystemException e) { return EFS.NONE; }
	}

	public long getLength() {
		try { return file.getContent().getSize(); }
		catch (FileSystemException e) { return EFS.NONE; }
	}

	public String getName() {
		return file.getName().getBaseName();
	}

	public String getStringAttribute(int attribute) {
		return null;
	}

	public boolean isDirectory() {
		try { return file.getType().equals(FileType.FOLDER); }
		catch (FileSystemException e) { return false; }
	}

	public void setAttribute(int attribute, boolean value) {
	}

	public void setLastModified(long time) {
		try { file.getContent().setLastModifiedTime(time); }
		catch (FileSystemException e) {  }
	}
	
	public int compareTo(Object o) {
		if (o == null) return -1;
		if (!(o instanceof VFSFileInfo)) return -1;
		return file.getName().getBaseName().compareTo(((VFSFileInfo)o).file.getName().getBaseName());
	}
}
