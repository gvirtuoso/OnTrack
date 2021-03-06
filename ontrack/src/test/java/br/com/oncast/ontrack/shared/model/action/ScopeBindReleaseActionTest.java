package br.com.oncast.ontrack.shared.model.action;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope.ScopeBindReleaseActionEntity;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.release.exceptions.ReleaseNotFoundException;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.utils.model.ProjectTestUtils;
import br.com.oncast.ontrack.utils.model.ReleaseTestUtils;
import br.com.oncast.ontrack.utils.model.ScopeTestUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ScopeBindReleaseActionTest extends ModelActionTest {

	private ProjectContext context;
	private Scope rootScope;
	private Release rootRelease;

	@Before
	public void setUp() {
		rootScope = ScopeTestUtils.getScope();
		rootRelease = ReleaseTestUtils.getRelease();
		context = ProjectTestUtils.createProjectContext(rootScope, rootRelease);
	}

	@Test
	public void shouldBindScopeToRelease() throws UnableToCompleteActionException {
		final Scope scope = rootScope.getChild(0);
		assertFalse(rootRelease.getChild(0).getScopeList().contains(scope));

		new ScopeBindReleaseAction(scope.getId(), "R1").execute(context, Mockito.mock(ActionContext.class));
		assertTrue(rootRelease.getChild(0).getScopeList().contains(scope));
	}

	@Test
	public void shouldUnbindScopeFromRelease() throws UnableToCompleteActionException {
		final Scope scope = rootScope.getChild(0);
		final Release release = rootRelease.getChild(0);
		release.addScope(scope);
		assertTrue(rootRelease.getChild(0).getScopeList().contains(scope));

		new ScopeBindReleaseAction(scope.getId(), "").execute(context, Mockito.mock(ActionContext.class));
		assertFalse(rootRelease.getChild(0).getScopeList().contains(scope));
	}

	@Test
	public void shouldCreateNewReleaseIfItDoesNotExist() throws UnableToCompleteActionException, ReleaseNotFoundException {
		final String releaseDescription = "R4";
		assertThatReleaseIsNotInContext(releaseDescription);

		final Scope scope = rootScope.getChild(0);
		new ScopeBindReleaseAction(scope.getId(), releaseDescription).execute(context, Mockito.mock(ActionContext.class));

		final Release newRelease = assertThatReleaseIsInContext(releaseDescription);
		assertTrue(newRelease.getScopeList().contains(scope));
	}

	@Test
	public void shouldNotCreateNewReleaseIfItAlreadyExists() throws UnableToCompleteActionException, ReleaseNotFoundException {
		final Release release = rootRelease.getChild(0);
		assertThatReleaseIsInContext(release.getDescription());

		final Scope scope = rootScope.getChild(0);
		new ScopeBindReleaseAction(scope.getId(), release.getDescription()).execute(context, Mockito.mock(ActionContext.class));

		final Release loadedRelease = assertThatReleaseIsInContext(release.getDescription());
		assertTrue(loadedRelease.getScopeList().contains(scope));
		assertEquals(release, loadedRelease);
	}

	@Test
	public void rollbackShouldDeletePreviouslyCreatedRelease() throws UnableToCompleteActionException, ReleaseNotFoundException {
		final String releaseDescription = "R4";
		assertThatReleaseIsNotInContext(releaseDescription);

		final Scope scope = rootScope.getChild(0);
		final ModelAction rollbackAction = new ScopeBindReleaseAction(scope.getId(), releaseDescription).execute(context, Mockito.mock(ActionContext.class));

		final Release newRelease = assertThatReleaseIsInContext(releaseDescription);
		assertTrue(newRelease.getScopeList().contains(scope));

		rollbackAction.execute(context, Mockito.mock(ActionContext.class));

		assertThatReleaseIsNotInContext(releaseDescription);
	}

	@Test
	public void rollbackShouldNotDeleteBoundReleaseIfThePreviouslyActionsDidNotCreatedIt() throws UnableToCompleteActionException, ReleaseNotFoundException {
		final String releaseDescription = "R1";
		assertThatReleaseIsInContext(releaseDescription);

		final Scope scope = rootScope.getChild(0);
		final ModelAction rollbackAction = new ScopeBindReleaseAction(scope.getId(), releaseDescription).execute(context, Mockito.mock(ActionContext.class));

		final Release newRelease = assertThatReleaseIsInContext(releaseDescription);
		assertTrue(newRelease.getScopeList().contains(scope));

		rollbackAction.execute(context, Mockito.mock(ActionContext.class));

		assertThatReleaseIsInContext(releaseDescription);
	}

	@Test
	public void shouldDefineTheEffortAsOneIfNoOneWasDeclaredOrInfered_WhenNewRelease() throws UnableToCompleteActionException, ReleaseNotFoundException {
		final String releaseDescription = "R4";
		assertThatReleaseIsNotInContext(releaseDescription);

		final Scope scope = rootScope.getChild(0);
		new ScopeBindReleaseAction(scope.getId(), releaseDescription).execute(context, Mockito.mock(ActionContext.class));

		final Release newRelease = assertThatReleaseIsInContext(releaseDescription);
		assertTrue(newRelease.getScopeList().contains(scope));
		assertEquals(1.0, newRelease.getScopeList().get(0).getDeclaredEffort().doubleValue(), 0);
	}

	@Test
	public void shouldNotDefineTheEffortAsOneIfInferedWasInformed_WhenNewRelease() throws UnableToCompleteActionException, ReleaseNotFoundException {
		final String releaseDescription = "R4";
		assertThatReleaseIsNotInContext(releaseDescription);

		final Scope scope = rootScope.getChild(0);
		scope.getEffort().setDeclared(4.0F);
		new ScopeBindReleaseAction(scope.getId(), releaseDescription).execute(context, Mockito.mock(ActionContext.class));

		final Release newRelease = assertThatReleaseIsInContext(releaseDescription);
		assertTrue(newRelease.getScopeList().contains(scope));
		assertEquals(4.0, newRelease.getScopeList().get(0).getDeclaredEffort().doubleValue(), 0);
	}

	@Test
	public void shouldDefineTheValueAsOneIfNoOneWasDeclaredOrInfered_WhenNewRelease() throws UnableToCompleteActionException, ReleaseNotFoundException {
		final String releaseDescription = "R4";
		assertThatReleaseIsNotInContext(releaseDescription);

		final Scope scope = rootScope.getChild(0);
		new ScopeBindReleaseAction(scope.getId(), releaseDescription).execute(context, Mockito.mock(ActionContext.class));

		final Release newRelease = assertThatReleaseIsInContext(releaseDescription);
		assertTrue(newRelease.getScopeList().contains(scope));
		assertEquals(1.0, newRelease.getScopeList().get(0).getDeclaredValue().doubleValue(), 0);
	}

	@Test
	public void shouldNotDefineTheValueAsOneIfInferedWasInformed_WhenNewRelease() throws UnableToCompleteActionException, ReleaseNotFoundException {
		final String releaseDescription = "R4";
		assertThatReleaseIsNotInContext(releaseDescription);

		final Scope scope = rootScope.getChild(0);
		scope.getValue().setDeclared(4.0F);
		new ScopeBindReleaseAction(scope.getId(), releaseDescription).execute(context, Mockito.mock(ActionContext.class));

		final Release newRelease = assertThatReleaseIsInContext(releaseDescription);
		assertTrue(newRelease.getScopeList().contains(scope));
		assertEquals(4.0, newRelease.getScopeList().get(0).getDeclaredValue().doubleValue(), 0);
	}

	private Release assertThatReleaseIsInContext(final String releaseDescription) throws ReleaseNotFoundException {
		final Release newRelease = context.findRelease(releaseDescription);
		return newRelease;
	}

	private void assertThatReleaseIsNotInContext(final String releaseDescription) {
		try {
			context.findRelease(releaseDescription);
			fail("The release should not exist in project context.");
		} catch (final ReleaseNotFoundException e) {}
	}

	@Override
	protected Class<? extends ModelActionEntity> getEntityType() {
		return ScopeBindReleaseActionEntity.class;
	}

	@Override
	protected Class<? extends ModelAction> getActionType() {
		return ScopeBindReleaseAction.class;
	}

	@Override
	protected ModelAction getNewInstance() {
		return new ScopeBindReleaseAction(new UUID(), "");
	}

}
