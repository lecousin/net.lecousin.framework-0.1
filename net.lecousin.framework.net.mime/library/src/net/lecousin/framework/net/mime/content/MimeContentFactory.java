package net.lecousin.framework.net.mime.content;

import java.io.IOException;

import net.lecousin.framework.log.Log;
import net.lecousin.framework.net.mime.MimeHeader;
import net.lecousin.framework.net.mime.transfer.Transfer;
import net.lecousin.framework.progress.WorkProgress;

public class MimeContentFactory {

	private MimeContentFactory() {}
	
	public static MimeContent create(Transfer transfer, MimeHeader header, WorkProgress progress, int amount) throws IOException {
		String type = header.getContentType();
		if (type == null) {
			if (Log.error(MimeContentFactory.class))
				Log.error(MimeContentFactory.class, "No content-type: unable to read the content.");
			progress.progress(amount);
			return null;
		}
		int i = type.indexOf(';');
		if (i > 0) type = type.substring(0, i);
		type = type.trim();
		i = type.indexOf('/');
		String mainType, subType;
		if (i > 0) {
			mainType = type.substring(0,i);
			subType = type.substring(i+1);
		} else {
			mainType = type;
			subType = "";
		}
		if (mainType.equalsIgnoreCase("text"))
			return new TextContent(transfer, header, progress, amount);
		if (mainType.equalsIgnoreCase("image"))
			return new ImageContent(transfer, header, progress, amount);
		if (mainType.equalsIgnoreCase("application"))
			return new ApplicationContent(transfer, header, progress, amount);
		if (Log.error(MimeContentFactory.class))
			Log.error(MimeContentFactory.class, "Content type '" + type + "' not supported.");
		progress.progress(amount);
		return null;
	}
	
}
