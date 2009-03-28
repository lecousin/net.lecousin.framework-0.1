package net.lecousin.framework.files.image;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeDetector;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.io.LCPartialBufferedInputStream;

public class ImageFileDetector implements FileTypeDetector {

	public String[] getSpecificURISchemeSupported() {
		return null;
	}
	public boolean isSupportingOnlyGivenURIScheme() {
		return false;
	}

	private static final String[] extensions = new String[] {
		"jpeg", "jpg", "gif", "png", "bmp", "ico", "tif", "tiff"
	};
	public String[] getSupportedExtensions() {
		return extensions;
	}
	public boolean relyOnlyOnExtension() {
		return false;
	}
	public FileType[] getSupportedFileTypes() {
		return ImageFile.types;
	}
	
	public TypedFile detect(String scheme, String extension, URI uri, LCPartialBufferedInputStream stream) {
		return ImageFile.detect(uri, stream);
	}
}
