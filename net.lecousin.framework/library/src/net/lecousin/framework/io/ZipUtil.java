package net.lecousin.framework.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.lecousin.framework.progress.WorkProgress;

public class ZipUtil {

	private ZipUtil() {}
	
	public static void unzip(File zipFile, File targetDir, WorkProgress progress, int work) throws IOException {
		ZipFile zip = new ZipFile(zipFile);
		targetDir.mkdirs();
		Enumeration<? extends ZipEntry> en = zip.entries();
		while (en.hasMoreElements()) {
			ZipEntry entry = en.nextElement();
			String name = entry.getName();
			File target = new File(targetDir, name);
			if (target.isDirectory()) {
				target.mkdirs();
				continue;
			} else
				target.getParentFile().mkdirs();
			InputStream in = zip.getInputStream(entry);
			FileOutputStream out = new FileOutputStream(target);
			IOUtil.copy(in, out);
			in.close();
			out.flush();
			out.close();
		}
	}
	
}
