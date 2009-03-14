package net.lecousin.framework.xml.parser;

import java.util.Iterator;
import java.util.List;

public class XmlDocument {

	public XmlDocument(StringBuilder xml) {
		resetContent(xml);
	}
	
	private StringBuilder xml;
	private List<XmlNode> root;
	
	public StringBuilder getSource() { return xml; }
	public void resetContent(StringBuilder xml) {
		this.xml = xml;
		this.root = XmlOpenParser.parse(xml);
	}
	
	public XmlNode getRootNode(String name) {
		for (Iterator<XmlNode> it = root.iterator(); it.hasNext(); ) {
			XmlNode n = it.next();
			if (n.getName().equals(name)) return n;
		}
		return null;
	}
	
	public XmlNode getNode(String name, boolean searchInSubNodes) {
		return XmlNodeUtil.getNode(root, new XmlNodeUtil.NodeSelectorByName(name), searchInSubNodes);
	}
	public XmlNode getNodeWithAttribute(String name, String attrName, String attrValue, boolean searchInSubNodes) {
		return XmlNodeUtil.getNode(root, 
				new XmlNodeUtil.NodeSelectorAnd(
					new XmlNodeUtil.NodeSelectorByName(name),
					new XmlNodeUtil.NodeSelectorByAttributeValue(attrName, attrValue)
				),
				searchInSubNodes);
	}
	public XmlNode searchNode(XmlNodeUtil.NodeSelector selector) {
		return XmlNodeUtil.getNode(root, selector, true);
	}
	public List<XmlNode> getNodes(String name, boolean searchInSubNodes) {
		return XmlNodeUtil.getNodes(root, new XmlNodeUtil.NodeSelectorByName(name), searchInSubNodes);
	}
	public List<XmlNode> getNodesStartingWith(String start, boolean searchInSubNodes) {
		return XmlNodeUtil.getNodes(root, new XmlNodeUtil.NodeSelectorByNameStartingWith(start), searchInSubNodes);
	}
	public List<XmlNode> getNodes(XmlNodeUtil.NodeSelector selector, boolean searchInSubNodes) {
		return XmlNodeUtil.getNodes(root, selector, searchInSubNodes);
	}
	
	public void rename(XmlNode node, String newName) {
		node.setName(newName);
		replace(xml, node.getOpeningStart(), node.getOpeningEnd(), node.generateOpeningTag());
		if (node.getClosingStart() != node.getClosingEnd())
			replace(xml, node.getClosingStart(), node.getClosingEnd(), node.generateClosingTag());
	}
	
	public void setAttributes(XmlNode node, String attributes) {
		node.getAttributes().clear();
		node.getAttributes().putAll(XmlOpenParser.parseAttributes(attributes));
		replace(xml, node.getOpeningStart(), node.getOpeningEnd(), node.generateOpeningTag());
	}
	
	public void addAttribute(XmlNode node, String attrName, String attrValue) {
		node.getAttributes().put(attrName, attrValue);
		replace(xml, node.getOpeningStart(), node.getOpeningEnd(), node.generateOpeningTag());
	}
	
	public XmlNode createNode(XmlNode parent, String name, String attributes) {
		XmlNode node = new XmlNode(parent, name, XmlNode.Type.NORMAL);
		node.getAttributes().putAll(XmlOpenParser.parseAttributes(attributes));
		return node;
	}
	
	public void addSubNode(XmlNode node, XmlNode subnode) {
		setNodePosition(subnode, node.getClosingStart());
		replace(xml, node.getClosingStart(), node.getClosingStart(), subnode.generateNode());
		node.addSubNode(subnode);
	}
	
	public void replaceNode(XmlNode oldNode, XmlNode newNode) {
		setNodePosition(newNode, oldNode.getOpeningStart());
		replace(xml, oldNode.getOpeningStart(), oldNode.getClosingEnd(), newNode.generateNode());
		replaceNodeInParent(oldNode, newNode);
	}
	
