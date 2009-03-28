package net.lecousin.framework.files.audio.mp3;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.files.audio.AudioFileInfo.Picture.Type;
import net.lecousin.framework.io.IOUtil;
import net.lecousin.framework.io.LCBufferedInputStream;
import net.lecousin.framework.log.Log;

public class ID3Format_v2 extends ID3Format {

	private ID3Format_v2() {
	}
	
	static Pair<ID3Format_v2,Long> create(byte[] buffer, int len, InputStream stream) throws IOException {
		ID3Format_v2 id3 = new ID3Format_v2();
		LCBufferedInputStream in = new LCBufferedInputStream(buffer, 0, len, stream);
		byte[] header = new byte[10];
		in.read(header);
		long size = readSize(header, 6);
		long pos = 0;
		boolean hasExtendedHeader = (header[5] & 0x40) != 0;
		//boolean hasFooter = (header[5] & 0x10) != 0;
		if (hasExtendedHeader) {
			byte[] extHeader = new byte[6];
			in.read(extHeader);
			long extHeaderSize = IOUtil.readLong(extHeader, 0);
			in.skip(extHeaderSize-6);
			pos += extHeaderSize;
		}
		long l;
		do {
			if (header[3] <= 2)
				l = id3.readFrame_1(in, pos, size);
			else
				l = id3.readFrame_2(in, pos, size);
			pos += l;
		} while (pos < size && l != 0);
		return new Pair<ID3Format_v2,Long>(id3, size+10);
	}
	
	private static long readSize(byte[] buf, int pos) {
		long value = 0;
		value += (buf[pos] & 0x7F) << 21;
		value += (buf[pos+1] & 0x7F) << 14;
		value += (buf[pos+2] & 0x7F) << 7;
		value += (buf[pos+3] & 0x7F);
		return value;
	}
	
	private long readFrame_1(InputStream in, long pos, long len) throws IOException {
		byte[] frameHeader = new byte[6];
		in.read(frameHeader);
		if (frameHeader[0] == 0x00)
			return 0;
		long frameSize = 0;
		frameSize += (frameHeader[3] & 0xFF) << 16;
		frameSize += (frameHeader[4] & 0xFF) << 8;
		frameSize += (frameHeader[5] & 0xFF);
		if (pos+6+frameSize > len)
			return 0;
		byte[] frameBody = new byte[(int)frameSize];
		in.read(frameBody);
		String type = new String(frameHeader, 0, 3);
		if (type.equals("TT2"))
			song_title = readText(frameBody);
		else if (type.equals("TAL"))
			album = readText(frameBody);
		else if (type.equals("TP1"))
			artist = readText(frameBody);
		else if (type.equals("TYE"))
			year = toInt(readText(frameBody));
		else if (type.equals("TCO"))
			genre = getGenre(readText(frameBody));
		else if (type.equals("COM"))
			comment = readText(frameBody);
		else if (type.equals("TRK")) {
			String s = readText(frameBody);
			int i = s.indexOf('/');
			if (i < 0)
				track_number = toInt(s);
			else {
				track_number = toInt(s.substring(0, i));
				number_of_tracks_in_album = toInt(s.substring(i+1));
			}
		} else if (type.equals("TLE"))
			setDuration(toLong(readText(frameBody)));
		else if (type.equals("TMT")) { /* media type, skip */ }
		else if (type.equals("TFT")) { /* file type, skip */ }
		else if (type.equals("TXX")) { /* user defined text, skip */ }
		else if (type.equals("TEN")) { /* encoded by, skip */ }
		else if (type.equals("MCI"))
			mcdi = frameBody;
		else if (type.equals("GEO")) { /* encapsulated object, skip */ }
		else if (type.equals("WXX")) { /* user defined link url, skip */ }
		else if (type.equals("PIC"))
			loadPicture(frameBody, 1);
		else if (type.charAt(0) == 'X' || type.charAt(0) == 'Y' || type.charAt(0) == 'Z')
		{ /* skip user defined/proprietary tags */ }
		else if (Log.warning(this))
			Log.warning(this, "ID3: frame ID (v1-2) not supported: " + type);

		return frameSize + 10;
	}

	private long readFrame_2(InputStream in, long pos, long len) throws IOException {
		byte[] frameHeader = new byte[10];
		in.read(frameHeader);
		if (frameHeader[0] == 0x00)
			return 0;
		long frameSize = IOUtil.readLong(frameHeader, 4);
		if (pos+10+frameSize > len)
			return 0;
		byte[] frameBody = new byte[(int)frameSize];
		in.read(frameBody);
		// TODO encoding... with flags
		String type = new String(frameHeader, 0, 4);
		if (type.equals("TIT2"))
			song_title = readText(frameBody);
		else if (type.equals("TALB"))
			album = readText(frameBody);
		else if (type.equals("TPE1"))
			artist = readText(frameBody);
		else if (type.equals("TYER"))
			year = toInt(readText(frameBody));
		else if (type.equals("TCON"))
			genre = getGenre(readText(frameBody));
		else if (type.equals("COMM"))
			comment = readText(frameBody);
		else if (type.equals("TRCK")) {
			String s = readText(frameBody);
			int i = s.indexOf('/');
			if (i < 0)
				track_number = toInt(s);
			else {
				track_number = toInt(s.substring(0, i));
				number_of_tracks_in_album = toInt(s.substring(i+1));
			}
		} else if (type.equals("TLEN"))
			setDuration(toLong(readText(frameBody)));
		else if (type.equals("TMED")) { /* media type, skip */ }
		else if (type.equals("TFLT")) { /* file type, skip */ }
		else if (type.equals("TXXX")) { /* user defined text, skip */ }
		else if (type.equals("TENC")) { /* encoded by, skip */ }
		else if (type.equals("TCOP")) { /* copyright, skip */ }
		else if (type.equals("MCDI"))
			mcdi = frameBody;
		else if (type.equals("PRIV")) { /* skip */ }
		else if (type.equals("GEOB")) { /* encapsulated object, skip */ }
		else if (type.equals("WXXX")) { /* user defined link url, skip */ }
		else if (type.equals("APIC"))
			loadPicture(frameBody, 2);
		else if (type.charAt(0) == 'X' || type.charAt(0) == 'Y' || type.charAt(0) == 'Z')
		{ /* skip user defined/proprietary tags */ }
		else if (Log.warning(this))
			Log.warning(this, "ID3: frame ID (v3-4) not supported: " + type);

		return frameSize + 10;
	}
	
