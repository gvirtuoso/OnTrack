package br.com.oncast.ontrack.shared.model.value;

import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.ScopeMoveLeftAction;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.prioritizationCriteria.ValueInferenceEngine;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.utils.mocks.models.UserRepresentationTestUtils;
import br.com.oncast.ontrack.utils.model.ProjectTestUtils;

import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;

import static br.com.oncast.ontrack.shared.model.value.ValueInferenceTestUtils.getModifiedScope;
import static br.com.oncast.ontrack.shared.model.value.ValueInferenceTestUtils.getOriginalScope;
import static br.com.oncast.ontrack.utils.assertions.AssertTestUtils.assertDeepEquals;

public class ValueInferenceEngineMoveLeftTest {

	final String FILE_NAME_PREFIX = "Project1";
	final Scope original = getOriginalScope(FILE_NAME_PREFIX);

	@Test
	public void shouldApplyInferenceWhenMoveScopeLeft() throws UnableToCompleteActionException {

		final Scope scope = original.getChild(0).getChild(1);
		final ScopeMoveLeftAction moveLeftAction = new ScopeMoveLeftAction(scope.getId());

		moveLeftAction.execute(ProjectTestUtils.createProjectContext(original, null), Mockito.mock(ActionContext.class));
		new ValueInferenceEngine().process(scope.getParent(), UserRepresentationTestUtils.getAdmin(), new Date());

		assertDeepEquals(getModifiedScope(FILE_NAME_PREFIX, 1), original);
	}
}
