package br.com.oncast.ontrack.client.ui.component.scopetree;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import br.com.oncast.ontrack.client.services.actions.ActionExecutionService;
import br.com.oncast.ontrack.client.services.context.ContextProviderService;
import br.com.oncast.ontrack.client.ui.components.scopetree.ScopeTree;
import br.com.oncast.ontrack.client.ui.components.scopetree.exceptions.ActionNotFoundException;
import br.com.oncast.ontrack.shared.project.Project;
import br.com.oncast.ontrack.shared.project.ProjectContext;
import br.com.oncast.ontrack.shared.release.Release;
import br.com.oncast.ontrack.shared.scope.Scope;
import br.com.oncast.ontrack.shared.scope.actions.ScopeMoveUpAction;

import com.octo.gwt.test.GwtTest;

public class MoveUpTest extends GwtTest {

	private Scope scope;
	private Scope rootScope;
	private Scope firstScope;
	private Scope lastScope;
	private ScopeTree tree;
	private ScopeTree treeAfterManipulation;
	private ProjectContext projectContext;
	private ActionExecutionService actionExecutionService;

	@Before
	public void setUp() {
		scope = getScope();
		tree = new ScopeTree();
		tree.setScope(scope);

		projectContext = new ProjectContext((new Project(scope, new Release(""))));
		final ContextProviderService contextService = new ContextProviderService();
		contextService.setProjectContext(projectContext);
		actionExecutionService = new ActionExecutionService(contextService);
		actionExecutionService.addActionExecutionListener(tree.getActionExecutionListener());
	}

	private Scope getScope() {
		rootScope = new Scope("Project");
		firstScope = new Scope("1");
		rootScope.add(firstScope);
		rootScope.add(new Scope("2"));
		rootScope.add(new Scope("3"));
		lastScope = new Scope("4");
		rootScope.add(lastScope);

		return rootScope;
	}

	private Scope getModifiedScope() {
		final Scope projectScope = new Scope("Project");
		projectScope.add(new Scope("1"));
		projectScope.add(new Scope("2"));
		projectScope.add(new Scope("4"));
		projectScope.add(new Scope("3"));

		return projectScope;
	}

	private Scope getUnmodifiedScope() {
		final Scope unmodifiedScope = new Scope("Project");
		unmodifiedScope.add(new Scope("1"));
		unmodifiedScope.add(new Scope("2"));
		unmodifiedScope.add(new Scope("3"));
		unmodifiedScope.add(new Scope("4"));

		return unmodifiedScope;
	}

	private ScopeTree getUnmodifiedTree() {
		treeAfterManipulation = new ScopeTree();
		treeAfterManipulation.setScope(getUnmodifiedScope());
		return treeAfterManipulation;
	}

	private ScopeTree getModifiedTree() {
		treeAfterManipulation = new ScopeTree();
		treeAfterManipulation.setScope(getModifiedScope());
		return treeAfterManipulation;
	}

	@Test
	public void shouldMoveUp() throws ActionNotFoundException {
		actionExecutionService.onActionExecutionRequest(new ScopeMoveUpAction(lastScope));

		assertTrue(getModifiedScope().deepEquals(scope));
		assertTrue(getModifiedTree().deepEquals(tree));
	}

	@Test(expected = RuntimeException.class)
	public void shouldNotMoveUpFirstItem() throws ActionNotFoundException {
		actionExecutionService.onActionExecutionRequest(new ScopeMoveUpAction(firstScope));
	}

	@Test(expected = RuntimeException.class)
	public void shouldNotMoveUpRoot() throws ActionNotFoundException {
		actionExecutionService.onActionExecutionRequest(new ScopeMoveUpAction(rootScope));
	}

	@Test
	public void shouldMoveDownItemAfterUndo() throws ActionNotFoundException {
		actionExecutionService.onActionExecutionRequest(new ScopeMoveUpAction(lastScope));

		assertTrue(getModifiedScope().deepEquals(scope));
		assertTrue(getModifiedTree().deepEquals(tree));

		actionExecutionService.onActionUndoRequest();
		assertTrue(getUnmodifiedScope().deepEquals(scope));
		assertTrue(getUnmodifiedTree().deepEquals(tree));
	}

	@Override
	public String getModuleName() {
		return "br.com.oncast.ontrack.Application";
	}

}
