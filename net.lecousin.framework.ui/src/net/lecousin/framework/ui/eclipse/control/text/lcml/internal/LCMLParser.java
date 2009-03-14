package net.lecousin.framework.ui.eclipse.control.text.lcml.internal;

import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.log.Log;
import net.lecousin.framework.strings.StringUtil;
import net.lecousin.framework.xml.XmlParsingUtil;
import net.lecousin.framework.xml.XmlUtil;
import net.lecousin.framework.xml.XmlParsingUtil.Node;

public class LCMLParser {

	public static Paragraph parse(String ml) {
		Paragraph p = new Paragraph();
		parse(p, ml, 0, new Style());
		return p;
	}
	
	private static class Style {
		int bold = 0;
		int italic = 0;
		String link = null;
		void put(String text, Paragraph p) {
			String[] lines = StringUtil.splitLines(text);
			boolean first = true;
			for (String line : lines) {
				line = XmlUtil.decodeXML(line);
				if (first) first = false;
				else p.add(new BreakLine());
				if (link != null)
					p.add(new Link(line, link));
				else
					p.add(new Text(line, bold > 0, italic > 0));
			}
		}
	}
	
	private static int parse(Paragraph p, String ml, int i, Style style) {
		do {
			int j = ml.indexOf('<', i);
			if (j < 0) {
				style.put(ml.substring(i), p);
				break;
			}
			style.put(ml.substring(i, j), p);
			Pair<String,Integer> close = XmlParsingUtil.isClosingNode(ml, j);
			if (close != null) {
				i = close.getValue2();
				if (!handleCloseNode(close.getValue1(), style))
					return i;
				continue;
			}
			Triple<Node,Boolean,Integer> open = XmlParsingUtil.parseOpenNode(ml, j);
			i = open.getValue3();
			Node node = open.getValue1();
			if (node == null) {
				if (Log.warning(LCMLParser.class))
					Log.warning(LCMLParser.class, "Invalid node at " + j + ". Text=" + ml);
				continue;
			}
			i = handleNode(node, open.getValue2(), p, ml, i, style);
		} while (i < ml.length());
		return i;
	}
	
	private static int handleNode(Node node, boolean nodeClosed, Paragraph p, String ml, int pos, Style style) {
		if (node.name.equals("b"))
			return handleBold(nodeClosed, p, ml, pos, style);
		if (node.name.equals("i"))
			return handleItalic(nodeClosed, p, ml, pos, style);
		if (node.name.equals("a"))
			return handleLink(node, nodeClosed, p, ml, pos, style);
		if (node.name.equals("br"))
			return handleBreakLine(node, nodeClosed, p, ml, pos, style);
		if (node.name.equals("p"))
			return handleParagraph(node, nodeClosed, p, ml, pos, style);
		if (Log.warning(LCMLParser.class))
			Log.warning(LCMLParser.class, "Unknown tag '" + node.name + "': ignored.");
		return pos;
	}
	private static boolean handleCloseNode(String name, Style style) {
		if (name.equals("b"))
			return handleCloseBold(style);
		if (name.equals("i"))
			return handleCloseItalic(style);
		if (name.equals("a"))
			return handleCloseLink(style);
		if (name.equals("br"))
			return handleCloseBreakLine(style);
		if (name.equals("p"))
			return handleCloseParagraph(style);
		return true;
	}
	
	private static int handleBold(boolean closed, Paragraph p, String ml, int pos, Style style) {
		if (closed) return pos;
		style.bold++;
		return pos;
	}
	private static boolean handleCloseBold(Style style) {
		if (style.bold > 0)
			style.bold--;
		return true;
	}

	private static int handleItalic(boolean closed, Paragraph p, String ml, int pos, Style style) {
		if (closed) return pos;
		style.italic++;
		return pos;
	}
	private static boolean handleCloseItalic(Style style) {
		if (style.italic > 0)
			style.italic--;
		return true;
	}

	private static int handleLink(Node node, boolean closed, Paragraph p, String ml, int pos, Style style) {
		style.link = null;
		if (closed) return pos;
		String ref = node.attributes.get("href");
		if (ref == null || ref.length() == 0) return pos;
		style.link = ref;
		return pos;
	}
	private static boolean handleCloseLink(Style style) {
		style.link = null;
		return true;
	}

	private static int handleBreakLine(Node node, boolean closed, Paragraph p, String ml, int pos, Style style) {
		p.add(new BreakLine());
		return pos;
	}
	private static boolean handleCloseBreakLine(Style style) {
		return true;
	}


	private static int handleParagraph(Node node, boolean closed, Paragraph p, String ml, int pos, Style style) {
		Paragraph newP = new Paragraph();
		return parse(newP, ml, pos, new Style());
	}
	private static boolean handleCloseParagraph(Style style) {
		return false;
	}
}
