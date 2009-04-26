package net.lecousin.eclipse.filesystem.apache_vfs;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import net.lecousin.eclipse.filesystem.apache_vfs.internal.EclipsePlugin;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class VFSFileStore implements IFileStore {

	VFSFileStore(VFSFileSystem fs, FileObject file) {
		this.fs = fs;
		this.file = file;
	}
	
	private VFSFileSystem fs;
	private FileObject file;
	
	private static IStatus getStatus(FileSystemException e) {
		return new Status(IStatus.ERROR, EclipsePlugin.ID, "File system error", e);
	}
	
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {
		try {
			FileObject[] children = file.getChildren();
			IFileInfo[] result = new IFileInfo[children.length];
			for (int i = 0; i < children.length; ++i)
				result[i] = new VFSFileInfo(children[i]);
			return result;
		} catch (FileSystemException e) {
			throw new CoreException(getStatus(e));
		}
	}

	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		try {
			FileObject[] children = file.getChildren();
			String[] result = new String[children.length];
			for (int i = 0; i < children.length; ++i)
				result[i] = children[i].getName().getBaseName();
			return result;
		} catch (FileSystemException e) {
			throw new CoreException(getStatus(e));
		}
	}

	public IFileStore[] childStores(int options, IProgressMonitor monitor) throws CoreException {
		try {
			FileObject[] children = file.getChildren();
			IFileStore[] result = new IFileStore[children.length];
			for (int i = 0; i < children.length; ++i)
				result[i] = new VFSFileStore(fs, children[i]);
			return result;
		} catch (FileSystemException e) {
			throw new CoreException(getStatus(e));
		}
	}

	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		try { file.delete(); }
		catch (FileSystemException e) { throw new CoreException(getStatus(e)); }
	}

	public IFileInfo fetchInfo() { return new VFSFileInfo(file); }

	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		return fetchInfo();
	}

	public IFileStore getChild(IPath path) {
		try { return new VFSFileStore(fs, file.resolveFile(path.toString())); }
		catch (FileSystemException e) { return null; }
	}

	public IFileStore getChild(String name) {
		try { return new VFSFileStore(fs, file.resolveFile(name)); }
		catch (FileSystemException e) { return null; }
	}

	public IFileStore getFileStore(IPath path) { return getChild(path); }

	public IFileSystem getFileSystem() { return fs; }

	public String getName() { return file.getName().getBaseName(); }

	public IFileStore getParent() {
		try { return new VFSFileStore(fs, file.getParent()); }
		catch (FileSystemException e) { return null; }
	}

	public boolean isParentOf(IFileStore other) {
		if (!(other instanceof VFSFileStore)) return false;
		FileObject f = ((VFSFileStore)other).file;
		try {
			FileObject parent = f.getParent();
			if (parent != null && parent.getName().getURI().equals(file.getName().getURI())) return true;
		} catch (FileSystemException e) {
		}
		return false;
	}

	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		try { file.createFolder(); return this; }
		catch (FileSystemException e) { throw new CoreException(getStatus(e)); }
	}


	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		try { return file.getContent().getInputStream(); }
		catch (FileSystemException e) { throw new CoreException(getStatus(e)); }
	}

	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		try { return file.getContent().getOutputStream(); }
		catch (FileSystemException e) { throw new CoreException(getStatus(e)); }
	}

	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
	}

	public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	public URI toURI() {
		try { return new URI(file.getName().getURI()); }
		catch (URISyntaxException e) { return null; }
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}


	public void copy(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
	}
	public void move(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
	}
}
