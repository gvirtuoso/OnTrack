package br.com.oncast.ontrack.server.business;

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.oncast.ontrack.mocks.actions.ActionMock;
import br.com.oncast.ontrack.mocks.models.ProjectMock;
import br.com.oncast.ontrack.shared.exceptions.business.InvalidIncomingAction;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToHandleActionException;
import br.com.oncast.ontrack.shared.model.actions.ModelAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeMoveUpAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeUpdateAction;
import br.com.oncast.ontrack.shared.model.project.Project;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.shared.services.actionExecution.ActionExecuter;
import br.com.oncast.ontrack.shared.services.requestDispatch.ModelActionSyncRequest;
import br.com.oncast.ontrack.utils.deepEquality.DeepEqualityTestUtils;

public class BusinessLogicTest {

	private EntityManager entityManager;

	@Before
	public void setUp() {
		entityManager = Persistence.createEntityManagerFactory("ontrackPU").createEntityManager();
	}

	@After
	public void tearDown() {
		entityManager.close();
	}

	@Test(expected = InvalidIncomingAction.class)
	public void shouldThrowExceptionWhenAnInvalidActionIsExecuted() throws UnableToHandleActionException {
		final BusinessLogic business = BusinessLogicMockFactoryTestUtils.createWithDumbPersistenceMockAndDumbBroadcastMock();
		final ArrayList<ModelAction> actionList = new ArrayList<ModelAction>();
		actionList.add(new ScopeUpdateAction(new UUID("id"), "bllla"));
		business.handleIncomingActionSyncRequest(new ModelActionSyncRequest(new UUID(), actionList));
	}

	@Test(expected = InvalidIncomingAction.class)
	public void shouldThrowExceptionAndNotPersistWhenAnInvalidActionIsExecuted() throws UnableToHandleActionException {
		final BusinessLogic business = BusinessLogicMockFactoryTestUtils.createWithDumbNonWritablePersistenceMockAndDumbBroadcastMock();
		final ArrayList<ModelAction> actionList = new ArrayList<ModelAction>();
		actionList.add(new ScopeMoveUpAction(new UUID("0")));
		business.handleIncomingActionSyncRequest(new ModelActionSyncRequest(new UUID(), actionList));
	}

	@Test
	public void goingToPersistence() throws Exception {
		final BusinessLogic business = BusinessLogicMockFactoryTestUtils.createWithJpaPersistenceMockAndDumbBroadcastMock();
		shouldConstructAScopeHierarchyFromActions(business);
	}

	private void shouldConstructAScopeHierarchyFromActions(final BusinessLogic business) throws Exception {
		final Project project = ProjectMock.getProject();
		final ProjectContext context = new ProjectContext(project);
		for (final ModelAction action : ActionMock.getActions()) {
			ActionExecuter.executeAction(context, action);
		}

		business.handleIncomingActionSyncRequest(new ModelActionSyncRequest(new UUID(), ActionMock.getActions()));
		final Scope projectScope = business.loadProject().getProjectScope();

		DeepEqualityTestUtils.assertObjectEquality(project.getProjectScope(), projectScope);
	}
}
