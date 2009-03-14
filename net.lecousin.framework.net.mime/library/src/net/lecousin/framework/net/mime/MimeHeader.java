package net.lecousin.framework.net.mime;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lecousin.framework.io.IOUtil;

public class MimeHeader {

	private Map<String,List<String>> fields = new HashMap<String,List<String>>();
	
	public MimeHeader(InputStream in) throws IOException {
		do {
			String line = IOUtil.readPart(in, "\r\n");
			if (line.length() == 0) break;
			parseLine(line);
		} while (true);
	}

	private void parseLine(String line) {
		int i = line.indexOf(':');
		String name = line.substring(0,i).trim().toLowerCase();
		String value = line.substring(i+1).trim();
		List<String> list = fields.get(name);
		if (list == null) {
			list = new LinkedList<String>();
			fields.put(name, list);
		}
		list.add(value);
	}
	
	public Set<Map.Entry<String,List<String>>> getFields() { return fields.entrySet(); }
	public List<String> getField(String name) { return fields.get(name.toLowerCase()); }
	public String getUniqueField(String name) { List<String> list = getField(name); if (list == null || list.isEmpty()) return null; return list.get(0); }
	
	public String getTransferEncoding() {
		List<String> list = fields.get("transfer-encoding");
		if (list == null) return null;
		return list.get(0);
	}
	public String getContentType() {
		List<String> list = fields.get("content-type");
		if (list == null) return null;
		return list.get(0);
	}
	public long getContentLength() {
		List<String> list = fields.get("content-length");
		if (list == null) return -1;
		String str = list.get(0);
		return Long.parseLong(str);
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String,List<String>> e : fields.entrySet()) {
			for (String s : e.getValue()) {
				if (first) first = false;
				else str.append("\r\n");
				str.append(e.getKey()).append(": ").append(s);
			}
		}
		return str.toString();
	}
}
