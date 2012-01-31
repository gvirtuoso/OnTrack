package br.com.oncast.ontrack.client.ui.keyeventhandler.modifier;

import br.com.oncast.ontrack.client.utils.RuntimeEnvironment;
import br.com.oncast.ontrack.client.utils.jquery.Event;

public enum ControlModifier implements ShortcutModifier {
	PRESSED,
	UNPRESSED,
	BOTH;

	@Override
	public boolean matches(final Event e) {
		if (this == BOTH) return true;
		return (RuntimeEnvironment.isMac() ? e.metaKey() && !e.ctrlKey() : e.ctrlKey()) == (this == PRESSED);
	}
}