package net.lecousin.framework.files.container.asf;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.video.VideoFile;

public class ASFVideoFile extends VideoFile {

	ASFVideoFile(URI uri, ASFFormat info) {
		super(uri, FILE_TYPE, info);
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "asf";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(VideoFile.FILE_TYPE, FILE_TYPE_NAME);
	}
}
