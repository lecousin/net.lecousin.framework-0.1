package net.lecousin.framework.files.audio.cda;

import java.io.InputStream;
import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.audio.AudioFile;

public class CDAFile extends AudioFile {

	public static CDAFile detect(URI uri, InputStream stream) {
		CDAFormat cda = CDAFormat.read(stream);
		if (cda == null) return null;
		return new CDAFile(uri, cda);
	}
	
	private CDAFile(URI uri, CDAFormat info) {
		super(uri, FILE_TYPE, info);
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "cda";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(AudioFile.FILE_TYPE, FILE_TYPE_NAME);
	}
}
