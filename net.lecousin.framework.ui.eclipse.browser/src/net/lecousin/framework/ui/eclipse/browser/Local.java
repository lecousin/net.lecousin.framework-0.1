package net.lecousin.framework.ui.eclipse.browser;

import net.lecousin.framework.application.Application;


public enum Local {

	Back("Back", "Précédent"),
	Forward("Forward", "Suivant"),
	Home("Home", "Accueil"),
	Loading("Loading", "Chargement"),
	Refresh("Refresh", "Rafraîchir"),
	Stop("Stop", "Stop"),
	
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
	public String toString() {
		switch (Application.language) {
		case FRENCH: return french;
		default: return english;
		}
	}
}
