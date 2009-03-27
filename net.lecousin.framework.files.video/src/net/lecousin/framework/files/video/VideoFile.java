package net.lecousin.framework.files.video;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.files.video.avi.AVIFile;

public abstract class VideoFile extends TypedFile {

	public VideoFile(URI uri, FileType type, VideoFileInfo info) {
		super(uri, type, info);
	}

	@Override
	public VideoFileInfo getInfo() {
		return (VideoFileInfo)super.getInfo();
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "video";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(null, FILE_TYPE_NAME);
		AVIFile.registerTypes(registry);
		OtherVideoFile.registerTypes(registry);
		types = new FileType[] {
			FILE_TYPE,
			AVIFile.FILE_TYPE,
			OtherVideoFile.FILE_TYPE,
		};
	}
	
	static FileType[] types;
}
