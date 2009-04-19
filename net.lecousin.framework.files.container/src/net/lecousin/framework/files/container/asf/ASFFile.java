package net.lecousin.framework.files.container.asf;

import java.io.InputStream;
import java.net.URI;

import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.TypedFile;

public class ASFFile {

	public static TypedFile detect(URI uri, InputStream stream) {
		ASFFormat asf = ASFFormat.read(stream);
		if (asf == null) return null;
		if (asf.isVideo())
			return new ASFVideoFile(uri, asf);
		if (asf.isAudio())
			return new ASFAudioFile(uri, asf);
		// TODO ImageData
//		if (asf.isImage())
//			return new ASFImageFile(uri, asf);
		return null;
	}
	
	private ASFFile() {}
	
	public static void registerTypes(FileTypeRegistry registry) {
		ASFAudioFile.registerTypes(registry);
		ASFVideoFile.registerTypes(registry);
	}
}
