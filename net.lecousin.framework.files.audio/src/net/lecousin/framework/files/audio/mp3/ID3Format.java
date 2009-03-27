package net.lecousin.framework.files.audio.mp3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.lecousin.framework.files.audio.AudioFileInfo;

public abstract class ID3Format implements AudioFileInfo {

	public static ID3Format load(File file) throws FileNotFoundException, IOException {
		FileInputStream stream = new FileInputStream(file);
		try {
			return load(stream);
		} finally {
			try { stream.close(); } catch (IOException e) {}
		}
	}
	public static ID3Format load(InputStream stream) throws IOException {
		return ID3Loader.detect(stream);
	}
	
}
