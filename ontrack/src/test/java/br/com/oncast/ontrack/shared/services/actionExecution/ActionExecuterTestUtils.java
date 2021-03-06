package br.com.oncast.ontrack.shared.services.actionExecution;

import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ScopeAction;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.utils.mocks.models.UserRepresentationTestUtils;

import java.util.Date;
import java.util.Set;

public class ActionExecuterTestUtils extends ScopeActionExecuter {

	private static final class ModelActionMockImpl implements ScopeAction {
		private static final long serialVersionUID = 1L;

		@Override
		public UUID getReferenceId() {
			return null;
		}

		@Override
		public ModelAction execute(final ProjectContext context, final ActionContext actionContext) throws UnableToCompleteActionException {
			return null;
		}

		@Override
		public boolean changesEffortInference() {
			return true;
		}

		@Override
		public boolean changesProgressInference() {
			return true;
		}

		@Override
		public boolean changesValueInference() {
			return true;
		}

	}

	public static Set<UUID> executeInferenceEnginesForTestingPurposes(final Scope scope) {
		return ActionExecuterTestUtils.executeInferenceEngines(new ModelActionMockImpl(), scope, UserRepresentationTestUtils.getAdmin(), new Date(0));
	}

	public static Scope getInferenceBaseScopeForTestingPurposes(final ProjectContext context, final ModelAction action) throws UnableToCompleteActionException {
		return getInferenceBaseScope(context, action);
	}
}
