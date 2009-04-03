package net.lecousin.framework.files.audio.wav;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.audio.AudioFile;
import net.lecousin.framework.io.LCMovableInputStream;

public class WAVFile extends AudioFile {

	public static WAVFile detect(URI uri, LCMovableInputStream stream) {
		WAVFormat wav = WAVFormat.read(stream);
		if (wav == null) return null;
		return new WAVFile(uri, wav);
	}
	
	private WAVFile(URI uri, WAVFormat info) {
		super(uri, FILE_TYPE, info);
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "wav";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(AudioFile.FILE_TYPE, FILE_TYPE_NAME);
	}
}
