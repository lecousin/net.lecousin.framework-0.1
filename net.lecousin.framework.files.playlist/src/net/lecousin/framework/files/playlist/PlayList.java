package net.lecousin.framework.files.playlist;

import java.net.URI;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.TypedFile;
import net.lecousin.framework.files.playlist.m3u.M3UPlayList;

public abstract class PlayList extends TypedFile {

	public PlayList(URI uri, FileType type, PlayListInfo info) {
		super(uri, type, info);
	}
	
	@Override
	public PlayListInfo getInfo() {
		return (PlayListInfo)super.getInfo();
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "playlist";

	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(null, FILE_TYPE_NAME);
		M3UPlayList.registerTypes(registry);
		types = new FileType[] {
			FILE_TYPE,
			M3UPlayList.FILE_TYPE,
		};
	}
	static FileType[] types;
}
