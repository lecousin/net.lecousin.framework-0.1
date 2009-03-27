package net.lecousin.framework.files.audio;

import net.lecousin.framework.files.FileTypeDetector;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.TypedFileRegister;

public class AudioFileRegister implements TypedFileRegister {

	public AudioFileRegister() {
	}

	public FileTypeDetector[] getDetectors() {
		return new FileTypeDetector[] {
			new AudioFileDetector(),
		};
	}

	public void registerTypes(FileTypeRegistry registry) {
		AudioFile.registerTypes(registry);
	}

}
