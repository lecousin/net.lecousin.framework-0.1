package net.lecousin.framework.files.playlist;

import java.util.List;

import net.lecousin.framework.files.TypedFileInfo;

public interface PlayListInfo extends TypedFileInfo {

	public String getName();
	public List<String> getFileList();
	
}
