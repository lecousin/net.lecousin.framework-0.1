package net.lecousin.framework.files.audio.mp3;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.audio.AudioFile;
import net.lecousin.framework.io.LCBufferedInputStream;

public class MP3File extends AudioFile {

	public static MP3File detect(URI uri, InputStream stream) {
		byte[] buf = new byte[2];
		try {
			if (stream.read(buf) != 2) return null;
		} catch (IOException e) { return null; }
		LCBufferedInputStream in = new LCBufferedInputStream(buf, 0, 2, stream);
		ID3Format id3 = null;
		try { 
			id3 = ID3Format.load(in); 
		} catch (IOException e) {}
		if (id3 == null)
			if ((buf[0] & 0xFF) != 0xFF || (buf[1] & 0xFF) != 0xFB) 
				return null;
		return new MP3File(uri, id3);
	}
	
	private MP3File(URI uri, ID3Format id3) {
		super(uri, FILE_TYPE, id3);
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "mp3";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(AudioFile.FILE_TYPE, FILE_TYPE_NAME);
	}
}
