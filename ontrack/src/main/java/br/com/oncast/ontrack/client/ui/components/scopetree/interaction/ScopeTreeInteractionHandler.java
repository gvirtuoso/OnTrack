package br.com.oncast.ontrack.client.ui.components.scopetree.interaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import br.com.oncast.ontrack.client.services.ClientServiceProvider;
import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionRequestHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.ScopeTreeItem;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.InternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.InternalActionExecutionRequestHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.NodeEditionInternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.OneStepInternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.TwoStepInternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeWidgetInteractionHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.exceptions.OperationNotAllowedException;
import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeWidget;
import br.com.oncast.ontrack.client.util.ScopeBindReleaseActionHelper;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ScopeBindReleaseAction;
import br.com.oncast.ontrack.shared.model.action.ScopeDeclareEffortAction;
import br.com.oncast.ontrack.shared.model.action.ScopeDeclareProgressAction;
import br.com.oncast.ontrack.shared.model.action.ScopeDeclareValueAction;
import br.com.oncast.ontrack.shared.model.action.ScopeUpdateAction;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.metadata.TagAssociationMetadata;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.tag.Tag;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

public final class ScopeTreeInteractionHandler implements ScopeTreeWidgetInteractionHandler {

	private class InternalActionHandler implements InternalActionExecutionRequestHandler {

		private TwoStepInternalAction pendingAction = null;

		@Override
		public void handle(final TwoStepInternalAction internalAction) {
			this.pendingAction = internalAction;
			try {
				execute(internalAction);
			}
			catch (final RuntimeException e) {
				this.pendingAction = null;
				throw e;
			}
		}

		@Override
		public void handle(final OneStepInternalAction internalAction) {
			execute(internalAction);
		}

		public boolean hasPendingAction() {
			return pendingAction != null;
		}

		public void rollbackPendingAction() {
			try {
				pendingAction.rollback(tree);
			}
			catch (final UnableToCompleteActionException e) {
				// TODO ++Implement an adequate exception treatment.
				throw new RuntimeException();
			}
			finally {
				this.pendingAction = null;
			}
		}

		public ModelAction getPendingActionEquivalentModelActionFor(final String value) {
			return pendingAction.createEquivalentModelAction(value);
		}

		private void execute(final InternalAction internalAction) {
			try {
				internalAction.execute(tree);
			}
			catch (final OperationNotAllowedException e) {
				ClientServiceProvider.getInstance().getClientAlertingService().showWarning(e.getLocalizedMessage());
			}
			catch (final UnableToCompleteActionException e) {
				ClientServiceProvider.getInstance().getClientAlertingService().showError(e.getLocalizedMessage());
			}
		}
	}

	private final InternalActionHandler internalActionHandler = new InternalActionHandler();
	private ActionExecutionRequestHandler applicationActionHandler;
	private ScopeTreeWidget tree;
	private ProjectContext context;

	@Override
	public Scope getSelectedScope() {
		return tree.getSelectedItem().getReferencedScope();
	}

	@Override
	public void onInternalAction(final OneStepInternalAction action) {
		internalActionHandler.handle(action);
	}

	@Override
	public void onInternalAction(final TwoStepInternalAction action) {
		internalActionHandler.handle(action);
	}

	@Override
	public void onUserActionExecutionRequest(final ModelAction action) {
		applicationActionHandler.onUserActionExecutionRequest(action);
	}

	@Override
	public ProjectContext getProjectContext() {
		return context;
	}

	@Override
	public void onItemEditionStart(final ScopeTreeItem item) {
		internalActionHandler.handle(new NodeEditionInternalAction(item.getReferencedScope()));
	}

	@Override
	public void onItemEditionEnd(final ScopeTreeItem item, final String value) {
		assureConfigured();

		if (internalActionHandler.hasPendingAction()) {
			final ModelAction action = internalActionHandler.getPendingActionEquivalentModelActionFor(value);
			internalActionHandler.rollbackPendingAction();
			applicationActionHandler.onUserActionExecutionRequest(action);
		}
		else applicationActionHandler.onUserActionExecutionRequest(new ScopeUpdateAction(item.getReferencedScope().getId(), value));
	}

	@Override
	public void onItemEditionCancel() {
		assureConfigured();

		if (!internalActionHandler.hasPendingAction()) return;
		internalActionHandler.rollbackPendingAction();
	}

	@Override
	public void onBindReleaseRequest(final UUID scopeId, final String releaseDescription) {
		if (!ScopeBindReleaseActionHelper.validadeHierarchicalCondition(scopeId, releaseDescription)) return;
		applicationActionHandler.onUserActionExecutionRequest(new ScopeBindReleaseAction(scopeId, releaseDescription));
	}

