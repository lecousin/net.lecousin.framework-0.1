package net.lecousin.framework.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.lecousin.framework.log.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public abstract class XmlUtil {

	private static DocumentBuilder docBuilder = null;
	public static Element loadFile(File file) 
	throws ParserConfigurationException, SAXException, IOException {
	    return loadFile(new FileInputStream(file));
	}
	public static synchronized Element loadFile(InputStream stream) 
	throws ParserConfigurationException, SAXException, IOException {
		if (stream.available() == 0)
			return null;
	    if (docBuilder == null)
	      docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    Document doc = docBuilder.parse(stream);
	    if (doc == null) return null;
	    return doc.getDocumentElement();
	}
	
	public static Element parse(String xml)
	throws ParserConfigurationException, SAXException, IOException {
		return loadFile(new ByteArrayInputStream(xml.getBytes()));
	}
	  
	public static Node get_child(Node parent, String child_name) {
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i)
		{
			Node child = children.item(i);
			if (child.getNodeName().equals(child_name))
				return child;
		}
		return null;
	}
	public static Element get_child_element(Element e, String child_name) {
		return (Element)get_child(e, child_name);
	}
	
	public static List<? extends Node> get_childs(Node parent, String child_name) {
		List<Node> result = new LinkedList<Node>();
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i)
		{
			Node child = children.item(i);
			if (child.getNodeName().equals(child_name))
				result.add(child);
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public static List<Element> get_childs_element(Element parent, String child_name) {
		return (List<Element>)get_childs(parent, child_name);
	}
	
	public static Element get_child_with_attr(Node parent, String child_name, String attr_name, String attr_value) {
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i)
		{
			Node child = children.item(i);
			if (child.getNodeName().equals(child_name)) {
				if (child instanceof Element) {
					Element e = (Element)child;
					if (e.getAttribute(attr_name).equals(attr_value))
						return e;
				}
			}
		}
		return null;
	}
	
	public static Node search_node_by_attribute(Node root, String attr_name, String attr_value)
	{
		if (root instanceof Element)
		{
			if (((Element)root).hasAttribute(attr_name) &&
				((Element)root).getAttribute(attr_name).equals(attr_value))
				return root;
		}
		NodeList list = root.getChildNodes();
		for (int i = 0; i < list.getLength(); ++i)
		{
			Node child = list.item(i);
			Node ret = search_node_by_attribute(child, attr_name, attr_value);
			if (ret != null)
				return ret;
		}
		return null;
	}

	public static Collection<Node> search_nodes_by_attribute(Node root, String attr_name, String attr_value)
	{
		Collection<Node> result = new LinkedList<Node>();
		if (root instanceof Element)
		{
			if (((Element)root).hasAttribute(attr_name) &&
				((Element)root).getAttribute(attr_name).equals(attr_value))
				result.add(root);
		}
		NodeList list = root.getChildNodes();
		for (int i = 0; i < list.getLength(); ++i)
		{
			Node child = list.item(i);
			Collection<Node> ret = search_nodes_by_attribute(child, attr_name, attr_value);
			result.addAll(ret);
		}
		return result;
	}
	
	public static Collection<Node> search_nodes_by_name(Node root, String name)
	{
		Collection<Node> result = new LinkedList<Node>();
		if (root.getNodeName().equals(name))
			result.add(root);
		NodeList list = root.getChildNodes();
		for (int i = 0; i < list.getLength(); ++i)
		{
			Node child = list.item(i);
			Collection<Node> ret = search_nodes_by_name(child, name);
			result.addAll(ret);
		}
		return result;
	}
	
	public static String get_inner_text(Node node)
	{
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i)
		{
			Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE)
                return child.getNodeValue();
            if (child instanceof Text)
                return ((Text)child).getData();
			/*if (child instanceof TextNode)
				return ((TextNode)child).getData();*/
		}
		return null;
	}
    
    public static String toAttribute(String value) {
        return "\'" + encodeXML(value) + "\'";
    }
    
    public static String encodeXML(String value) {
    	if (value == null) return "";
    	StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (c >= 32 && c < 128)
	            switch (c) {
	            case '\'': result.append("&apos;"); break;
	            case '\"': result.append("&quot;"); break;
	            case '<': result.append("&lt;"); break;
	            case '>': result.append("&gt;"); break;
	            case '&': result.append("&amp;"); break;
	            default: result.append(c);
	            }
            else if (c != 0)
            	result.append("&#").append(Integer.toString(c)).append(';');
        }
        return result.toString();
    }
    
    public static String decodeXML(String value) {
    	StringBuilder str = new StringBuilder();
    	int i = 0;
    	do {
    		int j = value.indexOf('&', i);
    		if (j < 0) {
    			str.append(value.substring(i));
    			break;
    		}
    		if (j > i)
    			str.append(value.substring(i, j));
    		if (j >= value.length()-2) break;
    		int k = value.indexOf(';', j+1);
    		if (k < 0) { i = j+1; if (Log.warning(XmlUtil.class)) Log.warning(XmlUtil.class, "invalid XML string: a & has been encountered without a closing ;"); continue; }
			i = k+1;
    		String special = value.substring(j+1, k);
    		if (special.length() == 0) { if (Log.warning(XmlUtil.class)) Log.warning(XmlUtil.class, "invalid XML string: &; is illegal");continue; }
    		if (special.charAt(0) == '#') {
    			special = special.substring(1);
    			int radix = 10;
    			if (special.startsWith("x")) {
    				special = special.substring(1);
    				radix=16;
    			}
    			int ascii;
    			try { ascii = Integer.parseInt(special, radix); }
    			catch (NumberFormatException e) {
    				if (Log.warning(XmlUtil.class)) Log.warning(XmlUtil.class, "invalid XML string: between &# and the closing ; it must be a valid number, '" + special + "' has been found");
    				continue; 
    			}
    			str.append((char)ascii);
    			continue;
    		}
    		if (special.equalsIgnoreCase("apos"))
    			str.append('\'');
    		else if (special.equalsIgnoreCase("quot"))
    			str.append('\"');
    		else if (special.equalsIgnoreCase("lt"))
    			str.append('<');
    		else if (special.equalsIgnoreCase("gt"))
    			str.append('>');
    		else if (special.equalsIgnoreCase("amp"))
    			str.append('&');
    		else if (special.equalsIgnoreCase("eacute"))
    			str.append('é');
    		else if (special.equalsIgnoreCase("egrave"))
    			str.append('è');
    		else if (special.equalsIgnoreCase("ugrave"))
    			str.append('ù');
    		else if (special.equalsIgnoreCase("ecirc"))
    			str.append('ê');
    		else if (special.equalsIgnoreCase("icirc"))
    			str.append('î');
    		else if (special.equalsIgnoreCase("acirc"))
    			str.append('â');
    		else if (special.equalsIgnoreCase("ocirc"))
    			str.append('ô');
    		else if (special.equalsIgnoreCase("ucirc"))
    			str.append('û');
    		else if (special.equalsIgnoreCase("agrave"))
    			str.append('à');
    		else if (special.equalsIgnoreCase("ccedil"))
    			str.append('ç');
    		else if (special.equalsIgnoreCase("oelig"))
    			str.append("oe");
    		else if (special.equalsIgnoreCase("copy"))
    			str.append("(c)");
    		else if (special.equalsIgnoreCase("nbsp"))
    			str.append(' ');
    		// TODO continue with http://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references
    		else
    			if (Log.warning(XmlUtil.class)) Log.warning(XmlUtil.class, "invalid XML string: &" + special + "; is not a known special character.");
    	} while (i < value.length());
    	return str.toString();
    }
}
