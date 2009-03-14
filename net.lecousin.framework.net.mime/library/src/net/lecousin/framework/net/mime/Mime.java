package net.lecousin.framework.net.mime;

import java.io.IOException;
import java.io.InputStream;

import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.mime.content.MimeContent;
import net.lecousin.framework.net.mime.content.MimeContentFactory;
import net.lecousin.framework.net.mime.transfer.Transfer;
import net.lecousin.framework.net.mime.transfer.TransferEncodingFactory;
import net.lecousin.framework.progress.WorkProgress;

public class Mime {

	private MimeHeader header = null;
	private MimeContent content = null;
	
	public Mime(InputStream in, WorkProgress progress, int amount) throws IOException {
		header = new MimeHeader(in);
		if (Log.debug(this)) 
			Log.debug(this, "Header read:\r\n" + header.toString());
		Transfer transfer = TransferEncodingFactory.create(in, header);
		content = MimeContentFactory.create(transfer, header, progress, amount);
		if (Log.debug(this))
			Log.debug(this, "Content read: " + (content != null ? content.getClass().getName() : "null"));
	}
	
	public MimeHeader getHeader() { return header; }
	public MimeContent getContent() { return content; }
	
	@Override
	public String toString() {
		return header == null ? "" : header.toString() + "\r\n" + (content != null ? content.toString() : "");
	}
}
