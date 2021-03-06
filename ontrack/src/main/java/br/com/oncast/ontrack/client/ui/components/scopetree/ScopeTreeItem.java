package br.com.oncast.ontrack.client.ui.components.scopetree;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ActivateTagFilterEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemBindReleaseEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemDeclareEffortEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemDeclareProgressEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemDeclareValueEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemEditionCancelEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemEditionEndEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeItemEditionStartEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.SubjectDetailUpdateEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeItemWidget;
import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeItemWidgetEditionHandler;
import br.com.oncast.ontrack.shared.model.color.Color;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.user.client.ui.IsTreeItem;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class ScopeTreeItem extends TreeItem implements IsTreeItem {

	private ScopeTreeItemWidget scopeItemWidget;

	private boolean hasFakeItens;

	ScopeTreeItem() {}

	public ScopeTreeItem(final Scope scope) {
		super();
		this.setWidget(scopeItemWidget = new ScopeTreeItemWidget(scope, new ScopeTreeItemWidgetEditionHandler() {

			@Override
			public void onEditionStart() {
				ScopeTreeItem.this.getTree().fireEvent(new ScopeTreeItemEditionStartEvent(ScopeTreeItem.this));
			}

			@Override
			public void onEditionEnd(final String pattern) {
				ScopeTreeItem.this.getTree().fireEvent(new ScopeTreeItemEditionEndEvent(ScopeTreeItem.this, pattern));
			}

			@Override
			public void onEditionCancel() {
				getTree().setSelectedItem(ScopeTreeItem.this);
				ScopeTreeItem.this.getTree().fireEvent(new ScopeTreeItemEditionCancelEvent());
			}

			@Override
			public void onDeselectTreeItemRequest() {
				getTree().setSelectedItem(null);
			}

			@Override
			public void bindRelease(final String releaseDescription) {
				ScopeTreeItem.this.getTree().fireEvent(new ScopeTreeItemBindReleaseEvent(getReferencedScope().getId(), releaseDescription));
			}

			@Override
			public void declareProgress(final String progressDescription) {
				ScopeTreeItem.this.getTree().fireEvent(new ScopeTreeItemDeclareProgressEvent(getReferencedScope().getId(), progressDescription));
			}

			@Override
			public void declareEffort(final String effortDescription) {
				ScopeTreeItem.this.getTree().fireEvent(new ScopeTreeItemDeclareEffortEvent(getReferencedScope().getId(), effortDescription));
			}

			@Override
			public void declareValue(final String valueDescription) {
				ScopeTreeItem.this.getTree().fireEvent(new ScopeTreeItemDeclareValueEvent(getReferencedScope().getId(), valueDescription));
			}

			@Override
			public void onEditionMenuClose() {
				if (ScopeTreeItem.this.getTree().getSelectedItem() != null) return;
				ScopeTreeItem.this.select();
			}

			@Override
			public Scope getScope() {
				return scope;
			}

			@Override
			public void onFilterByTagRequested(final UUID tagId) {
				ClientServices.get().eventBus().fireEvent(new ActivateTagFilterEvent(tagId));
			}

		}));

		addStyleName("ScopeTreeItem");

		if (scope.getChildCount() > 0) {
			super.insertItem(0, FakeScopeTreeItem.get());
			this.hasFakeItens = true;
		}
	}

	public boolean mountTwoLevels() {
		if (hasFakeItens) {
			this.hasFakeItens = false;
			super.removeItems();
			insertChildren();
			return true;
		}
		return false;
	}

	public boolean isFake() {
		return false;
	}

	private void insertChildren() {
		for (final Scope childScope : this.getReferencedScope().getChildren()) {
			final ScopeTreeItem item = new ScopeTreeItem(childScope);
			this.addItem(item);
		}
	}

	protected void select() {
		getTree().setSelectedItem(this);
	}

	public boolean isRoot() {
		return getParentItem() == null;
	}

	public void enterEditMode() {
		scopeItemWidget.switchToEditionMode();
		final Tree tree = getTree();
		if (tree != null) tree.setSelectedItem(null);
	}

	@Override
	public ScopeTreeItem getChild(final int index) {
		return (ScopeTreeItem) super.getChild(index);
	}

	@Override
	public ScopeTreeItem getParentItem() {
		return (ScopeTreeItem) super.getParentItem();
	}

	// TODO Analise deprecating (thus removing) this method and using 'getScopeTreeItemWidget().setScope()' instead.
	public void setReferencedScope(final Scope scope) {
		scopeItemWidget.setScope(scope);
	}

	// TODO Analise deprecating (thus removing) this method and using 'getScopeTreeItemWidget().getScope()' instead.
	public Scope getReferencedScope() {
		return scopeItemWidget.getScope();
	}

	// TODO Analise using (maybe deprecating this method) 'Tree#ensureSelectedItemVisible()' method, that ensures that the currently-selected item is visible,
	// opening its parents and scrolling the tree as necessary.
	public ScopeTreeItem setHierarchicalState(final boolean state) {
		ScopeTreeItem item = this;

		while (item != null) {
			if (!item.getState()) item.setState(state);
			item = item.getParentItem();
		}
		return this;
	}

	public ScopeTreeItemWidget getScopeTreeItemWidget() {
		return scopeItemWidget;
	}

	public void updateDetails(final SubjectDetailUpdateEvent event) {
		scopeItemWidget.updateDetails(event);
	}

	public void addSelectedMember(final UserRepresentation member, final Color selectionColor) {
		scopeItemWidget.addSelectedMember(member, selectionColor);
	}

	public void removeSelectedMember(final UserRepresentation member) {
		scopeItemWidget.removeSelectedMember(member);
	}

	public List<ScopeTreeItem> filter(final HashSet<Scope> neededScopes) {
		final List<ScopeTreeItem> hiddenItens = new ArrayList<ScopeTreeItem>();

		final boolean mustShow = neededScopes.contains(getReferencedScope());
		this.setVisible(mustShow);
		if (mustShow) {
			for (int i = 0; i < getChildCount(); i++) {
				hiddenItens.addAll(getChild(i).filter(neededScopes));
			}
		}
		else {
			hiddenItens.add(this);
		}
		return hiddenItens;
	}

	public void updateDisplay() {
		scopeItemWidget.updateDisplay();
	}

	@Override
	public void insertItem(final int beforeIndex, final TreeItem item) throws IndexOutOfBoundsException {
		if (hasFakeItens) return;
		super.insertItem(beforeIndex, item);
	}

}