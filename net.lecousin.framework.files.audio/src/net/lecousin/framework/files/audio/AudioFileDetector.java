package net.lecousin.framework.files.audio;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeDetector;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.files.audio.cda.CDAFile;
import net.lecousin.framework.files.audio.mp3.MP3File;
import net.lecousin.framework.files.audio.wav.WAVFile;
import net.lecousin.framework.io.LCPartialBufferedInputStream;

public class AudioFileDetector implements FileTypeDetector {

	public String[] getSpecificURISchemeSupported() {
		return null;
	}
	public boolean isSupportingOnlyGivenURIScheme() {
		return false;
	}

	private static final String[] extensions = new String[] {
		"mp3", "mpeg3", "mpg3", "cda", "wav", "wave"
	};
	public String[] getSupportedExtensions() {
		return extensions;
	}
	public boolean relyOnlyOnExtension() {
		return false;
	}
	public FileType[] getSupportedFileTypes() {
		return AudioFile.types;
	}
	
	public TypedFile detect(String scheme, String extension, URI uri, LCPartialBufferedInputStream stream) {
		boolean cda = false;
		boolean mp3 = false;
		boolean wav = false;
		if (extension.equals("mp3") || extension.equals("mpeg3") || extension.equals("mpg3")) {
			TypedFile file = MP3File.detect(uri, stream);
			if (file != null) return file;
			stream.move(0);
			mp3 = true;
		} else if (extension.equals("wav") || extension.equals("wave")) {
			TypedFile file = WAVFile.detect(uri, stream);
			if (file != null) return file;
			stream.move(0);
			wav = true;
		} else if (extension.equals("cda")) {
			TypedFile file = CDAFile.detect(uri, stream);
			if (file != null) return file;
			stream.move(0);
			cda = true;
		}
		if (!cda) {
			TypedFile file = CDAFile.detect(uri, stream);
			if (file != null) return file;
			stream.move(0);
		}
		if (!mp3) {
			TypedFile file = MP3File.detect(uri, stream);
			if (file != null) return file;
			stream.move(0);
		}
		if (!wav) {
			TypedFile file = WAVFile.detect(uri, stream);
			if (file != null) return file;
			stream.move(0);
		}
		return null;
	}
}
