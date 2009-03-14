package net.lecousin.framework.ui.eclipse.control.text.lcml.internal;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.Pair;
import net.lecousin.framework.ui.eclipse.UIUtil;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public abstract class TextSection extends Section {

	public TextSection() {
	}
	
	public abstract String getText();
	public abstract Font getFont(Composite parent);
	public abstract void configureLabel(Label label);
	
	private List<Label> labels = new LinkedList<Label>();
	
	private void refreshLabels(Composite parent, Font font, List<Pair<String,Rectangle>> expectedLabels) {
		int i;
		for (i = 0; i < expectedLabels.size(); ++i) {
			Pair<String,Rectangle> p = expectedLabels.get(i);
			if (labels.size() > i) {
				Label label = labels.get(i);
				if (!label.getText().equals(p.getValue1()))
					label.setText(p.getValue1());
				Rectangle r = label.getBounds();
				Rectangle r2 = p.getValue2();
				if (r.x != r2.x || r.y != r2.y || r.width != r2.width || r.height != r2.height)
					label.setBounds(r2);
			} else {
				Label label = UIUtil.newLabel(parent, p.getValue1());
				label.setBounds(p.getValue2());
				label.setFont(font);
				configureLabel(label);
				labels.add(label);
			}
		}
		for (; i < labels.size(); ++i) {
			labels.remove(i).dispose();
		}
	}
	
//	private int previousMaxWidth = 0;
	
	@Override
	protected void refreshSize(Composite parent, Position pos, int maxWidth, boolean updateControls) {
//		if (!labels.isEmpty()) {
//			if (previousMaxWidth == maxWidth) return;
//			if (labels.size() == 1) {
//				int w = labels.get(0).getSize().x;
//				if (maxWidth == -1 || maxWidth >= w) return;
//			}
//		}
//		previousMaxWidth = maxWidth;
		String text = getText();
		if (text.length() == 0) return;
		GC gc = new GC(parent);
		Font font = getFont(parent);
		gc.setFont(font);
		List<Pair<String,Rectangle>> expectedLabels = new LinkedList<Pair<String,Rectangle>>();
		int i = 0;
		if (maxWidth == 0) maxWidth = 1;
		if (pos.x >= maxWidth) {
			pos.x = 0;
			pos.y += pos.lineHeight;
			pos.lineHeight = 0;
		}
		do {
			Pair<Integer,Point> max = getMaxLength(text.substring(i), gc, maxWidth == -1 ? -1 : maxWidth - pos.x, pos.x == 0);
			if (max.getValue1() == 0) {
				pos.x = 0;
				pos.y += pos.lineHeight;
				pos.lineHeight = 0;
				continue;
			}
			if (max.getValue2().y > pos.lineHeight)
				pos.lineHeight = max.getValue2().y;
			expectedLabels.add(new Pair<String,Rectangle>(text.substring(i, i+max.getValue1()), new Rectangle(pos.x, pos.y, max.getValue2().x, max.getValue2().y)));
			i += max.getValue1();
			if (i >= text.length()) {
				pos.x += max.getValue2().x;
				if (pos.x > pos.width)
					pos.width = pos.x;
				break;
			}
			if (pos.x + max.getValue2().x > pos.width)
				pos.width = pos.x + max.getValue2().x; 
			pos.x = 0;
			pos.y += pos.lineHeight;
			pos.lineHeight = 0;
		} while (true);
		if (updateControls)
			refreshLabels(parent, font, expectedLabels);
	}

	private Pair<Integer,Point> getMaxLength(String text, GC gc, int width, boolean startingLine) {
		int i = text.length();
		if (width == -1)
			return new Pair<Integer,Point>(i, gc.textExtent(text));
		Point size;
		do {
			size = gc.textExtent(text.substring(0, i));
			if (size.x <= width) break;
			int ratio = size.x/width;
			// if really too big (>=200%)
			if (ratio > 5) ratio = 5;
			while (ratio-- > 1)
				i = i*75/100; // 75%
			int j=i-1;
			while (j > 0) {
				char c = text.charAt(j);
				if (isWordSeparator(c))
					break;
				j--;
			}
			if (j > 0)
				i = j;
			else
				i--;
		} while (i > 0);
		// if not enough place for one character
		if (i <= 0) {
			if (startingLine)
				return new Pair<Integer,Point>(1, size); // return only the first character
			return new Pair<Integer,Point>(0, size);
		}
		// enough place for the whole text
		if (i == text.length())
			return new Pair<Integer,Point>(i, size);
//		int j = i-1;
//		while (j > 0) {
//			char c = text.charAt(j);
//			if (isWordSeparator(c)) {
//				size = gc.textExtent(text.substring(0,j+1));
//				return new Pair<Integer,Point>(j+1, size);
//			}
//			j--;
//		}
		if (isWordSeparator(text.charAt(i)))
			return new Pair<Integer,Point>(i+1, size);
		if (!startingLine)
			return new Pair<Integer,Point>(0, null); // break line
		return new Pair<Integer,Point>(i, size);
	}
	private boolean isWordSeparator(char c) {
		return 
			c == ' ' ||
			c == '\t' ||
			c == '.' ||
			c == '-' ||
			c == '?' ||
			c == '!' ||
			c == '*' ||
			c == '/' ||
			c == '+' ||
			c == ')' ||
			c == ']' ||
			c == '}' ||
			c == '&' ||
			c == '%' ||
			c == ':' ||
			c == ';' ||
			c == ',' ||
			c == '=' ||
			c == '\\' ||
			c == '|'
				;
	}
}
