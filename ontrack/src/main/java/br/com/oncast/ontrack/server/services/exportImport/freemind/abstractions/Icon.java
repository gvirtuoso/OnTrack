package br.com.oncast.ontrack.server.services.exportImport.freemind.abstractions;

public enum Icon {
	INFO("info"),
	CALENDAR("calendar"),
	LAUNCH("launch"),
	WIZARD("wizard"),
	HOURGLASS("hourglass"),
	LIST("list"),
	CLOCK("clock"),
	PENCIL("pencil"),
	IDEA("idea"),
	DOWN("down"),
	UP("up"),
	OK("button_ok"),
	KEY("password"),
	STAR("bookmark");

	private final String freemindCode;

	private Icon(final String freemindCode) {
		this.freemindCode = freemindCode;
	}

	String getFreemindCode() {
		return freemindCode;
	}
}
