package net.lecousin.framework.xml.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lecousin.framework.Pair;
import net.lecousin.framework.strings.StringUtil;

public class XmlOpenParser {

	public static List<XmlNode> parse(CharSequence input) {
		//long start = System.currentTimeMillis();
		List<XmlNode> result = parseSubNodes(null, input, 0, input.length()).subnodesParsed;
		//long end = System.currentTimeMillis();
		//System.out.println("XmlOpenParser: stream of " + input.length() + " bytes parsed in " + (end - start) + "ms.");
		return result;
	}
	
	private static class EndFound {
		EndFound(XmlNode nodeClosed, int closingStart, int closingEnd, List<XmlNode> subnodesParsed)
		{ this.nodeClosed = nodeClosed; this.closingStart = closingStart; this.closingEnd = closingEnd; this.subnodesParsed = subnodesParsed; }
		XmlNode nodeClosed;
		int closingStart;
		int closingEnd;
		List<XmlNode> subnodesParsed;
	}
	
	private static EndFound parseSubNodes(List<XmlNode> nodesToClose, CharSequence input, int start, int end) {
		List<XmlNode> result = new LinkedList<XmlNode>();
		for (int i = start; i < end; ) {
			if (input.charAt(i) == '<') {
				if (i < end-1 && input.charAt(i+1) == '/') {
					int j = i+2;
					boolean space = false;
					XmlNode found = null;
					while (j < end) {
						char c = input.charAt(j);
						if (isSpace(c)) {
							if (nodesToClose != null) {
								for (Iterator<XmlNode> it = nodesToClose.iterator(); found == null && it.hasNext(); ) {
									XmlNode toClose = it.next();
									if (StringUtil.equals(toClose.getName(), input, i+2, j))
										found = toClose;
								}
							}
							space = true;
						} else if (c == '>') {
							if (!space) {
								if (nodesToClose != null) {
									for (Iterator<XmlNode> it = nodesToClose.iterator(); found == null && it.hasNext(); ) {
										XmlNode toClose = it.next();
										if (StringUtil.equals(toClose.getName(), input, i+2, j))
											found = toClose;
									}
								}
							}
							if (found != null)
								return new EndFound(found, i, j+1, result);
							j++;
							break;
						}
						j++;
					}
					i = j;
				} else {
					Pair<XmlNode,EndFound> subnode = parseNode(input, i, end, nodesToClose);
					if (subnode == null) { // invalid opening character
						i++;
						continue;
					}
					result.add(subnode.getValue1());
					if (subnode.getValue2() != null) {
						subnode.getValue2().subnodesParsed.addAll(0, result);
						return subnode.getValue2();
					}
					i = subnode.getValue1().getClosingEnd();
				}
			} else
				i++;
		}
		return new EndFound(null, -1, -1, result); // no ending tag
	}
	
