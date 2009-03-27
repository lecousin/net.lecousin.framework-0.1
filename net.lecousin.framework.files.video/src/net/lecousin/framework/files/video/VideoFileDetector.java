package net.lecousin.framework.files.video;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeDetector;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.files.video.avi.AVIFile;
import net.lecousin.framework.io.LCPartialBufferedInputStream;

public class VideoFileDetector implements FileTypeDetector {

	public String[] getSpecificURISchemeSupported() {
		return null;
	}
	public boolean isSupportingOnlyGivenURIScheme() {
		return false;
	}

	private static final String[] extensions = new String[] {
		"avi",
	};
	public String[] getSupportedExtensions() {
		return extensions;
	}
	public boolean relyOnlyOnExtension() {
		return false;
	}
	public FileType[] getSupportedFileTypes() {
		return VideoFile.types;
	}
	
	public TypedFile detect(String scheme, String extension, URI uri, LCPartialBufferedInputStream stream) {
		boolean avi = false;
		if (extension.equals("avi")) {
			TypedFile file = AVIFile.detect(uri, stream);
			if (file != null) return file;
			stream.move(0);
			avi = true;
		}
		
		if (!avi) {
			TypedFile file = AVIFile.detect(uri, stream);
			if (file != null) return file;
			stream.move(0);
		}

		return null;
	}
}
