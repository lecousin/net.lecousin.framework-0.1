package net.lecousin.framework.files.audio;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.files.audio.asf.ASFFile;
import net.lecousin.framework.files.audio.cda.CDAFile;
import net.lecousin.framework.files.audio.mp3.MP3File;
import net.lecousin.framework.files.audio.wav.WAVFile;

public abstract class AudioFile extends TypedFile {

	public AudioFile(URI uri, FileType type, AudioFileInfo info) {
		super(uri, type, info);
	}

	@Override
	public AudioFileInfo getInfo() {
		return (AudioFileInfo)super.getInfo();
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "audio";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(null, FILE_TYPE_NAME);
		ASFFile.registerTypes(registry);
		CDAFile.registerTypes(registry);
		MP3File.registerTypes(registry);
		WAVFile.registerTypes(registry);
		types = new FileType[] {
			FILE_TYPE,
			ASFFile.FILE_TYPE,
			CDAFile.FILE_TYPE,
			MP3File.FILE_TYPE,
			WAVFile.FILE_TYPE,
		};
	}
	
	static FileType[] types;
}
