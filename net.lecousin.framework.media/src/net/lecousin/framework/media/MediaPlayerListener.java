package net.lecousin.framework.media;

public interface MediaPlayerListener {

	/* media list related */
	public void mediaAdded(Media media);
	public void mediaRemoved(Media media);
	
	/* player related */
	public void mediaStarted(Media media);
	public void mediaPaused(Media currentMedia);
	public void mediaEnded(Media media);
	public void stopped(Media lastMediaPlayed);
	public void mediaPositionChanged(Media media);
	public void mediaTimeChanged(Media media, long time);
	
	public void volumeChanged(double volume);
	public void muteChanged(boolean mute);
}
