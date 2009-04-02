/**
 * Project:  GDev Eclipse Application
 * Package:  net.lecousin.framework.basics
 * Filename: StringUtils.java
 * Created on 18 déc. 2005 12:39:32
 * Author: Guillaume LE COUSIN
 */
package net.lecousin.framework.strings;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import net.lecousin.framework.math.RangeInteger;
import net.lecousin.framework.time.DateTimeUtil;

/**
 * @author Guillaume LE COUSIN
 *
 */
public class StringUtil
{
	private StringUtil() { /* instantiation not allowed */ }
	
  public static boolean contains_one_of(String str, String characters)
  {
    for (int i = 0; i < characters.length(); ++i)
      if (str.indexOf(characters.charAt(i)) >= 0)
        return true;
    return false;
  }
  
  public static Collection<String> split(String str, String sep)
  {
    LinkedList<String> ret = new LinkedList<String>();
    int i = 0, j;
    while ((j = str.indexOf(sep, i)) >= 0)
    {
      String s = j == i ? "" : str.substring(i, j);
      ret.add(s);
      i = j + sep.length();
    }
    if (i < str.length())
      ret.add(str.substring(i));
    return ret;
  }
  public static String[] splitArray(String str, String sep)
  {
	  Collection<String> col = split(str, sep);
	  return col.toArray(new String[col.size()]);
  }
  
  public static String[] splitLines(String str) {
	  LinkedList<String> lines = new LinkedList<String>();
	  splitLines(str, 0, lines);
	  return lines.toArray(new String[lines.size()]);
  }
  private static void splitLines(String str, int pos, LinkedList<String> lines) {
	  if (pos >= str.length()) {
		  lines.add("");
		  return;
	  }
	  int i = str.indexOf('\n', pos);
	  if (i < 0) {
		  lines.add(str.substring(pos));
		  return;
	  }
	  if (i > pos && str.charAt(i-1) == '\r') {
		  lines.add(str.substring(pos, i-1));
		  splitLines(str, i+1, lines);
		  return;
	  }
	  lines.add(str.substring(pos, i));
	  splitLines(str, i+1, lines);
  }
  
  public static String make_list(Collection<String> strs, String sep)
  {
    String ret = "";
    for (Iterator<String> it = strs.iterator(); it.hasNext(); )
    {
      if (ret.length() > 0)
        ret += sep;
      ret += it.next();
    }
    return ret;
  }
  
  public static boolean equals(CharSequence s1, CharSequence s2, int s2Start, int s2End) {
	  return equals(s1, 0, s1.length(), s2, s2Start, s2End);
  }
  public static boolean equals(CharSequence s1, int start1, int end1, CharSequence s2, int start2, int end2) {
	  if (end1-start1 != end2-start2) return false;
	  for (int i1 = start1, i2 = start2; i1 < end1; ++i1, ++i2) {
		  if (s1.charAt(i1) != s2.charAt(i2)) return false;
	  }
	  return true;
  }
  
  public static String toString(int value, int nbDigits) {
	  StringBuilder str = new StringBuilder();
	  str.append(value);
	  while (str.length() < nbDigits)
		  str.insert(0, '0');
	  return str.toString();
  }
  public static String toStringHex(long value, int nbDigits) {
	  long v = value >= 0 ? value : -value;
	  StringBuilder str = new StringBuilder(Long.toHexString(v));
	  while (str.length() < nbDigits)
		  str.insert(0, '0');
	  if (value < 0)
		  str.insert(0, '-');
	  return str.toString();
  }
  public static String toStringSep(long value) {
	  StringBuilder str = new StringBuilder();
	  long v = value >= 0 ? value : -value;
	  str.append(v%10);
	  long mul=10;
	  int dig = 1;
	  while (v >= mul) {
		  if ((dig % 3)==0)
			  str.insert(0, '.');
		  str.insert(0, (v/mul)%10);
		  mul *= 10;
		  dig++;
	  }
	  if (value < 0) str.insert(0,'-');
	  return str.toString();
  }
  public static String toStringSep(double value, int nbDigitsAfterComma) {
	  long v = (long)value;
	  StringBuilder str = new StringBuilder(toStringSep(v));
	  if (v<0)v=-v;
	  if (nbDigitsAfterComma > 0) {
		  str.append(',');
		  long mul = 10;
		  for (int i = 0; i < nbDigitsAfterComma; ++i) {
			  str.append(((long)(v*mul))%10);
			  mul *= 10;
		  }
	  }
	  return str.toString();
  }
  
