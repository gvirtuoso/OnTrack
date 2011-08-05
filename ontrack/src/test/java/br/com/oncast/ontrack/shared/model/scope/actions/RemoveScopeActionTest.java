package br.com.oncast.ontrack.shared.model.scope.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import br.com.oncast.ontrack.shared.model.actions.ModelAction;
import br.com.oncast.ontrack.shared.model.project.Project;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.exceptions.UnableToCompleteActionException;

// TODO Create tests that check if the releases of removed children are updated.
public class RemoveScopeActionTest {

	private Scope rootScope;
	private Scope child1Level1;
	private Scope child2Level1;
	private ProjectContext context;
	private Scope child1Level2;
	private Scope child1Level3;

	@Before
	public void setUp() {
		rootScope = new Scope("root");
		child1Level1 = new Scope("child1Level1");
		child2Level1 = new Scope("child2Level1");
		child1Level2 = new Scope("child1Level2");
		child1Level3 = new Scope("child2Level2");
		rootScope.add(child1Level1);
		rootScope.add(child2Level1);
		child1Level1.add(child1Level2);
		child1Level2.add(child1Level3);

		context = new ProjectContext(new Project(rootScope, new Release("")));
	}

	@Test(expected = UnableToCompleteActionException.class)
	public void rootScopeCantBeRemoved() throws UnableToCompleteActionException {
		new ScopeRemoveAction(rootScope.getId()).execute(context);
	}

	@Test
	public void aRemovedScopeShouldBeRemovedFromFather() throws UnableToCompleteActionException {
		assertEquals(2, rootScope.getChildren().size());
		assertEquals(child1Level1, rootScope.getChildren().get(0));
		assertEquals(child2Level1, rootScope.getChildren().get(1));

		new ScopeRemoveAction(child2Level1.getId()).execute(context);

		assertEquals(1, rootScope.getChildren().size());
		assertEquals(child1Level1, rootScope.getChildren().get(0));
		assertEquals(child1Level2, rootScope.getChildren().get(0).getChildren().get(0));
		assertEquals(child1Level3, rootScope.getChildren().get(0).getChildren().get(0).getChildren().get(0));
	}

	@Test
	public void rollbackShouldRecreateEntireScopeHierarchyInTheSameOrder() throws UnableToCompleteActionException {
		final Scope child1Level32 = new Scope("child1Level3.2");
		final Scope child1Level33 = new Scope("child1Level3.3");
		child1Level2.add(child1Level32);
		child1Level2.add(child1Level33);

		final ScopeRemoveRollbackAction rollbackAction = new ScopeRemoveAction(child1Level1.getId()).execute(context);
		rollbackAction.execute(context);

		assertEquals(child1Level1, rootScope.getChildren().get(0));
		assertEquals(child1Level2, rootScope.getChildren().get(0).getChildren().get(0));
		assertEquals(child1Level3, rootScope.getChildren().get(0).getChildren().get(0).getChildren().get(0));
		assertEquals(child1Level32, rootScope.getChildren().get(0).getChildren().get(0).getChildren().get(1));
		assertEquals(child1Level33, rootScope.getChildren().get(0).getChildren().get(0).getChildren().get(2));
	}

	@Test
	public void rollbackShouldRevertExecuteChanges() throws UnableToCompleteActionException {
		assertEquals(2, rootScope.getChildren().size());
		assertEquals(child1Level1, rootScope.getChildren().get(0));
		assertEquals(child2Level1, rootScope.getChildren().get(1));

		final ScopeRemoveAction removeScopeAction = new ScopeRemoveAction(child2Level1.getId());
		final ModelAction rollbackAction = removeScopeAction.execute(context);

		assertEquals(1, rootScope.getChildren().size());
		assertEquals(child1Level1, rootScope.getChildren().get(0));

		rollbackAction.execute(context);

		assertEquals(2, rootScope.getChildren().size());
		assertEquals(child1Level1, rootScope.getChildren().get(0));
		assertEquals(child2Level1, rootScope.getChildren().get(1));
	}

