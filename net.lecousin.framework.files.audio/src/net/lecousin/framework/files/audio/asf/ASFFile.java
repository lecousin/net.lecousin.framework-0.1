package net.lecousin.framework.files.audio.asf;

import java.io.InputStream;
import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.audio.AudioFile;

public class ASFFile extends AudioFile {

	public static ASFFile detect(URI uri, InputStream stream) {
		ASFFormat asf = ASFFormat.read(stream);
		if (asf == null) return null;
		return new ASFFile(uri, asf);
	}
	
	private ASFFile(URI uri, ASFFormat info) {
		super(uri, FILE_TYPE, info);
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "asf";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(AudioFile.FILE_TYPE, FILE_TYPE_NAME);
	}
}
