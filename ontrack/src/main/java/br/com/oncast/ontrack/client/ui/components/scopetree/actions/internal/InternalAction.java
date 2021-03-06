package br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal;

import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeWidget;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;

public interface InternalAction {

	public void execute(ScopeTreeWidget tree) throws UnableToCompleteActionException;

}