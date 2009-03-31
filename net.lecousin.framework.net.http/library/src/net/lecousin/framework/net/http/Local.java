package net.lecousin.framework.net.http;

import net.lecousin.framework.application.Application;

public enum Local {

	Contacting_server("Contacting server", "Contact le serveur"),
	Waiting_response("Waiting response", "Attente de la réponse"),
	
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
