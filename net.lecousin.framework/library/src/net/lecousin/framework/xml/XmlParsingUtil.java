package net.lecousin.framework.xml;

import java.util.HashMap;
import java.util.Map;

import net.lecousin.framework.Pair;
import net.lecousin.framework.Triple;
import net.lecousin.framework.log.Log;

public class XmlParsingUtil {

	private XmlParsingUtil() {}
	
	/** Return the parsed Node, a boolean indicating if the node is closed, and the index in the string just after the node */
	public static Triple<Node,Boolean,Integer> parseOpenNode(String s, int pos) {
		if (s.charAt(pos) != '<') {
			error("Invalid string on parseOpenNode: the string must starts with <");
			return new Triple<Node,Boolean,Integer>(null, true, pos);
		}
		char c;
		int i = pos+1;
		StringBuilder name = new StringBuilder();
		while (i < s.length()) {
			c = s.charAt(i++);
			if (c == '>')
				return new Triple<Node,Boolean,Integer>(new Node(name.toString()), false, i);
			if (c == '/') {
				i = skipSpaces(s, i);
				if (i >= s.length())
					return new Triple<Node,Boolean,Integer>(new Node(name.toString()), true, i);
				c = s.charAt(i++);
				if (c == '>')
					return new Triple<Node,Boolean,Integer>(new Node(name.toString()), true, i);
				i--;
				continue;
			}
			if (isSpace(c))
				break;
			name.append(c);
		}
		Node node = new Node(name.toString());
		i = skipSpaces(s, i);
		if (i >= s.length()) return new Triple<Node,Boolean,Integer>(node, false, i);
		c = s.charAt(i);
		if (c == '>')
			return new Triple<Node,Boolean,Integer>(node, false, i+1);
		if (c == '/') {
			i = skipSpaces(s, i+1);
			if (i >= s.length())
				return new Triple<Node,Boolean,Integer>(node, false, i);
			c = s.charAt(i);
			if (c == '>')
				return new Triple<Node,Boolean,Integer>(node, true, i+1);
			return new Triple<Node,Boolean,Integer>(node, false, i);
		}
		Pair<Integer,Boolean> p = readAttributes(node, s, i);
		return new Triple<Node,Boolean,Integer>(node, p.getValue2(), p.getValue1());
	}
	
	/** Return null if not a closing node, else the name with the position after the closing node */
	public static Pair<String,Integer> isClosingNode(String s, int pos) {
		if (s.charAt(pos) != '<') {
			error("Invalid string on parseOpenNode: the string must starts with <");
			return null;
		}
		int i = skipSpaces(s, pos+1);
		if (i >= s.length()) return null;
		char c = s.charAt(i);
		if (c != '/') return null;
		if (i >= s.length()) return new Pair<String,Integer>("",i);
		int j = s.indexOf('>', i+1);
		if (j < 0) return null;
		return new Pair<String,Integer>(s.substring(i+1,j).trim(), j+1);
	}
	
	private static boolean isSpace(char c) {
		return c == ' ' || c == '\n' || c == '\r' || c == '\t';
	}
	private static int skipSpaces(String s, int i) {
		while (i < s.length() && isSpace(s.charAt(i))) i++;
		return i;
	}
	
	private static Pair<Integer,Boolean> readAttributes(Node node, String s, int i) {
		StringBuilder name = new StringBuilder();
		char c;
		while (i < s.length()) {
			c = s.charAt(i++);
			if (c == '>') {
				if (name.length() > 0)
					node.attributes.put(name.toString().toLowerCase(), "");
				return new Pair<Integer,Boolean>(i, false);
			}
			if (c == '/') {
				if (name.length() > 0)
					node.attributes.put(name.toString().toLowerCase(), "");
				i = skipSpaces(s, i);
				if (i >= s.length())
					return new Pair<Integer,Boolean>(i, true);
				c = s.charAt(i);
				if (c == '>')
					return new Pair<Integer,Boolean>(i+1, true);
				continue;
			}
			if (c == '=') {
				Pair<String,Integer> value = readAttributeValue(s, i);
				i = value.getValue2();
				node.attributes.put(name.toString().toLowerCase(),value.getValue1());
				break;
			}
			if (isSpace(c)) {
				if (name.length() > 0)
					node.attributes.put(name.toString().toLowerCase(), "");
				break;
			}
			name.append(c);
		}
		i = skipSpaces(s, i);
		if (i >= s.length()) return new Pair<Integer,Boolean>(i, false);
		c = s.charAt(i);
		if (c == '>') return new Pair<Integer,Boolean>(i+1, false);
		if (c == '/') {
			i = skipSpaces(s, i+1);
			if (i >= s.length()) return new Pair<Integer,Boolean>(i, true);
			c = s.charAt(i);
			if (c == '>') return new Pair<Integer,Boolean>(i+1, false);
		}
		return readAttributes(node, s, i);
	}
	
