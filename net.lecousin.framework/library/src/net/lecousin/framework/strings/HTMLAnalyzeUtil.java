package net.lecousin.framework.strings;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

public class HTMLAnalyzeUtil {

	public static Pair<String,Integer> getSection(String page, String start, String end, int startPos) {
		int i = page.indexOf(start, startPos);
		if (i < 0) return null;
		int j = page.indexOf(end, i+start.length());
		if (j < 0) return null;
		return new Pair<String,Integer>(page.substring(i+start.length(), j), j+end.length());
	}
	
	public static String getInfoBoldFrom(String page, String startTag) {
		int i = page.indexOf(startTag);
		if (i < 0) return null;
		i = page.indexOf("<b>", i);
		if (i < 0) return null;
		int j = page.indexOf("</b>", i);
		if (j < 0) return null;
		return cleanInfo(page.substring(i+3, j), false);
	}
	
	public static String cleanInfo(String info, boolean keepStyle) {
		int i = 0;
		StringBuilder str = new StringBuilder();
		do {
			int j = info.indexOf('<', i);
			if (j < 0) {
				str.append(info.substring(i));
				break;
			}
			str.append(info.substring(i, j));
			Pair<String,Integer> close = XmlParsingUtil.isClosingNode(info, j);
			if (close != null) {
				String name = close.getValue1();
				i = close.getValue2();
				if (keepStyle && isStyleNode(name))
					str.append(info.substring(j, i));
				continue;
			}
			Triple<Node,Boolean,Integer> open = XmlParsingUtil.parseOpenNode(info, j);
			if (open.getValue1() == null)
				break;
			String name = open.getValue1().name;
			i = open.getValue3();
			if (keepStyle && isStyleNode(name)) {
				str.append(info.substring(j, i));
				continue;
			}
		} while (i < info.length()-1);
		return str.toString();
	}
	public static boolean isStyleNode(String name) {
		return
			name.equals("b") ||
			name.equals("i") ||
			name.equals("br");
	}
	
	public static Pair<String,String> getInfoLinkedFrom(String page, String startTag) {
		int i = page.indexOf(startTag);
		if (i < 0) return null;
		Pair<Pair<String,String>,Integer> p = getInfoLinked(page, i);
		return p != null ? p.getValue1() : null;
	}
	
	/** <name,url>,endPos */
	public static Pair<Pair<String,String>,Integer> getInfoLinked(String page, int i) {
		i = page.indexOf("<a", i);
		if (i < 0) return null;
		int end = page.indexOf("</a>", i);
		if (end < 0) return null;
		int hrefStart = page.indexOf("href=\"", i);
		if (hrefStart < 0 || hrefStart > end) return null;
		int hrefEnd = page.indexOf('\"', hrefStart+6);
		if (hrefEnd < 0 || hrefEnd > end) return null;
		String url = page.substring(hrefStart+6, hrefEnd);
		int start = page.indexOf('>', hrefEnd);
		if (start < 0 || start > end) return null;
		String name = page.substring(start+1, end);
		return new Pair<Pair<String,String>,Integer>(new Pair<String,String>(name, url), end+4);
	}
	
	public static List<Pair<String,String>> getInfoLinkedList(String page, String startTag, String endTag) {
		int i = page.indexOf(startTag);
		if (i < 0) return null;
		int j = page.indexOf(endTag, i+startTag.length());
		if (j < 0) return null;
		String section = page.substring(i + startTag.length(), j);
		List<Pair<String,String>> list = new LinkedList<Pair<String,String>>();
		i = 0;
		do {
			Pair<Pair<String,String>,Integer> p = getInfoLinked(section, i);
			if (p != null) {
				i = p.getValue2();
				list.add(p.getValue1());
			} else
				break;
		} while (true);
		return list;
	}
	
	public static String[] getColumns(String text) {
		List<String> result = new LinkedList<String>();
		int i = 0;
		do {
			Triple<Node,String,Integer> node = XmlParsingUtil.readNextNode("td", text, i);
			if (node == null) break;
			result.add(node.getValue2());
			i = node.getValue3();
		} while (true);
		return result.toArray(new String[result.size()]);
	}
	
	public static String removeAllTags(String text) {
		return removeAllTags(text, false);
	}
	public static String removeAllTags(String text, boolean keepStyle) {
		StringBuilder str = new StringBuilder();
		int i = 0;
		do {
			int j = text.indexOf('<', i);
			if (j < 0) {
				str.append(text.substring(i));
				break;
			}
			if (j > i)
				str.append(text.substring(i, j));
			Pair<String,Integer> close = XmlParsingUtil.isClosingNode(text, j);
			if (close != null) {
				if (keepStyle && isStyleNode(close.getValue1()))
					str.append(text.substring(j, close.getValue2()));
				i = close.getValue2();
				continue;
			}
			Triple<Node,Boolean,Integer> open = XmlParsingUtil.parseOpenNode(text, j);
			if (keepStyle && isStyle(open.getValue1().name))
				str.append(text.substring(j, open.getValue3()));
			i = open.getValue3();
		} while (i < text.length());
		return str.toString();
	}
	
	public static boolean isStyle(String tagName) {
		return 
			tagName.equalsIgnoreCase("b") ||
			tagName.equalsIgnoreCase("i");
		
	}
	
}
