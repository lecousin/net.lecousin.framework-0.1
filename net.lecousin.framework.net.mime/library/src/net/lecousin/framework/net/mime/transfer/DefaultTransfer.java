package net.lecousin.framework.net.mime.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.lecousin.framework.net.mime.MimeHeader;
import net.lecousin.framework.progress.WorkProgress;

public class DefaultTransfer extends Transfer {

	public DefaultTransfer(InputStream stream, MimeHeader header) {
		super(stream, header);
	}

	@Override
	public void read(OutputStream out, WorkProgress progress, int amount) throws IOException {
		long size = header.getContentLength();
//		if (size == -1) {
//			if (Log.error(this))
//				Log.error(this, "No content-length specified: unable to read the content.");
//			progress.progress(amount);
//			return;
//		}
		read(size, out, progress, amount);
	}

}
