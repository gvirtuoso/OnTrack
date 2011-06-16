package br.com.oncast.ontrack.shared.scope.actions;

import br.com.oncast.ontrack.shared.project.ProjectContext;
import br.com.oncast.ontrack.shared.scope.Scope;
import br.com.oncast.ontrack.shared.scope.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.util.uuid.UUID;

public class ScopeInsertSiblingDownAction implements ScopeInsertSiblingAction {
	private UUID selectedScopeId;
	private UUID newScopeId;

	public ScopeInsertSiblingDownAction(final Scope selectedScope) {
		this.selectedScopeId = selectedScope.getId();
	}

	protected ScopeInsertSiblingDownAction() {}

	@Override
	public void execute(final ProjectContext context) throws UnableToCompleteActionException {
		final Scope selectedScope = context.findScope(selectedScopeId);
		if (selectedScope.isRoot()) throw new UnableToCompleteActionException("It is not possible to create a sibling for a root node.");

		final Scope newScope = new Scope("");
		newScopeId = newScope.getId();

		final Scope parent = selectedScope.getParent();
		parent.add(parent.getChildIndex(selectedScope) + 1, newScope);
	}

	@Override
	public void rollback(final ProjectContext context) throws UnableToCompleteActionException {
		final Scope newScope = context.findScope(newScopeId);
		new ScopeRemoveAction(newScope).execute(context);
	}

	@Override
	public UUID getScopeId() {
		return selectedScopeId;
	}

	@Override
	public UUID getNewScopeId() {
		return newScopeId;
	}
}
