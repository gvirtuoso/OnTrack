package br.com.oncast.ontrack.client.ui.component.scopetree.actions;

import br.com.oncast.ontrack.shared.beans.Scope;

public class RemoveScopeAction implements ScopeAction {
	private final Scope selectedScope;

	public RemoveScopeAction(final Scope selectedScope) {
		this.selectedScope = selectedScope;
	}

	@Override
	public void execute() throws UnableToCompleteActionException {
		if (selectedScope.isRoot()) throw new UnableToCompleteActionException("It is not possible to remove a root node.");
		selectedScope.getParent().remove(selectedScope);
	}

}
