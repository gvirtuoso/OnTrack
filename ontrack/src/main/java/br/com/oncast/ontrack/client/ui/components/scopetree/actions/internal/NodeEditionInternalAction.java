package br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal;

import br.com.oncast.ontrack.client.ui.components.scopetree.ScopeTreeItem;
import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeWidget;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ScopeUpdateAction;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.scope.Scope;

public class NodeEditionInternalAction implements TwoStepInternalAction {

	private final Scope scope;
	private ScopeTreeItem selectedTreeItem;

	public NodeEditionInternalAction(final Scope scope) {
		this.scope = scope;
	}

	@Override
	public void execute(final ScopeTreeWidget tree) throws UnableToCompleteActionException {
		selectedTreeItem = tree.getSelectedItem();
		selectedTreeItem.enterEditMode();
	}

	@Override
	public void rollback(final ScopeTreeWidget tree) throws UnableToCompleteActionException {
		tree.setSelectedItem(selectedTreeItem, false);
	}

	@Override
	public ModelAction createEquivalentModelAction(final String newPattern) {
		return new ScopeUpdateAction(scope.getId(), newPattern);
	}

}
