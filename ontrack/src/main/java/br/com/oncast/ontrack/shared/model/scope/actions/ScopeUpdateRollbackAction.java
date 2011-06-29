package br.com.oncast.ontrack.shared.model.scope.actions;

import br.com.oncast.ontrack.shared.model.actions.ModelAction;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

public class ScopeUpdateRollbackAction implements ScopeAction {

	private UUID selectedScopeId;
	private String newPattern;
	private String oldDescription;
	private String oldReleaseDescription;

	public ScopeUpdateRollbackAction(final UUID selectedScopeId, final String newPattern, final String oldDescription, final String oldReleaseDescription) {
		this.selectedScopeId = selectedScopeId;
		this.newPattern = newPattern;
		this.oldDescription = oldDescription;
		this.oldReleaseDescription = oldReleaseDescription;
	}

	// IMPORTANT A package-visible default constructor is necessary for serialization. Do not remove this.
	protected ScopeUpdateRollbackAction() {}

	@Override
	public ModelAction execute(final ProjectContext context) throws UnableToCompleteActionException {
		if (oldDescription == null) throw new UnableToCompleteActionException("The action cannot be rolled back because it has never been executed.");

		final Scope selectedScope = context.findScope(selectedScopeId);
		final Release newRelease = selectedScope.getRelease();
		if (newRelease != null) newRelease.removeScope(selectedScope);

		selectedScope.setDescription(oldDescription);
		final Release oldRelease = context.loadRelease(oldReleaseDescription);
		selectedScope.setRelease(oldRelease);
		if (oldRelease != null) oldRelease.addScope(selectedScope);

		return new ScopeUpdateAction(selectedScopeId, newPattern);
	}

	@Override
	public UUID getReferenceId() {
		return selectedScopeId;
	}

}