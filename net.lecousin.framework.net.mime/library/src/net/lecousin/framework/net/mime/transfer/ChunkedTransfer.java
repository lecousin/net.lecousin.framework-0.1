package net.lecousin.framework.net.mime.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.mime.MimeHeader;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.strings.StringUtil;

public class ChunkedTransfer extends Transfer {

	public ChunkedTransfer(InputStream stream, MimeHeader header) {
		super(stream, header);
	}
	
	@Override
	public void read(OutputStream out, WorkProgress progress, int amount) throws IOException {
		int total = amount;
		int nb = 50;
		do {
			long size = readSize();
			if (Log.debug(this))
				Log.debug(this, "Chunk size read: " + size + " (" + StringUtil.toStringHex(size, 8) + ")");
			if (size == -1) break;
			if (size == 0) break;
			read(size, out, null, 0);
			if (nb > 0 && progress != null) {
				int step = total/nb--;
				total -= step;
				progress.progress(step);
			}
		} while (true);
		if (total > 0 && progress != null)
			progress.progress(total);
	}
		
	private long readSize() throws IOException {
		int pos = 0;
		long size = 0;
		do {
			int i = stream.read();
			if (pos == 0 && (i == '\r' || i == '\n')) continue;
			if (i == -1) {
				if (Log.error(this))
					Log.error(this, "End of stream reached while a chunk size is expected.");
				return -1;
			}
			if (i == ';') break;
			if ((i&0xFF) == 0x0D) break;
			int isize = StringUtil.decodeHexa((char)i);
			if (isize == -1) {
				if (Log.error(this))
					Log.error(this, "Invalid chunk size: character '" + (char)i + "' (0x" + StringUtil.toStringHex(i, 2) + ") is not a valid hexadecimal character.");
				return -1;
			}
			size = (size << 4) + isize;
			pos++;
		} while (pos < 8);
		int i;
		do {
			i = stream.read();
		} while (i != -1 && i != '\n');
		return size;
	}
}
