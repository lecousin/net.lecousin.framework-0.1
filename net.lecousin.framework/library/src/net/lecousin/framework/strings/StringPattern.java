package net.lecousin.framework.strings;

import java.util.LinkedList;
import java.util.List;

import net.lecousin.framework.Pair;

/**
 * Supports the following features:<ul>
 *  <li>XXX*YYY: starting with XXX, ending with YYY, with an optional string between them</li>
 *  <li>{XXX,YYY,ZZZ}: one of XXX, YYY, ZZZ</li>
 *  <li>[A-B,C]: a character between A and B, or character C</li>
 * </ul>
 * All features support the \ escape character<br>
 * Examples:<br>
 * abc*{toto,tutu}*[a-z,_,-,0-9] matches all strings:<ul>
 *  <li>starting with 'abc'</li>
 *  <li>followed by an optional string</li>
 *  <li>followed by 'toto' or 'tutu'</li>
 *  <li>followed by an optional string</li>
 *  <li>and ending with a character between a and z or _ or - or between 0 and 9</li>
 * </ul>
 */
public class StringPattern {

	public StringPattern(String pattern) {
		int i = 0;
		while (i < pattern.length()) {
			char c = pattern.charAt(i);
			if (c == '{')
				i = readList(pattern, i+1);
			else if (c == '[')
				i = readChars(pattern, i+1);
			else if (c == '*')
				i = readAnything(pattern, i+1);
			else
				i = readString(pattern, i);
		}
	}
	private int readString(String pattern, int i) {
		StringBuilder s = new StringBuilder();
		s.append(pattern.charAt(i++));
		while (i < pattern.length()) {
			char c = pattern.charAt(i);
			if (c == '{' || c == '[' || c == '*') break;
			if (c == '\\') {
				if (i+1 < pattern.length()) c = pattern.charAt(++i);
			}
			s.append(c);
			i++;
		}
		parts.add(new PatternString(s.toString()));
		return i;
	}
	private int readAnything(String pattern, int i) {
		parts.add(new PatternAnything());
		return i;
	}
	private int readChars(String pattern, int i) {
		List<String> strs = new LinkedList<String>();
		StringBuilder s = new StringBuilder();
		int j;
		for (j = i; j < pattern.length(); ++j) {
			char c  = pattern.charAt(j);
			if (c == ']') break;
			if (c == '\\') 
				c = pattern.charAt(++j);
			else if (c == ',') {
				if (s.length() > 0)
					strs.add(s.toString());
				s = new StringBuilder();
			}
			s.append(c);
		}
		i = j+1;
		if (s.length() > 0)
			strs.add(s.toString());
		PatternCharacter chars = new PatternCharacter();
		for (String str : strs) {
			if (str.length() == 1)
				chars.chars.add(str.charAt(0));
			else if (str.length() == 3 && str.charAt(1) == '-') {
				char c1 = str.charAt(0);
				char c2 = str.charAt(2);
				if (c1 > c2) {
					char c = c2;
					c2 = c1;
					c1 = c;
				}
				for (char c = c1; c <= c2; c++)
					chars.chars.add(c);
			}
		}
		parts.add(chars);
		return i;
	}
	private int readList(String pattern, int i) {
		List<String> strs = new LinkedList<String>();
		StringBuilder s = new StringBuilder();
		int j;
		for (j = i; j < pattern.length(); ++j) {
			char c  = pattern.charAt(j);
			if (c == '}') break;
			if (c == '\\') 
				c = pattern.charAt(++j);
			else if (c == ',') {
				if (s.length() > 0)
					strs.add(s.toString());
				s = new StringBuilder();
			}
			s.append(c);
		}
		i = j+1;
		if (s.length() > 0)
			strs.add(s.toString());
		PatternList list = new PatternList();
		for (String str : strs)
			list.strings.add(str);
		parts.add(list);
		return i;
	}
	
	private List<PatternPart> parts = new LinkedList<PatternPart>();
	
