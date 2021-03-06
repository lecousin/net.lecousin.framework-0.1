package net.lecousin.framework.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

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
	
	public static String[] readAllLines(File file, boolean includeEmptyLines) throws FileNotFoundException, IOException {
		return readAllLines(new LCBufferedInputStream(new FileInputStream(file)), includeEmptyLines);
	}
	public static String[] readAllLines(InputStream stream, boolean includeEmptyLines) throws IOException {
		TextLineInputStream in = new TextLineInputStream(stream);
		List<String> lines = new LinkedList<String>();
		String line;
		while ((line = in.readLine()) != null) {
			if (!includeEmptyLines && line.length() == 0)
				continue;
			lines.add(line);
		}
		return lines.toArray(new String[lines.size()]);
	}
	
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[65536];
		do {
			int nb = in.read(buffer);
			if (nb == -1) break;
			out.write(buffer, 0, nb);
		} while (true);
	}
	
	/** big-endian or motorola format */
	public static long readLongMotorola(byte[] buffer, int offset) {
		long value = 0;
		value += (buffer[offset] & 0xFF) << 24;
		value += (buffer[offset+1] & 0xFF) << 16;
		value += (buffer[offset+2] & 0xFF) << 8;
		value += (buffer[offset+3] & 0xFF);
		return value;
	}
	/** little-endian or intel format */
	public static long readLongIntel(byte[] buffer, int offset) {
		long value = 0;
		value += (buffer[offset] & 0xFF);
		value += (buffer[offset+1] & 0xFF) << 8;
		value += (buffer[offset+2] & 0xFF) << 16;
		value += (buffer[offset+3] & 0xFF) << 24;
		return value;
	}
	/** little-endian or intel format */
	public static short readShortIntel(byte[] buffer, int offset) {
		short value = 0;
		value += (buffer[offset] & 0xFF);
		value += (buffer[offset+1] & 0xFF) << 8;
		return value;
	}
	/** big-endian or motorola format */
	public static short readShortMotorola(byte[] buffer, int offset) {
		short value = 0;
		value += (buffer[offset] & 0xFF) << 8;
		value += (buffer[offset+1] & 0xFF);
		return value;
	}
	
	public static int readAllBuffer(InputStream stream, byte[] buffer) throws IOException {
		return readAllBuffer(stream, buffer, 0, buffer.length);
	}
	
	public static int readAllBuffer(InputStream stream, byte[] buffer, int off, int len) throws IOException {
		int total = 0;
		do {
			int l = len-total;
			int nb = stream.read(buffer, off, l);
			if (nb <= 0)
				return total;
			off += nb;
			total += nb;
		} while (total < len);
		return total;
	}
}
