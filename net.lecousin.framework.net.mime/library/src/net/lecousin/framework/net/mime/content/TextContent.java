package net.lecousin.framework.net.mime.content;

import java.io.IOException;
import java.io.OutputStream;

import net.lecousin.framework.io.MyByteArrayOutputStream;
import net.lecousin.framework.net.mime.MimeHeader;
import net.lecousin.framework.net.mime.transfer.Transfer;
import net.lecousin.framework.progress.WorkProgress;

public class TextContent extends MimeContent {

	public TextContent(Transfer transfer, MimeHeader header, WorkProgress progress, int amount) throws IOException {
		MyByteArrayOutputStream out = new MyByteArrayOutputStream();
		transfer.read(out, progress, amount);
		text = new String(out.getBuffer(), 0, out.getBufferSize());
	}
	
	private String text;
	
	public String getText() { return text; }
	
	@Override
	public String getAsString() {
		return text;
	}
	
	@Override
	public void write(OutputStream out) throws IOException {
		out.write(text.getBytes());
	}
}
