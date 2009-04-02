package net.lecousin.framework.files;

import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.progress.WorkProgress;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

public class TypedFolder {

	public TypedFolder(URI rootURI, IFileStore folder, boolean recurse, Iterable<FileType> restriction, WorkProgress progress, int amount) {
		if (progress.isCancelled()) return;
		progress.setSubDescription(URLDecoder.decode(rootURI.relativize(folder.toURI()).toString()));
		this.folder = folder;
		IFileStore[] children;
		try { children = folder.childStores(EFS.NONE, null); }
		catch (CoreException e) {
			if (Log.error(this))
				Log.error(this, "Unable to retrieve content of folder: " + folder.toString(), e);
			progress.progress(amount);
			return;
		}
		ArrayList<IFileStore> folders = new ArrayList<IFileStore>(children.length);
		ArrayList<IFileStore> files = new ArrayList<IFileStore>(children.length);
		for (IFileStore child : children) {
			if (progress.isCancelled()) return;
			IFileInfo info = child.fetchInfo();
			if (info.isDirectory()) {
				if (recurse)
					folders.add(child);
			} else
				files.add(child);
		}
		int nb = files.size() + folders.size()*5;
		for (IFileStore file : files) {
			if (progress.isCancelled()) return;
			int step = amount/nb--;
			amount -= step;
			TypedFile typed;
			try { typed = TypedFileDetector.detect(file, restriction); }
			catch (Throwable t) {
				if (Log.error(this))
					Log.error(this, "An error occured while detecting the type of file: " + file.toString(), t);
				typed = null;
			}
			if (typed != null)
				typedFiles.add(new Pair<IFileStore,TypedFile>(file,typed));
			else
				notTypedFiles.add(file);
			progress.progress(step);
		}
		nb /= 5;
		for (IFileStore f : folders) {
			if (progress.isCancelled()) return;
			int step = amount/nb--;
			amount -= step;
			subFolders.add(new TypedFolder(rootURI, f, recurse, restriction, progress, step));
		}
		progress.progress(amount);
	}
	
	public IFileStore folder;
	public List<Pair<IFileStore,TypedFile>> typedFiles = new LinkedList<Pair<IFileStore,TypedFile>>();
	public List<IFileStore> notTypedFiles = new LinkedList<IFileStore>();
	public List<TypedFolder> subFolders = new LinkedList<TypedFolder>();
	
}
