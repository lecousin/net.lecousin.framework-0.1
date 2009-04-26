package net.lecousin.framework.net.mime;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lecousin.framework.Pair;
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
	
	public Pair<String,Map<String,String>> decodeField(String field) {
		Map<String,String> m = new HashMap<String,String>();
		int i = field.indexOf(';');
		String value;
		if (i < 0)
			value = field;
		else
			value = field.substring(0, i).trim();
		while (i >= 0) {
			int j = field.indexOf(';', i+1);
			String s = j > 0 ? field.substring(i+1, j) : field.substring(i+1);
			i = j < 0 ? -1 : j+1;
			j = s.indexOf('=');
			if (j < 0)
				m.put(s.trim(), "");
			else
				m.put(s.substring(0,j).trim().toLowerCase(), s.substring(j+1).trim().toLowerCase());
		}
		return new Pair<String,Map<String,String>>(value, m);
	}
	
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
	public String getCharSet() {
		String ct = getContentType();
		Pair<String,Map<String,String>> p = decodeField(ct);
		String cs = p.getValue2().get("charset");
		if (cs == null) cs = "ISO-8859-1";
		return cs;
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
