package net.lecousin.framework.files.image;

import net.lecousin.framework.files.TypedFileInfo;

import org.eclipse.swt.graphics.ImageData;


public class ImageFileInfo implements TypedFileInfo {

	public ImageFileInfo(ImageData[] data) {
		this.data = data;
	}
	
	public ImageData[] data;
	
	public ImageData[] getData() { return data; }
	
}
