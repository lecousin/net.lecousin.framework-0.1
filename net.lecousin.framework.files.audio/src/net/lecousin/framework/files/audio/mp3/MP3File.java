package net.lecousin.framework.files.audio.mp3;

import java.io.IOException;
import java.net.URI;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import net.lecousin.framework.Pair;
import net.lecousin.framework.files.FileType;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.audio.AudioFile;
import net.lecousin.framework.io.IOUtil;
import net.lecousin.framework.io.LCMovableInputStream;
import net.lecousin.framework.log.Log;

public class MP3File extends AudioFile {

	public static MP3File detect(URI uri, LCMovableInputStream stream) {
		byte[] buf = new byte[128];
		int nb;
		try { nb = IOUtil.readAllBuffer(stream, buf); }
		catch (IOException e) { return null; }
		if (nb < 3) return null;

		ID3Format id3 = null;
		long startPos = 0;
		
		if (nb > 10 &&
			buf[0] == 'I' && buf[1] == 'D' && buf[2] == '3' &&
			buf[3] != 0xFF && buf[4] != 0xFF &&
			(buf[5] & 0x80) == 0 &&
			(buf[6] & 0x80) == 0 && (buf[7] & 0x80) == 0 && (buf[8] & 0x80) == 0 && (buf[9] & 0x80) == 0) {
			try {
				Pair<ID3Format_v2,Long> p = ID3Format_v2.create(buf, nb, stream);
				id3 = p.getValue1();
				startPos = p.getValue2();
			} catch (IOException e) {
			}
		} else if (nb == 128 && 
				buf[0] == 'T' && buf[1] == 'A' && buf[2] == 'G') {
			id3 = new ID3Format_v1(buf, 0, nb);
			startPos = 128;
		} else {
			try {
				nb = stream.readLastBytes(buf);
				if (nb == 128 && 
					buf[0] == 'T' && buf[1] == 'A' && buf[2] == 'G') {
					id3 = new ID3Format_v1(buf, 0, nb);
					startPos = 0;
				}
			} catch (IOException e) {}
		}
		
		if (id3 == null)
			id3 = new ID3Format_None();
		if (id3.getDuration() <= 0) {
			try {
				stream.move(startPos);
				if (stream.read() != 0xFF) return null;
				if ((stream.read() & 0xE0) != 0xE0) return null;
				if ((stream.read() & 0xF0) == 0xF0) return null;
				stream.move(startPos);
				Bitstream bs = new Bitstream(stream, true, true);
				Header h = bs.readFrame();
				if (h == null) return null;
				if (h != null)
					id3.setDuration((long)h.total_ms((int)stream.getSize()));
			} catch (BitstreamException e) {
				if (Log.error(MP3File.class))
					Log.error(MP3File.class, "Unable to read first MP3 frame to determine track duration", e);
			} catch (IOException e) {
				if (Log.error(MP3File.class))
					Log.error(MP3File.class, "Unable to read first MP3 frame to determine track duration", e);
			}
		}
		
		return new MP3File(uri, id3);
	}
	
	private MP3File(URI uri, ID3Format id3) {
		super(uri, FILE_TYPE, id3);
	}
	
	public static FileType FILE_TYPE;
	public static final String FILE_TYPE_NAME = "mp3";
	
	public static void registerTypes(FileTypeRegistry registry) {
		FILE_TYPE = registry.register(AudioFile.FILE_TYPE, FILE_TYPE_NAME);
	}

}
