package net.lecousin.framework.files.audio.wav;

import java.io.IOException;
import java.util.List;

import net.lecousin.framework.files.audio.AudioFileInfo;
import net.lecousin.framework.io.IOUtil;
import net.lecousin.framework.io.LCMovableInputStream;

public class WAVFormat implements AudioFileInfo {

	public static WAVFormat read(LCMovableInputStream stream) {
		byte[] buf = new byte[0x0C+0x1A];
		try {
			if (stream.read(buf) != 0x0C+0x1A) return null;
			if (buf[0x00] != 'R' || buf[0x01] != 'I' || buf[0x02] != 'F' || buf[0x03] != 'F') return null;
			if (buf[0x08] != 'W' || buf[0x09] != 'A' || buf[0x0A] != 'V' || buf[0x0B] != 'E') return null;
			if (buf[0x0C] != 'f' || buf[0x0D] != 'm' || buf[0x0E] != 't' || buf[0x0F] != ' ') return null;
			WAVFormat wav = new WAVFormat();
			//long sampleRate = IOUtil.readLongIntel(buf, 0x18);
			wav.compression = IOUtil.readShortIntel(buf, 0x14);
			long byteRate = IOUtil.readLongIntel(buf, 0x1C);
			if (wav.compression != COMP_PCM) {
				short extraSize = IOUtil.readShortIntel(buf, 0x24);
				int pos = buf.length+extraSize;
				if ((pos%4) != 0)
					extraSize += 4-(pos%4);
				stream.skip(extraSize);
			} else {
				stream.move(0x24);
			}
			String type = null;
			long size = 0;
			long dataPos = stream.getPosition();
			do {
				if (stream.read(buf, 0, 8) != 8) break;
				type = new String(buf, 0, 4);
				size = IOUtil.readLongIntel(buf, 4);
				if (type.equals("data")) break;
				stream.skip(size);
				dataPos = stream.getPosition();
			} while (true);
			if (type != null && type.equals("data")) {
				wav.length = size*1000/byteRate;
				wav.dataChunkPos = dataPos;
			}
			return wav;
		} catch (IOException e) { return null; }
	}
	private WAVFormat() {
	}
	
	private long length = -1;
	private short compression = 0;
	private long dataChunkPos = -1;
	
	public short getCompression() { return compression; }
	public static final short COMP_UNKNOWN 		= 0x0000;
	public static final short COMP_PCM 			= 0x0001;
	public static final short COMP_MS_ADPCM		= 0x0002;
	public static final short COMP_ITUG711_ALAW = 0x0006;
	public static final short COMP_ITUG711_AULAW= 0x0007;
	public static final short COMP_IMA_ADPCM	= 0x0011;
	public static final short COMP_ITUG723_ADPCM= 0x0016;
	public static final short COMP_GSM_610		= 0x0031;
	public static final short COMP_ITUG721_ADPCM= 0x0040;
	public static final short COMP_MPEG			= 0x0050;
	public static final short COMP_MP3 			= 0x0055;
	public long getDataChunkPos() { return dataChunkPos; }
	
	public int getTrackNumber() { return -1; }
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