  public static long LongValueOf(String s) throws NumberFormatException {
	  Collection<String> strs = split(s, ".");
	  long value = 0;
	  for (String str : strs) {
		  long v = Long.valueOf(str);
		  value *= 1000;
		  value += v;
	  }
	  return value;
  }
  
  public static double DoubleValueOf(String s) throws NumberFormatException {
	  int i = s.indexOf(',');
	  String ls = i > 0 ? s.substring(0, i) : s;
	  String cs = i > 0 ? s.substring(i+1) : null;
	  long lv = LongValueOf(ls);
	  if (cs == null) return (double)lv;
	  long cv = Long.valueOf(cs);
	  double div = 1;
	  for (i = 0; i < cs.length(); ++i) div *= 10;
	  return ((double)lv)+(((double)cv)/div);
  }
  
  public static int compareAdvanced(String s1, String s2) {
	  try {
		  long v1 = LongValueOf(s1);
		  long v2 = LongValueOf(s2);
		  return v1 < v2 ? -1 : v1 == v2 ? 0 : 1;
	  } catch (NumberFormatException e) {}
	  try {
		  double v1 = DoubleValueOf(s1);
		  double v2 = DoubleValueOf(s2);
		  return v1 < v2 ? -1 : v1 == v2 ? 0 : 1;
	  } catch (NumberFormatException e) {}
	  try {
		  long v1 = DateTimeUtil.getTimeFromMinimalString(s1);
		  long v2 = DateTimeUtil.getTimeFromMinimalString(s2);
		  return v1 < v2 ? -1 : v1 == v2 ? 0 : 1;
	  } catch (NumberFormatException e) {}
	  return s1.compareTo(s2);
  }
  
  public static class AdvancedComparator implements Comparator<String> {
	  public int compare(String o1, String o2) {
		  return compareAdvanced(o1, o2);
	}
  }
  
  public static final char[] hexaChar = new char[] { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };
  public static String encodeHexa(byte[] data) {
	  StringBuilder str = new StringBuilder();
	  for (int i = 0; i < data.length; ++i)
		  str.append(hexaChar[(int)((data[i]>>>4)&0xF)]).append(hexaChar[(int)(data[i]&0xF)]);
	  return str.toString();
  }
  public static byte[] decodeHexa(String s) {
	  byte[] data = new byte[s.length()/2];
	  for (int i = 0; i < data.length; ++i)
		  data[i] = (byte)((decodeHexa(s.charAt(i*2))<<4) + decodeHexa(s.charAt(i*2+1)));
	  return data;
  }
  public static int decodeHexa(char c) {
	  if (c >= '0' && c <= '9') return c-'0';
	  if (c >= 'A' && c <= 'F') return c-'A'+10;
	  if (c >= 'a' && c <= 'f') return c-'a'+10;
	  return -1;
  }
  
  public static boolean isHexa(char c) { return decodeHexa(c) != -1; }
  
  public static String percent(double value, double max, int nbDigitsAfterComma) {
	  return toStringSep(value*100/max, nbDigitsAfterComma);
  }
  
  public static String sizeString(long size) {
	  if (size < 1024)
		  return Long.toString(size)+"B";
	  if (size < (long)1024*1024)
		  return toStringSep(((double)size)/(double)1024, 2) + "KB";
	  if (size < (long)1024*1024*1024)
		  return toStringSep(((double)size)/((double)1024*1024), 2) + "MB";
	  return toStringSep(((double)size)/((double)1024*1024*1024), 2) + "GB";
  }
  
