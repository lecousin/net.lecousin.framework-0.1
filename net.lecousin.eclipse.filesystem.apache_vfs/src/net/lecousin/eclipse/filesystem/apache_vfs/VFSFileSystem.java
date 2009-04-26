package net.lecousin.eclipse.filesystem.apache_vfs;

import java.net.URI;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;

public class VFSFileSystem extends FileSystem {

	public VFSFileSystem() {
	}

	@Override
	public IFileStore getStore(URI uri) {
		try {
			return new VFSFileStore(this, VFS.getManager().resolveFile(uri.toString()));
		} catch (FileSystemException e) {
			return null;
		}
	}

}
