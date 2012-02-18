package br.com.oncast.ontrack.client.ui.components.scopetree.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.oncast.ontrack.client.ui.components.scopetree.ScopeTreeItem;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemBindReleaseEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemBindReleaseEventHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemDeclareEffortEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemDeclareEffortEventHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemDeclareProgressEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemDeclareProgressEventHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemDeclareValueEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemDeclareValueEventHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemEditionCancelEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemEditionCancelEventHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemEditionEndEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemEditionEndEventHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemEditionStartEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemEditionStartEventHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeWidgetInteractionHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.interaction.ScopeTreeShortcutMappings;
import br.com.oncast.ontrack.client.ui.keyeventhandler.ShortcutService;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.exceptions.ScopeNotFoundException;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeItemAdoptionListener;

public class ScopeTreeWidget extends Composite {

	private final Tree tree;

	private final Map<UUID, ScopeTreeItem> itemMapCache = new HashMap<UUID, ScopeTreeItem>();

	public ScopeTreeWidget(final ScopeTreeWidgetInteractionHandler interactionHandler) {
		initWidget(tree = new Tree());
		ShortcutService.register(tree, interactionHandler, ScopeTreeShortcutMappings.values());

		tree.addHandler(new ScopeTreeItemBindReleaseEventHandler() {
			@Override
			public void onBindReleaseRequest(final UUID scopeId, final String releaseDescription) {
				interactionHandler.onBindReleaseRequest(scopeId, releaseDescription);
			}
		}, ScopeTreeItemBindReleaseEvent.getType());

		tree.addHandler(new ScopeTreeItemDeclareEffortEventHandler() {

			@Override
			public void onDeclareEffortRequest(final UUID scopeId, final String effortDescription) {
				interactionHandler.onDeclareEffortRequest(scopeId, effortDescription);
			}

		}, ScopeTreeItemDeclareEffortEvent.getType());

		tree.addHandler(new ScopeTreeItemDeclareValueEventHandler() {

			@Override
			public void onDeclareValueRequest(final UUID scopeId, final String valueDescription) {
				interactionHandler.onDeclareValueRequest(scopeId, valueDescription);
			}
		}, ScopeTreeItemDeclareValueEvent.getType());

		tree.addHandler(new ScopeTreeItemDeclareProgressEventHandler() {

			@Override
			public void onDeclareProgressRequest(final UUID scopeId, final String progressDescription) {
				interactionHandler.onDeclareProgressRequest(scopeId, progressDescription);
			}
		}, ScopeTreeItemDeclareProgressEvent.getType());

		tree.addHandler(new ScopeTreeItemEditionStartEventHandler() {

			@Override
			public void onItemEditionStart(final ScopeTreeItem item) {
				interactionHandler.onItemEditionStart(item);
			}
		}, ScopeTreeItemEditionStartEvent.getType());

		tree.addHandler(new ScopeTreeItemEditionEndEventHandler() {

			@Override
			public void onItemEditionEnd(final ScopeTreeItem item, final String value) {
				interactionHandler.onItemEditionEnd(item, value);
			}
		}, ScopeTreeItemEditionEndEvent.getType());

		tree.addHandler(new ScopeTreeItemEditionCancelEventHandler() {

			@Override
			public void onItemEditionCancel() {
				interactionHandler.onItemEditionCancel();
			}
		}, ScopeTreeItemEditionCancelEvent.getType());

		tree.setTreeItemAdoptionListener(new TreeItemAdoptionListener() {

			@Override
			public void onTreeItemAdopted(final TreeItem treeItem) {
				final ScopeTreeItem scopeTreeItem = ((ScopeTreeItem) treeItem);
				addRecursivelyToCache(scopeTreeItem);
			}

			@Override
			public void onTreeItemAbandoned(final TreeItem treeItem) {
				final ScopeTreeItem scopeTreeItem = ((ScopeTreeItem) treeItem);
				removeRecusivelyFromCache(scopeTreeItem);
			}

			private void addRecursivelyToCache(final ScopeTreeItem scopeTreeItem) {
				final Scope scope = scopeTreeItem.getReferencedScope();
				if (itemMapCache.containsKey(scope.getId())) throw new RuntimeException(
						"You are trying to Add a widget for Scope '" + scope.getDescription() + "' that the ScopeTreeWidget already has");

				itemMapCache.put(scope.getId(), scopeTreeItem);
			}

			private void removeRecusivelyFromCache(final ScopeTreeItem scopeTreeItem) {
				final Scope scope = scopeTreeItem.getReferencedScope();
				if (!itemMapCache.containsKey(scope.getId())) throw new RuntimeException("You are trying to Remove a widget for Scope '"
						+ scope.getDescription() + "' that is not present in the ScopeTreeWidget");

				itemMapCache.remove(scope.getId());
			}
		});
		tree.addOpenHandler(new OpenHandler<TreeItem>() {
			@Override
			public void onOpen(final OpenEvent<TreeItem> event) {
				final ScopeTreeItem item = (ScopeTreeItem) event.getTarget();
				item.mountTwoLevels();
				tree.setSelectedItem(item);
			}
		});
	}

	public void add(final ScopeTreeItem item) {
		tree.addItem(item);
	}

	public void add(final int beforeIndex, final ScopeTreeItem item) {
		tree.insertItem(beforeIndex, item.asTreeItem());
	}

	public void remove(final ScopeTreeItem item) {
		tree.removeItem(item);
	}

	public void clear() {
		tree.clear();
	}

	public ScopeTreeItem getSelectedItem() {
		return (ScopeTreeItem) tree.getSelectedItem();
	}

	public void setSelectedItem(final ScopeTreeItem selected) {
		tree.setSelectedItem(selected);
	}

	public void setFocus(final boolean focus) {
		tree.setFocus(focus);
		if (getItemCount() > 0) setSelectedItem(getItem(0));
	}

	public ScopeTreeItem getItem(final int index) {
		return (ScopeTreeItem) tree.getItem(index);
	}

	public int getItemCount() {
		return tree.getItemCount();
	}

	public ScopeTreeItem findScopeTreeItem(final Scope scope) throws ScopeNotFoundException {
		final UUID scopeId = scope.getId();

		final ScopeTreeItem scopeTreeItem = itemMapCache.get(scopeId);
		if (scopeTreeItem != null) return scopeTreeItem;
		if (scope.isRoot()) throw new ScopeNotFoundException("It was not possible to find any tree item for the given scope.");

		findScopeTreeItem(scope.getParent()).mountTwoLevels();

		if (!itemMapCache.containsKey(scopeId)) throw new ScopeNotFoundException("It was not possible to find any tree item for the given scope.");

		return itemMapCache.get(scopeId);
	}

	public void showSearchWidget() {
		ScopeTreeSearchWidget.show(getAllItens());
	}

	private List<ScopeTreeItem> getAllItens() {
		final ArrayList<ScopeTreeItem> allItens = new ArrayList<ScopeTreeItem>();
		final ScopeTreeItem rootItem = getItem(0);
		allItens.add(rootItem);
		allItens.addAll(rootItem.getAllDescendantChilden());
		return allItens;
	}
}
