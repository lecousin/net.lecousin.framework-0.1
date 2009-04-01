package net.lecousin.framework.files.video;

import net.lecousin.framework.files.TypedFileInfo;
import net.lecousin.framework.geometry.PointInt;


public interface VideoFileInfo extends TypedFileInfo {

	public PointInt getDimension();
	
}
