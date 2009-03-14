package net.lecousin.framework.xml.parser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class XmlNodeUtil {

	public static interface NodeSelector {
		public boolean select(XmlNode node);
	}
	public static class NodeSelectorAnd implements NodeSelector {
		public NodeSelectorAnd(NodeSelector sel1, NodeSelector sel2) { this.sel1 = sel1; this.sel2 = sel2; }
		private NodeSelector sel1, sel2;
		public boolean select(XmlNode node) {
			return sel1.select(node) && sel2.select(node);
		}
	}
	public static class NodeSelectorOr implements NodeSelector {
		public NodeSelectorOr(NodeSelector sel1, NodeSelector sel2) { this.sel1 = sel1; this.sel2 = sel2; }
		private NodeSelector sel1, sel2;
		public boolean select(XmlNode node) {
			return sel1.select(node) || sel2.select(node);
		}
	}
	public static class NodeSelectorByName implements NodeSelector {
		public NodeSelectorByName(String name) { this.name = name; }
		private String name;
		public boolean select(XmlNode node) {
			return node.getName().equals(name);
		}
	}
	public static class NodeSelectorByNameStartingWith implements NodeSelector {
		public NodeSelectorByNameStartingWith(String name) { this.name = name; }
		private String name;
		public boolean select(XmlNode node) {
			return node.getName().startsWith(name);
		}
	}
	public static class NodeSelectorByAttributeName implements NodeSelector {
		public NodeSelectorByAttributeName(String name, boolean caseSensitive) { this.name = name; this.caseSensitive = caseSensitive; }
		public NodeSelectorByAttributeName(String name) { this(name, true); }
		private String name;
		private boolean caseSensitive;
		public boolean select(XmlNode node) {
			return node.hasAttribute(name, caseSensitive);
		}
	}
	public static class NodeSelectorByAttributeValue implements NodeSelector {
		public NodeSelectorByAttributeValue(String name, String value, boolean nameCaseSensitive, boolean valueCaseSensitive) { this.name = name; this.value = value; this.nameCaseSensitive = nameCaseSensitive; this.valueCaseSensitive = valueCaseSensitive; }
		public NodeSelectorByAttributeValue(String name, String value) { this(name, value, true, true); }
		private String name;
		private String value;
		private boolean nameCaseSensitive;
		private boolean valueCaseSensitive;
		public boolean select(XmlNode node) {
			String v = node.getAttributeValue(name, nameCaseSensitive);
			return v != null && (valueCaseSensitive ? v.equals(value) : v.equalsIgnoreCase(value));
		}
	}
	public static class NodeSelectorByInnerText implements NodeSelector {
		public NodeSelectorByInnerText(String text) { this.text = text; }
		private String text;
		public boolean select(XmlNode node) {
			return node.getInnerText().equals(text);
		}
	}

	public static XmlNode getNode(List<XmlNode> nodes, NodeSelector selector, boolean searchInSubNodes) {
		for (Iterator<XmlNode> it = nodes.iterator(); it.hasNext(); ) {
			XmlNode n = it.next();
			if (selector.select(n))
				return n;
		}
		if (searchInSubNodes)
			for (Iterator<XmlNode> it = nodes.iterator(); it.hasNext(); ) {
				XmlNode n = getNode(it.next().getSubNodes(), selector, true);
				if (n != null)
					return n;
			}
		return null;
	}
	
	public static List<XmlNode> getNodes(List<XmlNode> nodes, NodeSelector selector, boolean searchInSubNodes) {
		LinkedList<XmlNode> result = new LinkedList<XmlNode>();
		for (Iterator<XmlNode> it = nodes.iterator(); it.hasNext(); ) {
			XmlNode n = it.next();
			if (selector.select(n))
				result.add(n);
		}
		if (searchInSubNodes)
			for (Iterator<XmlNode> it = nodes.iterator(); it.hasNext(); ) {
				result.addAll(getNodes(it.next().getSubNodes(), selector, true));
			}
		return result;
	}
/*	
	public static void replaceNode(StringBuilder xml, List<XmlNode> rootNodes, XmlNode toReplace, String replaceBy) {
		if (toReplace == null) return;
		replace(xml, rootNodes, toReplace.getOpeningStart(), toReplace.getClosingEnd(), replaceBy);
	}
	
	public static void replaceOpeningNode(StringBuilder xml, List<XmlNode> rootNodes, XmlNode toReplace, String replaceBy) {
		if (toReplace == null) return;
		replace(xml, rootNodes, toReplace.getOpeningStart(), toReplace.getOpeningEnd(), replaceBy);
	}
	
	public static void replaceClosingNode(StringBuilder xml, List<XmlNode> rootNodes, XmlNode toReplace, String replaceBy) {
		if (toReplace == null) return;
		replace(xml, rootNodes, toReplace.getClosingStart(), toReplace.getClosingEnd(), replaceBy);
	}
	
	public static void replace(StringBuilder s, List<XmlNode> rootNodes, int start, int  end, String replaceBy) {
		s.replace(start, end, replaceBy);
		if (replaceBy.length() != (end - start))
			moveNodes(rootNodes, start, replaceBy.length() - (end - start));
	}
	
	public static void moveNodes(List<XmlNode> nodes, int start, int move) {
		for (Iterator<XmlNode> it = nodes.iterator(); it.hasNext(); ) {
			XmlNode n = it.next();
			if (n.getOpeningStart() >= start)
				n.setOpeningStart(n.getOpeningStart() + move);
			if (n.getOpeningEnd() >= start)
				n.setOpeningEnd(n.getOpeningEnd() + move);
			if (n.getClosingStart() >= start)
				n.setClosingStart(n.getClosingStart() + move);
			if (n.getClosingEnd() >= start)
				n.setClosingEnd(n.getClosingEnd() + move);
			moveNodes(n.getSubNodes(), start, move);
		}
	}*/
}
