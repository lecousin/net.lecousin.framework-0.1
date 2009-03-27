package net.lecousin.framework.files.video.avi;

import java.io.IOException;

import net.lecousin.framework.files.video.VideoFileInfo;
import net.lecousin.framework.io.IOUtil;
import net.lecousin.framework.io.LCPartialBufferedInputStream;

public class AVIFormat implements VideoFileInfo {

	public static AVIFormat read(LCPartialBufferedInputStream stream) {
		byte[] buf = new byte[12];
		try {
			if (stream.read(buf) != 12) return null;
		} catch (IOException e) { return null; }
		if (buf[0x00] != 'R' || buf[0x01] != 'I' || buf[0x02] != 'F' || buf[0x03] != 'F') return null;
		if (buf[0x08] != 'A' || buf[0x09] != 'V' || buf[0x0A] != 'I' || buf[0x0B] != ' ') return null;
		AVIFormat avi = new AVIFormat();
		try { if (!readMainHeader(avi, stream)) return null; }
		catch (IOException e) { return null; }
		return avi;
	}
	
	private static boolean readMainHeader(AVIFormat avi, LCPartialBufferedInputStream stream) throws IOException {
		byte[] buf = new byte[12];
		if (stream.read(buf) != 12) return false;
		if (buf[0x00] != 'L' || buf[0x01] != 'I' || buf[0x02] != 'S' || buf[0x03] != 'T') return false;
		long size = IOUtil.readLong(buf, 4);
		if (buf[0x08] != 'h' || buf[0x09] != 'd' || buf[0x0A] != 'r' || buf[0x0B] != 'l') return false;
		buf = null;
		long used = readAVIH(avi, stream);
		if (used < 0) return false;
		size -= used;
		int streamIndex = 0;
		do {
			used = readStreamHeader(avi.streams[streamIndex], stream);
			if (used < 0) break;
			size -= used;
			streamIndex++;
		} while (size > 0 && streamIndex < avi.streams.length);
		return true;
	}
	
	private static long readAVIH(AVIFormat avi, LCPartialBufferedInputStream stream) throws IOException {
		byte[] buf = new byte[0x40];
		if (stream.read(buf) != 0x40) return -1;
		if (buf[0x00] != 'a' || buf[0x01] != 'v' || buf[0x02] != 'i' || buf[0x03] != 'h') return -1;
		if (buf[0x04] != 0x38 || buf[0x05] != 0 || buf[0x06] != 0 || buf[0x07] != 0) return -1;
		avi.microSecPerFrame = IOUtil.readLong(buf, 0x08);
		avi.nbFrames = IOUtil.readLong(buf, 0x18);
		long nb = IOUtil.readLong(buf, 0x20);
		if (nb > 100) nb = 0;
		avi.streams = new Stream[(int)nb];
		for (int i = 0; i < avi.streams.length; ++i)
			avi.streams[i] = new Stream();
		avi.width = IOUtil.readLong(buf, 0x28);
		avi.height = IOUtil.readLong(buf, 0x2C);
		return 0x40;
	}
	
	private static long readStreamHeader(Stream avistream, LCPartialBufferedInputStream stream) throws IOException {
		byte[] buf = new byte[12];
		if (stream.read(buf) != 12) return -1;
		if (buf[0x00] != 'L' || buf[0x01] != 'I' || buf[0x02] != 'S' || buf[0x03] != 'T') return -1;
		long size = IOUtil.readLong(buf, 4);
		if (buf[0x08] != 's' || buf[0x09] != 't' || buf[0x0A] != 'r' || buf[0x0B] != 'l') return -1;
		buf = null;
		long remaining = size;
		do {
			buf = new byte[8];
			if (stream.read(buf) != 8) return -1;
			long partSize = IOUtil.readLong(buf, 4);
			buf = new byte[(int)partSize];
			if (stream.read(buf) != partSize) return -1;
			if (buf[0] == 's' || buf[1] == 't' || buf[2] == 'r') {
				switch (buf[3]) {
				case 'h': readSTRH(avistream, buf); break;
				case 'f': readSTRF(avistream, buf); break;
				case 'd': readSTRD(avistream, buf); break;
				case 'n': readSTRN(avistream, buf); break;
				}
			}
			remaining -= partSize+8;
		} while (remaining > 0);
		return size+8;
	}
	
	private static void readSTRH(Stream stream, byte[] buf) {
		String s = getString(buf, 0, 4);
		if (s == null) stream.type = null;
		else if (s.equals("auds")) stream.type = Stream.Type.AUDIO;
		else if (s.equals("mids")) stream.type = Stream.Type.MIDI;
		else if (s.equals("txts")) stream.type = Stream.Type.TEXT;
		else if (s.equals("vids")) stream.type = Stream.Type.VIDEO;
		stream.codec = getString(buf, 0x04, 4);
		stream.language = getString(buf, 0x10, 4);
	}
	private static void readSTRF(Stream stream, byte[] buf) {
		
	}
	private static void readSTRD(Stream stream, byte[] buf) {
		
	}
	private static void readSTRN(Stream stream, byte[] buf) {
		stream.streamName = getString(buf, 0, buf.length);
	}
	
	private static String getString(byte[] buf, int off, int len) {
		if (buf[off] == 0) return null;
		return new String(buf, off, len);
	}
	
	private AVIFormat() {
	}

	private long microSecPerFrame;
	private long nbFrames;
	private long width;
	private long height;
	private Stream[] streams;

	public static class Stream {
		public static enum Type {
			AUDIO, VIDEO, TEXT, MIDI
		}

		private Type type;
		private String codec;
		private String language;
		private String streamName;
	}
}
