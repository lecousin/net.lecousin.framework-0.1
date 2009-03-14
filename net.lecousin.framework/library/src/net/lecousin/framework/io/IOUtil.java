package net.lecousin.framework.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.lecousin.framework.collections.ArrayUtil;
import net.lecousin.framework.random.Random;

public class IOUtil {

	public static String generateUniqueFileName(File dir) {
		long l = Random.randLong(Long.MAX_VALUE);
		String[] files = dir.list();
		do {
			String s = Long.toHexString(l).toUpperCase();
			while (s.length() < 8)
				s = "0" + s;
			if (!ArrayUtil.contains(files, s))
				return s;
			if (l < Long.MAX_VALUE) ++l; else l = 0;
		} while (true);
	}
	
	public static byte[] readAll(InputStream in) throws IOException {
		byte[] result = new byte[in.available()];
		in.read(result);
		return result;
	}
	
	public static String readAllInString(InputStream in) throws IOException {
		return new String(readAll(in));
	}
	
	public static String readPart(InputStream in, String endDelimiter) throws IOException {
		StringBuilder str = new StringBuilder();
		do {
			int c = in.read();
			if (c == -1) return str.toString();
			str.append((char)c);
		} while (str.length() < endDelimiter.length() || !str.substring(str.length()-endDelimiter.length()).equals(endDelimiter));
		str.delete(str.length()-endDelimiter.length(), str.length());
		return str.toString();
	}
	
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[65536];
		do {
			int nb = in.read(buffer);
			if (nb == -1) break;
			out.write(buffer, 0, nb);
		} while (true);
	}
	
	public static long readLong(byte[] buffer, int offset) {
		long value = 0;
		value += (buffer[offset] & 0xFF) << 24;
		value += (buffer[offset+1] & 0xFF) << 16;
		value += (buffer[offset+2] & 0xFF) << 8;
		value += (buffer[offset+3] & 0xFF);
		return value;
	}
}
