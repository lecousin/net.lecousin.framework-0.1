package net.lecousin.framework.media;

import java.net.URI;

import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface MediaPlayerPlugin {

	public Media newMedia(URI uri) throws UnsupportedFormatException;
	public void freeMedia(Media media);
	
	public void start(Media media) throws UnsupportedFormatException;
	public void pause(Media media);
	public void stop(Media media);
	
	public double getPosition(Media media);
	public void setPosition(Media media, double pos);
	public long getTime(Media media);
	public void setTime(Media media, long time);
	
	public double getVolume();
	public void setVolume(double volume);
	public boolean getMute();
	public void setMute(boolean value);
	
	public Control createVisual(Composite parent);
	
	public void free();
	
	public Event<Media> started();
	public Event<Media> paused();
	public Event<Media> ended();
	public Event<Media> stopped();
	public Event<Media> positionChanged();
	public Event<Pair<Media,Long>> timeChanged();
	public Event<Double> volumeChanged();
	public Event<Boolean> muteChanged();
	
}
