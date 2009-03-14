package net.lecousin.framework.net.mime.content;

import java.io.IOException;

import net.lecousin.framework.net.mime.MimeHeader;
import net.lecousin.framework.net.mime.transfer.Transfer;
import net.lecousin.framework.progress.WorkProgress;

public class ImageContent extends DataContent {

	public ImageContent(Transfer transfer, MimeHeader header, WorkProgress progress, int amount) throws IOException {
		super(transfer, header, progress, amount);
	}
}
