package net.lecousin.framework.files.container.asf;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.audio.AudioFile;

public class ASFAudioFile extends AudioFile {

	ASFAudioFile(URI uri, ASFFormat info) {
		super(uri, FILE_TYPE, info);
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "asf";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(AudioFile.FILE_TYPE, FILE_TYPE_NAME);
	}
}
