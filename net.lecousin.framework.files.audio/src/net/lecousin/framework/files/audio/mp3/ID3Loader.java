package net.lecousin.framework.files.audio.mp3;

import java.io.IOException;
import java.io.InputStream;

import net.lecousin.framework.io.IOUtil;

public class ID3Loader {
	
	private ID3Loader() {}

	private static final int BUFFER_SIZE = 65536;
	private static final int BUFFER_OFFSET = 128;
	public static ID3Format detect(InputStream stream) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int nb = IOUtil.readAllBuffer(stream, buffer);
		if (nb < BUFFER_OFFSET) return null;

		if (nb >= 10 && buffer[0] == 'I' && buffer[1] == 'D' && buffer[2] == '3') {
			ID3Format result = detectV2(buffer, 0, nb, stream);
			if (result != null)
				return result;
		}
		if (nb >= 128 && buffer[0] == 'T' && buffer[1] == 'A' && buffer[2] == 'G') {
			ID3Format result = detectV1(buffer, 0, nb, stream);
			if (result != null)
				return result;
		}
		
		if (nb < 2 || (buffer[0] & 0xFF) != 0xFF || (buffer[1] & 0xE0) != 0xE0)
			return null;

		int i = stream.available();
		if (i > BUFFER_OFFSET) {
			stream.skip(i - BUFFER_OFFSET);
			i = 0;
		} else
			i = BUFFER_OFFSET;
		while (true) {
			if (i > 0)
				System.arraycopy(buffer, nb - i, buffer, 0, i);
			nb = IOUtil.readAllBuffer(stream, buffer, i, BUFFER_SIZE - i);
			if (nb <= 0) { nb = i; break; }
			nb += i;
			i = BUFFER_OFFSET;
		};
		if (nb >= 128 && buffer[nb-128] == 'T' && buffer[nb-127] == 'A' && buffer[nb-126] == 'G') {
			ID3Format format = detectV1(buffer, nb-128, nb, stream);
			if (format != null)
				return format;
		}
		
		return null;
	}
	private static ID3Format detectV1(byte[] buffer, int i, int len, InputStream stream) {
		if (i > len-128) return null;
		//if (buffer[i+1] != 'A' || buffer[i+2] != 'G') return null;
		return new ID3Format_v1(buffer, i, len);
	}
	private static ID3Format detectV2(byte[] buffer, int i, int len, InputStream stream) throws IOException {
		if (i > len-10) return null;
		//if (buffer[i] != 'I' || buffer[i+1] != 'D' || buffer[i+2] != '3') return null;
		if (buffer[i+3] == 0xFF || buffer[i+4] == 0xFF) return null;
		if ((buffer[i+5] & 0x80) != 0) return null;
		if ((buffer[i+6] & 0x80) != 0 || (buffer[i+7] & 0x80) != 0 || (buffer[i+8] & 0x80) != 0 || (buffer[i+9] & 0x80) != 0) return null;
		return new ID3Format_v2(buffer, i, len,stream);
	}
	
}
