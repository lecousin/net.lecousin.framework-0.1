package net.lecousin.framework.files.audio;

import java.util.List;

import net.lecousin.framework.files.TypedFileInfo;


public interface AudioFileInfo extends TypedFileInfo {

	public abstract String getSongTitle();
	public abstract String getArtist();
	public abstract String getAlbum();
	public abstract int getTrackNumber();
	public abstract int getNumberOfTracksInAlbum();
	public abstract int getYear();
	public abstract String getComment();
	public abstract String getGenre();
	public abstract long getDuration();
	public abstract byte[] getCDIdentifier();
	public abstract List<Picture> getPictures();

	public static class Picture {
		public enum Type {
			OTHER, ICON, COVER_FRONT, COVER_BACK, LEAFLET_PAGE, MEDIA, 
			LEAD_ARTIST, ARTIST_PERFORMER, CONDUCTOR, BAND_ORCHESTRA, COMPOSER,
			LYRICIST, RECORDING_LOCATION, DURING_RECORDING, DURING_PERFORMANCE,
			VIDEO_CAPTURE, BRIGHT_COLOURED_FISH, ILLUSTRATION, BAND_LOGO, PUBLISHER_LOGO
			;
		}
		public Type type;
		public String description;
		public byte[] data;
	}
	
}
