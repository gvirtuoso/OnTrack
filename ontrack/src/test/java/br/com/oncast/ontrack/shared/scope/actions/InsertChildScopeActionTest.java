package br.com.oncast.ontrack.shared.scope.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import br.com.oncast.ontrack.shared.model.actions.ModelAction;
import br.com.oncast.ontrack.shared.model.project.Project;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.actions.ScopeInsertChildAction;
import br.com.oncast.ontrack.shared.model.scope.exceptions.UnableToCompleteActionException;

public class InsertChildScopeActionTest {

	private Scope selectedScope;
	private ProjectContext context;
	private String newScopeDescription;
	private String newReleaseDescription;

	@Before
	public void setUp() {
		selectedScope = new Scope("root");
		selectedScope.add(new Scope("first child"));

		newScopeDescription = "description for new scope";
		newReleaseDescription = "Release1";

		context = new ProjectContext(new Project(selectedScope, new Release("")));
	}

	@Test
	public void mustInsertNewChild() throws UnableToCompleteActionException {
		assertEquals(selectedScope.getChildren().size(), 1);

		new ScopeInsertChildAction(selectedScope.getId(), newScopeDescription).execute(context);

		assertEquals(selectedScope.getChildren().size(), 2);
		assertEquals(selectedScope.getChildren().get(1).getDescription(), newScopeDescription);
	}

	@Test
	public void theInsertedChildMustBeTheLastChild() throws UnableToCompleteActionException {
		new ScopeInsertChildAction(selectedScope.getId(), newScopeDescription).execute(context);

		assertFalse(selectedScope.getChildren().get(0).getDescription().equals(newScopeDescription));
		assertEquals(newScopeDescription, selectedScope.getChildren().get(1).getDescription());
	}

	@Test
	public void rollbackMustRevertExecuteChanges() throws UnableToCompleteActionException {
		final ScopeInsertChildAction insertChildScopeAction = new ScopeInsertChildAction(selectedScope.getId(), newScopeDescription);
		final ModelAction rollbackAction = insertChildScopeAction.execute(context);

		assertEquals(2, selectedScope.getChildren().size());

		rollbackAction.execute(context);

		assertEquals(1, selectedScope.getChildren().size());
	}

	@Test
	public void mustAssociateScopeWithARelease() throws UnableToCompleteActionException {
		new ScopeInsertChildAction(selectedScope.getId(), newScopeDescription + " @" + newReleaseDescription).execute(context);

		assertEquals(selectedScope.getChildren().get(1).getDescription(), newScopeDescription);
		assertEquals(selectedScope.getChildren().get(1).getRelease().getDescription(), newReleaseDescription);
	}

	@Test
	public void mustDisassociateScopeFromReleaseAfterUndo() throws UnableToCompleteActionException {
		final ScopeInsertChildAction insertFatherScopeAction = new ScopeInsertChildAction(selectedScope.getId(), newScopeDescription + " @"
				+ newReleaseDescription);
		final ModelAction rollbackAction = insertFatherScopeAction.execute(context);

		final Scope insertedScope = selectedScope.getChildren().get(1);
		final Release release = insertedScope.getRelease();

		assertEquals(release.getDescription(), newReleaseDescription);
		assertTrue(release.getScopeList().contains(insertedScope));
		assertEquals(insertedScope.getDescription(), newScopeDescription);

		rollbackAction.execute(context);

		assertEquals(selectedScope.getChildren().size(), 1);
		assertFalse(release.getScopeList().contains(insertedScope));
	}
}
