package net.lecousin.framework.files.video.avi;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.video.VideoFile;
import net.lecousin.framework.io.LCPartialBufferedInputStream;

public class AVIFile extends VideoFile {

	public static AVIFile detect(URI uri, LCPartialBufferedInputStream stream) {
		AVIFormat avi = AVIFormat.read(stream);
		if (avi == null) return null;
		return new AVIFile(uri, avi);
	}
	
	private AVIFile(URI uri, AVIFormat info) {
		super(uri, FILE_TYPE, info);
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "avi";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(VideoFile.FILE_TYPE, FILE_TYPE_NAME);
	}
}
