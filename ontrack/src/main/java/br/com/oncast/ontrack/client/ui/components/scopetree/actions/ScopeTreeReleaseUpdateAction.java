package br.com.oncast.ontrack.client.ui.components.scopetree.actions;

import java.util.List;

import br.com.oncast.ontrack.client.ui.components.scopetree.ScopeTreeItem;
import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeWidget;
import br.com.oncast.ontrack.shared.model.ModelBeanNotFoundException;
import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.ReleaseAction;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;

public class ScopeTreeReleaseUpdateAction implements ScopeTreeAction {

	private final ScopeTreeWidget tree;
	private final ReleaseAction action;

	public ScopeTreeReleaseUpdateAction(final ScopeTreeWidget tree, final ReleaseAction action) {
		this.tree = tree;
		this.action = action;
	}

	@Override
	public void execute(final ProjectContext context, ActionContext actionContext, final boolean isUserInteraction) throws ModelBeanNotFoundException {
		final Release release = context.findRelease(action.getReferenceId());

		final List<Scope> scopesIncludingDescendantReleases = release.getAllScopesIncludingDescendantReleases();
		for (final Scope scope : scopesIncludingDescendantReleases) {
			final ScopeTreeItem treeItem = tree.findScopeTreeItem(scope);
			treeItem.getScopeTreeItemWidget().updateReleaseDisplay();
		}
	}

}
