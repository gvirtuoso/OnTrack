package br.com.oncast.ontrack.shared.model.action.kanban;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.KanbanColumnCreateAction;
import br.com.oncast.ontrack.shared.model.action.KanbanColumnRemoveAction;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ScopeDeclareProgressAction;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.progress.Progress;
import br.com.oncast.ontrack.shared.model.progress.Progress.ProgressState;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.utils.mocks.actions.ActionTestUtils;
import br.com.oncast.ontrack.utils.mocks.models.ProjectTestUtils;

public class KanbanColumnRemoveActionUndeRedoTest {

	private static final String DONE = ProgressState.DONE.getDescription();
	private static final String NOT_STARTED = Progress.DEFAULT_NOT_STARTED_NAME;
	private ProjectContext context;
	private Release release;
	private String columnDescription;

	@Before
	public void setUp() throws UnableToCompleteActionException {
		context = new ProjectContext(ProjectTestUtils.createPopulatedProject());
		release = context.getProjectRelease().getChild(0);
		columnDescription = "Blabla";
		new KanbanColumnCreateAction(release.getId(), columnDescription, false).execute(context, Mockito.mock(ActionContext.class));
	}

	@Test
	public void executionShouldLockTheUnlockedKanban() throws UnableToCompleteActionException {
		Assert.assertFalse("The kanban should be unlocked.", context.getKanban(release).isLocked());
		ActionTestUtils.assertExpectedKanbanColumns(context, release, 3, NOT_STARTED, columnDescription, DONE);

		new KanbanColumnRemoveAction(release.getId(), columnDescription, true).execute(context, Mockito.mock(ActionContext.class));

		ActionTestUtils.assertExpectedKanbanColumns(context, release, 2, NOT_STARTED, DONE);
		Assert.assertTrue("The kanban should be locked.", context.getKanban(release).isLocked());
	}

	@Test
	public void executionShouldNotLockTheUnlockedKanbanWhenParametersSaySo() throws UnableToCompleteActionException {
		Assert.assertFalse("The kanban should be unlocked.", context.getKanban(release).isLocked());
		ActionTestUtils.assertExpectedKanbanColumns(context, release, 3, NOT_STARTED, columnDescription, DONE);

		new KanbanColumnRemoveAction(release.getId(), columnDescription, false).execute(context, Mockito.mock(ActionContext.class));

		Assert.assertFalse("The kanban should be unlocked.", context.getKanban(release).isLocked());
		ActionTestUtils.assertExpectedKanbanColumns(context, release, 2, NOT_STARTED, DONE);
	}

	@Test
	public void kanbanShouldRemainLockedWhenExecutionIsNotMeantToLockIt() throws UnableToCompleteActionException {
		context.getKanban(release).setLocked(true);

		Assert.assertTrue("The kanban should be locked.", context.getKanban(release).isLocked());
		ActionTestUtils.assertExpectedKanbanColumns(context, release, 3, NOT_STARTED, columnDescription, DONE);

		new KanbanColumnRemoveAction(release.getId(), columnDescription, false).execute(context, Mockito.mock(ActionContext.class));

		Assert.assertTrue("The kanban should be locked.", context.getKanban(release).isLocked());
		ActionTestUtils.assertExpectedKanbanColumns(context, release, 2, NOT_STARTED, DONE);
	}

	@Test
	public void doUndoAndRedoShouldReturnToTheState() throws UnableToCompleteActionException {
		ModelAction action = new KanbanColumnRemoveAction(release.getId(), columnDescription, true);
		ModelAction rollbackAction;

		for (int i = 0; i < 10; i++) {
			rollbackAction = action.execute(context, Mockito.mock(ActionContext.class));
			ActionTestUtils.assertExpectedKanbanColumns(context, release, 2, NOT_STARTED, DONE);
			action = rollbackAction.execute(context, Mockito.mock(ActionContext.class));
			ActionTestUtils.assertExpectedKanbanColumns(context, release, 3, NOT_STARTED, columnDescription, DONE);
		}
	}