	private static Pair<XmlNode,EndFound> parseNode(CharSequence input, int start, int end, List<XmlNode> nodesToClose) {
		XmlNode node = new XmlNode();
		node.setOpeningStart(start);
		if (start >= end-1) {
			node.setOpeningEnd(end);
			node.setClosingStart(end);
			node.setClosingEnd(end);
			return new Pair<XmlNode, EndFound>(node, null);
		}
		if (input.charAt(start+1) == '/') { // must not be there except at the root level
			int j = start+2;
			boolean space = false;
			while (j < end) {
				char c = input.charAt(j);
				if (isSpace(c)) {
					node.setName(input.subSequence(start+2, j).toString());
					space = true;
				} else if (c == '>') {
					if (!space)
						node.setName(input.subSequence(start+2, j).toString());
					j++;
					break;
				}
				j++;
			}
			node.setOpeningEnd(start);
			node.setClosingStart(start);
			node.setClosingEnd(j);
			node.setType(XmlNode.Type.ONLY_CLOSE);
			return new Pair<XmlNode, EndFound>(node, null);
		}
		if (nodesToClose == null)
			nodesToClose = new LinkedList<XmlNode>();
		for (int i = start + 1; i < end; ++i) {
			char c = input.charAt(i);
			if (isSpace(c)) {
				if (i == start+1) return null; // invalid opening character
				node.setName(input.subSequence(start + 1, i).toString());
				if (node.getName().charAt(0) == '!') {
					if (node.getName().equals("!--")) {
						i = goTo(input, i+1, end, "-->", false);
						node.setType(XmlNode.Type.COMMENT);
					} else {
						i = goTo(input, i+1, end, ">", true);
						node.setType(XmlNode.Type.DOC_INFO);
					}
					node.setClosingStart(i+1);
					node.setClosingEnd(i+1);
					return new Pair<XmlNode, EndFound>(node, null);
				}
				i = readAttributes(node, node.getAttributes(), input, i + 1, end);
				if (node.getClosingStart() != -1) return new Pair<XmlNode, EndFound>(node, null);
				nodesToClose.add(0,node);
				EndFound subnodes = parseSubNodes(nodesToClose, input, i, end);
				nodesToClose.remove(0);
				if (subnodes.nodeClosed == node) {
					node.addSubNodes(subnodes.subnodesParsed);
					node.setClosingStart(subnodes.closingStart);
					node.setClosingEnd(subnodes.closingEnd);
					node.setType(XmlNode.Type.NORMAL);
					node.setInnerText(input.subSequence(i, node.getClosingStart()).toString());
					return new Pair<XmlNode,EndFound>(node, null);
				}
				node.setClosingStart(node.getOpeningEnd());
				node.setClosingEnd(node.getOpeningEnd());
				node.setType(XmlNode.Type.ONLY_OPEN);
				//System.err.println("XmlOpenParser: Warning: tag " + node.getName() + " is open and never closed.");
				return new Pair<XmlNode,EndFound>(node, subnodes);
			} else if (c == '>') {
				node.setName(input.subSequence(start + 1, i).toString());
				node.setOpeningEnd(i + 1);

				nodesToClose.add(0,node);
				EndFound subnodes = parseSubNodes(nodesToClose, input, i+1, end);
				nodesToClose.remove(0);
				if (subnodes.nodeClosed == node) {
					node.addSubNodes(subnodes.subnodesParsed);
					node.setClosingStart(subnodes.closingStart);
					node.setClosingEnd(subnodes.closingEnd);
					node.setType(XmlNode.Type.NORMAL);
					node.setInnerText(input.subSequence(i+1, node.getClosingStart()).toString());
					return new Pair<XmlNode,EndFound>(node, null);
				}
				node.setClosingStart(node.getOpeningEnd());
				node.setClosingEnd(node.getOpeningEnd());
				node.setType(XmlNode.Type.ONLY_OPEN);
				//System.err.println("XmlOpenParser: Warning: tag " + node.getName() + " is open and never closed.");
				return new Pair<XmlNode,EndFound>(node, subnodes);
			} else if (c == '/' && i < end-1 && input.charAt(i+1) == '>') {
				node.setName(input.subSequence(start + 1, i).toString());
				node.setType(XmlNode.Type.NORMAL);
				node.setOpeningEnd(i + 2);
				node.setClosingStart(i + 2);
				node.setClosingEnd(i + 2);
				return new Pair<XmlNode, EndFound>(node, null);
			}
		}
		node.setOpeningEnd(end);
		node.setClosingStart(end);
		node.setClosingEnd(end);
		return new Pair<XmlNode, EndFound>(node, null);
	}
	
	public static int goTo(CharSequence input, int start, int end, String toFind, boolean manageQuoted) {
		char quote = ' ';
		for (int i = start; i < end - toFind.length() + 1; ++i) {
			char c = input.charAt(i);
			if (manageQuoted) {
				if (quote == ' ') {
					if (c == '\'' || c == '\"') { quote = c; continue; }
				} else {
					if (c == '\\') { i++; continue; }
					if (c == quote) { quote = ' '; continue; }
				}
			}
			if (c == toFind.charAt(0)) {
				if (StringUtil.equals(toFind, input, i, i + toFind.length()))
					return i + toFind.length();
			}
		}
		return end;
	}
	
	public static Map<String,String> parseAttributes(CharSequence input) {
		return parseAttributes(input, 0, input.length());
	}
	
	public static Map<String,String> parseAttributes(CharSequence input, int start, int end) {
		Map<String,String> result = new HashMap<String,String>();
		readAttributes(null, result, input, start, end);
		return result;
	}
	
