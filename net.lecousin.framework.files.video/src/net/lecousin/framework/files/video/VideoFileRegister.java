package net.lecousin.framework.files.video;

import net.lecousin.framework.files.FileTypeDetector;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.TypedFileRegister;

public class VideoFileRegister implements TypedFileRegister {

	public VideoFileRegister() {
	}

	public FileTypeDetector[] getDetectors() {
		return new FileTypeDetector[] {
			new VideoFileDetector(),
		};
	}

	public void registerTypes(FileTypeRegistry registry) {
		VideoFile.registerTypes(registry);
	}

}
