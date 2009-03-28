package net.lecousin.framework.files.audio.mp3;

import java.util.List;

public class ID3Format_None extends ID3Format {

	ID3Format_None() {
	}
	
	public String getSongTitle() { return null; }
	public String getArtist() { return null; }
	public String getAlbum() { return null; }
	public String getComment() { return null; }
	public String getGenre() { return null; }
	public int getTrackNumber() { return -1; }
	public int getYear() { return -1; }
	
	public byte[] getCDIdentifier() { return null; }
	public int getNumberOfTracksInAlbum() { return -1; }
	public List<Picture> getPictures() { return null; }
}
