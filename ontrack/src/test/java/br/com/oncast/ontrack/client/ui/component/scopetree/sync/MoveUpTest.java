package br.com.oncast.ontrack.client.ui.component.scopetree.sync;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import br.com.oncast.ontrack.client.ui.component.scopetree.actions.MoveUpScopeAction;
import br.com.oncast.ontrack.client.ui.component.scopetree.exceptions.NotFoundException;
import br.com.oncast.ontrack.client.ui.component.scopetree.widget.ScopeTreeItem;
import br.com.oncast.ontrack.client.ui.component.scopetree.widget.ScopeTreeWidget;
import br.com.oncast.ontrack.client.ui.component.scopetree.widget.actions.ScopeTreeWidgetActionFactoryImpl;
import br.com.oncast.ontrack.client.ui.component.scopetree.widget.actions.ScopeTreeWidgetActionManager;
import br.com.oncast.ontrack.client.ui.component.scopetree.widget.event.ScopeTreeWidgetInteractionHandler;
import br.com.oncast.ontrack.shared.beans.Scope;

import com.octo.gwt.test.GwtTest;

public class MoveUpTest extends GwtTest {

	private Scope scope;
	private Scope rootScope;
	private Scope firstScope;
	private Scope lastScope;
	private ScopeTreeWidget tree;
	private ScopeTreeWidget treeAfterManipulation;
	private ScopeTreeWidgetActionManager scopeTreeWidgetActionManager;

	@Before
	public void setUp() {
		scope = getScope();

		final ScopeTreeWidgetInteractionHandler interactionHandler = mock(ScopeTreeWidgetInteractionHandler.class);
		tree = new ScopeTreeWidget(interactionHandler);
		treeAfterManipulation = new ScopeTreeWidget(interactionHandler);

		tree.add(new ScopeTreeItem(scope));

		scopeTreeWidgetActionManager = new ScopeTreeWidgetActionManager(new ScopeTreeWidgetActionFactoryImpl(tree));
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

	private ScopeTreeWidget getUnmodifiedTree() {
		treeAfterManipulation.clear();
		treeAfterManipulation.add(new ScopeTreeItem(getUnmodifiedScope()));

		return treeAfterManipulation;
	}

	private ScopeTreeWidget getModifiedTree() {
		treeAfterManipulation.clear();
		treeAfterManipulation.add(new ScopeTreeItem(getModifiedScope()));

		return treeAfterManipulation;
	}

	@Test
	public void shouldMoveUp() throws NotFoundException {
		scopeTreeWidgetActionManager.execute(new MoveUpScopeAction(lastScope));

		assertEquals(getModifiedScope(), scope);
		assertEquals(getModifiedTree(), tree);
	}

	@Test(expected = RuntimeException.class)
	public void shouldNotMoveUpFirstItem() throws NotFoundException {
		scopeTreeWidgetActionManager.execute(new MoveUpScopeAction(firstScope));
	}

	@Test(expected = RuntimeException.class)
	public void shouldNotMoveUpRoot() throws NotFoundException {
		scopeTreeWidgetActionManager.execute(new MoveUpScopeAction(rootScope));
	}

	@Test
	public void shouldMoveDownItemAfterUndo() throws NotFoundException {
		scopeTreeWidgetActionManager.execute(new MoveUpScopeAction(lastScope));

		assertEquals(getModifiedScope(), scope);
		assertEquals(getModifiedTree(), tree);

		scopeTreeWidgetActionManager.undo();

		assertEquals(getUnmodifiedScope(), scope);
		assertEquals(getUnmodifiedTree(), tree);
	}

	@Override
	public String getModuleName() {
		return "br.com.oncast.ontrack.Application";
	}

}