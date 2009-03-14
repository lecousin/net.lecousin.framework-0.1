package net.lecousin.framework.net.mime.content;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.lecousin.framework.net.mime.MimeHeader;
import net.lecousin.framework.net.mime.transfer.Transfer;
import net.lecousin.framework.progress.WorkProgress;

public class DataContent extends MimeContent {

	public DataContent(Transfer transfer, MimeHeader header, WorkProgress progress, int amount) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		transfer.read(out, progress, amount);
		data = out.toByteArray();
	}
	
	private byte[] data;
	
	public byte[] getData() { return data; }
	
	@Override
	public String getAsString() {
		return new String(data);
	}
	
	@Override
	public void write(OutputStream out) throws IOException {
		out.write(data);
	}
}
