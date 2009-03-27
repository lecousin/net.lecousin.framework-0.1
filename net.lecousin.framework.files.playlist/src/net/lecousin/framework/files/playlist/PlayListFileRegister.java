package net.lecousin.framework.files.playlist;

import net.lecousin.framework.files.FileTypeDetector;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.TypedFileRegister;

public class PlayListFileRegister implements TypedFileRegister {

	public PlayListFileRegister() {
	}

	public FileTypeDetector[] getDetectors() {
		return new FileTypeDetector[] {
			new PlayListDetector()
		};
	}

	public void registerTypes(FileTypeRegistry registry) {
		PlayList.registerTypes(registry);
	}

}
