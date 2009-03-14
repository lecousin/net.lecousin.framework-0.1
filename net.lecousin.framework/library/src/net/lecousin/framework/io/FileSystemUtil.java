package net.lecousin.framework.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.progress.WorkProgress;

public class FileSystemUtil {

	public static String makePathRelative(String absoluteFrom, String absoluteTo) {
		absoluteFrom = absoluteFrom.replace('\\', '/');
		absoluteTo = absoluteTo.replace('\\', '/');
		String[] from = absoluteFrom.split("/");
		String[] to = absoluteTo.split("/");
		int i;
		for (i = 0; i < from.length && i < to.length; ++i)
			if (!from[i].equals(to[i]))
				break;
		StringBuilder result = new StringBuilder("/");
		for (int j = i; j < from.length; ++j)
			result.append("../");
		for (int j = i; j < to.length; ++j)
			result.append(to[j]).append("/");
		result.deleteCharAt(result.length()-1);
		return result.toString();
	}
	
	public static String getExtension(String path) {
		return getFileNameExtension(getFileName(path));
	}
	
	public static String getFileNameExtension(String filename) {
		int i = filename.lastIndexOf('.');
		if (i < 0) return "";
		return filename.substring(i+1);
	}
	
	public static String getFileName(String path) {
		int i = path.lastIndexOf('/');
		int j = path.lastIndexOf('\\');
		if (j > i) i = j;
		if (i < 0) return path;
		return path.substring(i+1);
	}
	
	public static String getFileNameWithoutExtension(String path) {
		return removeFileNameExtension(getFileName(path));
	}
	
	public static String removeFileNameExtension(String filename) {
		int i = filename.lastIndexOf('.');
		if (i < 0) return filename;
		return filename.substring(0, i);
	}
	
	public static void copyFile(String src, String dst, WorkProgress progress, int amount) throws IOException {
		copyFile(new File(src), new File(dst), progress, amount);
	}
	public static void copyFile(File src, File dst, WorkProgress progress, int amount) throws IOException {
		dst.getParentFile().mkdirs();
		dst.createNewFile();
		FileInputStream in = new FileInputStream(src);
		FileOutputStream out = new FileOutputStream(dst);
		IOUtil.copy(in, out);
		out.flush();
		out.close();
		in.close();
		progress.progress(amount);
	}
	
	public static void copyDirectory(String src, String dst, WorkProgress progress, int amount) throws IOException {
		copyDirectory(new File(src), new File(dst), progress, amount);
	}
	public static void copyDirectory(File src, File dst, WorkProgress progress, int amount) throws IOException {
		dst.mkdirs();
		if (!src.exists())
			throw new IOException("Directory '" + src.getAbsolutePath() + "' doesn't exist.");
		if (!src.isDirectory())
			throw new IOException("'" + src.getAbsolutePath() + "' is not a directory.");
		File[] members = src.listFiles();
		if (members.length == 0) {
			progress.progress(amount);
			return;
		}
		List<File> subdirs = new LinkedList<File>();
		List<File> files = new LinkedList<File>();
		for (File file : members)
			if (file.isDirectory())
				subdirs.add(file);
			else
				files.add(file);
		int nb = subdirs.size()*3+files.size();
		int total = amount;
		for (File file : files) {
			int step = total/(nb--);
			total -= step;
			copyFile(file, new File(dst, file.getName()), progress, step);
		}
		nb /= 3;
		for (File file : subdirs) {
			int step = total/(nb--);
			total -= step;
			copyDirectory(file, new File(dst, file.getName()), progress, step);
		}
	}
	
	public static void deleteDirectory(String dir) {
		deleteDirectory(new File(dir));
	}
	public static void deleteDirectory(File dir) {
		if (!dir.exists()) return;
		for (File f : dir.listFiles())
			if (f.isDirectory())
				deleteDirectory(f);
			else
				f.delete();
		dir.delete();
	}
}
