package br.com.oncast.ontrack.client.ui.components.scopetree.actions;

import br.com.oncast.ontrack.client.ui.components.scopetree.ScopeTreeItem;
import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeWidget;
import br.com.oncast.ontrack.shared.project.ProjectContext;
import br.com.oncast.ontrack.shared.scope.Scope;
import br.com.oncast.ontrack.shared.scope.actions.ScopeInsertAction;
import br.com.oncast.ontrack.shared.scope.exceptions.ScopeNotFoundException;

class ScopeTreeInsertChildAction implements ScopeTreeAction {

	private final ScopeTreeWidget tree;
	private final ScopeInsertAction action;

	public ScopeTreeInsertChildAction(final ScopeTreeWidget tree, final ScopeInsertAction action) {
		this.tree = tree;
		this.action = action;
	}

	@Override
	public void execute(final ProjectContext context) throws ScopeNotFoundException {
		final Scope scope = context.findScope(action.getScopeId());
		final Scope newScope = context.findScope(action.getNewScopeId());

		final ScopeTreeItem parentTreeItem = tree.getScopeTreeItemFor(scope.getId());
		final ScopeTreeItem newItem = new ScopeTreeItem(newScope);

		parentTreeItem.insertItem(scope.getChildIndex(newScope), newItem);

		parentTreeItem.setState(true);
		newItem.setSelected(true);
		newItem.enterEditMode();
	}

	@Override
	public void rollback(final ProjectContext context) throws ScopeNotFoundException {
		final ScopeTreeItem treeItem = tree.getScopeTreeItemFor(action.getNewScopeId());

		final ScopeTreeItem parentItem = treeItem.getParentItem();
		final int childIndex = parentItem.getChildIndex(treeItem);
		parentItem.removeItem(treeItem);

		tree.setSelectedItem(((parentItem.getChildCount() > 0) ? parentItem.getChild((parentItem.getChildCount() - 1 < childIndex) ? parentItem.getChildCount() - 1
				: childIndex)
				: parentItem));
	}
}
