package net.lecousin.framework.files.image;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.io.LCPartialBufferedInputStream;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class ImageFile extends TypedFile {

	public static ImageFile detect(URI uri, LCPartialBufferedInputStream stream) {
		ImageLoader loader = new ImageLoader();
		try {
			ImageData[] data = loader.load(stream);
			return new ImageFile(uri, new ImageFileInfo(data));
		} catch (Throwable t) {
			return null;
		}
	}
	
	private ImageFile(URI uri, ImageFileInfo info) {
		super(uri, FILE_TYPE, info);
	}

	@Override
	public ImageFileInfo getInfo() {
		return (ImageFileInfo)super.getInfo();
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "image";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(null, FILE_TYPE_NAME);
		types = new FileType[] {
			FILE_TYPE,
		};
	}
	
	static FileType[] types;
}
