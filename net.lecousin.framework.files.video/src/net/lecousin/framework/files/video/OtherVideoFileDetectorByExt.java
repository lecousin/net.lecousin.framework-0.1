package net.lecousin.framework.files.video;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeDetector;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.io.LCPartialBufferedInputStream;

public class OtherVideoFileDetectorByExt implements FileTypeDetector {

	public String[] getSpecificURISchemeSupported() {
		return null;
	}
	public boolean isSupportingOnlyGivenURIScheme() {
		return false;
	}

	private static String[] extensions = new String[] {
		"avi", "mpg", "mpeg", "asf", "divx", "dv", "flv", "gfx", "m1v", "m2v", "m2ts", "m4v", "mkv",
		"mov", "mp2", "mp4", "mpeg1", "mpeg2", "mpeg4", "mts", "mxf", "ogm", "ts", "vob", "wmv"
	};
	public String[] getSupportedExtensions() {
		return extensions;
	}
	public boolean relyOnlyOnExtension() {
		return true;
	}
	public FileType[] getSupportedFileTypes() {
		return VideoFile.types;
	}
	
	public TypedFile detect(String scheme, String extension, URI uri, LCPartialBufferedInputStream stream) {
		return new OtherVideoFile(uri);
	}
}
