package net.lecousin.framework.media;

import net.lecousin.framework.application.Application;

public enum Local {

	Remove_from_play_list("Remove from play list", "Supprimer de la liste"),
	
	;
	
	private Local(String english, String french) {
		this.english = english;
		this.french = french;
	}
	private String english;
	private String french;
	@Override
	public java.lang.String toString() {
		switch (Application.language) {
		case FRENCH: return french;
		default: return english;
		}
	}
}
