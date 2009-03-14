package net.lecousin.framework.eclipse.resource;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class ResourceUtil {

	public static IFile getWorkspaceFile(IFileStore fileStore) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile[] files = root.findFilesForLocationURI(fileStore.toURI());
		List<IFile> existingFiles = filterNonExistentFiles(files);
		if (existingFiles == null || existingFiles.isEmpty())
			return null;
		// for now only return the first file
		return existingFiles.get(0);
	}
	
	public static List<IFile> filterNonExistentFiles(IFile[] files) {
		if (files == null)
			return null;
		ArrayList<IFile> existentFiles = new ArrayList<IFile>(files.length);
		for (IFile file : files)
			if (file.exists())
				existentFiles.add(file);
		return existentFiles;
	}
	
	public static void createFolderAndParents(IFolder folder) throws CoreException {
		if (folder.exists()) return;
		IContainer parent = folder.getParent();
		if (parent != null && parent instanceof IFolder)
			createFolderAndParents((IFolder)parent);
		folder.create(true, true, null);
	}
}