	private String readText(byte[] body) {
		if (body[0] == 0x00) {
			int len = body.length-1;
			while (len > 0 && body[len] == 0x00) len--;
			return new String(body, 1, len);
		}
		if (body[0] == 0x01) {
			int len = body.length-1;
			while (len > 0 && body[len] == 0x00 && body[len-1] == 0x00) len -= 2;
			try { return new String(body, 1, len, "UTF-16"); }
			catch (UnsupportedEncodingException e) {
				if (Log.error(this)) Log.error(this, "Unsupported UNICODE encoding", e);
				return "";
			}
		}
		int len = body.length;
		while (len > 0 && body[len-1] == 0x00) len--;
		return new String(body, 0, len);
	}
	
	private int toInt(String s) {
		s = s.trim();
		if (s.length() == 0) return -1;
		try { return Integer.parseInt(s); }
		catch (NumberFormatException e) { return -1; }
	}
	private long toLong(String s) {
		s = s.trim();
		if (s.length() == 0) return -1;
		try { return Long.parseLong(s); }
		catch (NumberFormatException e) { return -1; }
	}
	
	private void loadPicture(byte[] body, int version) {
		Picture picture = new Picture();
		int pos = 0;
		byte textEncoding = body[pos++];
		// format
		if (version == 1)
			pos += 3;
		else
			while (pos < body.length && body[pos++] != 0x00);
		picture.type = getPictureType(body[pos++]);
		if (textEncoding == 0x00) {
			int end = pos;
			while (body[end] != 0x00) end++;
			picture.description = new String(body, pos, end-pos);
			pos = end+1;
		} else if (textEncoding == 0x01) {
			int end = pos;
			while (body[end] != 0x00 || body[end+1] != 0x00) end += 2;
			try { picture.description = new String(body, pos, end-pos, "UTF-16"); }
			catch (UnsupportedEncodingException e) { picture.description = ""; }
			pos = end+2;
		} else {
			int end = pos;
			while (body[end] != 0x00) end++;
			picture.description = new String(body, pos, end-pos);
			pos = end+1;
		}
		picture.data = new byte[body.length-pos];
		System.arraycopy(body, pos, picture.data, 0, picture.data.length);
		pictures.add(picture);
	}
	
	private String song_title;
	private String album;
	private String artist;
	private String comment;
	private int track_number = -1;
	private int number_of_tracks_in_album = -1;
	private int year = -1;
	private String genre;
	private byte[] mcdi = null;
	private List<Picture> pictures = new LinkedList<Picture>();
	
	public String getSongTitle() { return song_title; }
	public String getAlbum() { return album; }
	public String getArtist() { return artist; }
	public String getComment() { return comment; }
	public int getTrackNumber() { return track_number; }
	public int getYear() { return year; }
	public String getGenre() { return genre; }
	public byte[] getCDIdentifier() { return mcdi; }
	public int getNumberOfTracksInAlbum() { return number_of_tracks_in_album; }
	public List<Picture> getPictures() { return pictures; }
	
	private static String getGenre(String str) {
		return str;
	}
	private static Type getPictureType(byte t) {
		switch (t) {
		default:
		case 0x00: return Type.OTHER;
		case 0x01:
		case 0x02: return Type.ICON;
		case 0x03: return Type.COVER_FRONT;
		case 0x04: return Type.COVER_BACK;
		case 0x05: return Type.LEAFLET_PAGE;
		case 0x06: return Type.MEDIA;
		case 0x07: return Type.LEAD_ARTIST;
		case 0x08: return Type.ARTIST_PERFORMER;
		case 0x09: return Type.CONDUCTOR;
		case 0x0A: return Type.BAND_ORCHESTRA;
		case 0x0B: return Type.COMPOSER;
		case 0x0C: return Type.LYRICIST;
		case 0x0D: return Type.RECORDING_LOCATION;
		case 0x0E: return Type.DURING_RECORDING;
		case 0x0F: return Type.DURING_PERFORMANCE;
		case 0x10: return Type.VIDEO_CAPTURE;
		case 0x11: return Type.BRIGHT_COLOURED_FISH;
		case 0x12: return Type.ILLUSTRATION;
		case 0x13: return Type.BAND_LOGO;
		case 0x14: return Type.PUBLISHER_LOGO;
		}
	}
	
}
