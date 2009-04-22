package net.lecousin.framework.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.application.Application;
import net.lecousin.framework.log.Log;
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
	public static String getPath(String path) {
		int i = path.lastIndexOf('/');
		int j = path.lastIndexOf('\\');
		if (j > i) i = j;
		if (i < 0) return "";
		return path.substring(0,i);
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
		copyDirectory(src, dst, null, progress, amount);
	}
	public static void copyDirectory(File src, File dst, Filter filter, WorkProgress progress, int amount) throws IOException {
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
			if (filter == null || filter.accept(file)) {
				if (file.isDirectory())
					subdirs.add(file);
				else
					files.add(file);
			}
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
		deleteDirectory(dir, null);
	}
	public static void deleteDirectory(File dir, Filter filter) {
		if (!dir.exists()) return;
		for (File f : dir.listFiles()) {
			if (filter != null && !filter.accept(f)) continue;
			if (f.isDirectory())
				deleteDirectory(f);
			else
				f.delete();
		}
		if (filter == null || filter.accept(dir))
			dir.delete();
	}
	
	public static interface Filter {
		public boolean accept(File file);
	}
	
	public static class Drive {
		private Drive(File root, Type type)
		{ this.root = root; this.type = type; }
		File root;
		Type type;
		
		public static enum Type {
			UNKNOWN,
			REMOVABLE,
			CDROM,
			NETWORK,
			FIXED,
			RAMDISK,
			;
			public static Type get(String s) {
				try { return get(Integer.parseInt(s)); }
				catch (NumberFormatException e) { return UNKNOWN; }
			}
			public static Type get(int i) {
				switch (i) {
				default:
				case 0: return UNKNOWN;
				case 1: return REMOVABLE;
				case 2: return FIXED;
				case 3: return NETWORK;
				case 4: return CDROM;
				case 5: return RAMDISK;
				}
			}
		}
		
		public File getRoot() { return root; }
		public Type getType() { return type; }
	}
	
	private static List<Drive> drives = null;
	public static void clearDrives() { drives = null; }
	
	private static void initDrives() {
		drives = new LinkedList<Drive>();
		try { Runtime.getRuntime().exec("wscript //B init.vbs", null, Application.deployPath); }
		catch (IOException e) {
			if (Log.error(FileSystemUtil.class))
				Log.error(FileSystemUtil.class, "Unable to launch script to search drives on the system", e);
			return;
		}
		File file = new File(Application.deployPath, "drives");
		if (!file.exists()) return;
		try {
			String[] lines = IOUtil.readAllLines(file, false);
			for (String line : lines) {
				int i = line.indexOf(':');
				if (i <= 0) continue;
				File root = new File(line.substring(0, i)+":\\");
				Drive.Type type = Drive.Type.get(line.substring(i+1));
				drives.add(new Drive(root, type));
			}
		} catch (IOException e) {
			if (Log.error(FileSystemUtil.class))
				Log.error(FileSystemUtil.class, "Unable to read drives on the system", e);
			return;
		}
	}
	
	/** a file init.vbs must be located in the Application.deployPath directory */
	public static List<Drive> getDrives() {
		if (drives == null) initDrives();
		return drives;
	}
	/** a file init.vbs must be located in the Application.deployPath directory */
	public static boolean isOnAmovibleDrive(File file) {
		return getAmovibleDrive(file) != null;
	}
	/** a file init.vbs must be located in the Application.deployPath directory */
	public static File getAmovibleDrive(File file) {
		if (drives == null) initDrives();
		for (Drive drive : drives)
			if (drive.getRoot().equals(file))
				if (isAmovible(drive))
					return file;
		File parent = file.getParentFile();
		if (parent == null) return null;
		return getAmovibleDrive(parent);
	}
	/** a file init.vbs must be located in the Application.deployPath directory */
	public static List<Drive> getAmovibleDrives() {
		if (drives == null) initDrives();
		List<Drive> result = new LinkedList<Drive>();
		for (Drive d : drives)
			if (isAmovible(d))
				result.add(d);
		return result;
	}
	
	public static boolean isAmovible(Drive drive) {
		switch (drive.getType()) {
		case REMOVABLE:
		case CDROM: return true;
		default: return false;
		}
	}
}
