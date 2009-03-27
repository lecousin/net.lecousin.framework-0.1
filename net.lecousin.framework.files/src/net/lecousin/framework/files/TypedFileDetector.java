package net.lecousin.framework.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.lecousin.framework.files.internal.TypedFileRegistry;
import net.lecousin.framework.io.LCPartialBufferedInputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

public class TypedFileDetector {

	private TypedFileDetector() {}
	
	public static TypedFile detect(String uri, Iterable<FileType> restriction) throws CoreException, IOException, URISyntaxException {
		return detect(new URI(uri), restriction, null, null);
	}
	public static TypedFile detect(URI uri, Iterable<FileType> restriction) throws CoreException, IOException {
		return detect(uri, restriction, null, null);
	}
	public static TypedFile detect(URL url, Iterable<FileType> restriction) throws CoreException, IOException, URISyntaxException {
		return detect(url.toURI(), restriction, null, null);
	}
	public static TypedFile detect(File file, Iterable<FileType> restriction) throws CoreException, IOException {
		return detect(file.toURI(), restriction, null, null);
	}
	public static TypedFile detect(IFileStore file, Iterable<FileType> restriction) throws CoreException, IOException {
		return detect(file.toURI(), restriction, null, file);
	}
	public static TypedFile detect(URI uri, LCPartialBufferedInputStream stream, Iterable<FileType> restriction) throws CoreException, IOException {
		return detect(uri, restriction, stream, null);
	}
	
	private static TypedFile detect(URI uri, Iterable<FileType> restriction, LCPartialBufferedInputStream stream, IFileStore store) throws CoreException, IOException {
		// retrieve detectors by sorting them: first by scheme, then by extension, and finally add all others
		Set<FileTypeDetector> detectors = new LinkedHashSet<FileTypeDetector>();
		String scheme = uri.getScheme();
		if (scheme != null && scheme.length() > 0) {
			scheme = scheme.toLowerCase();
			List<FileTypeDetector> list = TypedFileRegistry.getByScheme(scheme);
			if (list != null)
				detectors.addAll(list);
		}
		String path = uri.getPath();
		String extension = null;
		if (path != null && path.length() > 0) {
			int i = path.indexOf('.');
			if (i >= 0 && i < path.length()-1) {
				extension = path.substring(i+1).toLowerCase();
				List<FileTypeDetector> list = TypedFileRegistry.getByExtension(extension);
				if (list != null)
					detectors.addAll(list);
			}
		}
		for (FileTypeDetector d : TypedFileRegistry.getAll()) {
			if (detectors.contains(d)) continue;
			if (d.isSupportingOnlyGivenURIScheme() || d.relyOnlyOnExtension()) continue;
			detectors.add(d);
		}

		// filter detectors by type
		if (restriction != null) {
			Set<FileType> types = getEligibleFileTypes(restriction);
			for (Iterator<FileTypeDetector> it = detectors.iterator(); it.hasNext(); ) {
				FileTypeDetector detector = it.next();
				boolean found = false;
				for (FileType type : detector.getSupportedFileTypes())
					if (types.contains(type)) {
						found = true;
						break;
					}
				if (!found)
					it.remove();
			}
		}
		
		// do the detection
		if (detectors.isEmpty()) return null;
		return detect(scheme, extension, uri, detectors, stream, store);
	}
	
	private static Set<FileType> getEligibleFileTypes(Iterable<FileType> restriction) {
		Set<FileType> result = new HashSet<FileType>(50);
		if (restriction == null) {
			FileTypeRegistry.instance().fillAllTypes(result);
			return result;
		}
		for (FileType type : restriction)
			type.fillAllTypes(result);
		return result;
	}

	private static class Provider implements LCPartialBufferedInputStream.StreamProvider {
		Provider(IFileStore store) { this.store = store; }
		IFileStore store;
		public InputStream open() {
			try { return store.openInputStream(EFS.NONE, null); }
			catch (CoreException e) { return null; }
		}
	}
	private static TypedFile detect(String scheme, String extension, URI uri, Iterable<FileTypeDetector> detectors, LCPartialBufferedInputStream stream, IFileStore store) throws CoreException, IOException {
		if (stream == null) {
			if (store == null)
				store = EFS.getStore(uri);
			stream = new LCPartialBufferedInputStream(new Provider(store));
		}
		for (FileTypeDetector detector : detectors) {
			stream.move(0);
			TypedFile file = detector.detect(scheme, extension, uri, stream);
			if (file != null)
				return file;
		}
		return null;
	}
}
