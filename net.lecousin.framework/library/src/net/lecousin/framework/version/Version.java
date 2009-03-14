package net.lecousin.framework.version;

public class Version implements Comparable<Version> {

	public Version(String str) {
		int i = str.indexOf('.');
		if (i > 0) {
			try { major = Integer.parseInt(str.substring(0,i)); }
			catch (NumberFormatException e) { }
			if (i < str.length()-1) {
				str = str.substring(i+1);
				i = str.indexOf('.');
				if (i > 0) {
					try { minor = Integer.parseInt(str.substring(0,i)); }
					catch (NumberFormatException e) { }
					if (i < str.length()-1) {
						str = str.substring(i+1);
						try { sub = Integer.parseInt(str); }
						catch (NumberFormatException e) { }
					}
				}
			}
		} else
			try { major = Integer.parseInt(str); }
			catch (NumberFormatException e) {}
	}
	public Version(int maj, int min, int sub) {
		this.major = maj;
		this.minor = min;
		this.sub = sub;
	}
	
	public int major = 0;
	public int minor = 0;
	public int sub = 0;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Version)) return false;
		Version v = (Version)obj;
		return v.major == major && v.minor == minor && v.sub == sub;
	}
	@Override
	public int hashCode() {
		return major * 10000 + minor * 100 + sub;
	}
	
	public int compareTo(Version v) {
		if (v.major < major) return 1;
		if (v.major > major) return -1;
		if (v.minor < minor) return 1;
		if (v.minor > minor) return -1;
		if (v.sub < sub) return 1;
		if (v.sub > sub) return -1;
		return 0;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(major).append('.').append(minor).append('.').append(sub).toString();
	}
}
