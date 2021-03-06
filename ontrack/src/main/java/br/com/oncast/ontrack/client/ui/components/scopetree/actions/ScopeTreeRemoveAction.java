package br.com.oncast.ontrack.client.ui.components.scopetree.actions;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.ui.components.scopetree.ScopeTreeItem;
import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeWidget;
import br.com.oncast.ontrack.shared.model.ModelBeanNotFoundException;
import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.ScopeAction;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.action.helper.ActionHelper;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;
import br.com.oncast.ontrack.shared.model.user.exceptions.UserNotFoundException;

class ScopeTreeRemoveAction implements ScopeTreeAction {

	private final ScopeTreeWidget tree;
	private final ScopeAction action;

	public ScopeTreeRemoveAction(final ScopeTreeWidget tree, final ScopeAction action) {
		this.tree = tree;
		this.action = action;
	}

	@Override
	public void execute(final ProjectContext context, final ActionContext actionContext, final boolean isUserInteraction) throws ModelBeanNotFoundException {
		try {
			final UserRepresentation author = ActionHelper.findUser(actionContext.getUserId(), context, action);
			final ScopeTreeItem treeItem = tree.findScopeTreeItem(new Scope("", action.getReferenceId(), author, actionContext.getTimestamp()));

			final ScopeTreeItem parentItem = treeItem.getParentItem();
			final int childIndex = parentItem.getChildIndex(treeItem);
			parentItem.removeItem(treeItem);

			// TODO Is this necessary? The tree already receives a set of the modified scopes by the inference engines (effort, progress, ...).
			parentItem.getScopeTreeItemWidget().updateDisplay();

			if (isUserInteraction) {
				tree.setSelectedItem(
						((parentItem.getChildCount() > 0) ? parentItem.getChild((parentItem.getChildCount() - 1 < childIndex) ? parentItem.getChildCount() - 1 : childIndex) : parentItem), true);
			}
		} catch (final UnableToCompleteActionException e) {
			throw new UserNotFoundException(ClientServices.get().messages().userNotFound(actionContext.toString()));
		}
	}
}
