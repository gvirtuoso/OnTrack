package br.com.oncast.ontrack.client.ui.components.scopetree;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionListener;
import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionRequestHandler;
import br.com.oncast.ontrack.client.ui.components.Component;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.ScopeTreeActionFactory;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeInternalActionHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.interaction.ScopeTreeInstructionGuide;
import br.com.oncast.ontrack.client.ui.components.scopetree.interaction.ScopeTreeInteractionHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.interaction.ScopeTreeShortcutMappings;
import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeItemWidget;
import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeWidget;
import br.com.oncast.ontrack.client.ui.keyeventhandler.ShortcutService;
import br.com.oncast.ontrack.shared.model.ModelBeanNotFoundException;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.metadata.TagAssociationMetadata;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.tag.Tag;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.shared.services.actionExecution.ActionExecutionContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

public class ScopeTree implements Component {

	private final ScopeTreeWidget tree;

	private final ScopeTreeActionFactory treeActionFactory;

	private final ActionExecutionListener actionExecutionListener;

	private final ScopeTreeInteractionHandler treeInteractionHandler;

	private final ScopeTreeInstructionGuide instructionGuide;

	private List<ScopeTreeItem> filteredItems = new ArrayList<ScopeTreeItem>();

	private ProjectContext context;

	public ScopeTree() {
		treeInteractionHandler = new ScopeTreeInteractionHandler(this);
		tree = new ScopeTreeWidget(treeInteractionHandler);

		ShortcutService.register(tree, treeInteractionHandler, ScopeTreeShortcutMappings.values());

		instructionGuide = new ScopeTreeInstructionGuide(tree);

		actionExecutionListener = new ActionExecutionListener() {

			@Override
			public void onActionExecution(final ActionExecutionContext execution, final ProjectContext context, final boolean isUserAction) {
				try {
					final ModelAction action = execution.getModelAction();
					treeActionFactory.createEquivalentActionFor(action).execute(context, execution.createActionContext(), isUserAction);
					final HashSet<Scope> inferenceInfluencedScopes = new HashSet<Scope>();
					for (final UUID id : execution.getInferenceInfluencedScopeSet())
						inferenceInfluencedScopes.add(context.findScope(id));

					for (final Scope scope : inferenceInfluencedScopes) {
						tree.findScopeTreeItem(scope).updateDisplay();
					}

					if (isUserAction) instructionGuide.onActionExecution(action);
				} catch (final ModelBeanNotFoundException e) {
					// TODO ++Resync and Redraw the entire structure to eliminate inconsistencies
					throw new RuntimeException(ClientServices.get().messages().modelInconsistency(), e);
				}
			}
		};

		treeActionFactory = new ScopeTreeActionFactory(tree);
	}

	@Override
	public ActionExecutionListener getActionExecutionListener() {
		return actionExecutionListener;
	}

	@Override
	public void setActionExecutionRequestHandler(final ActionExecutionRequestHandler actionHandler) {
		treeInteractionHandler.configure(tree, actionHandler);
	}

	public void setContext(final ProjectContext context) {
		this.context = context;
		treeInteractionHandler.setContext(context);
		tree.clear();
		final ScopeTreeItem rootItem = new ScopeTreeItem(context.getProjectScope());

		tree.add(rootItem);
		rootItem.mountTwoLevels();
		rootItem.setState(true);
		tree.setSelectedItem(rootItem, true);

		instructionGuide.onSetContext(context);
	}

	@Override
	public Widget asWidget() {
		return tree;
	}

	public void setFocus(final boolean focus) {
		tree.setFocus(focus);
	}

	public ScopeTreeItemWidget getSelected() {
		return tree.getSelectedItem().getScopeTreeItemWidget();
	}

	public ScopeTreeInternalActionHandler getScopeTreeInternalActionHandler() {
		return treeInteractionHandler;
	}

	public void filterByTag(final UUID filteredTagId) {
		final ScopeTreeItem selectedItem = tree.getSelectedItem();
		tree.showFilteringIndicator();
		new Timer() {

			@Override
			public void run() {
				clearTagFilter();

				final HashSet<Scope> neededScopes = new HashSet<Scope>();
				final HashSet<Tag> tags = new HashSet<Tag>();
				for (final TagAssociationMetadata association : context.<TagAssociationMetadata> getAllMetadata(TagAssociationMetadata.getType())) {
					if (!association.getTag().getId().equals(filteredTagId)) continue;
					tags.add(association.getTag());

					final Scope scope = (Scope) association.getSubject();

					addAncestors(neededScopes, scope);
					tree.findAndMountScopeTreeItem(scope).setHierarchicalState(true);
					addDescendants(neededScopes, scope);
				}

				filteredItems = tree.getItem(0).filter(neededScopes);
				tree.showTagFilteringInfo(tags);
				tree.hideFilteringIndicator();
				if (selectedItem != null) selectedItem.select();
			}
		}.schedule(50);
	}

	public void clearTagFilter() {
		for (final ScopeTreeItem item : filteredItems) {
			item.setVisible(true);
		}
		filteredItems.clear();
		tree.hideTagFilteringInfo();
	}

	private void addDescendants(final HashSet<Scope> neededScopes, final Scope scope) {
		for (final Scope child : scope.getChildren()) {
			neededScopes.add(child);
			addDescendants(neededScopes, child);
		}
	}

	private void addAncestors(final HashSet<Scope> showingScopes, final Scope scope) {
		if (!scope.isRoot()) addAncestors(showingScopes, scope.getParent());
		showingScopes.add(scope);
	}
}
