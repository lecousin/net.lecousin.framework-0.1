package net.lecousin.framework.net.mime.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.mime.MimeHeader;
import net.lecousin.framework.progress.WorkProgress;
import net.lecousin.framework.strings.StringUtil;

public abstract class Transfer {

	public Transfer(InputStream stream, MimeHeader header) {
		this.stream = stream;
		this.header = header;
	}
	
	protected InputStream stream;
	protected MimeHeader header;
	
	public abstract void read(OutputStream out, WorkProgress progress, int amount) throws IOException;
	
	protected void read(long size, OutputStream out, WorkProgress progress, int amount) throws IOException {
		int bufSize = size > 65536 ? 65536 : (int)size;
		byte[] buffer = new byte[bufSize];
		long pos = 0;
		int amountUsed = 0;
		long start = System.currentTimeMillis();
		do {
			int maxSize = size - pos > bufSize ? bufSize : (int)(size - pos);
			int nb = stream.read(buffer, 0, maxSize);
			if (nb > 0 && progress != null) {
				int posAmount = (int)((long)amount*(pos+nb)/size);
				if (posAmount > amountUsed) {
					progress.progress(posAmount-amountUsed);
					amountUsed = posAmount;
				}
				long now = System.currentTimeMillis();
				long speed = pos+nb;
				long time = (now-start)/1000;
				if (time > 0) speed /= time;
				progress.setSubDescription(StringUtil.sizeString(speed)+"/s");
			}
			if (Log.debug(this))
				Log.debug(this, "Data read: read=" + nb + ", current=" + pos + ", total=" + (pos+nb) + ", expected=" + size);
			if (nb == -1) {
				if (Log.error(this))
					Log.error(this, "End of stream reached before end of data.");
				if (progress != null)
					progress.progress(amount-amountUsed);
				return;
			}
			out.write(buffer, 0, nb);
			pos += nb;
		} while (pos < size);
		if (progress != null)
			progress.progress(amount-amountUsed);
	}
}