	@Test
	public void executeShouldRemoveChildsFromRemovedScope() throws UnableToCompleteActionException {
		assertEquals(2, rootScope.getChildren().size());
		assertEquals(child1Level1, rootScope.getChildren().get(0));
		assertEquals(child2Level1, rootScope.getChildren().get(1));
		assertEquals(child1Level2, rootScope.getChildren().get(0).getChildren().get(0));
		assertEquals(child1Level3, rootScope.getChildren().get(0).getChildren().get(0).getChildren().get(0));

		final ScopeRemoveAction removeScopeAction = new ScopeRemoveAction(child1Level1.getId());
		removeScopeAction.execute(context);

		assertEquals(1, rootScope.getChildren().size());
		assertEquals(child2Level1, rootScope.getChildren().get(0));
		assertNull(child1Level1.getParent());
		assertNull(child1Level2.getParent());
		assertNull(child1Level3.getParent());
		assertEquals(child1Level1.getChildren().size(), 0);
		assertEquals(child1Level2.getChildren().size(), 0);
		assertEquals(child1Level3.getChildren().size(), 0);
	}

	@Test
	public void rollbackShouldRevertChildsFromRemovedScope() throws UnableToCompleteActionException {
		assertEquals(2, rootScope.getChildren().size());
		assertEquals(child1Level1, rootScope.getChildren().get(0));
		assertEquals(child2Level1, rootScope.getChildren().get(1));
		assertEquals(child1Level2, rootScope.getChildren().get(0).getChildren().get(0));
		assertEquals(child1Level3, rootScope.getChildren().get(0).getChildren().get(0).getChildren().get(0));

		final ScopeRemoveAction removeScopeAction = new ScopeRemoveAction(child1Level1.getId());
		final ModelAction rollbackAction = removeScopeAction.execute(context);

		assertEquals(1, rootScope.getChildren().size());
		assertEquals(child2Level1, rootScope.getChildren().get(0));
		assertNull(child1Level1.getParent());
		assertNull(child1Level2.getParent());
		assertNull(child1Level3.getParent());
		assertEquals(child1Level1.getChildren().size(), 0);
		assertEquals(child1Level2.getChildren().size(), 0);
		assertEquals(child1Level3.getChildren().size(), 0);

		rollbackAction.execute(context);

		assertEquals(2, rootScope.getChildren().size());
		assertEquals(child1Level1, rootScope.getChildren().get(0));
		assertEquals(child2Level1, rootScope.getChildren().get(1));
		assertEquals(child1Level2, rootScope.getChildren().get(0).getChildren().get(0));
		assertEquals(child1Level3, rootScope.getChildren().get(0).getChildren().get(0).getChildren().get(0));

	}

	@Test
	public void executeARolledbackActionShouldRemoveChildScopes() throws UnableToCompleteActionException {
		assertEquals(2, rootScope.getChildren().size());
		assertEquals(child1Level1, rootScope.getChildren().get(0));
		assertEquals(child2Level1, rootScope.getChildren().get(1));
		assertEquals(child1Level2, rootScope.getChildren().get(0).getChildren().get(0));
		assertEquals(child1Level3, rootScope.getChildren().get(0).getChildren().get(0).getChildren().get(0));

		final ScopeRemoveAction removeScopeAction = new ScopeRemoveAction(child1Level1.getId());
		final ModelAction rollbackAction = removeScopeAction.execute(context);

		assertEquals(1, rootScope.getChildren().size());
		assertEquals(child2Level1, rootScope.getChildren().get(0));
		assertNull(child1Level1.getParent());
		assertNull(child1Level2.getParent());
		assertNull(child1Level3.getParent());
		assertEquals(child1Level1.getChildren().size(), 0);
		assertEquals(child1Level2.getChildren().size(), 0);
		assertEquals(child1Level3.getChildren().size(), 0);

		final ModelAction redoAction = rollbackAction.execute(context);

		assertEquals(2, rootScope.getChildren().size());
		assertEquals(child1Level1, rootScope.getChildren().get(0));
		assertEquals(child2Level1, rootScope.getChildren().get(1));
		assertEquals(child1Level2, rootScope.getChildren().get(0).getChildren().get(0));
		assertEquals(child1Level3, rootScope.getChildren().get(0).getChildren().get(0).getChildren().get(0));

		redoAction.execute(context);

		assertEquals(1, rootScope.getChildren().size());
		assertEquals(child2Level1, rootScope.getChildren().get(0));
		assertNull(child1Level1.getParent());
		assertNull(child1Level2.getParent());
		assertNull(child1Level3.getParent());
		assertEquals(child1Level1.getChildren().size(), 0);
		assertEquals(child1Level2.getChildren().size(), 0);
		assertEquals(child1Level3.getChildren().size(), 0);
	}
}
