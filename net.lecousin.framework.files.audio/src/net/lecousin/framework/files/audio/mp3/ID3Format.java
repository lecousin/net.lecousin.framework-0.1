package net.lecousin.framework.files.audio.mp3;

import net.lecousin.framework.files.audio.AudioFileInfo;

public abstract class ID3Format implements AudioFileInfo {

	private long duration = -1;
	
	public long getDuration() { return duration; }
	public void setDuration(long value) { duration = value; }
	
}
