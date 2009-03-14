package net.lecousin.framework.net.mime.transfer;

import java.io.InputStream;

import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.mime.MimeHeader;

public class TransferEncodingFactory {

	private TransferEncodingFactory() {}
	
	public static Transfer create(InputStream stream, MimeHeader header) {
		String encoding = header.getTransferEncoding();
		if (encoding == null)
			return new DefaultTransfer(stream, header);
		if (encoding.equalsIgnoreCase("chunked"))
			return new ChunkedTransfer(stream, header);
		if (Log.error(TransferEncodingFactory.class))
			Log.error(TransferEncodingFactory.class, "Transfer encoding '" + encoding + "' not supported.");
		return null;
	}
	
}
