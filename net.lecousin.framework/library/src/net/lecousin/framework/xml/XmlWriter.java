package net.lecousin.framework.xml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;

import net.lecousin.framework.Pair;

public class XmlWriter {

    public XmlWriter() {
        super();
    }

    private StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
    private int indent = 0;
    
    public String getXML() { return xml.toString(); }
    
    private static class Tag {
        Tag(String name) { this.name = name; }
        String name;
        LinkedList<Pair<String,String>> attributes = new LinkedList<Pair<String,String>>();
        StringBuffer text = new StringBuffer();
        boolean opened = false;
    }
    
    private LinkedList<Tag> openTags = new LinkedList<Tag>();
    
    public XmlWriter openTag(String name) {
        flush();
        openTags.add(new Tag(name));
        return this;
    }
    
    public XmlWriter addAttribute(String name, String value) {
        openTags.getLast().attributes.add(new Pair<String,String>(name, value));
        return this;
    }
    public XmlWriter addAttribute(String name, long value) { return addAttribute(name, Long.toString(value)); }
    
    public XmlWriter addText(String text) {
        openTags.getLast().text.append(text);
        return this;
    }
    public XmlWriter addText(byte[] data) {
    	openTags.getLast().text.append(new String(data));
    	return this;
    }
    
    public XmlWriter closeTag() {
        Tag tag = openTags.removeLast();
        flush();
        if (!tag.opened) {
            indent();
            xml.append("<").append(tag.name);
            for (Iterator<Pair<String,String>> it = tag.attributes.iterator(); it.hasNext(); ) {
                Pair<String,String> attr = it.next();
                xml.append(" ").append(attr.getValue1()).append("=").append(XmlUtil.toAttribute(attr.getValue2()));
            }
            if (tag.text.length() == 0) {
                xml.append("/>\r\n");
                return this;
            }
            xml.append(">");
        }
        if (tag.text.length() > 0) {
        	xml.append("<![CDATA[");
        	ByteBuffer buffer = Charset.forName("UTF-8").encode(tag.text.toString());
        	xml.append(new String(buffer.array(), buffer.position(), buffer.limit()));
        	xml.append("]]>");
        	//if (tag.text.charAt(tag.text.length() - 1) != '\n')
        	//	xml.append("\r\n");
        }
        if (tag.opened)
            indent--;
        if (tag.text.length() == 0)
        	indent();
        xml.append("</").append(tag.name).append(">\r\n");
        return this;
    }
    
    private void flush() {
        for (Iterator<Tag> it = openTags.iterator(); it.hasNext(); ) {
            Tag tag = it.next();
            if (tag.opened) continue;
            tag.opened = true;
            indent();
            xml.append("<").append(tag.name);
            for (Iterator<Pair<String,String>> itAttr = tag.attributes.iterator(); itAttr.hasNext(); ) {
                Pair<String,String> attr = itAttr.next();
                xml.append(" ").append(attr.getValue1()).append("=").append(XmlUtil.toAttribute(attr.getValue2()));
            }
            xml.append(">\r\n");
            indent++;
        }
    }
    
    private void indent() {
        for (int i = 0; i < indent; ++i)
            xml.append("  ");
    }
    
    public void writeToFile(String filename) throws IOException {
    	FileOutputStream out = new FileOutputStream(filename);
    	out.write(getXML().getBytes());
    	out.flush();
    	out.close();
    }
}
