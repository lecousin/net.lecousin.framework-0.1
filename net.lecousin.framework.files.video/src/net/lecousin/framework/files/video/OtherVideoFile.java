package net.lecousin.framework.files.video;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;

public class OtherVideoFile extends VideoFile {

	public OtherVideoFile(URI uri) {
		super(uri, FILE_TYPE, null);
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "other";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(VideoFile.FILE_TYPE, FILE_TYPE_NAME);
	}
	
}
