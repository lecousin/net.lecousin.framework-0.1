package net.lecousin.framework.files.container;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeDetector;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.files.container.asf.ASFAudioFile;
import net.lecousin.framework.files.container.asf.ASFFile;
import net.lecousin.framework.files.container.asf.ASFVideoFile;
import net.lecousin.framework.files.image.ImageFile;
import net.lecousin.framework.io.LCPartialBufferedInputStream;

public class ContainerFileDetector implements FileTypeDetector {

	public String[] getSpecificURISchemeSupported() {
		return null;
	}
	public boolean isSupportingOnlyGivenURIScheme() {
		return false;
	}

	private static final String[] extensions = new String[] {
		"asf", "wma", "wmv"
	};
	public String[] getSupportedExtensions() {
		return extensions;
	}
	public boolean relyOnlyOnExtension() {
		return false;
	}
	public FileType[] getSupportedFileTypes() {
		return new FileType[] {
			ASFAudioFile.FILE_TYPE,
			ASFVideoFile.FILE_TYPE,
			ImageFile.FILE_TYPE,
		};
	}
	
	public TypedFile detect(String scheme, String extension, URI uri, LCPartialBufferedInputStream stream) {
		boolean asf = false;
		if (extension.equals("wma") || extension.equals("wmv") || extension.equals("asf")) {
			TypedFile file = ASFFile.detect(uri, stream);
			if (file != null) return file;
			stream.move(0);
			asf = true;
		}
		if (!asf) {
			TypedFile file = ASFFile.detect(uri, stream);
			if (file != null) return file;
			stream.move(0);
		}
		return null;
	}
}
