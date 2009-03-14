package net.lecousin.framework.xml.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class XmlNode {

	public XmlNode(XmlNode parent, String name, Type type) {
		this.parent = parent;
		this.name = name;
		this.type = type;
	}
	public XmlNode(XmlNode parent) {
		this(parent, "", Type.UNKNOWN);
	}
	
	public enum Type {
		NORMAL,
		ONLY_OPEN,
		ONLY_CLOSE,
		DOC_INFO, // starting with ! (like !DOCTYPE)
		COMMENT,
		UNKNOWN
	}
	
	private XmlNode parent;
	private Type type;
	private String name;
	private Map<String,String> attributes = new HashMap<String,String>();
	private List<XmlNode> subNodes = new LinkedList<XmlNode>();
	private String innerText = "";
	private int openingStart = -1;
	private int openingEnd = -1;
	private int closingStart = -1;
	private int closingEnd = -1;
	
	@Override
	public String toString() { return generateNode(); }
	
	public XmlNode getParent() { return parent; }
	
	public Type getType() { return type; }
	public void setType(Type type) { this.type = type; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public int getOpeningStart() { return openingStart; }
	public void setOpeningStart(int start) { this.openingStart = start; }
	public int getOpeningEnd() { return openingEnd; }
	public void setOpeningEnd(int end) { this.openingEnd = end; }
	
	public int getClosingStart() { return closingStart; }
	public void setClosingStart(int start) { this.closingStart = start; }
	public int getClosingEnd() { return closingEnd; }
	public void setClosingEnd(int end) { this.closingEnd = end; }
	
	public String getInnerText() { return innerText; }
	public void setInnerText(String text) { this.innerText = text; }
	
	public List<XmlNode> getSubNodes() { return subNodes; }
	public void addSubNode(XmlNode node) { subNodes.add(node); }
	public void addSubNodes(List<XmlNode> nodes) { subNodes.addAll(nodes); }
	public XmlNode getLastSubNode() {
		XmlNode node = null;
		for (Iterator<XmlNode> it = subNodes.iterator(); it.hasNext(); ) {
			XmlNode n = it.next();
			if (node == null || n.getClosingEnd() > node.getClosingEnd())
				node = n;
		}
		return node;
	}
	public List<XmlNode> getSubNodes(String name) { return getSubNodes(name, false); }
	public List<XmlNode> getSubNodes(String name, boolean searchInSubNodes) { return XmlNodeUtil.getNodes(subNodes, new XmlNodeUtil.NodeSelectorByName(name), searchInSubNodes); }
	
	public boolean hasAttribute(String name) { return attributes.containsKey(name); }
	public boolean hasAttribute(String name, boolean caseSensitive) {
		if (caseSensitive)
			return attributes.containsKey(name);
		return getAttributeValue(name, false) != null;
	}
	public String getAttributeValue(String name) { return attributes.get(name); }
	public String getAttributeValue(String name, boolean caseSensitive) {
		if (caseSensitive)
			return attributes.get(name);
		for (Iterator<Map.Entry<String, String>> it = attributes.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, String> e = it.next();
			if (e.getKey().equalsIgnoreCase(name))
				return e.getValue();
		}
		return null;
	}
	public Map<String,String> getAttributes() { return attributes; }
	public void addAttribute(String name, String value) { attributes.put(name, value); }
	
	public XmlNode getSubNode(String name, boolean searchInSubNodes) {
		return XmlNodeUtil.getNode(subNodes, new XmlNodeUtil.NodeSelectorByName(name), searchInSubNodes);
	}
	public XmlNode getSubNode(String name) {
		return getSubNode(name, false);
	}
	public List<XmlNode> getNodesStartingWith(String start, boolean searchInSubNodes) {
		return XmlNodeUtil.getNodes(subNodes, new XmlNodeUtil.NodeSelectorByNameStartingWith(start), searchInSubNodes);
	}

	public String generateOpeningTag() {
		StringBuilder s = new StringBuilder();
		s.append("<").append(name);
		for (Iterator<Map.Entry<String,String>> it = attributes.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, String> e = it.next();
			s.append(" ").append(generateAttribute(e.getKey(), e.getValue()));
		}
		if (innerText.length() == 0)
			s.append("/>");
		else
			s.append(">");
		return s.toString();
	}
	public String generateClosingTag() {
		if (closingStart == closingEnd || innerText.length() == 0) return "";
		return new StringBuilder("</").append(name).append(">").toString();
	}
	public String generateAttribute(String name, String value) {
		value = value.replace("\\", "\\\\");
		value = value.replace("\"", "\\\"");
		return new StringBuilder().append(name).append("=\"").append(value).append("\"").toString();
	}
	public String generateNode() { 
		return new StringBuilder().append(generateOpeningTag()).append(innerText).append(generateClosingTag()).toString(); 
	}
}
