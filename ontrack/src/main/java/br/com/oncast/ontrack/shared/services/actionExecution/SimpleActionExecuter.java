package br.com.oncast.ontrack.shared.services.actionExecution;

import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.UserAction;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;

public class SimpleActionExecuter implements ModelActionExecuter {

	// Constructor must be package visible.
	SimpleActionExecuter() {}

	@Override
	public ActionExecutionContext executeAction(final ProjectContext context, final UserAction action) throws UnableToCompleteActionException {
		final ModelAction reverseAction = action.execute(context);
		return new ActionExecutionContext(action, reverseAction);
	}

}
