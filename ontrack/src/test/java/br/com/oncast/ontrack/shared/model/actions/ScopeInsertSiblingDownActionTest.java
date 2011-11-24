package br.com.oncast.ontrack.shared.model.actions;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.ReleaseFactoryTestUtil;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.utils.mocks.models.ProjectTestUtils;

public class ScopeInsertSiblingDownActionTest {

	private Scope rootScope;
	private Scope firstChild;
	private Scope lastChild;
	private ProjectContext context;
	private String newScopeDescription;

	@Before
	public void setUp() {
		rootScope = new Scope("root");
		firstChild = new Scope("child");
		lastChild = new Scope("last");

		rootScope.add(firstChild);
		rootScope.add(lastChild);

		newScopeDescription = "description for new scope";

		context = ProjectTestUtils.createProjectContext(rootScope, ReleaseFactoryTestUtil.create(""));
	}

	@Test
	public void siblingDownMustBeDown() throws UnableToCompleteActionException {
		assertEquals(firstChild.getParent().getChildren().get(firstChild.getParent().getChildIndex(firstChild) + 1), lastChild);
		assertEquals(2, rootScope.getChildren().size());

		final ScopeInsertSiblingDownAction insertSiblingDownScopeAction = new ScopeInsertSiblingDownAction(firstChild.getId(), newScopeDescription);
		insertSiblingDownScopeAction.execute(context);

		assertEquals(3, rootScope.getChildren().size());
		assertEquals(firstChild.getParent().getChildren().get(1).getDescription(), newScopeDescription);
		assertEquals(firstChild.getParent().getChildren().get(firstChild.getParent().getChildIndex(firstChild) + 2), lastChild);
	}

	@Test(expected = UnableToCompleteActionException.class)
	public void rootCantAddSiblingDown() throws UnableToCompleteActionException {
		new ScopeInsertSiblingDownAction(rootScope.getId(), "text").execute(context);
	}

	@Test
	public void rollbackMustRevertExecuteChanges() throws UnableToCompleteActionException {
		assertEquals(firstChild.getParent().getChildren().get(firstChild.getParent().getChildIndex(firstChild) + 1), lastChild);
		assertEquals(2, rootScope.getChildren().size());

		final ScopeInsertSiblingDownAction insertSiblingDownScopeAction = new ScopeInsertSiblingDownAction(firstChild.getId(), newScopeDescription);
		final ModelAction rollbackAction = insertSiblingDownScopeAction.execute(context);

		assertEquals(3, rootScope.getChildren().size());
		assertEquals(firstChild.getParent().getChildren().get(1).getDescription(), newScopeDescription);
		assertEquals(firstChild.getParent().getChildren().get(2), lastChild);

		rollbackAction.execute(context);

		assertEquals(firstChild.getParent().getChildren().get(1), lastChild);
		assertEquals(2, rootScope.getChildren().size());
	}

}
