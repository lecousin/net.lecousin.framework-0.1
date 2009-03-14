package net.lecousin.framework.ui.eclipse;

import net.lecousin.framework.application.Application;

public enum Local {

	Cancel("Cancel", "Annuler"),
	Close("Close", "Fermer"),
	Filters("Filters", "Filtres"),
	Ignore("Ignore", "Ignorer"),
	Ok("Ok", "Ok"),
	on("on", "sur"),
	Page("Page", "page"),
	
	Elapsed_time("Elapsed time", "Temps écoulé"),
	Estimated_remaining_time("Estimated time left", "Temps restant éstimé"),
	Operation_in_progress("Operation in progress", "Opération en cours"),
	
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
