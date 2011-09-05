package br.com.oncast.ontrack.client.services.actionExecution;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.com.oncast.ontrack.client.services.context.ContextProviderService;
import br.com.oncast.ontrack.client.services.errorHandling.ErrorTreatmentService;
import br.com.oncast.ontrack.shared.model.actions.ModelAction;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

public class ActionExecutionServiceImpl implements ActionExecutionService {

	private final ActionExecutionManager actionManager;
	private final ContextProviderService contextService;
	private final List<ActionExecutionListener> actionExecutionListeners;
	private final ErrorTreatmentService errorTreatmentService;

	public ActionExecutionServiceImpl(final ContextProviderService contextService, final ErrorTreatmentService errorTreatmentService) {
		this.errorTreatmentService = errorTreatmentService;
		this.actionExecutionListeners = new ArrayList<ActionExecutionListener>();
		this.contextService = contextService;
		this.actionManager = new ActionExecutionManager(new ActionExecutionListener() {

			@Override
			public void onActionExecution(final ModelAction action, final ProjectContext context, final Set<UUID> inferenceInfluencedScopeSet,
					final boolean isClientAction) {
				notifyActionExecutionListeners(action, context, inferenceInfluencedScopeSet, isClientAction);
			}
		});
	}

	@Override
	public void onNonUserActionRequest(final ModelAction action) throws UnableToCompleteActionException {
		actionManager.doNonUserAction(action, contextService.getProjectContext());
	}

	@Override
	public void onUserActionExecutionRequest(final ModelAction action) {
		try {
			actionManager.doUserAction(action, contextService.getProjectContext());
		}
		catch (final UnableToCompleteActionException e) {
			errorTreatmentService.treatUserWarning("It was not possible to execute the requested user action.", e);
		}
	}

	@Override
	public void onUserActionUndoRequest() {
		try {
			actionManager.undoUserAction(contextService.getProjectContext());
		}
		catch (final UnableToCompleteActionException e) {
			errorTreatmentService.treatUserWarning("It was not possible to undo the requested user action.", e);
		}
	}

	@Override
	public void onUserActionRedoRequest() {
		try {
			actionManager.redoUserAction(contextService.getProjectContext());
		}
		catch (final UnableToCompleteActionException e) {
			errorTreatmentService.treatUserWarning("It was not possible to redo the requested user action.", e);
		}
	}

	private void notifyActionExecutionListeners(final ModelAction action, final ProjectContext context, final Set<UUID> inferenceInfluencedScopeSet,
			final boolean isUserAction) {
		for (final ActionExecutionListener handler : actionExecutionListeners) {
			handler.onActionExecution(action, context, inferenceInfluencedScopeSet, isUserAction);
		}
	}

	@Override
	public void addActionExecutionListener(final ActionExecutionListener actionExecutionListener) {
		if (this.actionExecutionListeners.contains(actionExecutionListener)) return;
		this.actionExecutionListeners.add(actionExecutionListener);
	}

	@Override
	public void removeActionExecutionListener(final ActionExecutionListener actionExecutionListener) {
		this.actionExecutionListeners.remove(actionExecutionListener);
	}
}