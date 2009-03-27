package net.lecousin.framework.files.playlist.m3u;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.playlist.PlayList;
import net.lecousin.framework.files.playlist.PlayListInfo;
import net.lecousin.framework.io.FileSystemUtil;
import net.lecousin.framework.io.TextLineInputStream;

public class M3UPlayList extends PlayList {

	public static M3UPlayList load(URI uri, InputStream stream) {
		TextLineInputStream in = new TextLineInputStream(stream);
		List<String> list = new LinkedList<String>();
		while (!in.isEndOfStream()) {
			String line;
			try { line = in.readLine(); }
			catch (IOException e) { break; }
			line = line.trim();
			if (line.length() == 0) continue;
			if (line.charAt(0) == '#') continue;
			list.add(line);
		}
		Info info = new Info(FileSystemUtil.getFileNameWithoutExtension(uri.getPath()), list);
		return new M3UPlayList(uri, info);
	}
	
	private M3UPlayList(URI uri, Info info) {
		super(uri, FILE_TYPE, info);
	}
	
	public static class Info implements PlayListInfo {
		Info(String name, List<String> list)
		{ this.name = name; this.list = list; }
		private String name;
		private List<String> list = new LinkedList<String>();
		public String getName() { return name; }
		public List<String> getFileList() { return list; }
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "m3u";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(PlayList.FILE_TYPE, FILE_TYPE_NAME);
	}
}
