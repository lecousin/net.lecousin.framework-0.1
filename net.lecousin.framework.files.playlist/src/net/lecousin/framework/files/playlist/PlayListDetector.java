package net.lecousin.framework.files.playlist;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeDetector;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.files.playlist.m3u.M3UPlayList;
import net.lecousin.framework.io.LCPartialBufferedInputStream;

public class PlayListDetector implements FileTypeDetector {

	public String[] getSpecificURISchemeSupported() {
		return null;
	}
	public boolean isSupportingOnlyGivenURIScheme() {
		return false;
	}

	private static final String[] extensions = new String[] {
		"m3u"
	};
	public String[] getSupportedExtensions() {
		return extensions;
	}
	public boolean relyOnlyOnExtension() {
		return true;
	}
	public FileType[] getSupportedFileTypes() {
		return PlayList.types;
	}
	
	public TypedFile detect(String scheme, String extension, URI uri, LCPartialBufferedInputStream stream) {
		if (extension.equals("m3u"))
			return M3UPlayList.load(uri, stream);
		return null;
	}
}
