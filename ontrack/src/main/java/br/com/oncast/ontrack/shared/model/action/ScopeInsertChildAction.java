package br.com.oncast.ontrack.shared.model.action;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope.ScopeInsertChildActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConversionAlias;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.action.helper.ActionHelper;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Element;

@ConvertTo(ScopeInsertChildActionEntity.class)
public class ScopeInsertChildAction implements ScopeInsertAction {

	private static final long serialVersionUID = 1L;

	@ConversionAlias("referenceId")
	@Element
	private UUID referenceId;

	@ConversionAlias("newScopeId")
	@Element
	private UUID newScopeId;

	@ConversionAlias("scopeUpdateAction")
	@Element
	private ScopeUpdateAction scopeUpdateAction;

	// IMPORTANT A package-visible default constructor is necessary for serialization. Do not remove this.
	protected ScopeInsertChildAction() {}

	public ScopeInsertChildAction(final UUID parentScopeId, final String pattern) {
		this(parentScopeId, new UUID(), pattern);
	}

	public ScopeInsertChildAction(final UUID parentScopeId, final UUID newScopeId, final String pattern) {
		this.referenceId = parentScopeId;
		this.newScopeId = newScopeId;
		this.scopeUpdateAction = new ScopeUpdateAction(newScopeId, pattern);
	}

	@Override
	public ModelAction execute(final ProjectContext context, final ActionContext actionContext) throws UnableToCompleteActionException {
		final Scope selectedScope = ActionHelper.findScope(referenceId, context, this);

		final List<ModelAction> subActionRollbackList = new ArrayList<ModelAction>();

		final UserRepresentation author = ActionHelper.findActionAuthor(actionContext, context, this);
		selectedScope.add(new Scope("", newScopeId, author, actionContext.getTimestamp()));

		subActionRollbackList.add(scopeUpdateAction.execute(context, actionContext));
		return new ScopeInsertChildRollbackAction(newScopeId, subActionRollbackList);
	}

	@Override
	public UUID getReferenceId() {
		return referenceId;
	}

	@Override
	public UUID getNewScopeId() {
		return newScopeId;
	}

	@Override
	public boolean changesEffortInference() {
		return true;
	}

	@Override
	public boolean changesValueInference() {
		return true;
	}

	@Override
	public boolean changesProgressInference() {
		return true;
	}
}