	@Test
	public void doUndoAndRedoShouldMaintainTheColumnPosition() throws UnableToCompleteActionException {
		final String columnBefore = columnDescription + "Before";
		final String columnAfter = columnDescription + "After";

		new KanbanColumnCreateAction(release.getId(), columnBefore, true, 0).execute(context, Mockito.mock(ActionContext.class));
		new KanbanColumnCreateAction(release.getId(), columnAfter, true).execute(context, Mockito.mock(ActionContext.class));

		ModelAction action = new KanbanColumnRemoveAction(release.getId(), columnDescription, true);
		ModelAction rollbackAction;

		for (int i = 0; i < 10; i++) {
			rollbackAction = action.execute(context, Mockito.mock(ActionContext.class));
			ActionTestUtils.assertExpectedKanbanColumns(context, release, 4, NOT_STARTED, columnBefore, columnAfter, DONE);
			action = rollbackAction.execute(context, Mockito.mock(ActionContext.class));
			ActionTestUtils.assertExpectedKanbanColumns(context, release, 5, NOT_STARTED, columnBefore, columnDescription, columnAfter, DONE);
		}
	}

	@Test
	public void doUndoAndRedoShouldMaintainTheReleaseScopesProgresses() throws UnableToCompleteActionException {
		final List<Scope> scopeList = release.getScopeList();
		new ScopeDeclareProgressAction(scopeList.get(0).getId(), columnDescription).execute(context, Mockito.mock(ActionContext.class));
		new ScopeDeclareProgressAction(scopeList.get(1).getId(), columnDescription).execute(context, Mockito.mock(ActionContext.class));
		ActionTestUtils.assertProgressForScopes(columnDescription, scopeList.get(0), scopeList.get(1));
		ActionTestUtils.assertProgressForScopes(NOT_STARTED, scopeList.get(2));

		ModelAction action = new KanbanColumnRemoveAction(release.getId(), columnDescription, true);
		ModelAction rollbackAction;

		for (int i = 0; i < 10; i++) {
			rollbackAction = action.execute(context, Mockito.mock(ActionContext.class));
			ActionTestUtils.assertProgressForScopes(NOT_STARTED, scopeList.get(0), scopeList.get(1), scopeList.get(2));

			action = rollbackAction.execute(context, Mockito.mock(ActionContext.class));
			ActionTestUtils.assertProgressForScopes(columnDescription, scopeList.get(0), scopeList.get(1));
			ActionTestUtils.assertProgressForScopes(NOT_STARTED, scopeList.get(2));
		}
	}

	@Test
	public void doUndoAndRedoShouldMaintainTheReleaseScopesProgresses2() throws UnableToCompleteActionException {
		final String columnBefore = "Before" + columnDescription;
		new KanbanColumnCreateAction(release.getId(), columnBefore, true, 0).execute(context, Mockito.mock(ActionContext.class));

		final List<Scope> scopeList = release.getScopeList();
		new ScopeDeclareProgressAction(scopeList.get(0).getId(), columnDescription).execute(context, Mockito.mock(ActionContext.class));
		new ScopeDeclareProgressAction(scopeList.get(1).getId(), columnDescription).execute(context, Mockito.mock(ActionContext.class));
		ActionTestUtils.assertProgressForScopes(columnDescription, scopeList.get(0), scopeList.get(1));
		ActionTestUtils.assertProgressForScopes(NOT_STARTED, scopeList.get(2));

		ModelAction action = new KanbanColumnRemoveAction(release.getId(), columnDescription, true);
		ModelAction rollbackAction;

		for (int i = 0; i < 10; i++) {
			rollbackAction = action.execute(context, Mockito.mock(ActionContext.class));
			ActionTestUtils.assertProgressForScopes(columnBefore, scopeList.get(0), scopeList.get(1));
			ActionTestUtils.assertProgressForScopes(NOT_STARTED, scopeList.get(2));

			action = rollbackAction.execute(context, Mockito.mock(ActionContext.class));
			ActionTestUtils.assertProgressForScopes(columnDescription, scopeList.get(0), scopeList.get(1));
			ActionTestUtils.assertProgressForScopes(NOT_STARTED, scopeList.get(2));
		}
	}

	@Test
	public void undoShouldNotUnlockThePreviouslyUnlockedKanban() throws UnableToCompleteActionException {
		Assert.assertFalse("The kanban should be unlocked.", context.getKanban(release).isLocked());

		ModelAction action = new KanbanColumnRemoveAction(release.getId(), columnDescription, true);
		ModelAction rollbackAction;

		for (int i = 0; i < 10; i++) {
			rollbackAction = action.execute(context, Mockito.mock(ActionContext.class));
			Assert.assertTrue("The kanban should be locked.", context.getKanban(release).isLocked());

			action = rollbackAction.execute(context, Mockito.mock(ActionContext.class));
			Assert.assertTrue("The kanban should be locked.", context.getKanban(release).isLocked());
		}
	}
}