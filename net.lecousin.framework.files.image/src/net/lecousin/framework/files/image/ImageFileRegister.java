package net.lecousin.framework.files.image;

import net.lecousin.framework.files.FileTypeDetector;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.TypedFileRegister;

public class ImageFileRegister implements TypedFileRegister {

	public ImageFileRegister() {
	}

	public FileTypeDetector[] getDetectors() {
		return new FileTypeDetector[] {
				new ImageFileDetector(),
		};
	}

	public void registerTypes(FileTypeRegistry registry) {
		ImageFile.registerTypes(registry);
	}

}
