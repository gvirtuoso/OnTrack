package br.com.oncast.ontrack.client.ui.component.scopetree;

import br.com.oncast.ontrack.client.services.ClientServicesTestUtils;
import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionServiceImpl;
import br.com.oncast.ontrack.client.ui.components.scopetree.ScopeTree;
import br.com.oncast.ontrack.client.ui.components.scopetree.exceptions.ActionNotFoundException;
import br.com.oncast.ontrack.shared.model.action.ScopeUpdateAction;
import br.com.oncast.ontrack.shared.model.prioritizationCriteria.Effort;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.utils.deepEquality.DeepEqualityTestUtils;
import br.com.oncast.ontrack.utils.deepEquality.custom.mocks.EffortDeepEqualityComparator;
import br.com.oncast.ontrack.utils.mocks.actions.ActionExecutionFactoryTestUtil;
import br.com.oncast.ontrack.utils.model.ProjectTestUtils;
import br.com.oncast.ontrack.utils.model.ReleaseTestUtils;
import br.com.oncast.ontrack.utils.model.ScopeTestUtils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.googlecode.gwt.test.GwtModule;
import com.googlecode.gwt.test.GwtTest;

@GwtModule("br.com.oncast.ontrack.Application")
public class UpdateTest extends GwtTest {

	private Scope scope;
	private Scope rootScope;
	private Scope firstScope;
	private ScopeTree tree;
	private ScopeTree treeAfterManipulation;
	private ProjectContext projectContext;
	private ActionExecutionServiceImpl actionExecutionService;

	@BeforeClass
	public static void beforeClass() throws Exception {
		DeepEqualityTestUtils.setCustomDeepEqualityComparator(Effort.class, new EffortDeepEqualityComparator());
		ClientServicesTestUtils.configure().mockEssential();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		DeepEqualityTestUtils.removeCustomDeepEqualityComparator(Effort.class);
		ClientServicesTestUtils.reset();
	}

	@Before
	public void setUp() throws Exception {
		scope = getScope();
		tree = new ScopeTree();
		tree.setContext(ProjectTestUtils.createProjectContext(scope, null));

		projectContext = ProjectTestUtils.createProjectContext(scope, ReleaseTestUtils.createRelease(""));
		actionExecutionService = ActionExecutionFactoryTestUtil.create(projectContext);
		actionExecutionService.addActionExecutionListener(tree.getActionExecutionListener());
	}

	@After
	public void cleanUp() {
		getBrowserSimulator().fireLoopEnd();
	}

	private Scope getScope() {
		rootScope = ScopeTestUtils.createScope("Project");
		firstScope = ScopeTestUtils.createScope("1");
		rootScope.add(firstScope);
		rootScope.add(ScopeTestUtils.createScope("2"));

		return rootScope;
	}

	private ProjectContext getModifiedContext() {
		final Scope projectScope = ScopeTestUtils.createScope("Project");
		projectScope.add(ScopeTestUtils.createScope("3"));
		projectScope.add(ScopeTestUtils.createScope("2"));

		return ProjectTestUtils.createProjectContext(projectScope, null);
	}

	private ProjectContext getModifiedRootContext() {
		final Scope projectScope = ScopeTestUtils.createScope("Root");
		projectScope.add(ScopeTestUtils.createScope("1"));
		projectScope.add(ScopeTestUtils.createScope("2"));

		return ProjectTestUtils.createProjectContext(projectScope, null);
	}

	private ProjectContext getUnmodifieldContext() {
		final Scope unmodifiedScope = ScopeTestUtils.createScope("Project");
		unmodifiedScope.add(ScopeTestUtils.createScope("1"));
		unmodifiedScope.add(ScopeTestUtils.createScope("2"));

		return ProjectTestUtils.createProjectContext(unmodifiedScope, null);
	}

	private ScopeTree getUnmodifiedTree() {
		treeAfterManipulation = new ScopeTree();
		treeAfterManipulation.setContext(getUnmodifieldContext());
		return treeAfterManipulation;
	}

	private ScopeTree getModifiedTree() {
		treeAfterManipulation = new ScopeTree();
		treeAfterManipulation.setContext(getModifiedContext());
		return treeAfterManipulation;
	}

	private ScopeTree getModifiedRootTree() {
		treeAfterManipulation = new ScopeTree();
		treeAfterManipulation.setContext(getModifiedRootContext());
		return treeAfterManipulation;
	}

	@Test
	public void shouldUpdateScopeWithNewValue() throws ActionNotFoundException {
		actionExecutionService.onUserActionExecutionRequest(new ScopeUpdateAction(firstScope.getId(), "3"));

		DeepEqualityTestUtils.assertObjectEquality(scope, getModifiedContext().getProjectScope());
		DeepEqualityTestUtils.assertObjectEquality(tree, getModifiedTree());
	}

	@Test
	public void shouldUpdateRootScope() throws ActionNotFoundException {
		actionExecutionService.onUserActionExecutionRequest(new ScopeUpdateAction(rootScope.getId(), "Root"));

		DeepEqualityTestUtils.assertObjectEquality(scope, getModifiedRootContext().getProjectScope());
		DeepEqualityTestUtils.assertObjectEquality(tree, getModifiedRootTree());
	}

	@Test
	public void shouldRollbackUpdatedScope() throws ActionNotFoundException {
		actionExecutionService.onUserActionExecutionRequest(new ScopeUpdateAction(firstScope.getId(), "3"));

		DeepEqualityTestUtils.assertObjectEquality(scope, getModifiedContext().getProjectScope());
		DeepEqualityTestUtils.assertObjectEquality(tree, getModifiedTree());

		actionExecutionService.onUserActionUndoRequest();

		DeepEqualityTestUtils.assertObjectEquality(scope, getUnmodifieldContext().getProjectScope());
		DeepEqualityTestUtils.assertObjectEquality(tree, getUnmodifiedTree());
	}
}