	private static int readAttributes(XmlNode node, Map<String,String> attributes, CharSequence input, int start, int end) {
		int i = start;
		while (i < end && isSpace(input.charAt(i))) i++;
		if (i == end) {
			if (node != null) {
				node.setOpeningEnd(end);
				node.setClosingStart(end);
				node.setClosingEnd(end);
			}
			return end;
		}
		int startName = i;
		while (i < end) {
			char c = input.charAt(i);
			if (isSpace(c)) {
				attributes.put(input.subSequence(startName, i).toString(), "");
				return readAttributes(node, attributes, input, i+1, end);
			}
			if (c == '=') {
				String name = input.subSequence(startName, i).toString();
				return readAttributeValue(node, attributes, name, input, i+1, end);
			}
			if (c == '>' || (i < end-1 && c == '/' && input.charAt(i+1) == '>')) {
				if (i > startName)
					attributes.put(input.subSequence(startName, i).toString(), "");
				boolean isClosing = c == '/';
				if (node != null) {
					if (isClosing) {
						node.setClosingStart(i + 2);
						node.setClosingEnd(i + 2);
					}
					node.setOpeningEnd(isClosing ? i + 2 : i + 1);
					return node.getOpeningEnd();
				}
				return isClosing ? i + 2 : i + 1;
			}
			i++;
		}
		attributes.put(input.subSequence(startName, end).toString(), "");
		return end;
	}
	
	private static int readAttributeValue(XmlNode node, Map<String,String> attributes, String attrName, CharSequence input, int start, int end) {
		if (start >= end) {
			attributes.put(attrName, "");
			if (node != null)
				node.setOpeningEnd(end);
			return end;
		}
		char c = input.charAt(start);
		if (c=='"' || c=='\'')
			return readAttributeQuotedValue(node, attributes, attrName, input, start+1, end, c);
		for (int i = start; i < end; ++i) {
			c = input.charAt(i);
			if (isSpace(c)) {
				attributes.put(attrName, input.subSequence(start, i).toString());
				return readAttributes(node, attributes, input, i+1, end);
			}
			if (c == '>' || (i < end-1 && c == '/' && input.charAt(i+1) == '>')) {
				attributes.put(attrName, input.subSequence(start, i).toString());
				boolean isClosing = input.charAt(i) == '/';
				if (node != null) {
					node.setOpeningEnd(isClosing ? i+2 : i+1);
					if (isClosing) {
						node.setClosingStart(i+2);
						node.setClosingEnd(i+2);
					}
					return node.getOpeningEnd();
				}
				return isClosing ? i+2 : i+1;
			}
		}
		attributes.put(attrName, input.subSequence(start, end).toString());
		return end;
	}
	
	private static int readAttributeQuotedValue(XmlNode node, Map<String,String> attributes, String attrName, CharSequence input, int start, int end, char quote) {
		if (start>= end) {
			attributes.put(attrName, "");
			return end;
		}
		StringBuilder value = new StringBuilder();
		for (int i = start; i < end; ++i) {
			char c = input.charAt(i);
			if (c == '\\' && i < end-1) {
				c = input.charAt(i+1);
				switch (c) {
				case '\'':
				case '\"':
				case '\\':
					value.append(c);
					i++;
				default:
					value.append('\\');
				}
				continue;
			}
			if (c == quote) {
				attributes.put(attrName, value.toString());
				return readAttributes(node, attributes, input, i+1, end);
			}
			value.append(c);
		}
		attributes.put(attrName, value.toString());
		if (node != null) {
			node.setOpeningEnd(end);
			node.setClosingStart(end);
			node.setClosingEnd(end);
		}
		return end;
	}	
	/*
	private static void setNodeEnd(XmlNode node, CharSequence input, int end) {
		XmlNode lsn = node.getLastSubNode();
		int i = lsn != null ? lsn.getClosingEnd() : node.getOpeningEnd();
		while (i < end) {
			if (input.charAt(i) == '<') {
				if (i < end-1 && input.charAt(i+1) == '/') {
					int j = i+2;
					boolean space = false, found = false;
					while (j < end) {
						if (isSpace(input.charAt(j))) {
							if (input.subSequence(i+2, j).toString().equals(node.getName()))
								found = true;
							space = true;
						} else if (input.charAt(j) == '>') {
							if (!space) {
								if (input.subSequence(i+2, j).toString().equals(node.getName()))
									found = true;
							}
							if (found) {
								node.setClosingStart(i);
								node.setClosingEnd(j+1);
								node.setType(XmlNode.Type.NORMAL);
								return;
							}
							i = j+1;
							break;
						}
						j++;
					}
					break;
				} else i++;
			} else
				i++;
		}
		node.setClosingStart(end);
		node.setClosingEnd(end);
		node.setType(XmlNode.Type.ONLY_OPEN);
	}*/
	
	private static boolean isSpace(char c) {
		return c == ' ' || c == '\t' || c == '\r' || c == '\n';
	}
}