  /** lower case and remove accents */
  public static String normalizeString(String s) {
	  byte[] chars = s.getBytes();
	  for (int i = chars.length-1; i >= 0; --i) {
		  chars[i] = normalize(chars[i]);
	  }
	  return new String(chars);
  }
  public static byte normalize(byte c) {
	  if (c >= 'A' && c <= 'Z') return (byte)(c-('A'-'a'));
	  if ((c & 0x80) == 0) return c;
	  if ((c&0xFF) < 0xA8) return normalized1[c&0x7F];
	  if ((c&0xFF) >= 0xC0) return normalized2[(c&0xFF)-0xC0];
	  return c;
  }
  // ascii
  public static byte[] normalized1 = new byte[] { // from 0x80 to 0xA7
	  'c', 'u', 'e', 'a', 'a', 'a', 'a', 'c', 'e', 'e', 'e', 'i', 'i', 'i', 'a', 'a',
	  'e', (byte)0x91, (byte)0x91, 'o', 'o', 'o', 'u', 'u', 'y', 'o', 'u', 'c', (byte)0x9C, (byte)0x9D, (byte)0x9E, (byte)0x9F,
	  'a', 'i', 'o', 'u', 'n', 'n', 'a', 'o', '?'
  };
  // latin-1
  public static byte[] normalized2 = new byte[] { // from 0xC0 to 0xFF
	  'a', 'a', 'a', 'a', 'a', 'a', 'a', 'c', 'e', 'e', 'e', 'e', 'i', 'i', 'i', 'i',
	  'd', 'n', 'o', 'o', 'o', 'o', 'o', 'x', 'o', 'u', 'u', 'u', 'u', 'y', 'p', 'b',
	  'a', 'a', 'a', 'a', 'a', 'a', 'a', 'c', 'e', 'e', 'e', 'e', 'i', 'i', 'i', 'i',
	  'o', 'n', 'o', 'o', 'o', 'o', 'o', '/', 'o', 'u', 'u', 'u', 'u', 'y', 'p', 'y'
  };
  
  public static RangeInteger toRangeInteger(String s) {
	  int i = s.indexOf('-');
	  if (i < 0) {
		  int v = Integer.parseInt(s.trim());
		  return new RangeInteger(v, v);
	  }
	  return new RangeInteger(Integer.parseInt(s.substring(0, i).trim()), Integer.parseInt(s.substring(i+1).trim()));
  }
  
  public static boolean isLetter(char c) {
	  c = (char)normalize((byte)(c & 0xFF));
	  if (c >= 'a' && c <= 'z') return true;
	  if (c >= 'A' && c <= 'Z') return true;
	  return false;
  }
  public static boolean isDigit(char c) {
	  return c >= '0' && c <= '9';
  }
  public static boolean isSpace(char c) {
	  return c == ' ' || c == '\t';
  }
  public static char upper(char c) {
	  if (c >= 'A' && c <= 'Z') return c;
	  if (c >= 'a' && c <= 'z') return (char)(c + 'A' - 'a');
	  if ((c&0xFF) >= 0xE0 && (c&0xFF) <= 0xFF) return (char)((c&0xFF)-0x20);
	  return c;
  }
  public static char lower(char c) {
	  if (c >= 'a' && c <= 'z') return c;
	  if (c >= 'A' && c <= 'Z') return (char)(c + 'a' - 'A');
	  if ((c&0xFF) >= 0xC0 && (c&0xFF) <= 0xDF) return (char)((c&0xFF)+0x20);
	  return c;
  }
  
  public static boolean containsWord(String str, String word, boolean caseSensitive) {
	  StringBuilder current = new StringBuilder();
	  int i = 0;
	  while (i < str.length()) {
		  char c = str.charAt(i++);
		  if (isLetter(c))
			  current.append(c);
		  else {
			  if (current.length() == 0) continue;
			  if (caseSensitive) {
				  if (current.toString().equals(word)) return true;
			  } else {
				  if (current.toString().equalsIgnoreCase(word)) return true;
			  }
			  current = new StringBuilder();
		  }
	  }
	  return false;
  }
}