	private static Pair<String,Integer> readAttributeValue(String s, int pos) {
		if (pos >= s.length()) return new Pair<String,Integer>("",pos);
		char c = s.charAt(pos);
		char quot = 0;
		if (c == '\'' || c == '\"') {
			quot = c;
			if (pos == s.length() - 1) return new Pair<String,Integer>("",pos+1);
			c = s.charAt(++pos);
		}
		StringBuilder value = new StringBuilder();
		do {
			if (quot != 0 && c == quot)
				return new Pair<String,Integer>(value.toString(), pos+1);
			if (c == '\\' && quot != 0) {
				if (pos == s.length()-1)
					return new Pair<String,Integer>(value.toString(), pos+1);
				c = s.charAt(++pos);
			}
			if (quot == 0 && (isSpace(c) || c=='>' || (c=='/' && pos < s.length()-1 && s.charAt(pos+1) == '>')))
				return new Pair<String,Integer>(value.toString(), pos);
			value.append(c);
			if (pos == s.length()-1)
				return new Pair<String,Integer>(value.toString(), pos+1);
			c = s.charAt(++pos);
		} while (true);
	}
	
	private static void error(String message) {
		if (Log.error(XmlParsingUtil.class))
			Log.error(XmlParsingUtil.class, message);
	}
	
	public static class Node {
		public Node(String name) { this.name = name; }
		public String name;
		public Map<String,String> attributes = new HashMap<String,String>();
	}

	/** Return &lt;Node,InnerText,EndPos&gt; or null if no node with given name has been found */
	public static Triple<Node,String,Integer> readNextNode(String name, String text, int pos) {
		do {
			int i = text.indexOf('<', pos);
			if (i < 0) break;
			Pair<String,Integer> close = isClosingNode(text, i);
			if (close != null) {
				pos = close.getValue2();
				continue;
			}
			Triple<Node,Boolean,Integer> open = parseOpenNode(text, i);
			if (!open.getValue1().name.equalsIgnoreCase(name)) {
				pos = open.getValue3();
				continue;
			}
			if (open.getValue2())
				return new Triple<Node,String,Integer>(open.getValue1(), "", open.getValue3());
			Pair<Integer,Integer> closing = goToClosingNode(name, text, open.getValue3());
			if (closing == null)
				return new Triple<Node,String,Integer>(open.getValue1(), text.substring(open.getValue3()), text.length());
			return new Triple<Node,String,Integer>(open.getValue1(), text.substring(open.getValue3(), closing.getValue1()), closing.getValue2());
		} while (pos < text.length());
		return null;
	}
	
	/** Return <StartPos,EndPos> of the given closing node, or null if it has not been found */
	public static Pair<Integer,Integer> goToClosingNode(String name, String text, int pos) {
		do {
			int i = text.indexOf('<', pos);
			if (i < 0) break;
			Pair<String,Integer> close = isClosingNode(text, i);
			if (close != null) {
				if (close.getValue1().equalsIgnoreCase(name))
					return new Pair<Integer,Integer>(i, close.getValue2());
				pos = close.getValue2();
				continue;
			}
			Triple<Node,Boolean,Integer> open = parseOpenNode(text, i);
			if (!open.getValue1().name.equalsIgnoreCase(name)) {
				pos = open.getValue3();
				continue;
			}
			if (open.getValue2()) {
				pos = open.getValue3();
				continue;
			}
			Pair<Integer,Integer> closing = goToClosingNode(name, text, open.getValue3());
			if (closing == null)
				return null;
			pos = closing.getValue2();
		} while (pos < text.length());
		return null;
	}
}
