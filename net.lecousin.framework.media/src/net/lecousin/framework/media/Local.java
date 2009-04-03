package net.lecousin.framework.media;

import net.lecousin.framework.application.Application;

public enum Local {

	Remove_from_play_list("Remove from play list", "Supprimer de la liste"),
	File__not_found("File %#1% doesn't exist.", "Le fichier %#1% n'existe pas."),
	
	;
	
	private Local(String english, String french) {
		this.english = english;
		this.french = french;
	}
	private String english;
	private String french;
	public static String process(Local text, Object...params) {
		int i = 1;
		String str = text.toString();
		for (Object param : params) {
			str = str.replace("%#"+i+"%", param.toString());
			i++;
		}
		return str;
	}
	@Override
	public java.lang.String toString() {
		switch (Application.language) {
		case FRENCH: return french;
		default: return english;
		}
	}
}