	public void removeNode(XmlNode node) {
		replace(xml, node.getOpeningStart(), node.getClosingEnd(), "");
		removeNodeInParent(node);
	}
	
	private void replaceNodeInParent(XmlNode oldNode, XmlNode newNode) {
		for (Iterator<XmlNode> it = root.iterator(); it.hasNext(); ) {
			XmlNode n = it.next();
			if (n == oldNode) {
				root.remove(n);
				root.add(newNode);
				return;
			}
			if (replaceNodeInParent(oldNode, newNode, n)) return;
		}
	}
	private boolean replaceNodeInParent(XmlNode oldNode, XmlNode newNode, XmlNode parent) {
		for (Iterator<XmlNode> it = parent.getSubNodes().iterator(); it.hasNext(); ) {
			XmlNode n = it.next();
			if (n == oldNode) {
				parent.getSubNodes().remove(n);
				parent.getSubNodes().add(newNode);
				return true;
			}
			if (replaceNodeInParent(oldNode, newNode, n)) return true;
		}
		return false;
	}
	private void removeNodeInParent(XmlNode node) {
		for (Iterator<XmlNode> it = root.iterator(); it.hasNext(); ) {
			XmlNode n = it.next();
			if (n == node) {
				root.remove(n);
				return;
			}
			if (removeNodeInParent(node, n)) return;
		}
	}
	private boolean removeNodeInParent(XmlNode node, XmlNode parent) {
		for (Iterator<XmlNode> it = parent.getSubNodes().iterator(); it.hasNext(); ) {
			XmlNode n = it.next();
			if (n == node) {
				parent.getSubNodes().remove(n);
				return true;
			}
			if (removeNodeInParent(node, n)) return true;
		}
		return false;
	}
	
	private void setNodePosition(XmlNode node, int start) {
		node.setOpeningStart(start);
		node.setOpeningEnd(node.getOpeningStart() + node.generateOpeningTag().length());
		node.setClosingStart(node.getOpeningEnd() + node.getInnerText().length());
		node.setClosingEnd(node.getOpeningEnd() + node.getInnerText().length() + node.generateClosingTag().length());
	}

	private void replace(StringBuilder s, int start, int  end, String replaceBy) {
		if (start == end)
			s.insert(start, replaceBy);
		else
			s.replace(start, end, replaceBy);
		replaceInInnerText(root, start, end, replaceBy);
		if (replaceBy.length() != (end - start))
			moveNodes(root, start, replaceBy.length() - (end - start));
	}
	private void replaceInInnerText(List<XmlNode> nodes, int start, int end, String replaceBy) {
		for (Iterator<XmlNode> it = nodes.iterator(); it.hasNext(); ) {
			XmlNode n = it.next();
			if (n.getOpeningEnd() >= end || n.getClosingStart() < start || n.getOpeningEnd() > start) continue;
			n.setInnerText(new StringBuilder(n.getInnerText()).replace(start - n.getOpeningEnd(), end - n.getOpeningEnd(), replaceBy).toString());
			replaceInInnerText(n.getSubNodes(), start, end, replaceBy);
		}
	}
	
	private void moveNodes(List<XmlNode> nodes, int start, int move) {
		for (Iterator<XmlNode> it = nodes.iterator(); it.hasNext(); ) {
			XmlNode n = it.next();
			if (n.getOpeningStart() >= start)
				n.setOpeningStart(n.getOpeningStart() + move);
			if (n.getOpeningEnd() > start)
				n.setOpeningEnd(n.getOpeningEnd() + move);
			if (n.getClosingStart() >= start)
				n.setClosingStart(n.getClosingStart() + move);
			if (n.getClosingEnd() > start)
				n.setClosingEnd(n.getClosingEnd() + move);
			moveNodes(n.getSubNodes(), start, move);
		}
	}
	
}
