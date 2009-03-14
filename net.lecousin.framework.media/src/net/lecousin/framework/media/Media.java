package net.lecousin.framework.media;

import java.net.URI;


public interface Media {

	public URI getURI();
	/** Return the duration in milliseconds */
	public long getDuration();
	
}