	@Override
	public void onDeclareProgressRequest(final UUID scopeId, final String progressDescription) {
		applicationActionHandler.onUserActionExecutionRequest(new ScopeDeclareProgressAction(scopeId, progressDescription));
	}

	@Override
	public void onDeclareEffortRequest(final UUID scopeId, final String effortDescription) {
		float declaredEffort;
		boolean hasDeclaredEffort;

		try {
			declaredEffort = Float.valueOf(effortDescription);
			hasDeclaredEffort = true;
		}
		catch (final NumberFormatException e) {
			declaredEffort = 0;
			hasDeclaredEffort = false;
		}

		applicationActionHandler.onUserActionExecutionRequest(new ScopeDeclareEffortAction(scopeId, hasDeclaredEffort, declaredEffort));
	}

	@Override
	public void onDeclareValueRequest(final UUID scopeId, final String valueDescription) {
		float declaredValue;
		boolean hasDeclaredValue;

		try {
			declaredValue = Float.valueOf(valueDescription);
			hasDeclaredValue = true;
		}
		catch (final NumberFormatException e) {
			declaredValue = 0;
			hasDeclaredValue = false;
		}

		applicationActionHandler.onUserActionExecutionRequest(new ScopeDeclareValueAction(scopeId, hasDeclaredValue, declaredValue));
	}

	private List<ScopeTreeItem> filteredItems = new ArrayList<ScopeTreeItem>();

	@Override
	public void filterByTag(final UUID filteredTagId) {
		clearTagFilter();

		final HashSet<Scope> neededScopes = new HashSet<Scope>();
		final HashSet<Tag> tags = new HashSet<Tag>();
		for (final TagAssociationMetadata association : context.<TagAssociationMetadata> getAllMetadata(TagAssociationMetadata.getType())) {
			if (!association.getTag().getId().equals(filteredTagId)) continue;
			tags.add(association.getTag());

			final Scope scope = (Scope) association.getSubject();

			addAncestors(neededScopes, scope);
			tree.findAndMountScopeTreeItem(scope).setHierarchicalState(true).setState(hasTaggedDescendant(neededScopes, scope));
			addDescendants(neededScopes, scope);
		}

		filteredItems = tree.getItem(0).filter(neededScopes);
		tree.showTagFilteringInfo(tags);
	}

	private boolean hasTaggedDescendant(final HashSet<Scope> neededScopes, final Scope scope) {
		boolean hasTaggedChild = false;
		for (final Scope child : scope.getChildren()) {
			if (neededScopes.contains(child)) {
				hasTaggedChild = true;
				break;
			}
		}
		return hasTaggedChild;
	}

	private void addDescendants(final HashSet<Scope> neededScopes, final Scope scope) {
		for (final Scope child : scope.getChildren()) {
			neededScopes.add(child);
			addDescendants(neededScopes, child);
		}
	}

	@Override
	public void clearTagFilter() {
		for (final ScopeTreeItem item : filteredItems) {
			item.setVisible(true);
		}
		filteredItems.clear();
		tree.hideTagFilteringInfo();
	}

	private void addAncestors(final HashSet<Scope> showingScopes, final Scope scope) {
		if (!scope.isRoot()) addAncestors(showingScopes, scope.getParent());
		showingScopes.add(scope);
	}

	@Override
	public void assureConfigured() {
		if (applicationActionHandler == null || tree == null || context == null) throw new RuntimeException("This class was not yet configured.");
	}

	public void configure(final ScopeTreeWidget tree, final ActionExecutionRequestHandler actionHandler) {
		this.tree = tree;
		this.applicationActionHandler = actionHandler;
	}

	public void setContext(final ProjectContext context) {
		this.context = context;
	}

	@Override
	public void focusTree() {
		tree.setFocus(true);
	}

	@Override
	public boolean hasPendingInternalAction() {
		return internalActionHandler.hasPendingAction();
	}

	@Override
	public Scope getVisibleScopeAbove(final Scope scope) {
		final ScopeTreeItem item = tree.findScopeTreeItem(scope);
		final ScopeTreeItem parent = item.getParentItem();
		for (int i = parent.getChildIndex(item) - 1; i >= 0; i--) {
			final ScopeTreeItem itemAbove = parent.getChild(i);
			if (itemAbove.isVisible()) return itemAbove.getReferencedScope();
		}
		return null;
	}

	@Override
	public Scope getVisibleScopeBelow(final Scope scope) {
		final ScopeTreeItem item = tree.findScopeTreeItem(scope);
		final ScopeTreeItem parent = item.getParentItem();

		for (int i = parent.getChildIndex(item) + 1; i < parent.getChildCount(); i++) {
			final ScopeTreeItem itemBelow = parent.getChild(i);
			if (itemBelow.isVisible()) return itemBelow.getReferencedScope();
		}
		return null;
	}
}
