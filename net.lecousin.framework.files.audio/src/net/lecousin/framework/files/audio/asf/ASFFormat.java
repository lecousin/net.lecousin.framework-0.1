package net.lecousin.framework.files.audio.asf;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

import net.lecousin.framework.collections.ArrayUtil;
import net.lecousin.framework.files.audio.AudioFileInfo;
import net.lecousin.framework.io.IOUtil;

public class ASFFormat implements AudioFileInfo {

	private static byte[] ASF_GUID = new byte[] 				{ (byte)0x30, (byte)0x26, (byte)0xB2, (byte)0x75, (byte)0x8E, (byte)0x66, (byte)0xCF, (byte)0x11, (byte)0xA6, (byte)0xD9, (byte)0x00, (byte)0xAA, (byte)0x00, (byte)0x62, (byte)0xCE, (byte)0x6C };
	private static byte[] FileProperties_GUID = new byte[] 		{ (byte)0xA1, (byte)0xDC, (byte)0xAB, (byte)0x8C, (byte)0x47, (byte)0xA9, (byte)0xCF, (byte)0x11, (byte)0x8E, (byte)0xE4, (byte)0x00, (byte)0xC0, (byte)0x0C, (byte)0x20, (byte)0x53, (byte)0x65 };
	private static byte[] ContentDescription_GUID = new byte[] 	{ (byte)0x33, (byte)0x26, (byte)0xB2, (byte)0x75, (byte)0x8E, (byte)0x66, (byte)0xCF, (byte)0x11, (byte)0xA6, (byte)0xD9, (byte)0x00, (byte)0xAA, (byte)0x00, (byte)0x62, (byte)0xCE, (byte)0x6C };
	
	public static ASFFormat read(InputStream stream) {
		long nbObjects;
		try {
			byte[] buf = new byte[16];
			if (IOUtil.readAllBuffer(stream, buf) != 16) return null;
			if (!ArrayUtil.equals(buf, ASF_GUID)) return null;
			if (stream.read(buf, 0, 8) != 8) return null;
			if (stream.read(buf, 0, 4) != 4) return null;
			nbObjects = IOUtil.readLongIntel(buf, 0);
			stream.skip(2);
		} catch (IOException e) { return null; }
		ASFFormat asf = new ASFFormat();
		try {
			for (long i = 0; i < nbObjects; ++i) {
				if (!readHeaderObject(stream, asf)) break;
			}
		} catch (IOException e) {}
		return asf;
	}
	private ASFFormat() {
	}
	
	private static boolean readHeaderObject(InputStream stream, ASFFormat asf) throws IOException {
		byte[] buf = new byte[16];
		if (IOUtil.readAllBuffer(stream, buf) != 16) return false;
		byte[] buf2 = new byte[8];
		if (stream.read(buf2, 0, 8) != 8) return false;
		long size = IOUtil.readLongIntel(buf2, 0);
		if (ArrayUtil.equals(buf, FileProperties_GUID))
			return readFileProperties(stream, size, asf);
		if (ArrayUtil.equals(buf, ContentDescription_GUID))
			return readContentDescription(stream, size, asf);
		stream.skip(size);
		return true;
	}
	
	private static boolean readFileProperties(InputStream stream, long size, ASFFormat asf) throws IOException {
		stream.skip(0x28);
		byte[] buf = new byte[8];
		if (stream.read(buf) != 8) return false;
		byte[] buf2 = new byte[8];
		buf2[0] = buf[7];
		buf2[1] = buf[6];
		buf2[2] = buf[5];
		buf2[3] = buf[4];
		buf2[4] = buf[3];
		buf2[5] = buf[2];
		buf2[6] = buf[1];
		buf2[7] = buf[0];
		BigInteger i = new BigInteger(buf2);
		i = i.divide(new BigInteger("10000"));
		asf.length = i.longValue();
		stream.skip(size-0x28-8);
		return true;
	}
	private static boolean readContentDescription(InputStream stream, long size, ASFFormat asf) throws IOException {
		byte[] buf = new byte[10];
		stream.read(buf);
		short titleSize = IOUtil.readShortIntel(buf, 0);
//		short authorSize = IOUtil.readShortIntel(buf, 2);
//		short copyrightSize = IOUtil.readShortIntel(buf, 4);
//		short descriptionSize = IOUtil.readShortIntel(buf, 6);
//		short ratingSize = IOUtil.readShortIntel(buf, 8);
		asf.title = readString(stream, titleSize);
		stream.skip(size-10-titleSize);
		return true;
	}
	private static String readString(InputStream stream, int size) throws IOException {
		byte[] buf = new byte[size];
		stream.read(buf);
		return new String(buf, "UTF-16LE");
	}
	
	private long length = -1;
	private String title = null;
	
	public int getTrackNumber() { return -1; }
	public long getDuration() { return length; }
	
	public String getAlbum() { return null; }
	public String getArtist() { return null; }
	public byte[] getCDIdentifier() { return null; }
	public String getComment() { return null; }
	public String getGenre() { return null; }
	public int getNumberOfTracksInAlbum() { return -1; }
	public List<Picture> getPictures() { return null; }
	public String getSongTitle() { return title; }
	public int getYear() { return -1; }
}
