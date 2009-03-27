package net.lecousin.framework.files.audio.cda;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.lecousin.framework.files.audio.AudioFileInfo;

public class CDAFormat implements AudioFileInfo {

	public static CDAFormat read(InputStream stream) {
		byte[] buf = new byte[44];
		try {
			if (stream.read(buf) != 44) return null;
		} catch (IOException e) { return null; }
		if (buf[0x00] != 'R' || buf[0x01] != 'I' || buf[0x02] != 'F' || buf[0x03] != 'F') return null;
		if (buf[0x08] != 'C' || buf[0x09] != 'D' || buf[0x0A] != 'D' || buf[0x0B] != 'A') return null;
		if (buf[0x0C] != 'f' || buf[0x0D] != 'm' || buf[0x0E] != 't' || buf[0x0F] != ' ') return null;
		CDAFormat cda = new CDAFormat();
		cda.length = ((long)(buf[0x2A]&0xFF)*60+((long)buf[0x29]&0xFF))*(long)1000;
		cda.trackNumber = (buf[0x16]&0xFF)+(((int)buf[0x17]&0xFF)<<8);
		return cda;
	}
	private CDAFormat() {
	}
	
	private long length;
	private int trackNumber;
	
	public int getTrackNumber() { return trackNumber; }
	public long getDuration() { return length; }
	
	public String getAlbum() { return null; }
	public String getArtist() { return null; }
	public byte[] getCDIdentifier() { return null; }
	public String getComment() { return null; }
	public String getGenre() { return null; }
	public int getNumberOfTracksInAlbum() { return -1; }
	public List<Picture> getPictures() { return null; }
	public String getSongTitle() { return null; }
	public int getYear() { return -1; }
}