	private static interface PatternPart {
		
	}
	private static class PatternString implements PatternPart {
		public PatternString(String string) {
			this.string = string;
		}
		private String string;
	}
	private static class PatternAnything implements PatternPart {
		
	}
	private static class PatternList implements PatternPart {
		public PatternList() {}
		private List<String> strings = new LinkedList<String>();
	}
	private static class PatternCharacter implements PatternPart {
		public PatternCharacter() {}
		private List<Character> chars = new LinkedList<Character>();
	}
	
	public boolean matches(String s) {
		if (parts.isEmpty()) return s.length() == 0;
		return matches(s, 0, 0);
	}
	private boolean matches(String s, int i, int partIndex) {
		PatternPart p = parts.get(partIndex);
		if (p instanceof PatternAnything) {
			if (partIndex == parts.size()-1) return true;
			for (int j = i; j < s.length(); ++j)
				if (matches(s, j, partIndex+1)) return true;
			return false;
		}
		if (p instanceof PatternCharacter) {
			if (i >= s.length()) return false;
			char c = s.charAt(i);
			if (!((PatternCharacter)p).chars.contains(c)) return false;
			if (partIndex == parts.size()-1) return i == s.length()-1;
			return matches(s, i+1, partIndex+1);
		}
		if (p instanceof PatternList) {
			if (i >= s.length()) return false;
			for (String str : ((PatternList)p).strings) {
				if (i+str.length() <= s.length() && s.substring(i, i+str.length()).equals(str)) {
					if (partIndex == parts.size()-1) {
						if (i+str.length() == s.length())
							return true;
					} else
						if (matches(s, i+str.length(), partIndex+1)) return true;
				}
			}
			return false;
		}
		if (p instanceof PatternString) {
			if (i >= s.length()) return false;
			String str = ((PatternString)p).string;
			if (i+str.length() <= s.length() && s.substring(i, i+str.length()).equals(str)) {
				if (partIndex == parts.size()-1) {
					if (i+str.length() == s.length())
						return true;
				} else
					if (matches(s, i+str.length(), partIndex+1)) return true;
			}
			return false;
		}
		return false;
	}
	
	public String getString() {
		StringBuilder s = new StringBuilder();
		for (PatternPart p : parts) {
			if (p instanceof PatternAnything)
				s.append('*');
			else if (p instanceof PatternString)
				s.append(escape(((PatternString)p).string));
			else if (p instanceof PatternCharacter) {
				s.append('[');
				List<Pair<Character,Character>> ranges = new LinkedList<Pair<Character,Character>>();
				for (Character c : ((PatternCharacter)p).chars) {
					boolean found = false;
					for (Pair<Character,Character> r : ranges)
						if (c == r.getValue1()-1) {
							r.setValue1(c);
							found = true;
							break;
						} else if (c == r.getValue2()+1) {
							r.setValue2(c);
							found = true;
							break;
						}
					if (!found)
						ranges.add(new Pair<Character,Character>(c,c));
				}
				for (int i = 1; i < ranges.size(); ++i) {
					Pair<Character,Character> r = ranges.get(i);
					for (int j = 0; j < i; ++j) {
						Pair<Character,Character> r2 = ranges.get(j);
						if (r.getValue1() == r2.getValue2()+1) {
							r2.setValue2(r.getValue2());
							ranges.remove(i--);
							break;
						} else if (r.getValue2() == r2.getValue1()-1) {
							r2.setValue1(r.getValue1());
							ranges.remove(i--);
							break;
						}
					}
				}
				boolean first = true;
				for (Pair<Character,Character> r : ranges) {
					if (first) first = false; else s.append(',');
					s.append(escape(r.getValue1()));
					if (r.getValue1() != r.getValue2())
						s.append('-').append(escape(r.getValue2()));
				}
				s.append(']');
			} else {
				s.append('{');
				boolean first = true;
				for (String str : ((PatternList)p).strings) {
					if (first) first = false; else s.append(',');
					s.append(escape(str));
				}
				s.append('}');
			}
		}
		return s.toString();
	}
	private String escape(String s) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < s.length(); ++i)
			result.append(escape(s.charAt(i)));
		return result.toString();
	}
	private String escape(char c) {
		if (c == '[' || c == '{' || c == '*' || c == '-' || c == ']' || c == '}')
			return "\""+c;
		return ""+c;
	}
}
